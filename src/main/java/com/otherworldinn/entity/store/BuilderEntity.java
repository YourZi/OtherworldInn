package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BuilderEntity extends StoreEntity {
    public BuilderEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BRICK));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        if (!level.isClientSide) {
            this.initDefaultStoreItems();
        }
    }

    private void initDefaultStoreItems() {
        this.addAllPlanks();
        this.addAllStoneVariants();
        this.addStoreItem("minecraft:white_wool", 4, 32);
        this.addStoreItem("minecraft:glass", 3, 64);
        this.addStoreItem("minecraft:glass_pane", 2, 64);
        this.addStoreItem("minecraft:bricks", 4, 64);
        this.addStoreItem("minecraft:stone_bricks", 4, 64);
        this.addStoreItem("minecraft:smooth_stone", 4, 64);
        this.addStoreItem("minecraft:terracotta", 6, 64);
        this.addStoreItem("minecraft:clay", 3, 32);
        this.addStoreItem("minecraft:brick", 3, 32);
        this.addStoreItem("minecraft:gravel", 2, 64);
        this.addStoreItem("minecraft:sand", 2, 64);
        this.addStoreItem("minecraft:red_sand", 3, 64);
        this.addStoreItem("minecraft:sandstone", 3, 64);
        this.addStoreItem("minecraft:red_sandstone", 4, 64);
        this.addStoreItem("minecraft:nether_bricks", 6, 64);
        this.addStoreItem("minecraft:prismarine", 8, 32);
        this.addStoreItem("minecraft:prismarine_bricks", 10, 32);
        this.addStoreItem("minecraft:dark_prismarine", 12, 32);
        this.addStoreItem("minecraft:sea_lantern", 12, 32);
        this.addStoreItem("minecraft:quartz_block", 10, 48);

        this.addFavorStoreItem(2, new ItemStack(Items.GLOWSTONE), 8, 32);
        this.addFavorStoreItem(4, new ItemStack(Items.OBSIDIAN), 20, 16);
        this.addFavorStoreItem(6, new ItemStack(Items.PURPUR_BLOCK), 16, 48);
        this.addFavorStoreItem(8, new ItemStack(Items.END_ROD), 16, 32);

        this.refreshRandomItems();
    }

    // ═══ 每日随机蓝图 ═══

    @Override
    protected void refreshRandomItems() {
        super.refreshRandomItems();
        long day = this.level().getGameTime() / 24000L;
        long seed = this.level().random.nextLong() ^ day;
        var picks = BuilderBlueprintManager.pickDailyRandom(4, seed);
        for (var entry : picks) {
            ItemStack stack = BuilderBlueprintManager.createSchematicStack(entry);
            int price = entry.minPrice()
                    + new java.util.Random(seed ^ entry.id().hashCode())
                            .nextInt(Math.max(1, entry.maxPrice() - entry.minPrice() + 1));
            this.addRandomStoreItem(stack, price, price, 1, 1);
        }
    }

    private void addAllPlanks() {
        this.addStoreItem("minecraft:oak_planks", 2, 64);
        this.addStoreItem("minecraft:spruce_planks", 2, 64);
        this.addStoreItem("minecraft:birch_planks", 2, 64);
        this.addStoreItem("minecraft:jungle_planks", 2, 64);
        this.addStoreItem("minecraft:acacia_planks", 2, 64);
        this.addStoreItem("minecraft:dark_oak_planks", 2, 64);
        this.addStoreItem("minecraft:mangrove_planks", 3, 64);
        this.addStoreItem("minecraft:cherry_planks", 3, 64);
        this.addStoreItem("minecraft:bamboo_planks", 3, 64);
        this.addStoreItem("minecraft:crimson_planks", 3, 64);
        this.addStoreItem("minecraft:warped_planks", 3, 64);
    }

    private void addAllStoneVariants() {
        this.addStoreItem("minecraft:cobblestone", 2, 64);
        this.addStoreItem("minecraft:stone", 2, 64);
        this.addStoreItem("minecraft:granite", 2, 64);
        this.addStoreItem("minecraft:diorite", 2, 64);
        this.addStoreItem("minecraft:andesite", 2, 64);
        this.addStoreItem("minecraft:tuff", 3, 64);
        this.addStoreItem("minecraft:calcite", 3, 64);
        this.addStoreItem("minecraft:cobbled_deepslate", 3, 64);
        this.addStoreItem("minecraft:deepslate", 4, 64);
        this.addStoreItem("minecraft:blackstone", 4, 64);
        this.addStoreItem("minecraft:basalt", 4, 64);
        this.addStoreItem("minecraft:dripstone_block", 4, 64);
    }

    @Override
    protected void applyCodeDefaultsAfterDebugReset() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BRICK));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.initDefaultStoreItems();
    }

    @Override
    public ResourceLocation getStoreBackground() {
        return ResourceLocation.fromNamespaceAndPath(
                OtherworldInn.MODID, "textures/gui/store/blacksmith.png");
    }

    @Override
    protected SoundEvent getOpenStoreSound() {
        return SoundEvents.STONE_PLACE;
    }
}
