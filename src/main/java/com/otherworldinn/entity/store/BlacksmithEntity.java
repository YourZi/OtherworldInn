package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * 铁匠实体
 *
 * <p>出售粗矿和矿锭 也会随机刷新一些带有损耗或附魔的铁制工具。
 */
public class BlacksmithEntity extends StoreEntity {

    // 随机工具池
    private final List<RandomItemData> toolPool = new ArrayList<>();
    private static final List<ResourceKey<Enchantment>> ARMOR_PRIMARY_ENCHANTMENTS =
            List.of(
                    Enchantments.PROTECTION,
                    Enchantments.FIRE_PROTECTION,
                    Enchantments.BLAST_PROTECTION,
                    Enchantments.PROJECTILE_PROTECTION);

    private enum ToolKind {
        TOOL,
        ARMOR
    }

    private record ToolEntry(
            Item item,
            int minPrice,
            int maxPrice,
            int minStock,
            int maxStock,
            int weight,
            ToolKind kind) {}

    /** 随机工具/装备池（同时供运行时抽取与 JEI 展示使用） */
    private static final List<ToolEntry> TOOL_ENTRIES =
            List.of(
                    new ToolEntry(Items.IRON_PICKAXE, 25, 35, 1, 1, 10, ToolKind.TOOL),
                    new ToolEntry(Items.IRON_AXE, 20, 30, 1, 1, 10, ToolKind.TOOL),
                    new ToolEntry(Items.IRON_SHOVEL, 10, 20, 1, 1, 10, ToolKind.TOOL),
                    new ToolEntry(Items.IRON_SWORD, 15, 25, 1, 1, 10, ToolKind.TOOL),
                    new ToolEntry(Items.IRON_HOE, 10, 20, 1, 1, 5, ToolKind.TOOL),
                    new ToolEntry(Items.GOLDEN_PICKAXE, 20, 30, 1, 1, 5, ToolKind.TOOL),
                    new ToolEntry(Items.GOLDEN_SWORD, 20, 30, 1, 1, 5, ToolKind.TOOL),
                    new ToolEntry(Items.IRON_HELMET, 28, 40, 1, 1, 8, ToolKind.ARMOR),
                    new ToolEntry(Items.IRON_CHESTPLATE, 40, 58, 1, 1, 6, ToolKind.ARMOR),
                    new ToolEntry(Items.IRON_LEGGINGS, 36, 52, 1, 1, 6, ToolKind.ARMOR),
                    new ToolEntry(Items.IRON_BOOTS, 24, 36, 1, 1, 8, ToolKind.ARMOR),
                    new ToolEntry(Items.CHAINMAIL_HELMET, 18, 28, 1, 1, 8, ToolKind.ARMOR),
                    new ToolEntry(Items.CHAINMAIL_CHESTPLATE, 30, 44, 1, 1, 6, ToolKind.ARMOR),
                    new ToolEntry(Items.CHAINMAIL_LEGGINGS, 26, 38, 1, 1, 6, ToolKind.ARMOR),
                    new ToolEntry(Items.CHAINMAIL_BOOTS, 16, 24, 1, 1, 8, ToolKind.ARMOR));

