package com.otherworldinn.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

/**
 * Client-side bridge consumed by Otherworld Inn HUD.
 *
 * <p>The returned tag is always a defensive copy and follows FormatVersion 1.
 */
public final class OtherworldInnHudSnapshotApi {
    private static CompoundTag taskHudSnapshot = createEmptySnapshot();

    private OtherworldInnHudSnapshotApi() {}

    public static CompoundTag getTaskHudSnapshot() {
        return taskHudSnapshot.copy();
    }

    public static void updateTaskHudSnapshot(CompoundTag snapshot) {
        taskHudSnapshot = normalizeSnapshot(snapshot);
    }

    public static void clearTaskHudSnapshot() {
        taskHudSnapshot = createEmptySnapshot();
    }

    public static CompoundTag createEmptySnapshot() {
        CompoundTag snapshot = new CompoundTag();
        snapshot.putInt("FormatVersion", 1);
        snapshot.put("Tasks", new ListTag());
        return snapshot;
    }

    private static CompoundTag normalizeSnapshot(CompoundTag snapshot) {
        if (snapshot == null || snapshot.isEmpty()) {
            return createEmptySnapshot();
        }
        CompoundTag normalized = snapshot.copy();
        normalized.putInt("FormatVersion", 1);
        if (!normalized.contains("Tasks")) {
            normalized.put("Tasks", new ListTag());
        }
        return normalized;
    }
}
