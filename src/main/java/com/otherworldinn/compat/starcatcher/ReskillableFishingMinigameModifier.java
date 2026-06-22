package com.otherworldinn.compat.starcatcher;

import com.wdiscute.starcatcher.minigame.ActiveSweetSpot;
import com.wdiscute.starcatcher.registry.minigamemodifiers.AbstractMinigameModifier;
import java.util.function.Supplier;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ReskillableFishingMinigameModifier extends AbstractMinigameModifier {
    private final int skillLevel;
    private final double minigameAssist;

    public ReskillableFishingMinigameModifier(int skillLevel) {
        this.skillLevel = Math.max(0, skillLevel);
        this.minigameAssist = StarcatcherFishingSkillScaling.getMinigameAssist(skillLevel);
    }

    @Override
    public ActiveSweetSpot onSpotAdded(ActiveSweetSpot spot) {
        if (spot == null || minigameAssist <= 0.0D) {
            return spot;
        }
        spot.thickness = Math.max(1, (int) Math.round(spot.thickness * (1.0D + minigameAssist)));
        spot.vanishingRate = (float) Math.max(0.01D, spot.vanishingRate * (1.0D - minigameAssist * 0.35D));
        spot.movingRate = (float) Math.max(0.01D, spot.movingRate * (1.0D - minigameAssist * 0.20D));
        return spot;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    @Override
    public com.mojang.serialization.MapCodec<? extends AbstractMinigameModifier> codec() {
        return null;
    }

    @Override
    public DeferredHolder<Supplier<AbstractMinigameModifier>, Supplier<AbstractMinigameModifier>> getRegistryHolder() {
        return null;
    }
}
