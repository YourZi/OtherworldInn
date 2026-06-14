package com.otherworldinn.world.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * 水晶球持久化数据
 *
 * <p>存储当前世界唯一水晶球的位置，确保同一存档只有一个水晶球。
 */
public class CrystalBallSavedData extends SavedData {
    private static final String DATA_NAME = "otherworldinn_crystal_ball";

    @Nullable
    private BlockPos ballPos;

    public static CrystalBallSavedData get(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(
                                CrystalBallSavedData::new,
                                CrystalBallSavedData::load,
                                null),
                        DATA_NAME);
    }

    public CrystalBallSavedData() {}

    public static CrystalBallSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        CrystalBallSavedData data = new CrystalBallSavedData();
        if (tag.contains("X")) {
            data.ballPos = new BlockPos(
                    tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(
            @NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        if (ballPos != null) {
            tag.putInt("X", ballPos.getX());
            tag.putInt("Y", ballPos.getY());
            tag.putInt("Z", ballPos.getZ());
        }
        return tag;
    }

    @Nullable
    public BlockPos getBallPos() {
        return ballPos;
    }

    public void setBallPos(@Nullable BlockPos pos) {
        this.ballPos = pos;
        this.setDirty();
    }
}
