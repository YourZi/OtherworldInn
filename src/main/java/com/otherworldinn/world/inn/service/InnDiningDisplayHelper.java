package com.otherworldinn.world.inn.service;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public final class InnDiningDisplayHelper {
    private static final ResourceLocation CREATE_DEPOT_ID =
            ResourceLocation.fromNamespaceAndPath("create", "depot");

    private InnDiningDisplayHelper() {}

    public static boolean isDiningDisplay(BlockState state) {
        if (state == null) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return CREATE_DEPOT_ID.equals(id);
    }

    @Nullable
    public static IItemHandler getDisplayItemHandler(ServerLevel level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, blockEntity, null);
    }

    public static boolean hasSellableItem(ServerLevel level, BlockPos pos, BlockState state) {
        IItemHandler itemHandler = getDisplayItemHandler(level, pos, state);
        if (itemHandler == null) {
            return false;
        }
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (InnMenuDishRegistry.isMenuDish(stack)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static ResourceLocation resolveDisplayedMenuDishId(
            ServerLevel level, BlockPos pos, BlockState state) {
        IItemHandler itemHandler = getDisplayItemHandler(level, pos, state);
        if (itemHandler == null) {
            return null;
        }
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ResourceLocation dishId =
                    InnMenuDishRegistry.resolveMenuDishId(itemHandler.getStackInSlot(slot));
            if (dishId != null) {
                return dishId;
            }
        }
        return null;
    }
}
