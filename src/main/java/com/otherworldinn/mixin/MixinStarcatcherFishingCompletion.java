package com.otherworldinn.mixin;

import com.otherworldinn.compat.starcatcher.StarcatcherFishingSkillXp;
import com.wdiscute.starcatcher.bobberentity.FishingBobEntity;
import com.wdiscute.starcatcher.io.FishCaughtCounter;
import com.wdiscute.starcatcher.registry.FishProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Pseudo
@Mixin(targets = "com.wdiscute.starcatcher.registry.FishProperties", remap = false)
public abstract class MixinStarcatcherFishingCompletion {
    @Inject(
            method = "spawnFishFromPlayerFishing",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/wdiscute/starcatcher/io/FishCaughtCounter;awardFishCaughtCounter(Lcom/wdiscute/starcatcher/registry/FishProperties;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/entity/player/Player;IIIFZZZ)V",
                            shift = At.Shift.AFTER),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false)
    private static void awardReskillableFishingXp(
            ServerPlayer player,
            int time,
            boolean completedTreasure,
            boolean perfectCatch,
            int hits,
            CallbackInfo ci,
            ServerLevel level,
            Entity levelEntity,
            FishingBobEntity fishingBob,
            FishProperties fishProperties,
            float percentile,
            int size,
            int weight,
            boolean golden) {
        ResourceLocation fishId =
                fishingBob.rlToFish != null
                        ? fishingBob.rlToFish
                        : FishProperties.getKey(player.level(), fishProperties);
        FishCaughtCounter counter =
                fishId != null
                        ? FishCaughtCounter.get(player, fishId)
                        : FishCaughtCounter.get(player, fishProperties);
        boolean firstCatch = counter != null && counter.count() <= 1;
        StarcatcherFishingSkillXp.awardFishingSkillExperience(
                player, fishProperties, firstCatch, perfectCatch, completedTreasure, golden);
    }
}
