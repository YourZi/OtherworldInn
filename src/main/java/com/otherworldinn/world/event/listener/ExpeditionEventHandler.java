package com.otherworldinn.world.event.listener;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.expedition.ExpeditionDimensions;
import com.otherworldinn.world.expedition.ExpeditionService;
import com.otherworldinn.world.expedition.ExpeditionSession;
import com.otherworldinn.world.teleport.TeleportUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID)
public class ExpeditionEventHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (ExpeditionService.hasDimComponent(
                entity.level().dimension(), "no_drops")) {
            entity.captureDrops(new java.util.ArrayList<>());
            if (entity.level() instanceof ServerLevel serverLevel) {
                int xp = entity.getExperienceReward(serverLevel, entity.getLastHurtByMob());
                if (xp > 0) {
                    ExperienceOrb.award(serverLevel, entity.position(), xp * 3);
                }
            }
        }

        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Level level = player.level();
        if (!ExpeditionDimensions.isExpeditionDimension(level.dimension())) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        ExpeditionSession session = ExpeditionService.getPlayerSession(player.getUUID());
        if (session != null) {
            ExpeditionService.markDeparted(player.getUUID(), player.getServer());
        }

        GameRules.BooleanValue keepInvRule =
                serverLevel.getGameRules().getRule(GameRules.RULE_KEEPINVENTORY);
        keepInvRule.set(false, serverLevel.getServer());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ExpeditionSession session = ExpeditionService.getPlayerSession(player.getUUID());
        boolean inExpeditionDim = ExpeditionDimensions.isExpeditionDimension(
                player.level().dimension());

        if (session == null && inExpeditionDim) {
            ServerLevel townLevel = player.getServer().getLevel(TownDimensions.TOWN_LEVEL);
            if (townLevel != null) {
                TeleportUtils.changeDimensionTo(player, townLevel,
                        new BlockPos(10, 71, 0));
            }
            return;
        }

        if (session != null && session.departedPlayers().contains(player.getUUID())) {
            ServerLevel townLevel = player.getServer().getLevel(TownDimensions.TOWN_LEVEL);
            if (townLevel != null) {
                TeleportUtils.changeDimensionTo(player, townLevel,
                        new BlockPos(10, 71, 0));
            }
        }
    }
}
