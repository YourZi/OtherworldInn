package com.otherworldinn.compat.jei;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.inn.service.FurnitureManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI"家具属性"类目插件：动态遍历方块注册表读取 {@link FurnitureManager} 的家具属性配置
 * （直接注册 / 标签 / 关键词规则），按属性组合分组生成配方。
 */
@JeiPlugin
public class FurnitureAttributeJeiPlugin implements IModPlugin {
    public static final RecipeType<FurnitureAttributeJeiRecipe> FURNITURE_ATTRIBUTE_RECIPE_TYPE =
            RecipeType.create(OtherworldInn.MODID, "furniture_attribute", FurnitureAttributeJeiRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "furniture_attribute_jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new FurnitureAttributeJeiCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 属性组合相同的方块合并为一条配方，槽内循环展示
        Map<FurnitureManager.FurnitureStats, List<ItemStack>> grouped = new LinkedHashMap<>();
        for (FurnitureManager.BlockFurniture furniture : FurnitureManager.getAllFurniture()) {
            ItemStack stack = new ItemStack(furniture.block());
            if (stack.isEmpty()) {
                continue;
            }
            grouped.computeIfAbsent(furniture.stats(), k -> new ArrayList<>()).add(stack);
        }
        List<FurnitureAttributeJeiRecipe> recipes = new ArrayList<>();
        grouped.entrySet().stream()
                .map(entry -> new FurnitureAttributeJeiRecipe(entry.getValue(), entry.getKey()))
                .sorted(
                        Comparator.comparingInt(
                                        (FurnitureAttributeJeiRecipe recipe) ->
                                                        Math.abs(recipe.stats().comfort())
                                                                + Math.abs(recipe.stats().light())
                                                                + Math.abs(recipe.stats().humidity()))
                                .reversed())
                .forEach(recipes::add);
        registration.addRecipes(FURNITURE_ATTRIBUTE_RECIPE_TYPE, recipes);
    }
}
