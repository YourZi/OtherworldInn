package com.otherworldinn.world.expedition;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

/**
 * 远征全局持久化数据，存储跨会话的计数器。
 */
public class ExpeditionSavedData extends SavedData {
    private static final String DATA_NAME = "otherworldinn_expedition";

    private int counter = 1;

    public static ExpeditionSavedData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(
                                ExpeditionSavedData::new,
                                ExpeditionSavedData::load,
                                null),
                        DATA_NAME);
    }

    public ExpeditionSavedData() {}

    public static ExpeditionSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        ExpeditionSavedData data = new ExpeditionSavedData();
        data.counter = tag.getInt("counter");
        if (data.counter < 1) data.counter = 1;
        return data;
    }

    @Override
    public @NotNull CompoundTag save(
            @NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        tag.putInt("counter", counter);
        return tag;
    }

    public int getCounter() {
        return counter;
    }

    public int incrementAndGet() {
        int val = counter;
        counter++;
        setDirty();
        return val;
    }
}
