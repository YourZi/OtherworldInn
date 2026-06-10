package com.otherworldinn.world.expedition.recipe;

import com.otherworldinn.init.ModItems;
import com.otherworldinn.item.ExpeditionChartItem;
import com.otherworldinn.item.NetherSpaceSphereItem;
import com.otherworldinn.world.expedition.ChartComponentType;
import com.otherworldinn.world.expedition.ExpeditionNbtHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class ChartDimensionRecipe extends CustomRecipe {

    public static RecipeSerializer<?> SERIALIZER;

    public ChartDimensionRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack chart = ItemStack.EMPTY;
        ItemStack sphere = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ExpeditionChartItem) {
                if (!chart.isEmpty()) return false;
                chart = stack;
            } else {
                if (!sphere.isEmpty()) return false;
                sphere = stack;
            }
        }

        if (chart.isEmpty() || sphere.isEmpty()) return false;

        ChartComponentType.DimensionCategory currentDim =
                ExpeditionChartItem.getChartDimension(chart);

        ChartComponentType.DimensionCategory targetDim =
                getDimensionFromSphere(sphere);
        if (targetDim == null) return false;

        return currentDim != targetDim;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack chart = ItemStack.EMPTY;
        ItemStack sphere = ItemStack.EMPTY;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ExpeditionChartItem) {
                chart = stack;
            } else {
                sphere = stack;
            }
        }

        if (chart.isEmpty() || sphere.isEmpty()) return ItemStack.EMPTY;

        ChartComponentType.DimensionCategory targetDim =
                getDimensionFromSphere(sphere);
        if (targetDim == null) return ItemStack.EMPTY;

        ItemStack result = chart.copy();
        CompoundTag tag = ExpeditionNbtHelper.readTag(result);
        tag.putString("dimension_category", targetDim.name());
        ExpeditionNbtHelper.writeTag(result, tag);

        return result;
    }

    private ChartComponentType.DimensionCategory getDimensionFromSphere(ItemStack sphere) {
        if (sphere.getItem() instanceof NetherSpaceSphereItem)
            return ChartComponentType.DimensionCategory.NETHER;
        return null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
