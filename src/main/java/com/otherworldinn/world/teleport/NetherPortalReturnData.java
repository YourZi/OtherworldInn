package com.otherworldinn.world.teleport;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * 玩家进入下界时的来源记录：用于决定从下界传送门返回时
 * 是回到城镇对应传送门（来源为城镇）还是执行原版默认行为。
 */
public class NetherPortalReturnData implements INBTSerializable<CompoundTag> {
    private boolean returnToTown;

    public boolean shouldReturnToTown() {
        return returnToTown;
    }

    public void setReturnToTown(boolean returnToTown) {
        this.returnToTown = returnToTown;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("ReturnToTown", returnToTown);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        returnToTown = tag.getBoolean("ReturnToTown");
    }
}