    public BlacksmithEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_INGOT));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.COPPER_INGOT));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        // 初始化商品列表
        if (!level.isClientSide) {
            this.initDefaultStoreItems();

            // 初始化工具池 (如果为空)
            if (this.toolPool.isEmpty()) {
                initToolPool();
            }
        }
    }

    private void initToolPool() {
        for (ToolEntry entry : TOOL_ENTRIES) {
            Consumer<ItemStack> modifier =
                    entry.kind() == ToolKind.TOOL
                            ? this::applyRandomToolModifier
                            : this::applyRandomArmorModifier;
            this.toolPool.add(
                    new RandomItemData(
                            entry.item(),
                            entry.minPrice(),
                            entry.maxPrice(),
                            entry.minStock(),
                            entry.maxStock(),
                            entry.weight(),
                            modifier));
        }
    }

    private void applyRandomToolModifier(ItemStack stack) {
        RandomSource random = this.getRandom();
        // 随机耐久损耗 (10% - 50%)
        int maxDamage = stack.getMaxDamage();
        int damage = (int) (maxDamage * (0.1f + random.nextFloat() * 0.4f));
        stack.setDamageValue(damage);

        // 随机附魔 (低级)
        if (random.nextBoolean()) {
            this.tryApplyEnchantment(
                    stack,
                    Enchantments.EFFICIENCY,
                    1 + random.nextInt(2),
                    "random tool efficiency");
        }
        if (random.nextFloat() < 0.3f) {
            this.tryApplyEnchantment(
                    stack, Enchantments.UNBREAKING, 1, "random tool unbreaking");
        }
    }

    private void applyRandomArmorModifier(ItemStack stack) {
        RandomSource random = this.getRandom();
        int maxDamage = stack.getMaxDamage();
        int damage = (int) (maxDamage * (0.05f + random.nextFloat() * 0.4f));
        stack.setDamageValue(damage);

        if (random.nextFloat() < 0.7f) {
            this.tryApplyRandomArmorProtection(stack, random);
        }
        if (random.nextFloat() < 0.25f) {
            this.tryApplyEnchantment(
                    stack, Enchantments.UNBREAKING, 1, "random armor unbreaking");
        }
    }

    @Override
    public void tick() {
        super.tick();
        // 初始生成随机商品
        if (!this.level().isClientSide
                && this.storeItems.size() == this.fixedItemsCount
                && !this.toolPool.isEmpty()) {
            this.refreshRandomItems();
        }
    }

    @Override
    protected void refreshRandomItems() {
        super.refreshRandomItems();
        if (this.toolPool.isEmpty()) {
            initToolPool();
        }
        // 随机抽取商品
        this.generateRandomItems(this.toolPool, 2, 4);
    }

    private void initDefaultStoreItems() {
        this.applyCatalog(createCatalog());
    }

    public static List<CatalogEntry> createCatalog() {
        List<CatalogEntry> entries = new ArrayList<>();
        // 原矿
        entries.add(new CatalogEntry(new ItemStack(Items.IRON_ORE), 8, 16));
        entries.add(new CatalogEntry(new ItemStack(AllBlocks.ZINC_ORE.get()), 6, 16));
        entries.add(new CatalogEntry(new ItemStack(Items.COPPER_ORE), 6, 16));
        entries.add(new CatalogEntry(new ItemStack(Items.GOLD_ORE), 12, 8));

        // 粗矿
        entries.add(new CatalogEntry(new ItemStack(Items.RAW_IRON), 8, 16));
        entries.add(new CatalogEntry(new ItemStack(AllItems.RAW_ZINC.get()), 6, 16));
        entries.add(new CatalogEntry(new ItemStack(Items.RAW_COPPER), 5, 16));
        entries.add(new CatalogEntry(new ItemStack(Items.RAW_GOLD), 12, 8));

        // 粉碎矿
        entries.add(new CatalogEntry(new ItemStack(AllItems.CRUSHED_IRON.get()), 8, 8));
        entries.add(new CatalogEntry(new ItemStack(AllItems.CRUSHED_ZINC.get()), 6, 8));
        entries.add(new CatalogEntry(new ItemStack(AllItems.CRUSHED_COPPER.get()), 5, 8));
        entries.add(new CatalogEntry(new ItemStack(AllItems.CRUSHED_GOLD.get()), 12, 4));

        // 矿锭
        entries.add(new CatalogEntry(new ItemStack(Items.IRON_INGOT), 10, 32));
        entries.add(new CatalogEntry(new ItemStack(AllItems.ZINC_INGOT.get()), 8, 32));
        entries.add(new CatalogEntry(new ItemStack(Items.COPPER_INGOT), 6, 32));
        entries.add(new CatalogEntry(new ItemStack(Items.GOLD_INGOT), 15, 16));

        // 好感度物品
        entries.add(new CatalogEntry(new ItemStack(AllItems.ANDESITE_ALLOY.get()), 4, 32, 2));
        entries.add(new CatalogEntry(new ItemStack(AllItems.BRASS_INGOT.get()), 10, 32, 4));
        entries.add(new CatalogEntry(new ItemStack(AllItems.POLISHED_ROSE_QUARTZ.get()), 12, 32, 6));
        entries.add(new CatalogEntry(new ItemStack(AllItems.STURDY_SHEET.get()), 8, 32, 8));
        return entries;
    }

    public static List<RandomOffer> createRandomOffers() {
        List<RandomOffer> offers = new ArrayList<>();
        for (ToolEntry entry : TOOL_ENTRIES) {
            offers.add(
                    new RandomOffer(
                            new ItemStack(entry.item()),
                            entry.minPrice(),
                            entry.maxPrice(),
                            entry.minStock(),
                            entry.maxStock()));
        }
        return offers;
    }

    @Override
    protected void applyCodeDefaultsAfterDebugReset() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_INGOT));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.COPPER_INGOT));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        if (this.toolPool.isEmpty()) {
            initToolPool();
        }
        this.initDefaultStoreItems();
    }

    private boolean tryApplyRandomArmorProtection(ItemStack stack, RandomSource random) {
        int startIndex = random.nextInt(ARMOR_PRIMARY_ENCHANTMENTS.size());
        for (int i = 0; i < ARMOR_PRIMARY_ENCHANTMENTS.size(); i++) {
            ResourceKey<Enchantment> enchantmentKey =
                    ARMOR_PRIMARY_ENCHANTMENTS.get((startIndex + i) % ARMOR_PRIMARY_ENCHANTMENTS.size());
            if (this.tryApplyEnchantment(
                    stack,
                    enchantmentKey,
                    1 + random.nextInt(2),
                    "random armor primary protection")) {
                return true;
            }
        }
        return false;
    }

    private boolean tryApplyEnchantment(
            ItemStack stack,
            ResourceKey<Enchantment> enchantmentKey,
            int level,
            String reason) {
        try {
            HolderLookup.RegistryLookup<Enchantment> registry =
                    this.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            return registry.get(enchantmentKey)
                    .map(
                            enchantment -> {
                                stack.enchant(enchantment, level);
                                return true;
                            })
                    .orElseGet(
                            () -> {
                                OtherworldInn.LOGGER.warn(
                                        "Skipping {} enchantment {} on {} because it is missing from the registry",
                                        reason,
                                        enchantmentKey.location(),
                                        stack.getItem());
                                return false;
                            });
        } catch (Exception exception) {
            OtherworldInn.LOGGER.warn(
                    "Skipping {} enchantment {} on {} because applying it failed",
                    reason,
                    enchantmentKey.location(),
                    stack.getItem(),
                    exception);
            return false;
        }
    }

    @Override
    public ResourceLocation getStoreBackground() {
        return ResourceLocation.fromNamespaceAndPath(
                OtherworldInn.MODID, "textures/gui/store/blacksmith.png");
    }

    @Override
    protected SoundEvent getOpenStoreSound() {
        return SoundEvents.SMITHING_TABLE_USE;
    }
}
