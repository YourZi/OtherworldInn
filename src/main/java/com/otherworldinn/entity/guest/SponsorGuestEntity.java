package com.otherworldinn.entity.guest;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.util.ClientServices;
import com.otherworldinn.util.service.MojangProfileService;
import com.otherworldinn.entity.base.VipGuestEntity;
import com.otherworldinn.util.service.SponsorNamePool;
import com.otherworldinn.world.inn.GuestData;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class SponsorGuestEntity extends VipGuestEntity {
    private static final EntityDataAccessor<Boolean> SLIM_MODEL =
            SynchedEntityData.defineId(SponsorGuestEntity.class, EntityDataSerializers.BOOLEAN);
    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/guest/sponsor_guest/1.png");
    private static final List<ResourceLocation> TEXTURES = new ArrayList<>();
    private static boolean texturesLoaded = false;
    public SponsorGuestEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    @Override
    protected GuestProfile getGuestProfile() {
        return new GuestProfile(
                new PreferenceRangeProfile(new GuestData.IntRange(24, 45), new GuestData.IntRange(60, 100)),
                new PreferenceRangeProfile(new GuestData.IntRange(26, 40), new GuestData.IntRange(50, 100)),
                new PreferenceRangeProfile(new GuestData.IntRange(5, 20), new GuestData.IntRange(65, 100)),
                new GuestData.IntRange(86, 140),
                1.8D);
    }

    @Override
    public ResourceLocation getSkinTexture() {
        String name = this.getName().getString();
        ResourceLocation fallback = resolveLocalFallbackTexture();
        return ClientServices.getMojangSkinTexture(name, fallback);
    }

    @Override
    public String getModelType() {
        return this.entityData.get(SLIM_MODEL) ? "slim" : "default";
    }

    private ResourceLocation resolveLocalFallbackTexture() {
        if (!texturesLoaded && this.level().isClientSide) {
            try {
                List<ResourceLocation> found =
                        ClientServices.findTexturesInFolder(
                                OtherworldInn.MODID, "textures/entity/guest/sponsor_guest");
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
        ServerLevel serverLevel = level instanceof ServerLevel server ? server : null;
        String sponsorName = SponsorNamePool.getRandomName(this.getRandom(), serverLevel);
        this.setCustomName(Component.literal(sponsorName));
        this.entityData.set(SLIM_MODEL, false);
        if (serverLevel != null) {
            final String lockedName = sponsorName;
            MojangProfileService.resolveSlimModelAsync(
                    sponsorName,
                    serverLevel.getServer(),
                    slim -> {
                        if (!this.isRemoved() && lockedName.equals(this.getName().getString())) {
                            this.entityData.set(SLIM_MODEL, slim);
                        }
                    });
        }
        this.setSkinVariant(this.getRandom().nextInt(10000));
        return spawnData;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLIM_MODEL, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("SponsorSlimModel", this.entityData.get(SLIM_MODEL));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(SLIM_MODEL, compound.getBoolean("SponsorSlimModel"));
    }
}
