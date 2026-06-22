package com.otherworldinn.world.inn.service;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.store.WanderingTraderEntity;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.init.ModEntities;
import com.otherworldinn.world.data.TownSavedData;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.event.TownStructurePlacer;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
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
    /** 游商隐藏位置：离开时传送到原位置下方 30 格 */
    private static final BlockPos HIDDEN_TRADER_POS = TRADER_POS.below(30);
    /** 游商面朝方向（West = 90°） */
    private static final float TRADER_YAW = 90.0F;
    /** 商船结构放置原点 */
    private static final BlockPos SHIP_POS = new BlockPos(103, 66, -105);
    /** 移除商船时覆盖的空水域结构原点 */
    private static final BlockPos WATER_POS = new BlockPos(103, 61, -105);
    /** 结构切换后清理掉落物的扫描范围 */
    private static final AABB SHIP_DROP_CLEANUP_BOX = new AABB(105, 63, -104, 124, 86, -60);

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
            moveTraderToHiddenPosition(townLevel, data);
            removeShip(townLevel);

            long nextArrival = gameTime + randomWaitTicks(townLevel);
            data.setTraderInactive(nextArrival);
        }
    }

    // === WAITING 阶段 ===

    private static void tickWaiting(ServerLevel townLevel, TownSavedData data, long gameTime) {
        // 首次创建世界时 nextTraderArrivalTime 默认为 0，初始化为随机延迟，避免一开档就到达
        if (data.getNextTraderArrivalTime() == 0) {
            data.setTraderInactive(gameTime + randomWaitTicks(townLevel));
            return;
        }

        if (gameTime >= data.getNextTraderArrivalTime()) {
            arriveTrader(townLevel, data);
            placeShip(townLevel);
            broadcastArrival(townLevel);

            data.setTraderActive(gameTime + STAY_TICKS);
        }
    }

    // === 生成 / 移除 ===

    private static void arriveTrader(ServerLevel townLevel, TownSavedData data) {
        WanderingTraderEntity trader = getOrCreateTrader(townLevel, data);
        trader.restockAll();
        teleportTrader(trader, TRADER_POS);
    }

    private static void moveTraderToHiddenPosition(ServerLevel townLevel, TownSavedData data) {
        WanderingTraderEntity trader = findTrader(townLevel, data);
        if (trader != null) {
            trader.setCurrentRecyclePlayer(null);
            teleportTrader(trader, HIDDEN_TRADER_POS);
        }
    }

    private static WanderingTraderEntity getOrCreateTrader(ServerLevel townLevel, TownSavedData data) {
        WanderingTraderEntity trader = findTrader(townLevel, data);
        if (trader != null) {
            return trader;
        }

        trader = new WanderingTraderEntity(ModEntities.WANDERING_TRADER.get(), townLevel);
        teleportTrader(trader, HIDDEN_TRADER_POS);
        townLevel.addFreshEntity(trader);
        data.setTraderEntityUuid(trader.getUUID());
        return trader;
    }

    private static WanderingTraderEntity findTrader(ServerLevel townLevel, TownSavedData data) {
        UUID uuid = data.getTraderEntityUuid();
        if (uuid == null) {
            return null;
        }
        Entity entity = townLevel.getEntity(uuid);
        return entity instanceof WanderingTraderEntity trader ? trader : null;
    }

    private static void teleportTrader(WanderingTraderEntity trader, BlockPos pos) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY();
        double z = pos.getZ() + 0.5D;
        trader.moveToAndLockPosition(x, y, z, TRADER_YAW, 0.0F);
        trader.setDeltaMovement(0.0D, 0.0D, 0.0D);
        trader.setYRot(TRADER_YAW);
        trader.yRotO = TRADER_YAW;
        trader.yHeadRot = TRADER_YAW;
        trader.yHeadRotO = TRADER_YAW;
        trader.setXRot(0.0F);
        trader.xRotO = 0.0F;
        trader.fallDistance = 0.0F;
        trader.hurtMarked = true;
    }

    private static void placeShip(ServerLevel townLevel) {
        TownStructurePlacer.placeStructureTemplateNoDrops(townLevel, SHIP_STRUCTURE, SHIP_POS);
        clearShipDrops(townLevel);
    }

    private static void removeShip(ServerLevel townLevel) {
        TownStructurePlacer.placeStructureTemplateNoDrops(townLevel, WATER_STRUCTURE, WATER_POS);
        clearShipDrops(townLevel);
    }

    private static void clearShipDrops(ServerLevel townLevel) {
        for (ItemEntity itemEntity : townLevel.getEntitiesOfClass(ItemEntity.class, SHIP_DROP_CLEANUP_BOX)) {
            itemEntity.discard();
        }
    }

    private static void broadcastArrival(ServerLevel townLevel) {
        Component msg = Component.translatable(ARRIVAL_MSG_KEY)
                .withStyle(style -> style.withColor(ModColors.BLUE));
        townLevel.getServer().getPlayerList().broadcastSystemMessage(msg, false);

        townLevel.playSound(
                null,
                TRADER_POS.getX() + 0.5D,
                TRADER_POS.getY(),
                TRADER_POS.getZ() + 0.5D,
                SoundEvents.NOTE_BLOCK_PLING.value(),
                SoundSource.NEUTRAL,
                0.8F,
                1.0F);
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
        arriveTrader(townLevel, data);
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

        moveTraderToHiddenPosition(townLevel, data);
        removeShip(townLevel);

        long nextArrival = townLevel.getGameTime() + randomWaitTicks(townLevel);
        data.setTraderInactive(nextArrival);
        return true;
    }
}
