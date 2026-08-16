package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.world.inventory.WanderingTraderRecycleMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WanderingTraderEntity extends StoreEntity {
    private static final String TAG_RECYCLE_FAVOR_GAINED = "RecycleFavorGained";
    private static final String TAG_RECYCLE_COIN_GAINED = "RecycleCoinGained";
    private static final String TAG_RECYCLED_ITEM_COUNTS = "RecycledItemCounts";

    private static final int RANDOM_ITEMS_COUNT = 16;
    private static final Map<ResourceLocation, SpecialVanillaOffer> SPECIAL_VANILLA_OFFERS =
            Map.ofEntries(
                    Map.entry(
                            ResourceLocation.withDefaultNamespace("elytra"),
                            new SpecialVanillaOffer(128, 1)),
                    Map.entry(
                            ResourceLocation.withDefaultNamespace("dragon_head"),
                            new SpecialVanillaOffer(128, 1)),
                    Map.entry(
                            ResourceLocation.withDefaultNamespace("nether_star"),
                            new SpecialVanillaOffer(112, 1)),
                    Map.entry(
                            ResourceLocation.withDefaultNamespace("totem_of_undying"),
                            new SpecialVanillaOffer(104, 1)),
                    Map.entry(
                            ResourceLocation.withDefaultNamespace("shulker_shell"),
                            new SpecialVanillaOffer(104, 1)),
                    Map.entry(
                            ResourceLocation.withDefaultNamespace("heart_of_the_sea"),
                            new SpecialVanillaOffer(120, 1)),
                    Map.entry(
                            ResourceLocation.withDefaultNamespace("trident"),
                            new SpecialVanillaOffer(116, 1)),
                    Map.entry(
                            ResourceLocation.withDefaultNamespace("wither_skeleton_skull"),
                            new SpecialVanillaOffer(120, 1)));
    private static final TagKey<Item> BLACKLIST =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            OtherworldInn.MODID, "wandering_trader_blacklist"));

    /** 完整命名空间黑名单 */
    private static final Set<String> NAMESPACE_BLACKLIST =
            Set.of("yuushya", "refinedstorage", "createutilities", "ftbquests", "starcatcher");

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

    /**
     * 游商可售商品范围（供 JEI 展示）。
     *
     * <p>游商本身完全动态：每日从注册表中随机抽取 16 种物品，外加 8 种固定价格的特殊原版物品。
     * JEI 展示这 8 种特殊原版物品
     */
    public static List<RandomOffer> createRandomOffers() {
        List<RandomOffer> offers = new ArrayList<>();
        for (Map.Entry<ResourceLocation, SpecialVanillaOffer> entry : SPECIAL_VANILLA_OFFERS.entrySet()) {
            ItemStack stack = BuiltInRegistries.ITEM.getOptional(entry.getKey()).map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
            if (stack.isEmpty()) {
                continue;
            }
            SpecialVanillaOffer offer = entry.getValue();
            offers.add(new RandomOffer(stack, offer.price(), offer.price(), offer.stock(), offer.stock()));
        }
        return offers;
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
        this.resetRecycleVisitLimits();
        super.refreshRandomItems();
        long day = this.level().getDayTime() / 24000L;
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
            int price = resolvePrice(item, rarity, rng);
            int stock = resolveStock(item, stack, rng);
            this.addRandomStoreItem(stack, price, price, stock, stock);
            picked++;
        }
    }

    private static int resolvePrice(Item item, Rarity rarity, java.util.Random rng) {
        SpecialVanillaOffer specialOffer = SPECIAL_VANILLA_OFFERS.get(BuiltInRegistries.ITEM.getKey(item));
        if (specialOffer != null) {
            return specialOffer.price();
        }
        return rarityPrice(rarity, rng);
    }

    private static int resolveStock(Item item, ItemStack stack, java.util.Random rng) {
        SpecialVanillaOffer specialOffer = SPECIAL_VANILLA_OFFERS.get(BuiltInRegistries.ITEM.getKey(item));
        if (specialOffer != null) {
            return specialOffer.stock();
        }
        // 可堆叠 → 1-8, 不可堆叠 → 1
        return stack.isStackable() ? 1 + rng.nextInt(8) : 1;
    }

    private static int rarityPrice(Rarity rarity, java.util.Random rng) {
        return switch (rarity) {
            case COMMON -> 8 + rng.nextInt(17);   // 8-24
            case UNCOMMON -> 15 + rng.nextInt(34); // 15-48
            case RARE -> 49 + rng.nextInt(16);     // 49-64
            case EPIC -> 65 + rng.nextInt(64);     // 65-128
            default -> {
                OtherworldInn.LOGGER.warn("Unknown wandering trader item rarity {}, falling back to COMMON pricing", rarity);
                yield 8 + rng.nextInt(17);
            }
        };
    }

    private record SpecialVanillaOffer(int price, int stock) {}

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
        return SoundEvents.BARREL_OPEN;
    }

    // === 回收 ===

    /** 当前正在使用回收菜单的玩家（互斥锁） */
    @Nullable
    private Player currentRecyclePlayer;
    private final Map<Item, Integer> recycledItemCounts = new HashMap<>();
    private int recycleFavorGained;
    private int recycleCoinGained;

    @Nullable
    public Player getCurrentRecyclePlayer() {
        return currentRecyclePlayer;
    }

    public void setCurrentRecyclePlayer(@Nullable Player player) {
        this.currentRecyclePlayer = player;
    }

    public void resetRecycleVisitLimits() {
        this.recycledItemCounts.clear();
        this.recycleFavorGained = 0;
        this.recycleCoinGained = 0;
    }

    public Map<Item, Integer> copyRecycledItemCounts() {
        return new HashMap<>(this.recycledItemCounts);
    }

    public int getRecycleFavorGained() {
        return this.recycleFavorGained;
    }

    public int getRecycleCoinGained() {
        return this.recycleCoinGained;
    }

    public void applyRecycleVisitProgress(Map<Item, Integer> itemCounts, int favorToAdd, int coinsToAdd) {
        for (Map.Entry<Item, Integer> entry : itemCounts.entrySet()) {
            int addedCount = Math.max(0, entry.getValue());
            if (addedCount <= 0) {
                continue;
            }
            this.recycledItemCounts.merge(entry.getKey(), addedCount, Integer::sum);
        }
        this.recycleFavorGained = Math.max(0, this.recycleFavorGained + Math.max(0, favorToAdd));
        this.recycleCoinGained = Math.max(0, this.recycleCoinGained + Math.max(0, coinsToAdd));
    }

    private void writeRecycleMenuSnapshot(FriendlyByteBuf buf) {
        buf.writeInt(this.getId());
        buf.writeVarInt(this.recycleFavorGained);
        buf.writeVarInt(this.recycleCoinGained);
        buf.writeVarInt(this.recycledItemCounts.size());
        for (Map.Entry<Item, Integer> entry : this.recycledItemCounts.entrySet()) {
            buf.writeVarInt(BuiltInRegistries.ITEM.getId(entry.getKey()));
            buf.writeVarInt(Math.max(0, entry.getValue()));
        }
    }

    /**
     * 尝试为此玩家打开回收菜单。已有玩家占用时返回 false。
     */
    public boolean tryOpenRecycleMenu(ServerPlayer player) {
        if (currentRecyclePlayer != null && currentRecyclePlayer != player) {
            return false;
        }
        currentRecyclePlayer = player;

        MenuProvider provider = new SimpleMenuProvider(
                (containerId, inv, p) -> new WanderingTraderRecycleMenu(containerId, inv, this),
                Component.translatable("screen.otherworldinn.recycle.title"));
        player.openMenu(provider, this::writeRecycleMenuSnapshot);
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(TAG_RECYCLE_FAVOR_GAINED, this.recycleFavorGained);
        compound.putInt(TAG_RECYCLE_COIN_GAINED, this.recycleCoinGained);

        CompoundTag recycledCountsTag = new CompoundTag();
        for (Map.Entry<Item, Integer> entry : this.recycledItemCounts.entrySet()) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(entry.getKey());
            if (itemId != null) {
                recycledCountsTag.putInt(itemId.toString(), Math.max(0, entry.getValue()));
            }
        }
        compound.put(TAG_RECYCLED_ITEM_COUNTS, recycledCountsTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.recycleFavorGained = Math.max(0, compound.getInt(TAG_RECYCLE_FAVOR_GAINED));
        this.recycleCoinGained = Math.max(0, compound.getInt(TAG_RECYCLE_COIN_GAINED));
        this.recycledItemCounts.clear();

        if (!compound.contains(TAG_RECYCLED_ITEM_COUNTS, CompoundTag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag recycledCountsTag = compound.getCompound(TAG_RECYCLED_ITEM_COUNTS);
        for (String key : recycledCountsTag.getAllKeys()) {
            ResourceLocation itemId = ResourceLocation.tryParse(key);
            if (itemId == null) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item != null && item != Items.AIR) {
                int count = Math.max(0, recycledCountsTag.getInt(key));
                if (count > 0) {
                    this.recycledItemCounts.put(item, count);
                }
            }
        }
    }
}
