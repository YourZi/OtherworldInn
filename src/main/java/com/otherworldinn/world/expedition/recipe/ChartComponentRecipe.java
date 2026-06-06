package com.otherworldinn.world.expedition.recipe;

import com.otherworldinn.item.ChartComponentItem;
import com.otherworldinn.world.expedition.ChartComponentType;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import com.otherworldinn.world.expedition.ExpeditionNbtHelper;

public class ChartComponentRecipe extends CustomRecipe {

    public static RecipeSerializer<?> SERIALIZER;

    private static final Map<Item, String> ITEM_TO_COMPONENT = new LinkedHashMap<>();

    static {
        ITEM_TO_COMPONENT.put(Items.GRASS_BLOCK, "surface_world");
        ITEM_TO_COMPONENT.put(Items.END_STONE, "floating_islands");
        ITEM_TO_COMPONENT.put(Items.MOSSY_COBBLESTONE, "amplified_world");
        ITEM_TO_COMPONENT.put(Items.DEEPSLATE, "cave_world");
        ITEM_TO_COMPONENT.put(Items.NETHER_BRICKS, "nether_cave");
        ITEM_TO_COMPONENT.put(Items.END_STONE_BRICKS, "end_void");

        ITEM_TO_COMPONENT.put(Items.SUNFLOWER, "plains_biome");
        ITEM_TO_COMPONENT.put(Items.OAK_SAPLING, "forests_biome");
        ITEM_TO_COMPONENT.put(Items.SPRUCE_SAPLING, "taigas_biome");
        ITEM_TO_COMPONENT.put(Items.ACACIA_SAPLING, "savannas_biome");
        ITEM_TO_COMPONENT.put(Items.SAND, "desert_biome");
        ITEM_TO_COMPONENT.put(Items.SNOWBALL, "snowy_biome");
        ITEM_TO_COMPONENT.put(Items.JUNGLE_SAPLING, "jungle_biome");
        ITEM_TO_COMPONENT.put(Items.LILY_PAD, "swamp_biome");
        ITEM_TO_COMPONENT.put(Items.WATER_BUCKET, "ocean_biome");
        ITEM_TO_COMPONENT.put(Items.SNOW_BLOCK, "mountain_biome");
        ITEM_TO_COMPONENT.put(Items.RED_MUSHROOM, "mushroom_biome");
        ITEM_TO_COMPONENT.put(Items.DARK_OAK_SAPLING, "dark_forest_biome");
        ITEM_TO_COMPONENT.put(Items.SCULK_CATALYST, "sculk_biome");

        ITEM_TO_COMPONENT.put(Items.NETHERRACK, "nether_wastes_biome");
        ITEM_TO_COMPONENT.put(Items.CRIMSON_FUNGUS, "crimson_biome");
        ITEM_TO_COMPONENT.put(Items.WARPED_FUNGUS, "warped_biome");
        ITEM_TO_COMPONENT.put(Items.BASALT, "basalt_biome");
        ITEM_TO_COMPONENT.put(Items.SOUL_SAND, "soul_valley_biome");

        ITEM_TO_COMPONENT.put(Items.CHORUS_FLOWER, "end_highlands_biome");
        ITEM_TO_COMPONENT.put(Items.CHORUS_FRUIT, "end_islands_biome");

        ITEM_TO_COMPONENT.put(Items.STONE, "stone_base");
        ITEM_TO_COMPONENT.put(Items.POLISHED_DEEPSLATE, "deepslate_base");
        ITEM_TO_COMPONENT.put(Items.GRANITE, "granite_base");
        ITEM_TO_COMPONENT.put(Items.ANDESITE, "andesite_base");
        ITEM_TO_COMPONENT.put(Items.DIORITE, "diorite_base");
        ITEM_TO_COMPONENT.put(Items.SANDSTONE, "sandstone_base");
        ITEM_TO_COMPONENT.put(Items.TUFF, "tuff_base");

        ITEM_TO_COMPONENT.put(Items.TRIDENT, "thunderstorm");
        ITEM_TO_COMPONENT.put(Items.GLOWSTONE_DUST, "eternal_day");
        ITEM_TO_COMPONENT.put(Items.CLOCK, "eternal_night");
        ITEM_TO_COMPONENT.put(Items.EMERALD, "thriving_realm");
        ITEM_TO_COMPONENT.put(Items.LAVA_BUCKET, "lava_flood");
        ITEM_TO_COMPONENT.put(Items.FEATHER, "gravity_low");
        ITEM_TO_COMPONENT.put(Items.DEAD_BUSH, "dry_land");
        ITEM_TO_COMPONENT.put(Items.TROPICAL_FISH, "water_world");
        ITEM_TO_COMPONENT.put(Items.GOLDEN_APPLE, "one_hp");
        ITEM_TO_COMPONENT.put(Items.IRON_SWORD, "universal_anger");
        ITEM_TO_COMPONENT.put(Items.CAULDRON, "eternal_rain");
        ITEM_TO_COMPONENT.put(Items.PHANTOM_MEMBRANE, "insomniacs");
        ITEM_TO_COMPONENT.put(Items.BONE, "no_drops");
        ITEM_TO_COMPONENT.put(Items.COD, "fish_out_of_water");
    }

    public ChartComponentRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int w = input.width();
        int h = input.height();
        if (w < 3 || h < 3) return false;
        if (w > 3 || h > 3) return false;

        String componentId = null;
        int materialCount = 0;

        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                int idx = row * w + col;
                ItemStack stack = input.getItem(idx);

                if (col == 1 && row == 1) {
                    if (!(stack.getItem() instanceof ChartComponentItem)
                            || !"blank".equals(ChartComponentItem.getComponentType(stack))) {
                        return false;
                    }
                    continue;
                }

                if (col != 1 || row != 1) {
                    if (stack.isEmpty()) return false;
                    String comp = ITEM_TO_COMPONENT.get(stack.getItem());
                    if (comp == null) return false;
                    if (componentId == null) {
                        componentId = comp;
                    } else if (!componentId.equals(comp)) {
                        return false;
                    }
                    materialCount++;
                }
            }
        }

        if (materialCount != 8) return false;
        return ChartComponentType.byId(componentId) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack blank = ItemStack.EMPTY;
        String componentId = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof ChartComponentItem) {
                if ("blank".equals(ChartComponentItem.getComponentType(stack)))
                    blank = stack;
                continue;
            }
            if (componentId == null) {
                componentId = ITEM_TO_COMPONENT.get(stack.getItem());
            }
        }

        if (blank.isEmpty() || componentId == null) return ItemStack.EMPTY;

        ChartComponentType type = ChartComponentType.byId(componentId);
        if (type == null) return ItemStack.EMPTY;

        ItemStack result = new ItemStack(blank.getItem());
        CompoundTag tag = ExpeditionNbtHelper.readTag(result);
        tag.putString("component_type", type.id());
        ExpeditionNbtHelper.writeTag(result, tag);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 9;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
