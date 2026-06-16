package com.otherworldinn.world.economy.service;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CPriceSyncPacket;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * 服务端价格计算与同步
 *
 * <p>每次有维度加载时从 overworld 捕获种子，确保切换存档后种子被更新。
 * 玩家登录时全量发送浮动后价格表给客户端。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class PriceSyncListener {

    private static final double MIN_MULTIPLIER = 0.9;
    private static final double MAX_MULTIPLIER = 1.1;

    private static long worldSeed;
    private static boolean seedReady;

    private PriceSyncListener() {}

    /** 每次有 ServerLevel 加载时从 overworld 更新种子 */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        worldSeed = serverLevel.getServer().overworld().getSeed();
        seedReady = true;
    }

    /** 玩家登录时计算本存档价格并发给客户端 */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!seedReady) return;
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;

        ModMessages.sendToPlayer(new S2CPriceSyncPacket(computeAll()), sp);
    }

    /** 玩家退出时清空客户端旧价格表 */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ItemSellPriceManager.clearFloatedPrices();
    }

    private static Map<String, Integer> computeAll() {
        Map<String, Integer> result = new HashMap<>();
        for (var entry : ItemSellPriceManager.BASE_PRICES.entrySet()) {
            String id = entry.getKey().toString();
            long itemSeed = worldSeed ^ id.hashCode();
            RandomSource random = RandomSource.create(itemSeed);
            double mul = MIN_MULTIPLIER + random.nextFloat() * (MAX_MULTIPLIER - MIN_MULTIPLIER);
            result.put(id, Math.max(1, (int) Math.round(entry.getValue() * mul)));
        }
        return result;
    }
}
