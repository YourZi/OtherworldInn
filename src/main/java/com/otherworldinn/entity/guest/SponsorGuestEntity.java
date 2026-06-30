package com.otherworldinn.entity.guest;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.VipGuestEntity;
import com.otherworldinn.world.sponsor.SponsorAppearanceSnapshot;
import com.otherworldinn.world.sponsor.SponsorDefinition;
import com.otherworldinn.world.sponsor.SponsorProfileSourceType;
import com.otherworldinn.world.sponsor.SponsorRegistry;
import com.otherworldinn.world.sponsor.service.SponsorProfileResolver;
import com.otherworldinn.world.inn.GuestData;
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
    private static final EntityDataAccessor<String> SPONSOR_NAME =
            SynchedEntityData.defineId(SponsorGuestEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> MODEL_TYPE =
            SynchedEntityData.defineId(SponsorGuestEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> RESOLVED_SKIN_URL =
            SynchedEntityData.defineId(SponsorGuestEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> PROFILE_SOURCE =
            SynchedEntityData.defineId(SponsorGuestEntity.class, EntityDataSerializers.INT);
    private static final String TAG_SPONSOR_NAME = "SponsorName";
    private static final String TAG_MODEL_TYPE = "SponsorModelType";
    private static final String TAG_SKIN_URL = "SponsorSkinUrl";
    private static final String TAG_PROFILE_SOURCE = "SponsorProfileSource";
    private static final ResourceLocation DEFAULT_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/guest/ordinary_guest/1.png");

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
        return DEFAULT_TEXTURE;
    }

    @Override
    public String getModelType() {
        String modelType = this.entityData.get(MODEL_TYPE);
        return "slim".equalsIgnoreCase(modelType) ? "slim" : "default";
    }

    @Override
    protected boolean shouldUseRandomName() {
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        ServerLevel serverLevel = level instanceof ServerLevel server ? server : null;
        SponsorDefinition sponsor = SponsorRegistry.pickRandom(this.getRandom(), serverLevel);
        this.setSponsorName(sponsor.playerName());
        this.applyAppearanceSnapshot(SponsorAppearanceSnapshot.localFallback(sponsor.playerName()));
        if (serverLevel != null) {
            this.resolveAppearanceAsync(serverLevel);
        }
        return spawnData;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPONSOR_NAME, "");
        builder.define(MODEL_TYPE, "default");
        builder.define(RESOLVED_SKIN_URL, "");
        builder.define(PROFILE_SOURCE, SponsorProfileSourceType.LOCAL_FALLBACK.syncCode());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString(TAG_SPONSOR_NAME, this.getSponsorName());
        compound.putString(TAG_MODEL_TYPE, this.getModelType());
        compound.putString(TAG_SKIN_URL, this.entityData.get(RESOLVED_SKIN_URL));
        compound.putInt(TAG_PROFILE_SOURCE, this.entityData.get(PROFILE_SOURCE));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setSponsorName(compound.getString(TAG_SPONSOR_NAME));
        this.entityData.set(MODEL_TYPE, normalizeModelType(compound.getString(TAG_MODEL_TYPE)));
        this.entityData.set(RESOLVED_SKIN_URL, compound.getString(TAG_SKIN_URL));
        this.entityData.set(PROFILE_SOURCE, compound.getInt(TAG_PROFILE_SOURCE));
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            this.resolveAppearanceAsync(serverLevel);
        }
    }

    public String getSponsorName() {
        String sponsorName = this.entityData.get(SPONSOR_NAME);
        if (sponsorName != null && !sponsorName.isBlank()) {
            return sponsorName;
        }
        return this.getName().getString();
    }

    public String getResolvedSkinUrl() {
        return this.entityData.get(RESOLVED_SKIN_URL);
    }

    private void setSponsorName(String sponsorName) {
        String normalizedName = sponsorName == null ? "" : sponsorName.trim();
        this.entityData.set(SPONSOR_NAME, normalizedName);
        if (!normalizedName.isBlank()) {
            this.setCustomName(Component.literal(normalizedName));
        }
    }

    private void applyAppearanceSnapshot(SponsorAppearanceSnapshot snapshot) {
        this.entityData.set(MODEL_TYPE, normalizeModelType(snapshot.modelType()));
        this.entityData.set(RESOLVED_SKIN_URL, snapshot.skinUrl());
        this.entityData.set(PROFILE_SOURCE, snapshot.sourceType().syncCode());
        if (!snapshot.displayName().isBlank()) {
            this.setCustomName(Component.literal(snapshot.displayName()));
        }
    }

    private void resolveAppearanceAsync(ServerLevel serverLevel) {
        String sponsorName = this.getSponsorName();
        if (sponsorName.isBlank()) {
            return;
        }
        SponsorDefinition definition = SponsorRegistry.resolveByName(sponsorName);
        String lockedSponsorName = sponsorName;
        SponsorProfileResolver.resolveAppearanceAsync(
                definition,
                serverLevel.getServer(),
                snapshot -> {
                    if (this.isRemoved() || !lockedSponsorName.equalsIgnoreCase(this.getSponsorName())) {
                        return;
                    }
                    this.applyAppearanceSnapshot(snapshot);
                });
    }

    private static String normalizeModelType(String modelType) {
        return "slim".equalsIgnoreCase(modelType) ? "slim" : "default";
    }
}
