package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.world.expedition.ExpeditionNbtHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
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
        this.addStoreItem("sophisticatedbackpacks:upgrade_base", 12, 16);
        this.addStoreItem("refinedstorage:storage_housing", 12, 16);
        this.addStoreItem("otherworldinn:land_deed", 32, 1);
        this.addStoreItem("otherworldinn:inn_upgrade_voucher", 128, 1);
        this.addStoreItem("otherworldinn:facility_upgrade_template", 32, 4);
        this.addLimitedStoreItem("otherworldinn:pioneer_chart", 12, 2,
                stack -> {
                    CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
                    tag.putInt("star_level", 5);
                    tag.putInt("max_slots", 6);
                    ExpeditionNbtHelper.writeTag(stack, tag);
                });
        this.addLimitedStoreItem("otherworldinn:chart_component", 8, 1);
        this.addStoreItem("minecraft:book", 6, 16);
        this.addStoreItem("minecraft:paper", 2, 64);
        this.addStoreItem("minecraft:name_tag", 12, 8);
        this.addStoreItem("minecraft:slime_ball", 10, 16);
        this.addAllDyes();
        this.addFavorStoreItem(2, "refinedstorage:1k_storage_part", 32, 8);
        this.addFavorStoreItem(4, "refinedstorage:4k_storage_part", 64, 6);
        this.addFavorStoreItem(6, "refinedstorage:16k_storage_part", 128, 4);
        this.addFavorStoreItem(8, "refinedstorage:64k_storage_part", 256, 2);
    }

    private void addAllDyes() {
        this.addStoreItem("minecraft:white_dye", 2, 32);
        this.addStoreItem("minecraft:orange_dye", 2, 32);
        this.addStoreItem("minecraft:magenta_dye", 2, 32);
        this.addStoreItem("minecraft:light_blue_dye", 2, 32);
        this.addStoreItem("minecraft:yellow_dye", 2, 32);
        this.addStoreItem("minecraft:lime_dye", 2, 32);
        this.addStoreItem("minecraft:pink_dye", 2, 32);
        this.addStoreItem("minecraft:gray_dye", 2, 32);
        this.addStoreItem("minecraft:light_gray_dye", 2, 32);
        this.addStoreItem("minecraft:cyan_dye", 2, 32);
        this.addStoreItem("minecraft:purple_dye", 2, 32);
        this.addStoreItem("minecraft:blue_dye", 2, 32);
        this.addStoreItem("minecraft:brown_dye", 2, 32);
        this.addStoreItem("minecraft:green_dye", 2, 32);
        this.addStoreItem("minecraft:red_dye", 2, 32);
        this.addStoreItem("minecraft:black_dye", 2, 32);
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
