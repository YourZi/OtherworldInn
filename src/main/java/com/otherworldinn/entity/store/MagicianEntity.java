package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.init.ModItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class MagicianEntity extends StoreEntity {

    private static final int RANDOM_BOOK_COUNT = 4;
    private static final int RANDOM_BOOK_BASE_PRICE = 32;
    private static final ResourceLocation ADVANCEMENT_WE_NEED_TO_GO_DEEPER =
            ResourceLocation.fromNamespaceAndPath("minecraft", "story/enter_the_nether");

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

        // 每日随机下界/稀有材料
        long day = this.level().getDayTime() / 24000L;
        RandomSource dailyRandom = RandomSource.create(this.level().random.nextLong() ^ day);
        List<ItemStack> dailyPool = new ArrayList<>();
        dailyPool.add(new ItemStack(Items.ECHO_SHARD));
        dailyPool.add(new ItemStack(Items.CRIMSON_FUNGUS));
        dailyPool.add(new ItemStack(Items.WARPED_FUNGUS));
        dailyPool.add(new ItemStack(Items.SOUL_SAND));
        java.util.Collections.shuffle(dailyPool, new java.util.Random(day));
        int types = 1 + dailyRandom.nextInt(3); // 1~3 种
        for (int i = 0; i < Math.min(types, dailyPool.size()); i++) {
            ItemStack stack = dailyPool.get(i);
            int stock = 2 + dailyRandom.nextInt(4); // 2~5
            int price;
            if (stack.is(Items.ECHO_SHARD)) price = 16;
            else if (stack.is(Items.CRIMSON_FUNGUS) || stack.is(Items.WARPED_FUNGUS)) price = 6;
            else price = 4; // soul_sand
            this.addRandomStoreItem(stack, price, price, stock, stock);
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
        this.addStoreItem(new ItemStack(Items.LAPIS_LAZULI), 8, 64);
        this.addStoreItem(new ItemStack(Items.EMERALD), 12, 32);
        this.addStoreItem(new ItemStack(Items.AMETHYST_SHARD), 4, 64);
        this.addStoreItem(new ItemStack(Items.REDSTONE), 2, 64);
        this.addStoreItem(new ItemStack(Items.GUNPOWDER), 2, 64);
        this.addStoreItem(new ItemStack(Items.GLASS_BOTTLE), 2, 64);
        this.addStoreItem(new ItemStack(Items.BLAZE_ROD), 16, 32);
        this.addStoreItem(new ItemStack(Items.GLOWSTONE_DUST), 8, 48);

        this.addFavorStoreItem(2, new ItemStack(ModItems.SPACE_SPHERE.get()), 64, 4);
        this.addFavorStoreItem(2, new ItemStack(Items.BOOK), 8, 64);
        this.addFavorStoreItem(4, new ItemStack(Items.DIAMOND), 64, 8);
        this.addFavorStoreItem(4, new ItemStack(Items.ENCHANTED_BOOK), 128, 2, this::applyMendingBook);
        this.addAchievementsStoreItem(
                new ItemStack(Items.QUARTZ),
                16,
                32,
                ADVANCEMENT_WE_NEED_TO_GO_DEEPER);
        this.addAchievementsStoreItem(
                new ItemStack(Items.ENDER_PEARL),
                64,
                16,
                ADVANCEMENT_WE_NEED_TO_GO_DEEPER);
        this.addFavorStoreItem(
                6, new ItemStack(Items.ENCHANTED_BOOK), 256, 2, this::applySwiftSneakBook);
        this.addFavorStoreItem(8, new ItemStack(Items.EXPERIENCE_BOTTLE), 32, 16);
        this.addFavorStoreItem(
                8, new ItemStack(Items.ENCHANTED_BOOK), 256, 2, this::applyWindBurstBook);
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

    private void applyMendingBook(ItemStack stack) {
        this.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(Enchantments.MENDING))
                .ifPresent(enchantment -> stack.enchant(enchantment, 1));
    }

    private void applySwiftSneakBook(ItemStack stack) {
        this.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(Enchantments.SWIFT_SNEAK))
                .ifPresent(enchantment -> stack.enchant(enchantment, 3));
    }

    private void applyWindBurstBook(ItemStack stack) {
        this.registryAccess()
                .lookup(Registries.ENCHANTMENT)
                .flatMap(registry -> registry.get(Enchantments.WIND_BURST))
                .ifPresent(enchantment -> stack.enchant(enchantment, 3));
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
