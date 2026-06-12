package com.otherworldinn.foundation;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** 蓝图生存打印功能的 NBT 帮助类。 */
public final class SchematicSurvivalPrintHelper {
    private static final String KEY = "otherworldinn:survival_print";

    private SchematicSurvivalPrintHelper() {}

    public static boolean isEnabled(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.getBoolean(KEY);
    }

    public static void toggle(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(KEY, !tag.getBoolean(KEY));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
