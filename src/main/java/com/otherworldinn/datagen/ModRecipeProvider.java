package com.otherworldinn.datagen;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.init.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(
            PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ROOM_KEY.get(), 2)
                .pattern("G ")
                .pattern(" I")
                .define('G', Items.GOLD_INGOT)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ROOM_REGISTER.get(), 1)
                .requires(ModItems.ROOM_KEY.get())
                .requires(Items.BOOK)
                .unlockedBy("has_room_key", has(ModItems.ROOM_KEY.get()))
                .unlockedBy("has_book", has(Items.BOOK))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.INN_KEY.get(), 1)
                .pattern("G ")
                .pattern(" G")
                .define('G', Items.GOLD_INGOT)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BED_SHEET.get(), 1)
                .pattern("WWW")
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_wool", has(ItemTags.WOOL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PICNIC_BOX.get(), 1)
                .pattern("SLS")
                .pattern("P P")
                .pattern("PPP")
                .define('S', Items.STRING)
                .define('L', Items.LEATHER)
                .define('P', ItemTags.PLANKS)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(recipeOutput);

        // 使用本模组命名空间保存，避免覆盖原版书与笔配方
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WRITABLE_BOOK, 1)
                .requires(Items.WRITTEN_BOOK)
                .unlockedBy("has_written_book", has(Items.WRITTEN_BOOK))
                .save(
                        recipeOutput,
                        ResourceLocation.fromNamespaceAndPath(
                                OtherworldInn.MODID, "writable_book_from_written_book"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.END_SPACE_SPHERE.get(), 1)
                .pattern("EPE")
                .pattern("PSP")
                .pattern("EPE")
                .define('E', Items.END_STONE)
                .define('P', Items.ENDER_PEARL)
                .define('S', ModItems.NETHER_SPACE_SPHERE.get())
                .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.NETHER_SPACE_SPHERE.get(), 1)
                .pattern("ONO")
                .pattern("NSN")
                .pattern("ONO")
                .define('O', Items.OBSIDIAN)
                .define('N', Items.NETHERRACK)
                .define('S', ModItems.SPACE_SPHERE.get())
                .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SPACE_SPHERE.get(), 1)
                .requires(ModBlocks.CRYSTAL_BALL.get())
                .unlockedBy("has_crystal_ball", has(ModBlocks.CRYSTAL_BALL.get()))
                .save(recipeOutput, "space_sphere_from_crystal_ball");

        // 使用本模组命名空间保存，避免覆盖原版甘蔗→纸配方
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.PAPER, 1)
                .requires(ModItems.RECALL_SCROLL.get())
                .unlockedBy("has_recall_scroll", has(ModItems.RECALL_SCROLL.get()))
                .save(
                        recipeOutput,
                        ResourceLocation.fromNamespaceAndPath(
                                OtherworldInn.MODID, "paper_from_recall_scroll"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.EMERALD, 1)
                .requires(ModItems.COIN.get(), 2)
                .unlockedBy("has_coin", has(ModItems.COIN.get()))
                .save(
                        recipeOutput,
                        ResourceLocation.fromNamespaceAndPath(
                                OtherworldInn.MODID, "emerald_from_coin"));
    }
}
