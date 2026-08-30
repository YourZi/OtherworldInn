package com.otherworldinn.compat.jei;

import com.otherworldinn.entity.base.StoreEntity.CatalogEntry;
import com.otherworldinn.entity.base.StoreEntity.RandomOffer;
import com.otherworldinn.entity.store.BlacksmithEntity;
import com.otherworldinn.entity.store.BuilderEntity;
import com.otherworldinn.entity.store.ButcherEntity;
import com.otherworldinn.entity.store.FarmerEntity;
import com.otherworldinn.entity.store.FishermanEntity;
import com.otherworldinn.entity.store.GrocerEntity;
import com.otherworldinn.entity.store.MagicianEntity;
import com.otherworldinn.entity.store.WanderingTraderEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * JEI 商店配方数据源：直接读取各商店实体的静态 createCatalog()/createRandomOffers()，
 * 与游戏内商店共享同一数据来源，保证 JEI 展示与 NPC 实际出售逻辑一致。
 */
final class NpcStoreJeiData {
    private static final String FARMER = "entity.otherworldinn.farmer";
    private static final String BLACKSMITH = "entity.otherworldinn.blacksmith";
    private static final String MAGICIAN = "entity.otherworldinn.magician";
    private static final String GROCER = "entity.otherworldinn.grocer";
    private static final String BUTCHER = "entity.otherworldinn.butcher";
    private static final String BUILDER = "entity.otherworldinn.builder";
    private static final String FISHERMAN = "entity.otherworldinn.fisherman";
    private static final String WANDERING_TRADER = "entity.otherworldinn.wandering_trader";

    private NpcStoreJeiData() {}

    static List<NpcStoreJeiRecipe> getAllRecipes() {
        List<NpcStoreJeiRecipe> recipes = new ArrayList<>();
        addStore(recipes, FARMER, FarmerEntity::createCatalog, FarmerEntity::createRandomOffers);
        addStore(recipes, BLACKSMITH, BlacksmithEntity::createCatalog, BlacksmithEntity::createRandomOffers);
        addStore(recipes, MAGICIAN, MagicianEntity::createCatalog, MagicianEntity::createRandomOffers);
        addStore(recipes, GROCER, GrocerEntity::createCatalog, null);
        addStore(recipes, BUTCHER, ButcherEntity::createCatalog, ButcherEntity::createRandomOffers);
        addStore(recipes, BUILDER, BuilderEntity::createCatalog, BuilderEntity::createRandomOffers);
        addStore(recipes, FISHERMAN, FishermanEntity::createCatalog, FishermanEntity::createRandomOffers);
        addStore(recipes, WANDERING_TRADER, null, WanderingTraderEntity::createRandomOffers);
        return List.copyOf(recipes);
    }

    private static void addStore(
            List<NpcStoreJeiRecipe> recipes,
            String storeNameKey,
            Supplier<List<CatalogEntry>> catalogSupplier,
            Supplier<List<RandomOffer>> randomSupplier) {
        if (catalogSupplier != null) {
            for (CatalogEntry entry : catalogSupplier.get()) {
                if (entry.stack().isEmpty()) {
                    continue;
                }
                recipes.add(
                        new NpcStoreJeiRecipe(
                                storeNameKey,
                                entry.stack(),
                                entry.price(),
                                entry.price(),
                                entry.maxStock(),
                                entry.maxStock(),
                                entry.requiredFavorLevel(),
                                entry.requiredAdvancementTitleKey(),
                                false,
                                entry.festivalId()));
            }
        }
        if (randomSupplier != null) {
            for (RandomOffer offer : randomSupplier.get()) {
                if (offer.stack().isEmpty()) {
                    continue;
                }
                recipes.add(
                        new NpcStoreJeiRecipe(
                                storeNameKey,
                                offer.stack(),
                                offer.minPrice(),
                                offer.maxPrice(),
                                offer.minStock(),
                                offer.maxStock(),
                                offer.requiredFavorLevel(),
                                null,
                                true,
                                null));
            }
        }
    }
}
