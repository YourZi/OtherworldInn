package com.otherworldinn.world.event.listener;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CFatigueSyncPacket;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.fatigue.FatigueCalculator;
import com.otherworldinn.world.fatigue.FatigueData;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class PlayerFatigueHandler {
    private static final int EFFECT_REFRESH_DURATION = 50;
    private static final int MAX_FATIGUE_POISON_DURATION = 220;
    private static final String FATIGUE_STAGE_1_KEY = "message.otherworldinn.fatigue.stage_1";
    private static final String FATIGUE_STAGE_2_KEY = "message.otherworldinn.fatigue.stage_2";
    private static final String FATIGUE_STAGE_3_KEY = "message.otherworldinn.fatigue.stage_3";
    private static final String FATIGUE_STAGE_4_KEY = "message.otherworldinn.fatigue.stage_4";

    private PlayerFatigueHandler() {}

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!tracksFatigue(player.level(), player)) {
            return;
        }
        player.getData(ModAttachments.PLAYER_FATIGUE).recordBlockPlaced();
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled()) {
            return;
        }
        Player rawPlayer = event.getPlayer();
        if (!(rawPlayer instanceof ServerPlayer player)) {
            return;
        }
        if (!tracksFatigue(player.level(), player)) {
            return;
        }
        player.getData(ModAttachments.PLAYER_FATIGUE).recordBlockBroken();
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!tracksFatigue(player.level(), player)) {
            return;
        }
        if (event.getNewDamage() <= 0.0F) {
            return;
        }
        player.getData(ModAttachments.PLAYER_FATIGUE).recordDamageTaken(event.getNewDamage());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }

        FatigueData data = player.getData(ModAttachments.PLAYER_FATIGUE);
        FatigueCalculator.FatigueStage previousStage = FatigueCalculator.getStage(data.getFatigue());
        FatigueCalculator.FatigueComputation computation =
                FatigueCalculator.evaluateSecond(player, data);
        data.addFatigue(computation.totalGain());
        data.storeComputation(computation);

        sendFatigueSync(player, data, computation);

        if (shouldRecoverInTown(player.level(), player)) {
            data.reduceFatigue(FatigueCalculator.getTownRecoveryPerSecond(data.getFatigue()));
            data.setLastNotifiedStage(FatigueCalculator.getStage(data.getFatigue()).level());
            return;
        }

        if (!tracksFatigue(player.level(), player)) {
            return;
        }

        FatigueCalculator.FatigueStage currentStage = FatigueCalculator.getStage(data.getFatigue());
        notifyStageMilestone(player, data, previousStage, currentStage);
        applyMaxFatiguePoison(player, data);
        applyStageEffects(player, currentStage);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!event.isWasDeath()) {
            return;
        }
        if (!event.getOriginal().hasData(ModAttachments.PLAYER_FATIGUE)) {
            return;
        }
        FatigueData clonedData = player.getData(ModAttachments.PLAYER_FATIGUE);
        clonedData.deserializeNBT(
                player.registryAccess(),
                event.getOriginal()
                        .getData(ModAttachments.PLAYER_FATIGUE)
                        .serializeNBT(event.getOriginal().registryAccess()));
    }

    private static boolean tracksFatigue(Level level, ServerPlayer player) {
        return level.dimension() != TownDimensions.TOWN_LEVEL
                && !player.isCreative()
                && !player.isSpectator();
    }

    private static boolean shouldRecoverInTown(Level level, ServerPlayer player) {
        return level.dimension() == TownDimensions.TOWN_LEVEL
                && !player.isCreative()
                && !player.isSpectator();
    }

    private static void notifyStageMilestone(
            ServerPlayer player,
            FatigueData data,
            FatigueCalculator.FatigueStage previousStage,
            FatigueCalculator.FatigueStage currentStage) {
        if (currentStage.level() < data.getLastNotifiedStage()) {
            data.setLastNotifiedStage(currentStage.level());
            return;
        }
        if (currentStage.level() <= previousStage.level() || currentStage.level() <= data.getLastNotifiedStage()) {
            return;
        }

        String key = switch (currentStage) {
            case _1 -> FATIGUE_STAGE_1_KEY;
            case _2 -> FATIGUE_STAGE_2_KEY;
            case _3 -> FATIGUE_STAGE_3_KEY;
            case _4 -> FATIGUE_STAGE_4_KEY;
            default -> null;
        };
        if (key == null) {
            return;
        }

        player.sendSystemMessage(Component.translatable(key).withStyle(style -> style.withColor(ModColors.ERROR)));
        data.setLastNotifiedStage(currentStage.level());
    }

    private static void applyMaxFatiguePoison(ServerPlayer player, FatigueData data) {
        if (data.getFatigue() < FatigueData.MAX_FATIGUE) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.POISON, MAX_FATIGUE_POISON_DURATION, 0, false, true));
    }

    private static void applyStageEffects(
            ServerPlayer player, FatigueCalculator.FatigueStage fatigueStage) {
        applyEffect(player, MobEffects.MOVEMENT_SLOWDOWN, fatigueStage.slownessAmplifier());
        applyEffect(player, MobEffects.DIG_SLOWDOWN, fatigueStage.miningFatigueAmplifier());
        applyEffect(player, MobEffects.WEAKNESS, fatigueStage.weaknessAmplifier());

        if (fatigueStage.blindnessDuration() > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, fatigueStage.blindnessDuration(), 0, false, true));
        }
    }

    private static void applyEffect(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        if (amplifier < 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, EFFECT_REFRESH_DURATION, amplifier, false, true));
    }

    private static void sendFatigueSync(
            ServerPlayer player, FatigueData data, FatigueCalculator.FatigueComputation computation) {
        FatigueCalculator.FatigueStage stage = FatigueCalculator.getStage(data.getFatigue());
        double journeyMinutes = data.getCurrentJourneyTicks() / 1200.0D;

        ModMessages.sendToPlayer(
                new S2CFatigueSyncPacket(
                        data.getFatigue(),
                        stage.level(),
                        computation.totalGain(),
                        computation.dimensionMultiplier(),
                        journeyMinutes),
                player);
    }

    public static void syncCurrentState(ServerPlayer player) {
        FatigueData data = player.getData(ModAttachments.PLAYER_FATIGUE);
        FatigueCalculator.FatigueStage stage = FatigueCalculator.getStage(data.getFatigue());
        double journeyMinutes = data.getCurrentJourneyTicks() / 1200.0D;

        ModMessages.sendToPlayer(
                new S2CFatigueSyncPacket(
                        data.getFatigue(),
                        stage.level(),
                        data.getLastSecondGain(),
                        data.getLastDimensionMultiplier(),
                        journeyMinutes),
                player);
    }
}
