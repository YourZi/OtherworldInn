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

    private static ItemStack createStack(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

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
