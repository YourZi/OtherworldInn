package com.otherworldinn.entity.guest;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.util.ClientServices;
import com.otherworldinn.world.inn.GuestData;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class HeavyPackGuestEntity extends GuestEntity {
    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/guest/ordinary_guest/1.png");
    private static final ResourceLocation IRON_INGOT_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "iron_ingot");
    private static final ResourceLocation COPPER_INGOT_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "copper_ingot");
    private static final ResourceLocation ANDESITE_ALLOY_ID =
            ResourceLocation.fromNamespaceAndPath("create", "andesite_alloy");
    private static final ResourceLocation BRASS_INGOT_ID =
            ResourceLocation.fromNamespaceAndPath("create", "brass_ingot");
    private static final ResourceLocation PRECISION_MECHANISM_ID =
            ResourceLocation.fromNamespaceAndPath("create", "precision_mechanism");
    private static final int HIGH_WEIGHT = 12;
    private static final int MEDIUM_WEIGHT = 5;
    private static final int ULTRA_LOW_WEIGHT = 1;
    private static final List<RewardOption> REWARD_POOL = List.of(
            new RewardOption(IRON_INGOT_ID, 0, 3, MEDIUM_WEIGHT),
            new RewardOption(COPPER_INGOT_ID, 0, 4, MEDIUM_WEIGHT),
            new RewardOption(ANDESITE_ALLOY_ID, 0, 2, MEDIUM_WEIGHT),
            new RewardOption(ResourceLocation.fromNamespaceAndPath("minecraft", "wheat"), 0, 5, HIGH_WEIGHT),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "wheat_seeds"), 0, 6, HIGH_WEIGHT),
            new RewardOption(ResourceLocation.fromNamespaceAndPath("minecraft", "carrot"), 0, 5, HIGH_WEIGHT),
            new RewardOption(ResourceLocation.fromNamespaceAndPath("minecraft", "potato"), 0, 5, HIGH_WEIGHT),
            new RewardOption(ResourceLocation.fromNamespaceAndPath("minecraft", "beetroot"), 0, 5, HIGH_WEIGHT),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "beetroot_seeds"), 0, 6, HIGH_WEIGHT),
            new RewardOption(ResourceLocation.fromNamespaceAndPath("minecraft", "pumpkin"), 0, 2, HIGH_WEIGHT),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "pumpkin_seeds"), 0, 5, HIGH_WEIGHT),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "melon_slice"), 0, 6, HIGH_WEIGHT),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "melon_seeds"), 0, 5, HIGH_WEIGHT),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "sweet_berries"), 0, 6, HIGH_WEIGHT),
            new RewardOption(ResourceLocation.fromNamespaceAndPath("minecraft", "apple"), 0, 3, HIGH_WEIGHT),
            new RewardOption(ResourceLocation.fromNamespaceAndPath("minecraft", "bread"), 0, 3, HIGH_WEIGHT),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "baked_potato"), 0, 3, HIGH_WEIGHT),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "cooked_chicken"), 0, 2, HIGH_WEIGHT),
            new RewardOption(BRASS_INGOT_ID, 0, 1, ULTRA_LOW_WEIGHT),
            new RewardOption(PRECISION_MECHANISM_ID, 0, 1, ULTRA_LOW_WEIGHT));
    private static final List<ResourceLocation> TEXTURES = new ArrayList<>();
    private static boolean texturesLoaded = false;

    public HeavyPackGuestEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected GuestProfile getGuestProfile() {
        return new GuestProfile(
                new PreferenceRangeProfile(new GuestData.IntRange(18, 42), new GuestData.IntRange(62, 88)),
                new PreferenceRangeProfile(new GuestData.IntRange(20, 45), new GuestData.IntRange(54, 90)),
                new PreferenceRangeProfile(new GuestData.IntRange(5, 20), new GuestData.IntRange(50, 66)),
                new GuestData.IntRange(12, 55),
                1.1D);
    }

    @Override
    protected void initRewardItems() {
        List<RewardOption> options = new ArrayList<>(REWARD_POOL);
        int count = 3 + this.getRandom().nextInt(3);
        int selectedCount = Math.min(count, options.size());
        for (int i = 0; i < selectedCount; i++) {
            int totalWeight = 0;
            for (RewardOption option : options) {
                totalWeight += option.weight();
            }
            int roll = this.getRandom().nextInt(totalWeight);
            int weightedIndex = 0;
            for (int idx = 0; idx < options.size(); idx++) {
                roll -= options.get(idx).weight();
                if (roll < 0) {
                    weightedIndex = idx;
                    break;
                }
            }
            RewardOption option = options.remove(weightedIndex);
            this.getGuestData().addRewardItem(option.itemId(), option.minCount(), option.maxCount());
        }
    }

    @Override
    public ResourceLocation getSkinTexture() {
        if (!texturesLoaded && this.level().isClientSide) {
            try {
                List<ResourceLocation> found =
                        ClientServices.findTexturesInFolder(
                                OtherworldInn.MODID, "textures/entity/guest/heavy_pack_guest");
                if (!found.isEmpty()) {
                    TEXTURES.clear();
                    TEXTURES.addAll(found);
                }
            } catch (Throwable e) {
            }
            texturesLoaded = true;
        }

        if (TEXTURES.isEmpty()) {
            return DEFAULT_TEXTURE;
        }
        return TEXTURES.get(Math.abs(this.getSkinVariant()) % TEXTURES.size());
    }

    @Override
    public String getModelType() {
        return "slim";
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.setSkinVariant(this.getRandom().nextInt(10000));
        return spawnData;
    }

    private record RewardOption(ResourceLocation itemId, int minCount, int maxCount, int weight) {}
}
