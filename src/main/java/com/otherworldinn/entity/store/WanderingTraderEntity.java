package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

public class WanderingTraderEntity extends StoreEntity {

    private static final int RANDOM_ITEMS_COUNT = 16;
    private static final TagKey<Item> BLACKLIST =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            OtherworldInn.MODID, "wandering_trader_blacklist"));

    /** 完整命名空间黑名单 */
    private static final Set<String> NAMESPACE_BLACKLIST =
            Set.of("yuushya", "refinedstorage", "createutilities", "ftbquests");

    public WanderingTraderEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.suppressAutoRestock = true; // 由 WanderingTraderManager 控制生命周期，禁止每日自动补货
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.EMERALD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        if (!level.isClientSide) {
            this.initDefaultStoreItems();
        }
    }

    private void initDefaultStoreItems() {
        this.refreshRandomItems();
    }

    @Override
    protected void applyCodeDefaultsAfterDebugReset() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.EMERALD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.initDefaultStoreItems();
    }

    @Override
    protected void refreshRandomItems() {
        super.refreshRandomItems();
        long day = this.level().getGameTime() / 24000L;
        java.util.Random rng = new java.util.Random(this.level().random.nextLong() ^ day);

        List<Item> allItems = new ArrayList<>(BuiltInRegistries.ITEM.stream()
                .filter(item -> !item.getDefaultInstance().is(BLACKLIST))
                .filter(item -> !isHardcodedBlacklisted(item))
                .filter(item -> !isNamespaceBlacklisted(item))
                .toList());
        java.util.Collections.shuffle(allItems, rng);

        int picked = 0;
        for (int i = 0; i < allItems.size() && picked < RANDOM_ITEMS_COUNT; i++) {
            Item item = allItems.get(i);
            ItemStack stack = new ItemStack(item);
            if (stack.isEmpty()) continue;

            Rarity rarity = stack.getRarity();
            int price = rarityPrice(rarity, rng);
            // 可堆叠 → 1-8, 不可堆叠 → 1
            int stock = stack.isStackable() ? 1 + rng.nextInt(8) : 1;
            this.addRandomStoreItem(stack, price, price, stock, stock);
            picked++;
        }
    }

    private static int rarityPrice(Rarity rarity, java.util.Random rng) {
        return switch (rarity) {
            case COMMON -> 8 + rng.nextInt(17);   // 8-24
            case UNCOMMON -> 15 + rng.nextInt(34); // 15-48
            case RARE -> 49 + rng.nextInt(16);     // 49-64
            case EPIC -> 65 + rng.nextInt(64);     // 65-128
        };
    }

    /** 硬编码黑名单：刷怪蛋等不应出现在商店的物品 */
    private static boolean isHardcodedBlacklisted(Item item) {
        return item instanceof SpawnEggItem;
    }

    /** 命名空间黑名单：排除指定模组的物品*/
    private static boolean isNamespaceBlacklisted(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        if (key == null) return false;
        String namespace = key.getNamespace();
        if (NAMESPACE_BLACKLIST.contains(namespace)) return true;
        return namespace.toLowerCase(java.util.Locale.ROOT).contains("mcw");
    }

    @Override
    public ResourceLocation getStoreBackground() {
        return ResourceLocation.fromNamespaceAndPath(
                OtherworldInn.MODID, "textures/gui/store/fisher.png");
    }

    @Override
    protected SoundEvent getOpenStoreSound() {
        return SoundEvents.WANDERING_TRADER_AMBIENT;
    }
}
