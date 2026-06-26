package com.otherworldinn.compat.starcatcher;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.io.FishCaughtCounter;
import com.wdiscute.starcatcher.io.attachments.FishingGuideAttachment;
import com.wdiscute.starcatcher.registry.FishProperties;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class StarcatcherFishingGuideCompletion {
    private StarcatcherFishingGuideCompletion() {}

    public static StarcatcherCollectionProgress getCurrentProgress(@Nullable ServerPlayer player) {
        return evaluateProgress(player, null, false);
    }

    public static StarcatcherCollectionProgress evaluateAfterCatch(
            @Nullable ServerPlayer player, @Nullable ResourceLocation fishId) {
        return evaluateProgress(player, fishId, true);
    }

    public static boolean hasCompletedCollection(@Nullable ServerPlayer player) {
        return getCurrentProgress(player).complete();
    }

    public static boolean hasJustCompletedCollection(
            @Nullable ServerPlayer player, @Nullable ResourceLocation fishId) {
        return evaluateAfterCatch(player, fishId).justCompleted();
    }

    private static StarcatcherCollectionProgress evaluateProgress(
            @Nullable ServerPlayer player, @Nullable ResourceLocation fishId, boolean evaluateNewCatch) {
        if (player == null || player.level().isClientSide) {
            return StarcatcherCollectionProgress.empty();
        }

        Registry<FishProperties> fishRegistry =
                player.level().registryAccess().registryOrThrow(Starcatcher.FISH_REGISTRY_KEY);
        Map<ResourceLocation, FishCaughtCounter> fishesCaught =
                FishingGuideAttachment.getFishesCaught(player);

        int totalCount = countGuideFishEntries(fishRegistry);
        int caughtCount = countCaughtGuideFishEntries(fishRegistry, fishesCaught);
        boolean newFishCatch =
                evaluateNewCatch && fishId != null && isNewGuideFishCatch(fishRegistry, fishesCaught, fishId);
        boolean complete = totalCount > 0 && caughtCount >= totalCount;
        boolean justCompleted = complete && newFishCatch;

        return new StarcatcherCollectionProgress(
                complete, justCompleted, newFishCatch, caughtCount, totalCount, newFishCatch ? fishId : null);
    }

    private static int countGuideFishEntries(Registry<FishProperties> fishRegistry) {
        int totalCount = 0;
        for (FishProperties fishProperties : fishRegistry) {
            if (fishProperties != null && fishProperties.hasGuideEntry()) {
                totalCount++;
            }
        }
        return totalCount;
    }

    private static int countCaughtGuideFishEntries(
            Registry<FishProperties> fishRegistry, Map<ResourceLocation, FishCaughtCounter> fishesCaught) {
        int caughtCount = 0;
        for (Map.Entry<ResourceLocation, FishCaughtCounter> entry : fishesCaught.entrySet()) {
            FishCaughtCounter counter = entry.getValue();
            if (counter == null || counter.count() <= 0 || !isGuideFish(fishRegistry, entry.getKey())) {
                continue;
            }
            caughtCount++;
        }
        return caughtCount;
    }

    private static boolean isNewGuideFishCatch(
            Registry<FishProperties> fishRegistry,
            Map<ResourceLocation, FishCaughtCounter> fishesCaught,
            ResourceLocation fishId) {
        FishCaughtCounter counter = fishesCaught.get(fishId);
        return counter != null && counter.count() <= 1 && isGuideFish(fishRegistry, fishId);
    }

    private static boolean isGuideFish(
            Registry<FishProperties> fishRegistry, @Nullable ResourceLocation fishId) {
        if (fishId == null) {
            return false;
        }
        FishProperties fishProperties = fishRegistry.get(fishId);
        return fishProperties != null && fishProperties.hasGuideEntry();
    }
}
