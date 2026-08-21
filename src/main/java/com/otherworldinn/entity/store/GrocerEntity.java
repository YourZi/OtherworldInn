package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class GrocerEntity extends StoreEntity {
    public GrocerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        if (!level.isClientSide) {
            this.initDefaultStoreItems();
        }
    }

    private void initDefaultStoreItems() {
        this.applyCatalog(createCatalog());
    }

    public static List<CatalogEntry> createCatalog() {
        List<CatalogEntry> entries = new ArrayList<>();
        entries.add(new CatalogEntry(createStack("sophisticatedbackpacks:upgrade_base"), 12, 16));
        entries.add(new CatalogEntry(createStack("refinedstorage:storage_housing"), 12, 16));
        entries.add(new CatalogEntry(createStack("otherworldinn:land_deed"), 32, 1));
        entries.add(new CatalogEntry(createStack("otherworldinn:inn_upgrade_voucher"), 128, 1));
        entries.add(new CatalogEntry(createStack("otherworldinn:facility_upgrade_template"), 32, 4));
        entries.add(new CatalogEntry(createStack("minecraft:book"), 6, 16));
        entries.add(new CatalogEntry(createStack("minecraft:paper"), 2, 64));
        entries.add(new CatalogEntry(createStack("minecraft:name_tag"), 12, 8));
        entries.add(new CatalogEntry(createStack("minecraft:slime_ball"), 10, 16));
        for (String dye : DYED_ITEMS) {
            entries.add(new CatalogEntry(createStack("minecraft:" + dye), 2, 32));
        }
        entries.add(new CatalogEntry(createStack("refinedstorage:1k_storage_part"), 32, 8, 2));
        entries.add(new CatalogEntry(createStack("refinedstorage:4k_storage_part"), 64, 6, 4));
        entries.add(new CatalogEntry(createStack("refinedstorage:16k_storage_part"), 128, 4, 6));
        entries.add(new CatalogEntry(createStack("refinedstorage:64k_storage_part"), 256, 2, 8));
        // 节日限定（隆冬节，原冬祭并入）
        entries.add(new CatalogEntry(createStack("minecraft:snowball"), 8, 16, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:snow_block"), 16, 16, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:powder_snow_bucket"), 40, 8, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:ice"), 12, 16, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:packed_ice"), 32, 12, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:blue_ice"), 96, 6, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:spruce_sapling"), 20, 12, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:firework_rocket"), 36, 12, "deep_winter_festival"));

        // 节日限定（仲夏夜）
        entries.add(new CatalogEntry(createStack("minecraft:music_disc_cat"), 48, 4, "midsummer_night"));
        entries.add(new CatalogEntry(createStack("minecraft:music_disc_13"), 48, 4, "midsummer_night"));
        entries.add(new CatalogEntry(createStack("minecraft:music_disc_strad"), 48, 4, "midsummer_night"));
        entries.add(new CatalogEntry(createStack("minecraft:lily_pad"), 6, 16, "midsummer_night"));
        entries.add(new CatalogEntry(createStack("minecraft:melon_slice"), 3, 32, "midsummer_night"));
        // 仲夏夜跨模组商品（灯会装饰/夏果）
        entries.add(new CatalogEntry(createStack("mcwlights:white_paper_lamp"), 16, 12, "midsummer_night"));
        entries.add(new CatalogEntry(createStack("yuushya:oriental_lantern"), 24, 12, "midsummer_night"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_cookery:watermelon_platter"), 20, 8, "midsummer_night"));

        // 节日限定（春祭）
        entries.add(new CatalogEntry(createStack("otherworldinn:organic_fertilizer"), 12, 32, "spring_festival"));

        // 节日限定（秋收祭）
        entries.add(new CatalogEntry(createStack("minecraft:pumpkin_pie"), 12, 16, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:cake"), 30, 8, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:jack_o_lantern"), 16, 16, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:golden_apple"), 48, 4, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:brown_mushroom"), 4, 16, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:red_mushroom"), 4, 16, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("minecraft:hay_block"), 12, 16, "harvest_festival"));
        // 秋收祭跨模组商品
        entries.add(new CatalogEntry(createStack("kaleidoscope_cookery:sticky_rice_cake"), 18, 12, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_cookery:fondant_pie"), 24, 12, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_cookery:stargazy_pie"), 28, 8, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_cookery:golden_salad"), 20, 12, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_cookery:apple_platter"), 16, 12, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_tavern:champagne"), 48, 8, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_tavern:sweet_berry_wine"), 26, 8, "harvest_festival"));
        entries.add(new CatalogEntry(createStack("otherworldinn:organic_fertilizer"), 10, 32, "harvest_festival"));

        // 节日限定（隆冬节）
        entries.add(new CatalogEntry(createStack("kaleidoscope_cookery:laba_congee"), 20, 12, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_cookery:dongpo_pork"), 36, 8, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_cookery:dumpling"), 14, 16, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_tavern:mother_snow"), 42, 8, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_tavern:ice_wine"), 44, 8, "deep_winter_festival"));
        entries.add(new CatalogEntry(createStack("mcwlights:festive_lantern"), 20, 12, "deep_winter_festival"));
        return entries;
    }

    private static final List<String> DYED_ITEMS =
            List.of(
                    "white_dye",
                    "orange_dye",
                    "magenta_dye",
                    "light_blue_dye",
                    "yellow_dye",
                    "lime_dye",
                    "pink_dye",
                    "gray_dye",
                    "light_gray_dye",
                    "cyan_dye",
                    "purple_dye",
                    "blue_dye",
                    "brown_dye",
                    "green_dye",
                    "red_dye",
                    "black_dye");

    @Override
    protected void applyCodeDefaultsAfterDebugReset() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STICK));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.initDefaultStoreItems();
    }

    @Override
    public ResourceLocation getStoreBackground() {
        return ResourceLocation.fromNamespaceAndPath(
                OtherworldInn.MODID, "textures/gui/store/farmer.png");
    }

    @Override
    protected SoundEvent getOpenStoreSound() {
        return SoundEvents.CHEST_OPEN;
    }
}
