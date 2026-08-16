package com.otherworldinn.entity.store;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
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

public class ButcherEntity extends StoreEntity {
    private static final List<RandomDropItem> DAILY_RANDOM_DROPS =
            List.of(
                    new RandomDropItem(() -> new ItemStack(Items.RABBIT_FOOT), 10, 10, 2, 4),
                    new RandomDropItem(() -> new ItemStack(Items.STRING), 3, 5, 8, 16),
                    new RandomDropItem(() -> new ItemStack(Items.FEATHER), 2, 4, 8, 16),
                    new RandomDropItem(() -> new ItemStack(Items.INK_SAC), 6, 8, 2, 4),
                    new RandomDropItem(() -> new ItemStack(Items.GLOW_INK_SAC), 10, 12, 1, 2),
                    new RandomDropItem(() -> new ItemStack(Items.RABBIT_HIDE), 3, 5, 4, 8));

    public ButcherEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.IRON_KITCHEN_KNIFE.get()));
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
        // 生肉
        entries.add(new CatalogEntry(new ItemStack(Items.BEEF), 4, 8));
        entries.add(new CatalogEntry(new ItemStack(Items.PORKCHOP), 4, 8));
        entries.add(new CatalogEntry(new ItemStack(Items.CHICKEN), 3, 8));
        entries.add(new CatalogEntry(new ItemStack(Items.MUTTON), 3, 8));
        entries.add(new CatalogEntry(new ItemStack(Items.RABBIT), 4, 32));
        entries.add(new CatalogEntry(new ItemStack(Items.COD), 3, 8));
        entries.add(new CatalogEntry(new ItemStack(Items.SALMON), 3, 8));

        entries.add(new CatalogEntry(new ItemStack(Items.LEATHER), 4, 8));
        entries.add(new CatalogEntry(new ItemStack(Items.BONE), 3, 8));
        entries.add(new CatalogEntry(new ItemStack(Items.EGG), 2, 8));

        entries.add(new CatalogEntry(new ItemStack(ModItems.IRON_KITCHEN_KNIFE.get()), 16, 1));

        entries.add(new CatalogEntry(new ItemStack(Items.MILK_BUCKET), 6, 8, 2));
        entries.add(new CatalogEntry(new ItemStack(Items.HONEY_BOTTLE), 8, 16, 4));
        entries.add(new CatalogEntry(new ItemStack(Items.SLIME_BALL), 10, 32, 6));
        entries.add(new CatalogEntry(new ItemStack(Items.LEAD), 16, 8, 8));
        return entries;
    }

    public static List<RandomOffer> createRandomOffers() {
        List<RandomOffer> offers = new ArrayList<>();
        for (RandomDropItem drop : DAILY_RANDOM_DROPS) {
            offers.add(
                    new RandomOffer(
                            drop.stackSupplier().get(),
                            drop.minPrice(),
                            drop.maxPrice(),
                            drop.minStock(),
                            drop.maxStock()));
        }
        return offers;
    }

    @Override
    protected void applyCodeDefaultsAfterDebugReset() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.IRON_KITCHEN_KNIFE.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.initDefaultStoreItems();
    }

    @Override
    protected void refreshRandomItems() {
        super.refreshRandomItems();
        for (RandomDropItem drop : DAILY_RANDOM_DROPS) {
            this.addRandomStoreItem(
                    drop.stackSupplier().get(),
                    drop.minPrice(),
                    drop.maxPrice(),
                    drop.minStock(),
                    drop.maxStock());
        }
    }

    @Override
    public ResourceLocation getStoreBackground() {
        return ResourceLocation.fromNamespaceAndPath(
                OtherworldInn.MODID, "textures/gui/store/fisher.png");
    }

    @Override
    protected SoundEvent getOpenStoreSound() {
        return SoundEvents.AXE_STRIP;
    }

    private record RandomDropItem(
            java.util.function.Supplier<ItemStack> stackSupplier,
            int minPrice, int maxPrice, int minStock, int maxStock) {}
}
