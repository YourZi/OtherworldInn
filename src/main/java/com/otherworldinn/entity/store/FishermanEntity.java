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
        this.applyCatalog(createCatalog());
        this.refreshRandomItems();
    }

    public static List<CatalogEntry> createCatalog() {
        List<CatalogEntry> entries = new ArrayList<>();
        addStarcatcher(entries, "starcatcher_guide", 6, 1, 1);
        addStarcatcher(entries, "starcatcher_rod", 10, 1, 1);
        addStarcatcher(entries, "hook", 4, 4, 1);
        addStarcatcher(entries, "bobber", 4, 4, 1);
        addStarcatcher(entries, "worm", 4, 16, 1);

        addStarcatcher(entries, "starcatcher_twine", 5, 8, 2);
        addStarcatcher(entries, "vanilla_hook", 6, 2, 2);
        addStarcatcher(entries, "vanilla_bobber", 6, 2, 2);
        addStarcatcher(entries, "tackle_box", 18, 1, 2);

        addStarcatcher(entries, "steady_bobber", 12, 2, 4);
        addStarcatcher(entries, "leaf_bobber", 12, 2, 4);
        addStarcatcher(entries, "mossy_hook", 14, 2, 4);
        addStarcatcher(entries, "murkwater_bait", 10, 4, 4);

        addStarcatcher(entries, "shiny_hook", 22, 1, 6);
        addStarcatcher(entries, "aqua_bobber", 18, 1, 6);
        addStarcatcher(entries, "seeking_worm", 18, 2, 6);
        addStarcatcher(entries, "meteorological_bait", 18, 2, 6);

        addStarcatcher(entries, "fish_radar", 32, 1, 8);
        addStarcatcher(entries, "aquarium", 48, 1, 8);
        addStarcatcher(entries, "display", 24, 1, 8);
        return entries;
    }

    private static void addStarcatcher(
            List<CatalogEntry> entries, String itemPath, int price, int maxStock, int favorLevel) {
        ItemStack stack = createStarcatcherStack(itemPath, 1);
        if (!stack.isEmpty()) {
            entries.add(new CatalogEntry(stack, price, maxStock, favorLevel));
        }
    }

    public static List<RandomOffer> createRandomOffers() {
        List<RandomOffer> offers = new ArrayList<>();
        for (RandomProduct product : DAILY_FISH_POOL) {
            ItemStack stack = createRegisteredStack(product.itemId(), product.count());
            if (stack.isEmpty()) {
                continue;
            }
            offers.add(
                    new RandomOffer(
                            stack,
                            product.minPrice(),
                            product.maxPrice(),
                            product.minStock(),
                            product.maxStock(),
                            product.requiredFavorLevel()));
        }
        return offers;
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
        long day = this.level().getDayTime() / 24000L;
        long seed = this.level().random.nextLong() ^ day;
        List<RandomProduct> pool =
                DAILY_FISH_POOL.stream()
                        .filter(product -> product.requiredFavorLevel() <= this.getFavorLevel())
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        java.util.Collections.shuffle(pool, new java.util.Random(seed));
        int count = Math.min(2, pool.size());
        for (int i = 0; i < count; i++) {
            RandomProduct p = pool.get(i);
            ItemStack stack = createRegisteredStack(p.itemId(), p.count());
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

    private static ItemStack createStarcatcherStack(String itemPath, int count) {
        Item item =
                BuiltInRegistries.ITEM
                        .getOptional(ResourceLocation.fromNamespaceAndPath(STARCATCHER, itemPath))
                        .orElse(Items.AIR);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    private static ItemStack createRegisteredStack(String itemId, int count) {
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
