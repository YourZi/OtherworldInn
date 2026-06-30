package com.otherworldinn.entity.guest;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.VipGuestEntity;
import com.otherworldinn.util.ClientServices;
import com.otherworldinn.world.inn.GuestData;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class AdvancedVipGuestEntity extends VipGuestEntity {
    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/guest/ordinary_guest/1.png");
    private static final List<ResourceLocation> TEXTURES = new ArrayList<>();
    private static boolean texturesLoaded = false;
    private static final List<RewardOption> REWARD_POOL = List.of(
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "diamond"), 1, 2, 18),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "netherite_ingot"), 1, 1, 4),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "ender_pearl"), 2, 6, 20),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "dragon_breath"), 1, 2, 10),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "experience_bottle"), 2, 6, 24),
            new RewardOption(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "nether_star"), 1, 1, 1));

    public AdvancedVipGuestEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected GuestProfile getGuestProfile() {
        return new GuestProfile(
                new PreferenceRangeProfile(new GuestData.IntRange(44, 62), new GuestData.IntRange(80, 100)),
                new PreferenceRangeProfile(new GuestData.IntRange(46, 64), new GuestData.IntRange(70, 100)),
                new PreferenceRangeProfile(new GuestData.IntRange(5, 20), new GuestData.IntRange(65, 100)),
                new GuestData.IntRange(86, 140),
                1.5D);
    }

    @Override
    protected int getVipMealMinPriceExclusive() {
        return 30;
    }

    @Override
    protected void initRewardItems() {
        RewardOption option = pickRewardOption(this.getRandom());
        if (option == null) {
            return;
        }
        this.getGuestData().addRewardItem(option.itemId(), option.minCount(), option.maxCount());
    }

    @Override
    public ResourceLocation getSkinTexture() {
        if (!texturesLoaded && this.level().isClientSide) {
            try {
                List<ResourceLocation> found =
                        ClientServices.findTexturesInFolder(
                                OtherworldInn.MODID, "textures/entity/guest/advanced_vip_guest");
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
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.setSkinVariant(this.getRandom().nextInt(10000));
        return spawnData;
    }

    private RewardOption pickRewardOption(RandomSource random) {
        int totalWeight = 0;
        for (RewardOption option : REWARD_POOL) {
            totalWeight += option.weight();
        }
        if (totalWeight <= 0) {
            return null;
        }
        int roll = random.nextInt(totalWeight);
        int current = 0;
        for (RewardOption option : REWARD_POOL) {
            current += option.weight();
            if (roll < current) {
                return option;
            }
        }
        return REWARD_POOL.get(REWARD_POOL.size() - 1);
    }

    private record RewardOption(ResourceLocation itemId, int minCount, int maxCount, int weight) {}
}
