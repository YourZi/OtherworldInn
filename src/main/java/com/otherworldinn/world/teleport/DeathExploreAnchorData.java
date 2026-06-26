package com.otherworldinn.world.teleport;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class DeathExploreAnchorData implements INBTSerializable<CompoundTag> {
    @Nullable private ResourceLocation pendingDeathDimensionId;
    @Nullable private BlockPos pendingDeathPos;
    private boolean clientHasPendingDeathPos;

    public boolean hasPendingDeathPos() {
        return pendingDeathDimensionId != null && pendingDeathPos != null;
    }

    @Nullable
    public BlockPos getPendingDeathPos() {
        return pendingDeathPos;
    }

    @Nullable
    public ResourceKey<Level> getPendingDeathDimension() {
        if (pendingDeathDimensionId == null) {
            return null;
        }
        return ResourceKey.create(Registries.DIMENSION, pendingDeathDimensionId);
    }

    public void setPendingDeathAnchor(ResourceKey<Level> dimension, BlockPos pos) {
        pendingDeathDimensionId = dimension.location();
        pendingDeathPos = pos.immutable();
    }

    public void clearPendingDeathPos() {
        pendingDeathDimensionId = null;
        pendingDeathPos = null;
    }

    public boolean hasClientPendingDeathPos() {
        return clientHasPendingDeathPos;
    }

    public void applyClientSyncSnapshot(boolean hasPendingDeathPos) {
        this.clientHasPendingDeathPos = hasPendingDeathPos;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (pendingDeathDimensionId != null && pendingDeathPos != null) {
            tag.putString("PendingDeathDimension", pendingDeathDimensionId.toString());
            tag.putInt("PendingDeathX", pendingDeathPos.getX());
            tag.putInt("PendingDeathY", pendingDeathPos.getY());
            tag.putInt("PendingDeathZ", pendingDeathPos.getZ());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("PendingDeathDimension")
                && tag.contains("PendingDeathX")
                && tag.contains("PendingDeathY")
                && tag.contains("PendingDeathZ")) {
            ResourceLocation dimensionId =
                    ResourceLocation.tryParse(tag.getString("PendingDeathDimension"));
            if (dimensionId == null) {
                clearPendingDeathPos();
                return;
            }
            pendingDeathDimensionId = dimensionId;
            pendingDeathPos =
                    new BlockPos(
                            tag.getInt("PendingDeathX"),
                            tag.getInt("PendingDeathY"),
                            tag.getInt("PendingDeathZ"));
            return;
        }
        clearPendingDeathPos();
    }
}
