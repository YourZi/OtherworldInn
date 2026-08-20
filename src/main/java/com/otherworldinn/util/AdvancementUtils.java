package com.otherworldinn.util;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.entity.store.BlacksmithEntity;
import com.otherworldinn.entity.store.BuilderEntity;
import com.otherworldinn.entity.store.ButcherEntity;
import com.otherworldinn.entity.store.FarmerEntity;
import com.otherworldinn.entity.store.FishermanEntity;
import com.otherworldinn.entity.store.GrocerEntity;
import com.otherworldinn.entity.store.MagicianEntity;
import java.util.List;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public final class AdvancementUtils {
    public static final ResourceLocation REPAIR_BOILER_ROOM =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "repair_boiler_room");
    public static final ResourceLocation REPAIR_GREENHOUSE =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "repair_greenhouse");
    public static final ResourceLocation INN_RATING_1 =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "inn_rating_1");
    public static final ResourceLocation INN_RATING_2 =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "inn_rating_2");
    public static final ResourceLocation INN_RATING_3 =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "inn_rating_3");
    public static final ResourceLocation INN_RATING_4 =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "inn_rating_4");
    public static final ResourceLocation INN_RATING_5 =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "inn_rating_5");
    public static final ResourceLocation SERVE_ONE_VIP =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "serve_one_vip");
    public static final ResourceLocation CREATE_FIRST_ROOM =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "create_first_room");
    public static final ResourceLocation SERVE_FIRST_GUEST =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "serve_first_guest");
    public static final ResourceLocation COMPLETE_1_COMMISSION =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "complete_1_commission");
    public static final ResourceLocation COMPLETE_20_COMMISSIONS =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "complete_20_commissions");
    public static final ResourceLocation BLACKSMITH_MAX_FAVOR =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "blacksmith_max_favor");
    public static final ResourceLocation FARMER_MAX_FAVOR =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "farmer_max_favor");
    public static final ResourceLocation MAGICIAN_MAX_FAVOR =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "magician_max_favor");
    public static final ResourceLocation GROCER_MAX_FAVOR =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "grocer_max_favor");
    public static final ResourceLocation BUILDER_MAX_FAVOR =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "builder_max_favor");
    public static final ResourceLocation BUTCHER_MAX_FAVOR =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "butcher_max_favor");
    public static final ResourceLocation FISHERMAN_MAX_FAVOR =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "fisherman_max_favor");
    public static final ResourceLocation ALL_NPC_MAX_FAVOR =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "all_npc_max_favor");
    public static final ResourceLocation BOILER_ROOM_MAX_LEVEL =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "boiler_room_max_level");
    public static final ResourceLocation GREENHOUSE_MAX_LEVEL =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "greenhouse_max_level");
    public static final ResourceLocation MINE_MAX_LEVEL =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "mine_max_level");
    public static final ResourceLocation ALL_FACILITY_MAX_LEVEL =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "all_facility_max_level");
    public static final ResourceLocation TOO_MANY_BEDS =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "too_many_beds");
    public static final ResourceLocation STARCATCHER_FULL_COLLECTION =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "starcatcher_full_collection");
    public static final ResourceLocation FIELD_GUIDE_COMPLETE =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "field_guide_complete");
    private static final AABB TOWN_NPC_SCAN_BOX = new AABB(-1024, -64, -1024, 1024, 384, 1024);

    private AdvancementUtils() {}

    public static void award(ServerPlayer player, ResourceLocation advancementId) {
        if (player == null || advancementId == null) {
            return;
        }
        AdvancementHolder advancement = player.server.getAdvancements().get(advancementId);
        if (advancement == null) {
            return;
        }
        for (String criterion : player.getAdvancements().getOrStartProgress(advancement).getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    public static void awardInnRatingProgress(ServerPlayer player, int rating) {
        if (rating >= 1) {
            award(player, INN_RATING_1);
        }
        if (rating >= 2) {
            award(player, INN_RATING_2);
        }
        if (rating >= 3) {
            award(player, INN_RATING_3);
        }
        if (rating >= 4) {
            award(player, INN_RATING_4);
        }
        if (rating >= 5) {
            award(player, INN_RATING_5);
        }
    }

    public static void awardStoreFavorProgress(
            ServerPlayer player, StoreEntity storeEntity, ServerLevel townLevel) {
        if (player == null || storeEntity == null || townLevel == null) {
            return;
        }
        int maxFavor = StoreEntity.getMaxFavorLevelValue();
        if (storeEntity.getFavorLevel() >= maxFavor) {
            if (storeEntity instanceof BlacksmithEntity) {
                award(player, BLACKSMITH_MAX_FAVOR);
            } else if (storeEntity instanceof BuilderEntity) {
                award(player, BUILDER_MAX_FAVOR);
            } else if (storeEntity instanceof ButcherEntity) {
                award(player, BUTCHER_MAX_FAVOR);
            } else if (storeEntity instanceof FarmerEntity) {
                award(player, FARMER_MAX_FAVOR);
            } else if (storeEntity instanceof FishermanEntity) {
                award(player, FISHERMAN_MAX_FAVOR);
            } else if (storeEntity instanceof MagicianEntity) {
                award(player, MAGICIAN_MAX_FAVOR);
            } else if (storeEntity instanceof GrocerEntity) {
                award(player, GROCER_MAX_FAVOR);
            }
        }
        if (isAllCoreStoreNpcAtMaxFavor(townLevel, maxFavor)) {
            award(player, ALL_NPC_MAX_FAVOR);
        }
    }

    private static boolean isAllCoreStoreNpcAtMaxFavor(ServerLevel townLevel, int maxFavor) {
        List<StoreEntity> stores = townLevel.getEntitiesOfClass(StoreEntity.class, TOWN_NPC_SCAN_BOX);
        boolean blacksmithMax = false;
        boolean builderMax = false;
        boolean butcherMax = false;
        boolean farmerMax = false;
        boolean fishermanMax = false;
        boolean magicianMax = false;
        boolean grocerMax = false;
        for (StoreEntity store : stores) {
            if (store instanceof BlacksmithEntity && store.getFavorLevel() >= maxFavor) {
                blacksmithMax = true;
            } else if (store instanceof BuilderEntity && store.getFavorLevel() >= maxFavor) {
                builderMax = true;
            } else if (store instanceof ButcherEntity && store.getFavorLevel() >= maxFavor) {
                butcherMax = true;
            } else if (store instanceof FarmerEntity && store.getFavorLevel() >= maxFavor) {
                farmerMax = true;
            } else if (store instanceof FishermanEntity && store.getFavorLevel() >= maxFavor) {
                fishermanMax = true;
            } else if (store instanceof MagicianEntity && store.getFavorLevel() >= maxFavor) {
                magicianMax = true;
            } else if (store instanceof GrocerEntity && store.getFavorLevel() >= maxFavor) {
                grocerMax = true;
            }
        }
        return blacksmithMax && builderMax && butcherMax && farmerMax && fishermanMax && magicianMax && grocerMax;
    }
}
