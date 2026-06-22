package com.otherworldinn.compat.jei;

import com.otherworldinn.init.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;

final class NpcStoreJeiData {
    private static final String FARMER = "entity.otherworldinn.farmer";
    private static final String BLACKSMITH = "entity.otherworldinn.blacksmith";
    private static final String MAGICIAN = "entity.otherworldinn.magician";
    private static final String GROCER = "entity.otherworldinn.grocer";
    private static final String ADV_ENTER_NETHER_TITLE = "advancements.story.enter_the_nether.title";
    private static final HolderLookup.Provider VANILLA_LOOKUP = VanillaRegistries.createLookup();

    private NpcStoreJeiData() {}

    static List<NpcStoreJeiRecipe> getAllRecipes() {
        List<NpcStoreJeiRecipe> recipes = new ArrayList<>();
        addFarmer(recipes);
        addBlacksmith(recipes);
        addMagician(recipes);
        addGrocer(recipes);
        return List.copyOf(recipes);
    }

    private static void addFarmer(List<NpcStoreJeiRecipe> recipes) {
        add(recipes, FARMER, new ItemStack(Items.SUGAR_CANE), 2, 2, 64, 64, 1, null, true);
        add(recipes, FARMER, new ItemStack(Items.WHEAT_SEEDS), 1, 1, 16, 16, 1, null, true);
        add(recipes, FARMER, new ItemStack(Items.BEETROOT_SEEDS), 1, 1, 16, 16, 1, null, true);
        add(recipes, FARMER, new ItemStack(Items.CARROT), 2, 2, 64, 64, 1, null, true);
        add(recipes, FARMER, new ItemStack(Items.POTATO), 2, 2, 64, 64, 1, null, true);
        add(recipes, FARMER, new ItemStack(Items.PUMPKIN_SEEDS), 4, 4, 16, 16, 1, null, true);
        add(recipes, FARMER, new ItemStack(Items.MELON_SEEDS), 1, 1, 16, 16, 1, null, true);
        add(recipes, FARMER, new ItemStack(Items.GLOW_BERRIES), 2, 2, 64, 64, 1, null, true);
        add(recipes, FARMER, new ItemStack(Items.SWEET_BERRIES), 2, 2, 64, 64, 1, null, true);
        add(
                recipes,
                FARMER,
                new ItemStack(com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.TOMATO_SEED.get()),
                2,
                2,
                16,
                16,
                1,
                null,
                true);
        add(
                recipes,
                FARMER,
                new ItemStack(com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.CHILI_SEED.get()),
                2,
                2,
                16,
                16,
                1,
                null,
                true);
        add(
                recipes,
                FARMER,
                new ItemStack(com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.LETTUCE_SEED.get()),
                2,
                2,
                16,
                16,
                1,
                null,
                true);
        add(
                recipes,
                FARMER,
                new ItemStack(com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems.WILD_RICE_SEED.get()),
                2,
                2,
                16,
                16,
                1,
                null,
                true);
        add(
                recipes,
                FARMER,
                new ItemStack(com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems.GRAPEVINE.get()),
                4,
                4,
                64,
                64,
                1,
                null,
                true);

        add(recipes, FARMER, new ItemStack(Items.BONE_MEAL), 4, 4, 64, 64, 2, null, false);
        add(recipes, FARMER, new ItemStack(AllItems.TREE_FERTILIZER.get()), 6, 6, 64, 64, 4, null, false);
        add(recipes, FARMER, new ItemStack(Items.BONE_MEAL), 1, 1, 32, 32, 6, null, false);
        add(recipes, FARMER, new ItemStack(Items.NETHERITE_HOE), 64, 64, 1, 1, 8, null, false);
    }

