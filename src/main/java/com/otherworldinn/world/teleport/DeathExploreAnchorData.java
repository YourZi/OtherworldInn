package com.otherworldinn.world.teleport;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class DeathExploreAnchorData implements INBTSerializable<CompoundTag> {
    @Nullable private BlockPos pendingDeathPos;

    public boolean hasPendingDeathPos() {
        return pendingDeathPos != null;
    }

    @Nullable
    public BlockPos getPendingDeathPos() {
        return pendingDeathPos;
    }

    public void setPendingDeathPos(BlockPos pos) {
        pendingDeathPos = pos.immutable();
    }

    public void clearPendingDeathPos() {
        pendingDeathPos = null;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (pendingDeathPos != null) {
            tag.putInt("PendingDeathX", pendingDeathPos.getX());
            tag.putInt("PendingDeathY", pendingDeathPos.getY());
            tag.putInt("PendingDeathZ", pendingDeathPos.getZ());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("PendingDeathX") && tag.contains("PendingDeathY") && tag.contains("PendingDeathZ")) {
            pendingDeathPos =
                    new BlockPos(
                            tag.getInt("PendingDeathX"),
                            tag.getInt("PendingDeathY"),
                            tag.getInt("PendingDeathZ"));
            return;
        }
        pendingDeathPos = null;
    }
}
