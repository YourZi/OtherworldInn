package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.util.WorldDayUtils;
import com.otherworldinn.init.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class MagicianEntity extends StoreEntity {

    private static final int RANDOM_BOOK_COUNT = 4;
    private static final int RANDOM_BOOK_BASE_PRICE = 32;
    /** 随机附魔书在 JEI 中展示的价格上限估算（32 * 1.5^4，对应约 5 级附魔书） */
    private static final int RANDOM_BOOK_DISPLAY_MAX_PRICE = 162;
    private static final ResourceLocation ADVANCEMENT_WE_NEED_TO_GO_DEEPER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "story/enter_the_nether");

    private record DailyNetherMaterial(ItemStack stack, int price) {}

    /** 每日随机下界/稀有材料（同时供运行时刷新与 JEI 展示使用） */
    private static final List<DailyNetherMaterial> DAILY_NETHER_MATERIALS =
            List.of(
                    new DailyNetherMaterial(new ItemStack(Items.ECHO_SHARD), 16),
                    new DailyNetherMaterial(new ItemStack(Items.CRIMSON_FUNGUS), 6),
                    new DailyNetherMaterial(new ItemStack(Items.WARPED_FUNGUS), 6),
                    new DailyNetherMaterial(new ItemStack(Items.SOUL_SAND), 4));

    public MagicianEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.REDSTONE));
        BuiltInRegistries.ITEM
                .getOptional(ResourceLocation.parse("majobroom:majo_hat"))
                .ifPresent(item -> this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(item)));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        if (!level.isClientSide) {
            this.initDefaultStoreItems();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.storeItems.size() == this.fixedItemsCount) {
            this.refreshRandomItems();
        }
    }

    @Override
    protected void refreshRandomItems() {
        super.refreshRandomItems();
        List<EnchantmentOffer> offers = this.buildAllOffers();
        RandomSource random = this.getRandom();
        int count = Math.min(RANDOM_BOOK_COUNT, offers.size());
        for (int i = 0; i < count; i++) {
            int selectedIndex = random.nextInt(offers.size());
            EnchantmentOffer selected = offers.remove(selectedIndex);
            int price =
                    Math.max(
                            1,
                            Math.round(
                                    RANDOM_BOOK_BASE_PRICE
                                            * (float) Math.pow(1.5D, selected.level() - 1)));
            this.addRandomStoreItem(
                    new ItemStack(Items.ENCHANTED_BOOK),
                    price,
                    price,
                    1,
                    2,
                    stack -> stack.enchant(selected.enchantment(), selected.level()));
        }

        long day = WorldDayUtils.currentDay(this.level());
        RandomSource dailyRandom = RandomSource.create(this.level().random.nextLong() ^ day);
        List<DailyNetherMaterial> dailyPool = new ArrayList<>(DAILY_NETHER_MATERIALS);
        java.util.Collections.shuffle(dailyPool, new java.util.Random(day));
        int types = 1 + dailyRandom.nextInt(3); // 1~3 种
        for (int i = 0; i < Math.min(types, dailyPool.size()); i++) {
            DailyNetherMaterial material = dailyPool.get(i);
            int stock = 2 + dailyRandom.nextInt(4); // 2~5
            this.addRandomStoreItem(
                    material.stack().copy(), material.price(), material.price(), stock, stock);
        }
    }

    private List<EnchantmentOffer> buildAllOffers() {
        List<EnchantmentOffer> offers = new ArrayList<>();
        this.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .ifPresent(
                        registry ->
                                registry.listElements()
                                        .forEach(
                                                holder -> {
                                                    Enchantment enchantment = holder.value();
                                                    if (holder.is(EnchantmentTags.TREASURE)
                                                            || holder.is(Enchantments.MENDING)
                                                            || holder.is(Enchantments.SWIFT_SNEAK)
                                                            || holder.is(Enchantments.WIND_BURST)) {
                                                        return;
                                                    }
                                                    for (int level = enchantment.getMinLevel();
                                                            level <= enchantment.getMaxLevel();
                                                            level++) {
                                                        offers.add(new EnchantmentOffer(holder, level));
                                                    }
                                                }));
        return offers;
    }

    private void initDefaultStoreItems() {
        this.applyCatalog(createCatalog());
    }

    public static List<CatalogEntry> createCatalog() {
        List<CatalogEntry> entries = new ArrayList<>();
        entries.add(new CatalogEntry(new ItemStack(Items.LAPIS_LAZULI), 8, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.EMERALD), 12, 32));
        entries.add(new CatalogEntry(new ItemStack(Items.AMETHYST_SHARD), 4, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.REDSTONE), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.GUNPOWDER), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.GLASS_BOTTLE), 2, 64));
        entries.add(new CatalogEntry(new ItemStack(Items.BLAZE_ROD), 16, 32));
        entries.add(new CatalogEntry(new ItemStack(Items.GLOWSTONE_DUST), 8, 48));

        entries.add(new CatalogEntry(new ItemStack(ModItems.SPACE_SPHERE.get()), 64, 4, 2));
        entries.add(new CatalogEntry(new ItemStack(Items.BOOK), 8, 64, 2));
        entries.add(new CatalogEntry(new ItemStack(Items.DIAMOND), 64, 8, 4));
        entries.add(new CatalogEntry(enchantedBook(Enchantments.MENDING, 1), 128, 2, 4));
        String netherTitle = buildAdvancementTitleKey(ADVANCEMENT_WE_NEED_TO_GO_DEEPER);
        entries.add(
                new CatalogEntry(
                        new ItemStack(Items.QUARTZ),
                        16,
                        32,
                        1,
                        ADVANCEMENT_WE_NEED_TO_GO_DEEPER.toString(),
                        netherTitle));
        entries.add(
                new CatalogEntry(
                        new ItemStack(Items.ENDER_PEARL),
                        64,
                        16,
                        1,
                        ADVANCEMENT_WE_NEED_TO_GO_DEEPER.toString(),
                        netherTitle));
        entries.add(new CatalogEntry(enchantedBook(Enchantments.SWIFT_SNEAK, 3), 256, 2, 6));
        entries.add(new CatalogEntry(new ItemStack(Items.EXPERIENCE_BOTTLE), 32, 16, 8));
        entries.add(new CatalogEntry(enchantedBook(Enchantments.WIND_BURST, 3), 256, 2, 8));

        // 节日限定（仲夏夜）
        entries.add(new CatalogEntry(new ItemStack(Items.GLOW_INK_SAC), 24, 16, "midsummer_night"));
        entries.add(new CatalogEntry(new ItemStack(Items.GLOWSTONE), 40, 16, "midsummer_night"));
        entries.add(new CatalogEntry(new ItemStack(Items.SEA_LANTERN), 64, 12, "midsummer_night"));
        entries.add(new CatalogEntry(new ItemStack(Items.GLOW_BERRIES), 16, 16, "midsummer_night"));
        // 仲夏夜跨模组商品（酒馆荧光酒）
        entries.add(new CatalogEntry(createStack("kaleidoscope_tavern:glowflower_brew"), 36, 8, "midsummer_night"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_tavern:luminous_bride"), 44, 8, "midsummer_night"));
        entries.add(new CatalogEntry(createStack("kaleidoscope_tavern:polaris_sweet_white"), 48, 8, "midsummer_night"));
        return entries;
    }

    public static List<RandomOffer> createRandomOffers() {
        List<RandomOffer> offers = new ArrayList<>();
        offers.add(
                new RandomOffer(
                        new ItemStack(Items.ENCHANTED_BOOK),
                        RANDOM_BOOK_BASE_PRICE,
                        RANDOM_BOOK_DISPLAY_MAX_PRICE,
                        1,
                        2));
        for (DailyNetherMaterial material : DAILY_NETHER_MATERIALS) {
            offers.add(
                    new RandomOffer(
                            material.stack(),
                            material.price(),
                            material.price(),
                            2,
                            5));
        }
        return offers;
    }

    private static ItemStack enchantedBook(ResourceKey<Enchantment> key, int level) {
        var enchantment =
                VanillaRegistries.createLookup()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(key);
        return EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
    }

    @Override
    protected void applyCodeDefaultsAfterDebugReset() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.REDSTONE));
        BuiltInRegistries.ITEM
                .getOptional(ResourceLocation.parse("majobroom:majo_hat"))
                .ifPresent(item -> this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(item)));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.initDefaultStoreItems();
    }

    @Override
    public ResourceLocation getStoreBackground() {
        return ResourceLocation.fromNamespaceAndPath(
                OtherworldInn.MODID, "textures/gui/store/magician.png");
    }

    @Override
    protected SoundEvent getOpenStoreSound() {
        return SoundEvents.ENCHANTMENT_TABLE_USE;
    }

    private record EnchantmentOffer(Holder<Enchantment> enchantment, int level) {}
}
