package com.otherworldinn.datagen;

import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.init.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

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

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.WRITABLE_BOOK, 1)
                .requires(Items.WRITTEN_BOOK)
                .unlockedBy("has_written_book", has(Items.WRITTEN_BOOK))
                .save(recipeOutput);

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

        TagKey<Item> glassBlocks = TagKey.create(Registries.ITEM,
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c", "glass_blocks"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CRYSTAL_BALL.get(), 1)
                .pattern("GQG")
                .pattern("QSQ")
                .pattern("GQG")
                .define('G', glassBlocks)
                .define('Q', Items.QUARTZ)
                .define('S', ModItems.SPACE_SPHERE.get())
                .unlockedBy("has_space_sphere", has(ModItems.SPACE_SPHERE.get()))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SPACE_SPHERE.get(), 1)
                .requires(ModBlocks.CRYSTAL_BALL.get())
                .unlockedBy("has_crystal_ball", has(ModBlocks.CRYSTAL_BALL.get()))
                .save(recipeOutput, "space_sphere_from_crystal_ball");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.PAPER, 1)
                .requires(ModItems.RECALL_SCROLL.get())
                .unlockedBy("has_recall_scroll", has(ModItems.RECALL_SCROLL.get()))
                .save(recipeOutput);
    }
}
