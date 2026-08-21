package com.otherworldinn.world.commission;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.festival.FestivalEndedEvent;
import com.otherworldinn.world.festival.FestivalStartedEvent;
import com.otherworldinn.world.hud.TaskHudSnapshotSync;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.TeamSavedData;
import com.otherworldinn.world.team.service.TeamManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class CommissionEventHandler {
    private CommissionEventHandler() {}

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (level.dimension() != TownDimensions.TOWN_LEVEL) {
            return;
        }
        TeamSavedData data = TeamManager.getInstance().getData(level.getServer());
        if (data == null) {
            return;
        }
        for (TeamData team : data.getTeams().values()) {
            if (CommissionService.tick(level, team)) {
                TeamManager.getInstance().syncTeam(team, level.getServer());
                TaskHudSnapshotSync.syncTeam(team, level);
            }
        }
    }

    /** 节日开始：对无有效接取的队伍强制刷新委托板，让节日限定委托立即上板（含冷启动） */
    @SubscribeEvent
    public static void onFestivalStarted(FestivalStartedEvent event) {
        forEachTeam(event.getTownLevel(), team -> CommissionService.refreshBoardForFestivalStart(
                event.getTownLevel(), team));
    }

    /** 节日结束：把仍挂在板上未接取的节日限定委托换回普通委托（已接取的保留至过期） */
    @SubscribeEvent
    public static void onFestivalEnded(FestivalEndedEvent event) {
        forEachTeam(event.getTownLevel(), team -> CommissionService.removeIdleFestivalEntries(
                event.getTownLevel(), team));
    }

    private static void forEachTeam(ServerLevel level, java.util.function.Consumer<TeamData> action) {
        TeamSavedData data = TeamManager.getInstance().getData(level.getServer());
        if (data == null) {
            return;
        }
        for (TeamData team : data.getTeams().values()) {
            action.accept(team);
        }
    }

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            CommissionService.onTeamMemberMobKilled(player, event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (level.getBlockState(event.getPos()).is(ModBlocks.COMMISSION_BOARD.get())) {
            // 潜行左键时不拦截，交由原版方块破坏流程处理。
            if (player.isShiftKeyDown()) {
                return;
            }
            event.setCanceled(true);
            CommissionService.handleBoardLeftClickSubmit(player, event.getPos());
        }
    }
}
