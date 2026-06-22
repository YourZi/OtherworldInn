package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
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

public class FishermanEntity extends StoreEntity {
    private static final String STARCATCHER = "starcatcher";
    private static final List<RandomProduct> DAILY_FISH_POOL =
            List.of(
                    new RandomProduct(1, "kaleidoscope_cookery:sashimi", 1, 12, 16, 2, 4),
                    new RandomProduct(1, "minecraft:ink_sac", 1, 4, 6, 2, 4),
                    new RandomProduct(2, "starcatcher:fish_bones", 1, 8, 12, 2, 4),
                    new RandomProduct(2, "minecraft:bone_meal", 1, 2, 4, 3, 6),
                    new RandomProduct(4, "minecraft:glow_ink_sac", 1, 10, 14, 1, 2),
                    new RandomProduct(4, "minecraft:dried_kelp", 1, 2, 3, 3, 5),
                    new RandomProduct(6, "minecraft:nautilus_shell", 1, 18, 24, 1, 2),
                    new RandomProduct(6, "starcatcher:pearl", 1, 14, 18, 1, 2),
                    new RandomProduct(8, "minecraft:prismarine_shard", 1, 10, 14, 1, 2),
                    new RandomProduct(8, "minecraft:prismarine_crystals", 1, 12, 16, 1, 2));

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
        this.addBaseStarcatcherItem("starcatcher_guide", 6, 1);
        this.addBaseStarcatcherItem("starcatcher_rod", 10, 1);
        this.addBaseStarcatcherItem("hook", 4, 4);
        this.addBaseStarcatcherItem("bobber", 4, 4);
        this.addBaseStarcatcherItem("worm", 4, 16);

        this.addFavorStarcatcherItem(2, "starcatcher_twine", 5, 8);
        this.addFavorStarcatcherItem(2, "vanilla_hook", 6, 2);
        this.addFavorStarcatcherItem(2, "vanilla_bobber", 6, 2);
        this.addFavorStarcatcherItem(2, "tackle_box", 18, 1);

        this.addFavorStarcatcherItem(4, "steady_bobber", 12, 2);
        this.addFavorStarcatcherItem(4, "leaf_bobber", 12, 2);
        this.addFavorStarcatcherItem(4, "mossy_hook", 14, 2);
        this.addFavorStarcatcherItem(4, "murkwater_bait", 10, 4);

        this.addFavorStarcatcherItem(6, "shiny_hook", 22, 1);
        this.addFavorStarcatcherItem(6, "aqua_bobber", 18, 1);
        this.addFavorStarcatcherItem(6, "seeking_worm", 18, 2);
        this.addFavorStarcatcherItem(6, "meteorological_bait", 18, 2);

        this.addFavorStarcatcherItem(8, "fish_radar", 32, 1);
        this.addFavorStarcatcherItem(8, "aquarium", 48, 1);
        this.addFavorStarcatcherItem(8, "display", 24, 1);
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
        List<RandomProduct> pool =
                DAILY_FISH_POOL.stream()
                        .filter(product -> product.requiredFavorLevel() <= this.getFavorLevel())
                        .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        java.util.Collections.shuffle(pool, new java.util.Random(seed));
        int count = Math.min(2, pool.size());
        for (int i = 0; i < count; i++) {
            RandomProduct p = pool.get(i);
            ItemStack stack = this.createRegisteredStack(p.itemId(), p.count());
            if (stack.isEmpty()) {
                continue;
            }
            int price = p.minPrice()
                    + new java.util.Random(seed ^ i).nextInt(Math.max(1, p.maxPrice() - p.minPrice() + 1));
            int stock = p.minStock()
                    + new java.util.Random(seed ^ ~i).nextInt(Math.max(1, p.maxStock() - p.minStock() + 1));
            this.addRandomStoreItem(stack, price, price, stock, stock);
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

    private void addBaseStarcatcherItem(String itemPath, int price, int maxStock) {
        this.addBaseStarcatcherItem(itemPath, price, maxStock, 1);
    }

    private void addBaseStarcatcherItem(String itemPath, int price, int maxStock, int count) {
        ItemStack stack = this.createStarcatcherStack(itemPath, count);
        if (!stack.isEmpty()) {
            this.addStoreItem(stack, price, maxStock);
        }
    }

    private void addFavorStarcatcherItem(int requiredFavorLevel, String itemPath, int price, int maxStock) {
        this.addFavorStarcatcherItem(requiredFavorLevel, itemPath, price, maxStock, 1);
    }

    private void addFavorStarcatcherItem(
            int requiredFavorLevel, String itemPath, int price, int maxStock, int count) {
        ItemStack stack = this.createStarcatcherStack(itemPath, count);
        if (!stack.isEmpty()) {
            this.addFavorStoreItem(requiredFavorLevel, stack, price, maxStock);
        }
    }

    private ItemStack createStarcatcherStack(String itemPath, int count) {
        Item item =
                BuiltInRegistries.ITEM
                        .getOptional(ResourceLocation.fromNamespaceAndPath(STARCATCHER, itemPath))
                        .orElse(Items.AIR);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    private ItemStack createRegisteredStack(String itemId, int count) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(Items.AIR);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    private record RandomProduct(
            int requiredFavorLevel,
            String itemId,
            int count,
            int minPrice, int maxPrice, int minStock, int maxStock) {}
}
