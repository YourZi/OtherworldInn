package com.otherworldinn.init;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.item.ChartComponentItem;
import com.otherworldinn.world.expedition.ChartComponentType;
import com.otherworldinn.world.expedition.ExpeditionNbtHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 创造模式选项卡注册 */
public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OtherworldInn.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> OTHERWORLD_INN_TAB =
            CREATIVE_MODE_TABS.register(
                    "otherworld_inn_tab",
                    () ->
                            CreativeModeTab.builder()
                                    .title(Component.translatable("itemGroup.otherworldinn"))
                                    .icon(() -> new ItemStack(ModItems.RECALL_SCROLL.get()))
                                    .displayItems(
                                            (parameters, output) -> {
                                                output.accept(ModItems.RECALL_SCROLL.get());
                                                output.accept(ModItems.ROOM_REGISTER.get());
                                                output.accept(ModItems.BED_SHEET.get());
                                                output.accept(ModItems.MESSY_BED_SHEET.get());
                                                output.accept(ModItems.LAND_DEED.get());
                                                output.accept(ModItems.INN_KEY.get());
                                                output.accept(ModItems.INN_UPGRADE_VOUCHER.get());
                                                output.accept(ModItems.ROOM_KEY.get());
                                                output.accept(ModItems.SPACE_SPHERE.get());
                                                output.accept(ModItems.NETHER_SPACE_SPHERE.get());
                                                output.accept(ModItems.END_SPACE_SPHERE.get());
                                                output.accept(ModItems.FACILITY_UPGRADE_TEMPLATE.get());
                                                output.accept(ModItems.COIN.get());
                                                output.accept(ModBlocks.COMMISSION_BOARD.get());
                                            })
                                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXPEDITION_TAB =
            CREATIVE_MODE_TABS.register(
                    "expedition_tab",
                    () ->
                            CreativeModeTab.builder()
                                    .title(Component.translatable("itemGroup.otherworldinn.expedition"))
                                    .icon(() -> {
                                        ItemStack icon = new ItemStack(ModItems.CHART_COMPONENT.get());
                                        CompoundTag tag = new CompoundTag();
                                        tag.putString("component_type", "ocean_biome");
                                        ExpeditionNbtHelper.writeTag(icon, tag);
                                        return icon;
                                    })
                                    .displayItems(
                                            (parameters, output) -> {
                                                ItemStack maxChart = new ItemStack(ModItems.PIONEER_CHART.get());
                                                CompoundTag chartTag = new CompoundTag();
                                                chartTag.putInt("max_slots", 6);
                                                ExpeditionNbtHelper.writeTag(maxChart, chartTag);
                                                output.accept(maxChart);

                                                ItemStack blankComponent = new ItemStack(ModItems.CHART_COMPONENT.get());
                                                output.accept(blankComponent);

                                                for (ChartComponentType type : ChartComponentType.values()) {
                                                    ItemStack stack = new ItemStack(ModItems.CHART_COMPONENT.get());
                                                    CompoundTag tag = new CompoundTag();
                                                    tag.putString("component_type", type.id());
                                                    ExpeditionNbtHelper.writeTag(stack, tag);
                                                    output.accept(stack);
                                                }
                                            })
                                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RECIPE_BOOKS_TAB =
            CREATIVE_MODE_TABS.register(
                    "recipe_books_tab",
                    () ->
                            CreativeModeTab.builder()
                                    .title(Component.translatable("itemGroup.otherworldinn.recipe_books"))
                                    .icon(() -> new ItemStack(
                                            com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.RECIPE_ITEM
                                                    .get()))
                                    .displayItems(
                                            (parameters, output) -> {
                                                output.accept(new ItemStack(
                                                        com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.RECIPE_ITEM
                                                                .get()));

                                                Minecraft mc = Minecraft.getInstance();
                                                if (mc.level == null) return;
                                                RecipeManager rm = mc.level.getRecipeManager();
                                                Set<ResourceLocation> seen = new HashSet<>();

                                                for (RecipeHolder<PotRecipe> holder : rm
                                                        .getAllRecipesFor(ModRecipes.POT_RECIPE)) {
                                                    PotRecipe recipe = holder.value();
                                                    ItemStack result = recipe.getResultItem(
                                                            mc.level.registryAccess());
                                                    if (result.isEmpty()) continue;
                                                    ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(result.getItem());
                                                    if (!seen.add(foodId)) continue;
                                                    ItemStack recipeStack = new ItemStack(
                                                            com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.RECIPE_ITEM
                                                                    .get());
                                                    RecipeItem.setRecipe(recipeStack,
                                                            new RecipeItem.RecipeRecord(
                                                                    List.of(),
                                                                    result.copy(),
                                                                    RecipeItem.POT));
                                                    output.accept(recipeStack);
                                                }

                                                for (RecipeHolder<StockpotRecipe> holder : rm
                                                        .getAllRecipesFor(ModRecipes.STOCKPOT_RECIPE)) {
                                                    StockpotRecipe recipe = holder.value();
                                                    ItemStack result = recipe.getResultItem(
                                                            mc.level.registryAccess());
                                                    if (result.isEmpty()) continue;
                                                    ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(result.getItem());
                                                    if (!seen.add(foodId)) continue;
                                                    ItemStack recipeStack = new ItemStack(
                                                            com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.RECIPE_ITEM
                                                                    .get());
                                                    RecipeItem.setRecipe(recipeStack,
                                                            new RecipeItem.RecipeRecord(
                                                                    List.of(),
                                                                    result.copy(),
                                                                    RecipeItem.STOCKPOT));
                                                    output.accept(recipeStack);
                                                }

                                                for (RecipeHolder<TeapotRecipe> holder : rm
                                                        .getAllRecipesFor(ModRecipes.TEAPOT_RECIPE)) {
                                                    TeapotRecipe recipe = holder.value();
                                                    ItemStack result = recipe.getResultItem(
                                                            mc.level.registryAccess());
                                                    if (result.isEmpty()) continue;
                                                    ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(result.getItem());
                                                    if (!seen.add(foodId)) continue;
                                                    ItemStack recipeStack = new ItemStack(
                                                            com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.RECIPE_ITEM
                                                                    .get());
                                                    RecipeItem.setRecipe(recipeStack,
                                                            new RecipeItem.RecipeRecord(
                                                                    List.of(),
                                                                    result.copy(),
                                                                    RecipeItem.STOCKPOT));
                                                    output.accept(recipeStack);
                                                }
                                            })
                                    .build());
}
