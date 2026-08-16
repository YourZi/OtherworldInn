package com.otherworldinn.entity.store;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import com.simibubi.create.AllItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
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
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;
import sereneseasons.init.ModConfig;

public class FarmerEntity extends StoreEntity {
    private static final String SERENE_SEASONS_MOD_ID = "sereneseasons";
    private static final TagKey<Item> SPRING_CROPS =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(SERENE_SEASONS_MOD_ID, "spring_crops"));
    private static final TagKey<Item> SUMMER_CROPS =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(SERENE_SEASONS_MOD_ID, "summer_crops"));
    private static final TagKey<Item> AUTUMN_CROPS =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(SERENE_SEASONS_MOD_ID, "autumn_crops"));
    private static final TagKey<Item> WINTER_CROPS =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(SERENE_SEASONS_MOD_ID, "winter_crops"));
    private static final TagKey<Item> YEAR_ROUND_CROPS =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(SERENE_SEASONS_MOD_ID, "year_round_crops"));
    private static final List<SeasonalProduct> SEASONAL_PRODUCTS =
            List.of(
                    new SeasonalProduct(() -> new ItemStack(Items.SUGAR_CANE), 2, 64),
                    new SeasonalProduct(() -> new ItemStack(Items.WHEAT_SEEDS), 1, 16),
                    new SeasonalProduct(() -> new ItemStack(Items.BEETROOT_SEEDS), 1, 16),
                    new SeasonalProduct(() -> new ItemStack(Items.CARROT), 2, 64),
                    new SeasonalProduct(() -> new ItemStack(Items.POTATO), 2, 64),
                    new SeasonalProduct(() -> new ItemStack(Items.PUMPKIN_SEEDS), 4, 16),
                    new SeasonalProduct(() -> new ItemStack(Items.MELON_SEEDS), 1, 16),
                    new SeasonalProduct(() -> new ItemStack(Items.GLOW_BERRIES), 2, 64),
                    new SeasonalProduct(() -> new ItemStack(Items.SWEET_BERRIES), 2, 64),
                    new SeasonalProduct(() -> new ItemStack(ModItems.TOMATO_SEED.get()), 2, 16),
                    new SeasonalProduct(() -> new ItemStack(ModItems.CHILI_SEED.get()), 2, 16),
                    new SeasonalProduct(() -> new ItemStack(ModItems.LETTUCE_SEED.get()), 2, 16),
                    new SeasonalProduct(() -> new ItemStack(ModItems.WILD_RICE_SEED.get()), 2, 16),
                    new SeasonalProduct(
                            () ->
                                    new ItemStack(
                                            com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems.GRAPEVINE
                                                    .get()),
                            4,
                            64));

    public FarmerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WHEAT));
        equipDefaultStrawHat();
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        if (!level.isClientSide) {
            this.initDefaultStoreItems();
        }
    }

    private void equipDefaultStrawHat() {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.STRAW_HAT.get()));
    }

    private void initDefaultStoreItems() {
        this.applyCatalog(createCatalog());
        this.refreshRandomItems();
    }

    public static List<CatalogEntry> createCatalog() {
        List<CatalogEntry> entries = new ArrayList<>();
        entries.add(new CatalogEntry(new ItemStack(Items.BONE_MEAL), 4, 64, 2));
        entries.add(new CatalogEntry(new ItemStack(AllItems.TREE_FERTILIZER.get()), 6, 64, 4));
        entries.add(new CatalogEntry(new ItemStack(ModItems.SCARECROW.get()), 24, 8, 6));
        entries.add(new CatalogEntry(new ItemStack(Items.NETHERITE_HOE), 64, 1, 8));
        return entries;
    }

    public static List<RandomOffer> createRandomOffers() {
        List<RandomOffer> offers = new ArrayList<>();
        for (SeasonalProduct product : SEASONAL_PRODUCTS) {
            ItemStack stack = product.stackSupplier().get();
            if (stack.isEmpty()) {
                continue;
            }
            offers.add(
                    new RandomOffer(
                            stack, product.price(), product.price(), product.stock(), product.stock()));
        }
        return offers;
    }

    @Override
    protected void applyCodeDefaultsAfterDebugReset() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WHEAT));
        equipDefaultStrawHat();
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.initDefaultStoreItems();
    }

    @Override
    protected void refreshRandomItems() {
        super.refreshRandomItems();
        boolean useSeasonRules = this.shouldUseSeasonRules();
        Season currentSeason = useSeasonRules ? this.getCurrentSeason() : null;
        for (SeasonalProduct product : SEASONAL_PRODUCTS) {
            ItemStack stack = product.stackSupplier().get();
            if (stack.isEmpty()) {
                continue;
            }
            if (this.shouldSellProduct(stack, useSeasonRules, currentSeason)) {
                this.addRandomStoreItem(stack, product.price(), product.price(), product.stock(), product.stock());
            }
        }
    }

    private boolean shouldUseSeasonRules() {
        if (!ModList.get().isLoaded(SERENE_SEASONS_MOD_ID)) {
            return false;
        }
        try {
            if (ModConfig.fertility == null || ModConfig.seasons == null) {
                return false;
            }
            if (!ModConfig.fertility.seasonalCrops) {
                return false;
            }
            return ModConfig.seasons.isDimensionWhitelisted(this.level().dimension());
        } catch (Throwable ignored) {
            return false;
        }
    }

    private Season getCurrentSeason() {
        try {
            return SeasonHelper.getSeasonState(this.level()).getSeason();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private boolean shouldSellProduct(ItemStack stack, boolean useSeasonRules, Season currentSeason) {
        if (!useSeasonRules || currentSeason == null) {
            return true;
        }
        if (stack.is(YEAR_ROUND_CROPS)) {
            return true;
        }
        boolean managedBySeasonTags =
                stack.is(SPRING_CROPS)
                        || stack.is(SUMMER_CROPS)
                        || stack.is(AUTUMN_CROPS)
                        || stack.is(WINTER_CROPS);
        if (!managedBySeasonTags) {
            return true;
        }
        return switch (currentSeason) {
            case SPRING -> stack.is(SPRING_CROPS);
            case SUMMER -> stack.is(SUMMER_CROPS);
            case AUTUMN -> stack.is(AUTUMN_CROPS);
            case WINTER -> stack.is(WINTER_CROPS);
        };
    }

    @Override
    public ResourceLocation getStoreBackground() {
        return ResourceLocation.fromNamespaceAndPath(
                OtherworldInn.MODID, "textures/gui/store/farmer.png");
    }

    @Override
    protected SoundEvent getOpenStoreSound() {
        return SoundEvents.COMPOSTER_FILL_SUCCESS;
    }

    private record SeasonalProduct(Supplier<ItemStack> stackSupplier, int price, int stock) {}
}
