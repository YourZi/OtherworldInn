package com.otherworldinn.compat.starcatcher;

import com.wdiscute.starcatcher.bobberentity.FishingBobEntity;
import com.wdiscute.starcatcher.io.FishCaughtCounter;
import com.wdiscute.starcatcher.registry.FishProperties;
import com.wdiscute.starcatcher.registry.catchmodifiers.AbstractCatchModifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ReskillableFishingCatchModifier extends AbstractCatchModifier {
    private final int skillLevel;
    private final double waitBonus;
    private final double newFishBias;
    private final double treasureBonus;
    private final double qualityBonus;
    private FishingBobEntity fishingBob;

    public ReskillableFishingCatchModifier(int skillLevel) {
        this.skillLevel = Math.max(0, skillLevel);
        this.waitBonus = StarcatcherFishingSkillScaling.getWaitBonus(skillLevel);
        this.newFishBias = StarcatcherFishingSkillScaling.getNewFishBias(skillLevel);
        this.treasureBonus = StarcatcherFishingSkillScaling.getTreasureBonus(skillLevel);
        this.qualityBonus = StarcatcherFishingSkillScaling.getQualityBonus(skillLevel);
    }

    @Override
    public void onAdd(FishingBobEntity fishingBobEntity) {
        super.onAdd(fishingBobEntity);
        this.fishingBob = fishingBobEntity;
    }

    @Override
    public int adjustMinTicksToFish(int minTicksToFish) {
        return scaleFishingTicks(minTicksToFish);
    }

    @Override
    public int adjustMaxTicksToFish(int maxTicksToFish) {
        return scaleFishingTicks(maxTicksToFish);
    }

    @Override
    public List<FishProperties> modifyAvailablePool(List<FishProperties> available) {
        if (available.isEmpty() || newFishBias <= 0.0D || fishingBob == null) {
            return available;
        }
        List<FishProperties> adjusted = new ArrayList<>(available.size() * 2);
        for (FishProperties fish : available) {
            adjusted.add(fish);
            if (shouldBiasTowardsFish(fish) && fishingBob.getRandom().nextDouble() < newFishBias) {
                adjusted.add(fish);
            }
        }
        return adjusted;
    }

    @Override
    public boolean forceAwardTreasure(
            FishingBobEntity fishingBobEntity,
            int time,
            boolean completedTreasure,
            boolean perfectCatch,
            int hits) {
        return !completedTreasure
                && treasureBonus > 0.0D
                && fishingBobEntity != null
                && fishingBobEntity.getRandom().nextDouble() < treasureBonus;
    }

    @Override
    public boolean shouldBeGolden() {
        if (qualityBonus <= 0.0D
                || fishingBob == null
                || !(fishingBob.player instanceof ServerPlayer serverPlayer)
                || fishingBob.fpToFish == null
                || !fishingBob.fpToFish.hasGuideEntry()) {
            return false;
        }
        return FishCaughtCounter.canCatchGolden(fishingBob.fpToFish, serverPlayer)
                && fishingBob.getRandom().nextDouble() < qualityBonus;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    private int scaleFishingTicks(int baseTicks) {
        int adjusted = (int) Math.round(baseTicks * (1.0D - waitBonus));
        return Math.max(1, adjusted);
    }

    private boolean shouldBiasTowardsFish(FishProperties fish) {
        if (fishingBob == null || fishingBob.player == null || !fish.hasGuideEntry()) {
            return false;
        }
        ResourceLocation key = FishProperties.getKey(fishingBob.level(), fish);
        return key != null && FishCaughtCounter.get(fishingBob.player, key) == null;
    }
}
