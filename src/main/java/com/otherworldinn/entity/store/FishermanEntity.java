package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FishermanEntity extends StoreEntity {
    private static final List<RandomProduct> DAILY_FISH_POOL =
            List.of(
                    new RandomProduct(() -> new ItemStack(Items.COD), 3, 5, 8, 16),
                    new RandomProduct(() -> new ItemStack(Items.SALMON), 4, 6, 8, 16),
                    new RandomProduct(() -> new ItemStack(Items.TROPICAL_FISH), 5, 8, 4, 8),
                    new RandomProduct(() -> new ItemStack(Items.PUFFERFISH), 8, 12, 2, 4),
                    new RandomProduct(() -> new ItemStack(Items.INK_SAC), 3, 5, 4, 8),
                    new RandomProduct(() -> new ItemStack(Items.GLOW_INK_SAC), 6, 8, 2, 4),
                    new RandomProduct(() -> new ItemStack(Items.NAUTILUS_SHELL), 12, 16, 1, 2),
                    new RandomProduct(() -> new ItemStack(Items.PRISMARINE_CRYSTALS), 4, 6, 4, 8),
                    new RandomProduct(() -> new ItemStack(Items.PRISMARINE_SHARD), 2, 4, 8, 16),
                    new RandomProduct(() -> new ItemStack(Items.SEA_PICKLE), 3, 5, 4, 8),
                    new RandomProduct(() -> new ItemStack(Items.KELP), 1, 2, 16, 32),
                    new RandomProduct(() -> new ItemStack(Items.LILY_PAD), 2, 3, 4, 8));

    public FishermanEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        if (!level.isClientSide) {
            this.initDefaultStoreItems();
        }
    }

    private void initDefaultStoreItems() {

        ItemStack rod = new ItemStack(Items.FISHING_ROD);
        var enchReg = this.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        rod.enchant(enchReg.getOrThrow(Enchantments.LURE), 3);
        this.addFavorStoreItem(2, rod, 8, 1);
        this.addFavorStoreItem(4, new ItemStack(Items.HEART_OF_THE_SEA), 32, 1);
        this.addFavorStoreItem(6, new ItemStack(Items.TRIDENT), 64, 1);
        this.refreshRandomItems();
    }

    @Override
    protected void applyCodeDefaultsAfterDebugReset() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.initDefaultStoreItems();
    }

    @Override
    protected void refreshRandomItems() {
        super.refreshRandomItems();
        long day = this.level().getGameTime() / 24000L;
        long seed = this.level().random.nextLong() ^ day;
        List<RandomProduct> pool = new java.util.ArrayList<>(DAILY_FISH_POOL);
        java.util.Collections.shuffle(pool, new java.util.Random(seed));
        int count = Math.min(5, pool.size());
        for (int i = 0; i < count; i++) {
            RandomProduct p = pool.get(i);
            int price = p.minPrice()
                    + new java.util.Random(seed ^ i).nextInt(Math.max(1, p.maxPrice() - p.minPrice() + 1));
            int stock = p.minStock()
                    + new java.util.Random(seed ^ ~i).nextInt(Math.max(1, p.maxStock() - p.minStock() + 1));
            this.addRandomStoreItem(p.stackSupplier().get(), price, price, stock, stock);
        }
    }

    @Override
    public ResourceLocation getStoreBackground() {
        return ResourceLocation.fromNamespaceAndPath(
                OtherworldInn.MODID, "textures/gui/store/fisher.png");
    }

    @Override
    protected SoundEvent getOpenStoreSound() {
        return SoundEvents.FISHING_BOBBER_SPLASH;
    }

    private record RandomProduct(
            java.util.function.Supplier<ItemStack> stackSupplier,
            int minPrice, int maxPrice, int minStock, int maxStock) {}
}