    private static void addBlacksmith(List<NpcStoreJeiRecipe> recipes) {
        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_ORE), 8, 8, 16, 16, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(AllBlocks.ZINC_ORE.get()), 6, 6, 16, 16, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(Items.COPPER_ORE), 6, 6, 16, 16, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(Items.GOLD_ORE), 12, 12, 8, 8, 1, null, false);

        add(recipes, BLACKSMITH, new ItemStack(Items.RAW_IRON), 5, 5, 64, 64, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(AllItems.RAW_ZINC.get()), 4, 4, 64, 64, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(Items.RAW_COPPER), 3, 3, 64, 64, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(Items.RAW_GOLD), 8, 8, 32, 32, 1, null, false);

        add(recipes, BLACKSMITH, new ItemStack(AllItems.CRUSHED_IRON.get()), 5, 5, 32, 32, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(AllItems.CRUSHED_ZINC.get()), 4, 4, 32, 32, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(AllItems.CRUSHED_COPPER.get()), 3, 3, 32, 32, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(AllItems.CRUSHED_GOLD.get()), 8, 8, 16, 16, 1, null, false);

        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_INGOT), 10, 10, 32, 32, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(AllItems.ZINC_INGOT.get()), 8, 8, 32, 32, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(Items.COPPER_INGOT), 6, 6, 32, 32, 1, null, false);
        add(recipes, BLACKSMITH, new ItemStack(Items.GOLD_INGOT), 15, 15, 16, 16, 1, null, false);

        add(recipes, BLACKSMITH, new ItemStack(AllItems.ANDESITE_ALLOY.get()), 4, 4, 32, 32, 2, null, false);
        add(recipes, BLACKSMITH, new ItemStack(AllItems.BRASS_INGOT.get()), 10, 10, 32, 32, 4, null, false);
        add(
                recipes,
                BLACKSMITH,
                new ItemStack(AllItems.POLISHED_ROSE_QUARTZ.get()),
                12,
                12,
                32,
                32,
                6,
                null,
                false);
        add(recipes, BLACKSMITH, new ItemStack(AllItems.STURDY_SHEET.get()), 8, 8, 32, 32, 8, null, false);

        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_PICKAXE), 25, 35, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_AXE), 20, 30, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_SHOVEL), 10, 20, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_SWORD), 15, 25, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_HOE), 10, 20, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.GOLDEN_PICKAXE), 20, 30, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.GOLDEN_SWORD), 20, 30, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_HELMET), 28, 40, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_CHESTPLATE), 40, 58, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_LEGGINGS), 36, 52, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.IRON_BOOTS), 24, 36, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.CHAINMAIL_HELMET), 18, 28, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.CHAINMAIL_CHESTPLATE), 30, 44, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.CHAINMAIL_LEGGINGS), 26, 38, 1, 1, 1, null, true);
        add(recipes, BLACKSMITH, new ItemStack(Items.CHAINMAIL_BOOTS), 16, 24, 1, 1, 1, null, true);
    }

    private static void addMagician(List<NpcStoreJeiRecipe> recipes) {
        add(recipes, MAGICIAN, new ItemStack(Items.LAPIS_LAZULI), 8, 8, 64, 64, 1, null, false);
        add(recipes, MAGICIAN, new ItemStack(Items.EMERALD), 12, 12, 32, 32, 1, null, false);
        add(recipes, MAGICIAN, new ItemStack(Items.AMETHYST_SHARD), 4, 4, 64, 64, 1, null, false);
        add(recipes, MAGICIAN, new ItemStack(Items.REDSTONE), 2, 2, 64, 64, 1, null, false);
        add(recipes, MAGICIAN, new ItemStack(Items.GUNPOWDER), 2, 2, 64, 64, 1, null, false);
        add(recipes, MAGICIAN, new ItemStack(Items.GLASS_BOTTLE), 2, 2, 64, 64, 1, null, false);
        add(recipes, MAGICIAN, new ItemStack(Items.BLAZE_ROD), 16, 16, 32, 32, 1, null, false);
        add(recipes, MAGICIAN, new ItemStack(Items.GLOWSTONE_DUST), 8, 8, 48, 48, 1, null, false);

        add(recipes, MAGICIAN, new ItemStack(ModItems.SPACE_SPHERE.get()), 64, 64, 4, 4, 2, null, false);
        add(recipes, MAGICIAN, new ItemStack(Items.BOOK), 8, 8, 64, 64, 2, null, false);
        add(recipes, MAGICIAN, new ItemStack(Items.DIAMOND), 64, 64, 8, 8, 4, null, false);
        add(recipes, MAGICIAN, enchantedBook(Enchantments.MENDING, 1), 128, 128, 2, 2, 4, null, false);
        add(
                recipes,
                MAGICIAN,
                new ItemStack(Items.QUARTZ),
                16,
                16,
                32,
                32,
                1,
                ADV_ENTER_NETHER_TITLE,
                false);
        add(
                recipes,
                MAGICIAN,
                new ItemStack(Items.ENDER_PEARL),
                64,
                64,
                16,
                16,
                1,
                ADV_ENTER_NETHER_TITLE,
                false);
        add(recipes, MAGICIAN, enchantedBook(Enchantments.SWIFT_SNEAK, 3), 256, 256, 2, 2, 6, null, false);
        add(recipes, MAGICIAN, new ItemStack(Items.EXPERIENCE_BOTTLE), 32, 32, 16, 16, 8, null, false);
        add(recipes, MAGICIAN, enchantedBook(Enchantments.WIND_BURST, 3), 256, 256, 2, 2, 8, null, false);

        add(recipes, MAGICIAN, new ItemStack(Items.ENCHANTED_BOOK), 32, 162, 1, 2, 1, null, true);
    }

    private static void addGrocer(List<NpcStoreJeiRecipe> recipes) {
        addById(recipes, GROCER, "sophisticatedbackpacks:upgrade_base", 12, 12, 16, 16, 1, null, false);
        addById(recipes, GROCER, "refinedstorage:storage_housing", 12, 12, 16, 16, 1, null, false);
        addById(recipes, GROCER, "otherworldinn:land_deed", 32, 32, 1, 1, 1, null, false);
        addById(recipes, GROCER, "otherworldinn:inn_upgrade_voucher", 128, 128, 1, 1, 1, null, false);
        addById(
                recipes,
                GROCER,
                "otherworldinn:facility_upgrade_template",
                32,
                32,
                4,
                4,
                1,
                null,
                false);

        addById(recipes, GROCER, "minecraft:oak_planks", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:spruce_planks", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:birch_planks", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:jungle_planks", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:acacia_planks", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:dark_oak_planks", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:mangrove_planks", 3, 3, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:cherry_planks", 3, 3, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:bamboo_planks", 3, 3, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:crimson_planks", 3, 3, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:warped_planks", 3, 3, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:cobblestone", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:stone", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:granite", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:diorite", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:andesite", 2, 2, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:tuff", 3, 3, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:calcite", 3, 3, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:cobbled_deepslate", 3, 3, 64, 64, 1, null, false);
        addById(recipes, GROCER, "minecraft:white_wool", 2, 2, 64, 64, 1, null, false);

        addById(recipes, GROCER, "refinedstorage:1k_storage_part", 32, 32, 8, 8, 2, null, false);
        addById(recipes, GROCER, "refinedstorage:4k_storage_part", 64, 64, 6, 6, 4, null, false);
        addById(recipes, GROCER, "refinedstorage:16k_storage_part", 128, 128, 4, 4, 6, null, false);
        addById(recipes, GROCER, "refinedstorage:64k_storage_part", 256, 256, 2, 2, 8, null, false);
    }

    private static void addById(
            List<NpcStoreJeiRecipe> recipes,
            String storeNameKey,
            String itemId,
            int minPrice,
            int maxPrice,
            int minStock,
            int maxStock,
            int requiredFavorLevel,
            String requiredAdvancementTitleKey,
            boolean randomOffer) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return;
        }
        BuiltInRegistries.ITEM
                .getOptional(id)
                .ifPresent(
                        item ->
                                add(
                                        recipes,
                                        storeNameKey,
                                        new ItemStack(item),
                                        minPrice,
                                        maxPrice,
                                        minStock,
                                        maxStock,
                                        requiredFavorLevel,
                                        requiredAdvancementTitleKey,
                                        randomOffer));
    }

    private static void add(
            List<NpcStoreJeiRecipe> recipes,
            String storeNameKey,
            ItemStack output,
            int minPrice,
            int maxPrice,
            int minStock,
            int maxStock,
            int requiredFavorLevel,
            String requiredAdvancementTitleKey,
            boolean randomOffer) {
        if (output.isEmpty() || output.getItem() == Items.AIR) {
            return;
        }
        recipes.add(
                new NpcStoreJeiRecipe(
                        storeNameKey,
                        output,
                        minPrice,
                        maxPrice,
                        minStock,
                        maxStock,
                        requiredFavorLevel,
                        requiredAdvancementTitleKey,
                        randomOffer));
    }

    private static ItemStack enchantedBook(net.minecraft.resources.ResourceKey<Enchantment> enchantmentKey, int level) {
        var enchantment = VANILLA_LOOKUP.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantmentKey);
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
    }
}
