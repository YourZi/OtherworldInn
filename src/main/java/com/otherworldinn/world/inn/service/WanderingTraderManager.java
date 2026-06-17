package com.otherworldinn.world.inn.service;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.store.WanderingTraderEntity;
import com.otherworldinn.init.ModEntities;
import com.otherworldinn.world.data.TownSavedData;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.event.TownStructurePlacer;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 游商管理器
 *
 * <p>管理游商的出现/消失循环：每 2-5 天出现一次，停留 3 天后离开。
 * 使用世界时间（GameTime），睡觉跳过时间也会同步推进。
 * 出现时全服广播，同时放置/移除商船结构。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class WanderingTraderManager {

    // === 坐标 ===
    /** 游商生成位置 */
    private static final BlockPos TRADER_POS = new BlockPos(101, 73, -81);
    /** 游商面朝方向（West = 90°） */
    private static final float TRADER_YAW = 90.0F;
    /** 商船结构放置原点 */
    private static final BlockPos SHIP_POS = new BlockPos(103, 66, -105);
    /** 移除商船时覆盖的空水域结构原点 */
    private static final BlockPos WATER_POS = new BlockPos(103, 61, -105);

    // === 结构 NBT ID ===
    private static final ResourceLocation SHIP_STRUCTURE =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "wandering_trader_ship");
    private static final ResourceLocation WATER_STRUCTURE =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "wandering_trader_ship_removed");

    // === 时间常量（游戏刻，24000 = 1 天） ===
    /** 游商停留天数 */
    private static final long STAY_DAYS = 3L;
    private static final long STAY_TICKS = STAY_DAYS * 24000L;
    /** 下次出现的最小/最大间隔（天） */
    private static final long MIN_WAIT_DAYS = 2L;
    private static final long MAX_WAIT_DAYS = 5L;

    // === 广播消息键 ===
    private static final String ARRIVAL_MSG_KEY = "message.otherworldinn.wandering_trader_arrival";

    private WanderingTraderManager() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel townLevel = event.getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) return;

        TownSavedData data = TownSavedData.get(townLevel);
        long gameTime = townLevel.getGameTime();

        if (data.isTraderActive()) {
            tickArrived(townLevel, data, gameTime);
        } else {
            tickWaiting(townLevel, data, gameTime);
        }
    }

    // === ARRIVED 阶段 ===

    private static void tickArrived(ServerLevel townLevel, TownSavedData data, long gameTime) {
        // 停留时间结束 → 离开
        if (gameTime >= data.getTraderDepartureTime()) {
            despawnTrader(townLevel, data);
            removeShip(townLevel);

            long nextArrival = gameTime + randomWaitTicks(townLevel);
            data.setTraderInactive(nextArrival);
        }
    }

    // === WAITING 阶段 ===

    private static void tickWaiting(ServerLevel townLevel, TownSavedData data, long gameTime) {
        if (gameTime >= data.getNextTraderArrivalTime()) {
            spawnTrader(townLevel, data);
            placeShip(townLevel);
            broadcastArrival(townLevel);

            data.setTraderActive(gameTime + STAY_TICKS);
        }
    }

    // === 生成 / 移除 ===

    private static void spawnTrader(ServerLevel townLevel, TownSavedData data) {
        WanderingTraderEntity trader =
                new WanderingTraderEntity(ModEntities.WANDERING_TRADER.get(), townLevel);
        trader.setPos(TRADER_POS.getX() + 0.5, TRADER_POS.getY(), TRADER_POS.getZ() + 0.5);
        trader.setYRot(TRADER_YAW);
        trader.yHeadRot = TRADER_YAW;
        townLevel.addFreshEntity(trader);
        data.setTraderEntityUuid(trader.getUUID());
    }

    private static void despawnTrader(ServerLevel townLevel, TownSavedData data) {
        UUID uuid = data.getTraderEntityUuid();
        if (uuid != null) {
            Entity entity = townLevel.getEntity(uuid);
            if (entity != null) {
                entity.discard();
            }
            data.setTraderEntityUuid(null);
        }
    }

    private static void placeShip(ServerLevel townLevel) {
        TownStructurePlacer.placeStructureTemplate(townLevel, SHIP_STRUCTURE, SHIP_POS, 18);
    }

    private static void removeShip(ServerLevel townLevel) {
        TownStructurePlacer.placeStructureTemplate(townLevel, WATER_STRUCTURE, WATER_POS, 18);
    }

    private static void broadcastArrival(ServerLevel townLevel) {
        Component msg = Component.translatable(ARRIVAL_MSG_KEY);
        townLevel.getServer().getPlayerList().broadcastSystemMessage(msg, false);
    }

    // === 工具方法 ===

    /** 随机生成 2-5 天的等待时间（tick） */
    private static long randomWaitTicks(ServerLevel townLevel) {
        long days = MIN_WAIT_DAYS + townLevel.random.nextInt((int) (MAX_WAIT_DAYS - MIN_WAIT_DAYS + 1));
        return days * 24000L;
    }

    // === 调试命令入口 ===

    /**
     * 强制使游商到达。如果已在停留中则刷新商品并重置停留计时。
     *
     * @param townLevel 城镇维度
     * @return true 表示操作成功
     */
    public static boolean forceArrive(ServerLevel townLevel) {
        TownSavedData data = TownSavedData.get(townLevel);

        // 如果已在停留中，先清理旧实体
        if (data.isTraderActive()) {
            despawnTrader(townLevel, data);
            removeShip(townLevel);
        }

        spawnTrader(townLevel, data);
        placeShip(townLevel);
        broadcastArrival(townLevel);

        data.setTraderActive(townLevel.getGameTime() + STAY_TICKS);
        return true;
    }

    /**
     * 强制使游商离开。如果不在停留中则无操作。
     *
     * <p>离开后会按正常周期在 2-5 天后再次出现。
     *
     * @param townLevel 城镇维度
     * @return true 表示操作成功（游商确实被移除），false 表示游商本来就不在
     */
    public static boolean forceLeave(ServerLevel townLevel) {
        TownSavedData data = TownSavedData.get(townLevel);
        if (!data.isTraderActive()) {
            return false;
        }

        despawnTrader(townLevel, data);
        removeShip(townLevel);

        long nextArrival = townLevel.getGameTime() + randomWaitTicks(townLevel);
        data.setTraderInactive(nextArrival);
        return true;
    }
}
