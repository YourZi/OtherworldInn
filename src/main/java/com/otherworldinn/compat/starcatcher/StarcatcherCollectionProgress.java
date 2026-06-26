package com.otherworldinn.compat.starcatcher;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

public record StarcatcherCollectionProgress(
        boolean complete,
        boolean justCompleted,
        boolean newFishCatch,
        int caughtCount,
        int totalCount,
        @Nullable ResourceLocation newlyCaughtFishId) {

    public static StarcatcherCollectionProgress empty() {
        return new StarcatcherCollectionProgress(false, false, false, 0, 0, null);
    }
}
