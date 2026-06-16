package com.otherworldinn.datagen;

import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.world.expedition.ChartComponentType;
import com.otherworldinn.world.expedition.ExpeditionNbtHelper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import com.otherworldinn.world.expedition.recipe.ChartAttachmentRecipe;
import com.otherworldinn.world.expedition.recipe.ChartComponentRecipe;
import com.otherworldinn.world.expedition.recipe.ChartDimensionRecipe;

public class ModRecipeProvider extends RecipeProvider {

    private record ComponentCraftEntry(Ingredient corner, Ingredient edge) {
        static ComponentCraftEntry of(Item corner, Item edge) {
            return new ComponentCraftEntry(Ingredient.of(corner), Ingredient.of(edge));
        }
        static ComponentCraftEntry ofCorner(Item corner, Ingredient edge) {
            return new ComponentCraftEntry(Ingredient.of(corner), edge);
        }
    }

    private static final Map<String, ComponentCraftEntry> COMPONENT_CRAFT = new LinkedHashMap<>();

    private static final Map<String, Item> BIOME_CRAFT = new LinkedHashMap<>();

    static {
        COMPONENT_CRAFT.put("mushroom_biome",
                ComponentCraftEntry.of(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM));
        COMPONENT_CRAFT.put("surface_world",
                ComponentCraftEntry.of(Items.COBBLESTONE, Items.DIRT));
        COMPONENT_CRAFT.put("floating_islands",
                ComponentCraftEntry.of(Items.END_STONE, Items.FEATHER));
        COMPONENT_CRAFT.put("amplified_world",
                ComponentCraftEntry.of(Items.MOSSY_COBBLESTONE, Items.STONE));
        COMPONENT_CRAFT.put("cave_world",
                ComponentCraftEntry.of(Items.COBBLESTONE, Items.COBBLED_DEEPSLATE));
        COMPONENT_CRAFT.put("stone_base",
                ComponentCraftEntry.of(Items.STONE, Items.STONE));
        COMPONENT_CRAFT.put("deepslate_base",
                ComponentCraftEntry.of(Items.DEEPSLATE, Items.DEEPSLATE));
        COMPONENT_CRAFT.put("granite_base",
                ComponentCraftEntry.of(Items.GRANITE, Items.GRANITE));
        COMPONENT_CRAFT.put("andesite_base",
                ComponentCraftEntry.of(Items.ANDESITE, Items.ANDESITE));
        COMPONENT_CRAFT.put("diorite_base",
                ComponentCraftEntry.of(Items.DIORITE, Items.DIORITE));
        COMPONENT_CRAFT.put("sandstone_base",
                ComponentCraftEntry.of(Items.SANDSTONE, Items.SANDSTONE));
        COMPONENT_CRAFT.put("tuff_base",
                ComponentCraftEntry.of(Items.TUFF, Items.TUFF));

        BIOME_CRAFT.put("plains_biome", Items.SUNFLOWER);
        BIOME_CRAFT.put("forests_biome", Items.OAK_SAPLING);
        BIOME_CRAFT.put("taigas_biome", Items.SPRUCE_SAPLING);
        BIOME_CRAFT.put("savannas_biome", Items.ACACIA_SAPLING);
        BIOME_CRAFT.put("desert_biome", Items.SAND);
        BIOME_CRAFT.put("snowy_biome", Items.SNOWBALL);
        BIOME_CRAFT.put("jungle_biome", Items.JUNGLE_SAPLING);
        BIOME_CRAFT.put("swamp_biome", Items.LILY_PAD);
        BIOME_CRAFT.put("ocean_biome", Items.KELP);
        BIOME_CRAFT.put("mountain_biome", Items.SNOW_BLOCK);
        BIOME_CRAFT.put("dark_forest_biome", Items.DARK_OAK_SAPLING);
        BIOME_CRAFT.put("sculk_biome", Items.ECHO_SHARD);
        BIOME_CRAFT.put("nether_wastes_biome", Items.NETHERRACK);
        BIOME_CRAFT.put("crimson_biome", Items.CRIMSON_FUNGUS);
        BIOME_CRAFT.put("warped_biome", Items.WARPED_FUNGUS);
        BIOME_CRAFT.put("basalt_biome", Items.BASALT);
        BIOME_CRAFT.put("soul_valley_biome", Items.SOUL_SAND);
    }

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

        SpecialRecipeBuilder.special(
                        (CraftingBookCategory cat) -> new ChartAttachmentRecipe(cat))
                .save(recipeOutput, "chart_attachment");
        SpecialRecipeBuilder.special(
                        (CraftingBookCategory cat) -> new ChartDimensionRecipe(cat))
                .save(recipeOutput, "chart_dimension");
        SpecialRecipeBuilder.special(
                        (CraftingBookCategory cat) -> new ChartComponentRecipe(cat))
                .save(recipeOutput, "chart_component");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CHART_COMPONENT.get(), 2)
                .requires(Items.PAPER, 4)
                .requires(Items.FEATHER, 2)
                .requires(Items.INK_SAC, 2)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(recipeOutput, "blank_chart_component");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.END_SPACE_SPHERE.get(), 1)
                .pattern("BPB")
                .pattern("PSP")
                .pattern("BPB")
                .define('B', Items.BLAZE_POWDER)
                .define('P', Items.ENDER_PEARL)
                .define('S', ModItems.SPACE_SPHERE.get())
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

        for (var entry : BIOME_CRAFT.entrySet()) {
            String componentId = entry.getKey();
            Item material = entry.getValue();
            ChartComponentType type = ChartComponentType.byId(componentId);
            if (type == null) continue;

            ItemStack result = new ItemStack(ModItems.CHART_COMPONENT.get());
            CompoundTag tag = ExpeditionNbtHelper.readTag(result);
            tag.putString("component_type", componentId);
            ExpeditionNbtHelper.writeTag(result, tag);

            Map<Character, Ingredient> keys = Map.of(
                    'M', Ingredient.of(material),
                    'B', Ingredient.of(ModItems.CHART_COMPONENT.get()));

            ShapedRecipePattern pattern = ShapedRecipePattern.of(
                    keys,
                    List.of("MMM", "MBM", "MMM"));

            ShapedRecipe recipe = new ShapedRecipe("", CraftingBookCategory.MISC,
                    pattern, result);

            recipeOutput.accept(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                            "otherworldinn", "chart_component_" + componentId),
                    recipe,
                    null);
        }

        for (var entry : COMPONENT_CRAFT.entrySet()) {
            String componentId = entry.getKey();
            ComponentCraftEntry materials = entry.getValue();
            ChartComponentType type = ChartComponentType.byId(componentId);
            if (type == null) continue;

            ItemStack result = new ItemStack(ModItems.CHART_COMPONENT.get());
            CompoundTag tag = ExpeditionNbtHelper.readTag(result);
            tag.putString("component_type", componentId);
            ExpeditionNbtHelper.writeTag(result, tag);

            Map<Character, Ingredient> keys = Map.of(
                    'C', materials.corner(),
                    'E', materials.edge(),
                    'B', Ingredient.of(ModItems.CHART_COMPONENT.get()));

            ShapedRecipePattern pattern = ShapedRecipePattern.of(
                    keys,
                    List.of("CEC", "EBE", "CEC"));

            ShapedRecipe recipe = new ShapedRecipe("", CraftingBookCategory.MISC,
                    pattern, result);

            recipeOutput.accept(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                            "otherworldinn", "chart_component_" + componentId),
                    recipe,
                    null);
        }
    }
}
