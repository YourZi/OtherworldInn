package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.entity.store.BuilderBlueprintManager.BlueprintEntry;
import java.util.ArrayList;
import java.util.List;
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
        this.applyCatalog(createCatalog());
        this.refreshRandomItems();
    }

    public static List<CatalogEntry> createCatalog() {
        List<CatalogEntry> entries = new ArrayList<>();
        // 木板
        entries.add(new CatalogEntry(new ItemStack(Items.OAK_PLANKS), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.SPRUCE_PLANKS), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.BIRCH_PLANKS), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.JUNGLE_PLANKS), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.ACACIA_PLANKS), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.DARK_OAK_PLANKS), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.MANGROVE_PLANKS), 3, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.CHERRY_PLANKS), 3, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.BAMBOO_PLANKS), 3, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.CRIMSON_PLANKS), 3, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.WARPED_PLANKS), 3, 64));
        // 石质方块
        entries.add(new CatalogEntry(new ItemStack(Items.COBBLESTONE), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.STONE), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.GRANITE), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.DIORITE), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.ANDESITE), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.TUFF), 3, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.CALCITE), 3, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.COBBLED_DEEPSLATE), 3, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.DEEPSLATE), 4, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.BLACKSTONE), 4, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.BASALT), 4, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.DRIPSTONE_BLOCK), 4, 64));
        // 其他建筑材料
        entries.add(new CatalogEntry(new ItemStack(Items.WHITE_WOOL), 4, 32));
        entries.add(new CatalogEntry(new ItemStack(Items.GLASS), 3, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.GLASS_PANE), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.BRICKS), 4, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.STONE_BRICKS), 4, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.SMOOTH_STONE), 4, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.TERRACOTTA), 6, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.GRAVEL), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.SAND), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.RED_SAND), 3, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.SANDSTONE), 3, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.NETHER_BRICKS), 6, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.PRISMARINE), 8, 32));
        entries.add(new CatalogEntry(new ItemStack(Items.PRISMARINE_BRICKS), 10, 32));
        entries.add(new CatalogEntry(new ItemStack(Items.DARK_PRISMARINE), 12, 32));
        entries.add(new CatalogEntry(new ItemStack(Items.SEA_LANTERN), 12, 32));
        entries.add(new CatalogEntry(new ItemStack(Items.QUARTZ_BLOCK), 10, 48));
        // 好感度物品
        entries.add(new CatalogEntry(new ItemStack(Items.GLOWSTONE), 8, 32, 2));
        entries.add(new CatalogEntry(new ItemStack(Items.OBSIDIAN), 20, 16, 4));
        entries.add(new CatalogEntry(new ItemStack(Items.PURPUR_BLOCK), 16, 48, 6));
        entries.add(new CatalogEntry(new ItemStack(Items.END_ROD), 16, 32, 8));
        return entries;
    }

    public static List<RandomOffer> createRandomOffers() {
        List<RandomOffer> offers = new ArrayList<>();
        for (BlueprintEntry entry : BuilderBlueprintManager.POOL) {
            offers.add(
                    new RandomOffer(
                            BuilderBlueprintManager.createStorePreviewStack(entry),
                            entry.minPrice(),
                            entry.maxPrice(),
                            1,
                            1));
        }
        return offers;
    }

    // ═══ 每日随机蓝图 ═══

    @Override
    protected void refreshRandomItems() {
        super.refreshRandomItems();
        long day = this.level().getDayTime() / 24000L;
        long seed = this.level().random.nextLong() ^ day;
        var picks = BuilderBlueprintManager.pickDailyRandom(4, seed);
        for (var entry : picks) {
            ItemStack stack = BuilderBlueprintManager.createStorePreviewStack(entry);
            int price = entry.minPrice()
                    + new java.util.Random(seed ^ entry.id().hashCode())
                            .nextInt(Math.max(1, entry.maxPrice() - entry.minPrice() + 1));
            this.addRandomStoreItem(stack, price, price, 1, 1);
        }
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
