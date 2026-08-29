package com.otherworldinn.world.quest;

import com.otherworldinn.OtherworldInn;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 任务事件监听：交互事件做即时判定，1 秒兜底轮询覆盖背包/进度/区域等状态判定型目标。
 * 事件只负责及时反馈，判定正确性由轮询保证（漏报不会卡死任务）。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class QuestEventHandler {
    private static final int REEVALUATE_INTERVAL_TICKS = 20;
    private static int tickCounter = 0;

    private QuestEventHandler() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter >= REEVALUATE_INTERVAL_TICKS) {
            tickCounter = 0;
            QuestService.reevaluateAll(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        QuestService.onEntityInteract(player, event.getTarget());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        QuestService.onBlockInteract(
                player, event.getLevel(), event.getPos(), event.getLevel().getBlockState(event.getPos()));
    }

}
