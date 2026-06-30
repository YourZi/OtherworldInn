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

public class RichGuestEntity extends GuestEntity {
    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/guest/ordinary_guest/1.png");
    private static final ResourceLocation DIAMOND_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "diamond");
    private static final ResourceLocation EMERALD_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "emerald");
    private static final ResourceLocation AMETHYST_SHARD_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "amethyst_shard");
    private static final ResourceLocation POLISHED_ROSE_QUARTZ_ID =
            ResourceLocation.fromNamespaceAndPath("create", "polished_rose_quartz");
    private static final List<ResourceLocation> TEXTURES = new ArrayList<>();
    private static boolean texturesLoaded = false;

    public RichGuestEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected GuestProfile getGuestProfile() {
        return new GuestProfile(
                new PreferenceRangeProfile(new GuestData.IntRange(28, 42), new GuestData.IntRange(72, 88)),
                new PreferenceRangeProfile(new GuestData.IntRange(30, 45), new GuestData.IntRange(74, 90)),
                new PreferenceRangeProfile(new GuestData.IntRange(5, 20), new GuestData.IntRange(70, 86)),
                new GuestData.IntRange(24, 48),
                1.2D);
    }

    @Override
    protected void initRewardItems() {
        this.getGuestData().addRewardItem(DIAMOND_ID, 0, 1);
        this.getGuestData().addRewardItem(EMERALD_ID, 0, 2);
        this.getGuestData().addRewardItem(AMETHYST_SHARD_ID, 0, 3);
        this.getGuestData().addRewardItem(POLISHED_ROSE_QUARTZ_ID, 0, 1);
    }

    @Override
    public ResourceLocation getSkinTexture() {
        if (!texturesLoaded && this.level().isClientSide) {
            try {
                List<ResourceLocation> found =
                        ClientServices.findTexturesInFolder(
                                OtherworldInn.MODID, "textures/entity/guest/rich_guest");
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
        return "default";
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
}
