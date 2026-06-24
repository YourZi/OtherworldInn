package com.otherworldinn.entity.guest;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.world.storyguest.StoryGuestDefinition;
import com.otherworldinn.world.storyguest.StoryGuestRegistry;
import com.otherworldinn.world.storyguest.StoryGuestSavedData;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class StoryGuestEntity extends GuestEntity {
    private static final String TAG_STORY_GUEST_ID = "StoryGuestId";
    private static final String TAG_VISIT_STAGE_SNAPSHOT = "VisitStageSnapshot";
    private static final String TAG_VISIT_STAGE_CONSUMED = "VisitStageConsumed";
    private static final ResourceLocation FALLBACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/guest/ordinary_guest/1.png");

    @Nullable private String storyGuestId;
    @Nullable private StoryGuestDefinition definition;
    private int visitStageSnapshot = -1;
    private boolean visitStageConsumed = false;

    public StoryGuestEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public void setStoryGuestId(String storyGuestId) {
        this.storyGuestId = normalizeStoryGuestId(storyGuestId);
        this.definition = this.storyGuestId == null ? null : StoryGuestRegistry.get(this.storyGuestId);
        this.visitStageSnapshot = -1;
        this.visitStageConsumed = false;
        applyDefinitionState();
    }

    @Nullable
    public String getStoryGuestId() {
        return normalizeStoryGuestId(this.storyGuestId);
    }

    @Nullable
    public StoryGuestDefinition getStoryGuestDefinition() {
        String normalizedId = normalizeStoryGuestId(this.storyGuestId);
        if (this.definition == null && normalizedId != null) {
            this.definition = StoryGuestRegistry.get(normalizedId);
        }
        return this.definition;
    }

    public void refreshAssignedDialogue(ServerLevel level) {
        StoryGuestDefinition storyDefinition = getStoryGuestDefinition();
        if (storyDefinition == null) {
            return;
        }
        ensureVisitStageInitialized(level, storyDefinition);
        this.setAssignedDialogueId(
                storyDefinition.resolveVisitDialogueId(this.visitStageSnapshot, this.visitStageConsumed));
    }

    public void markVisitStageConsumed(ServerLevel level) {
        StoryGuestDefinition storyDefinition = getStoryGuestDefinition();
        if (storyDefinition == null) {
            return;
        }
        ensureVisitStageInitialized(level, storyDefinition);
        this.visitStageConsumed = true;
        refreshAssignedDialogue(level);
    }

    @Override
    protected boolean shouldUseRandomDialogue() {
        return false;
    }

    @Override
    protected boolean shouldUseRandomName() {
        return false;
    }

    @Override
    protected GuestProfile getGuestProfile() {
        StoryGuestDefinition storyDefinition = getStoryGuestDefinition();
        return storyDefinition != null ? storyDefinition.guestProfile() : super.getGuestProfile();
    }

    @Override
    protected long getStayDuration() {
        return (2L + this.getRandom().nextInt(2)) * 24000L;
    }

    @Override
    public ResourceLocation getSkinTexture() {
        StoryGuestDefinition storyDefinition = getStoryGuestDefinition();
        return storyDefinition != null ? storyDefinition.skinTexture() : FALLBACK_TEXTURE;
    }

    @Override
    public String getModelType() {
        StoryGuestDefinition storyDefinition = getStoryGuestDefinition();
        if (storyDefinition == null || storyDefinition.modelType().isBlank()) {
            return super.getModelType();
        }
        return storyDefinition.modelType();
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);
        applyDefinitionState();
        if (level.getLevel() instanceof ServerLevel serverLevel) {
            refreshAssignedDialogue(serverLevel);
        }
        return spawnData;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        String normalizedId = normalizeStoryGuestId(this.storyGuestId);
        if (normalizedId != null) {
            compound.putString(TAG_STORY_GUEST_ID, normalizedId);
        }
        if (this.visitStageSnapshot >= 0) {
            compound.putInt(TAG_VISIT_STAGE_SNAPSHOT, this.visitStageSnapshot);
        }
        compound.putBoolean(TAG_VISIT_STAGE_CONSUMED, this.visitStageConsumed);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_STORY_GUEST_ID)) {
            this.storyGuestId = normalizeStoryGuestId(compound.getString(TAG_STORY_GUEST_ID));
        }
        if (compound.contains(TAG_VISIT_STAGE_SNAPSHOT)) {
            this.visitStageSnapshot = compound.getInt(TAG_VISIT_STAGE_SNAPSHOT);
        }
        this.visitStageConsumed = compound.getBoolean(TAG_VISIT_STAGE_CONSUMED);
        this.definition = this.storyGuestId == null ? null : StoryGuestRegistry.get(this.storyGuestId);
        applyDefinitionState();
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            refreshAssignedDialogue(serverLevel);
        }
    }

    private void applyDefinitionState() {
        StoryGuestDefinition storyDefinition = getStoryGuestDefinition();
        if (storyDefinition == null) {
            return;
        }
        this.setCustomName(
                Component.translatable(storyDefinition.nameKey()).withStyle(ChatFormatting.LIGHT_PURPLE));
        this.setSkinVariant(storyDefinition.fixedSkinVariant());
        this.setBudget(randomInRange(storyDefinition.guestProfile().budgetRange()));
    }

    private int randomInRange(com.otherworldinn.world.inn.GuestData.IntRange range) {
        if (range.min() >= range.max()) {
            return range.min();
        }
        return range.min() + this.getRandom().nextInt(range.max() - range.min() + 1);
    }

    private void ensureVisitStageInitialized(ServerLevel level, StoryGuestDefinition storyDefinition) {
        if (this.visitStageSnapshot >= 0) {
            return;
        }
        this.visitStageSnapshot =
                StoryGuestSavedData.get(level)
                        .getOrCreateProgress(storyDefinition.id())
                        .getStoryStage();
        this.visitStageConsumed = false;
    }

    @Nullable
    private static String normalizeStoryGuestId(@Nullable String storyGuestId) {
        if (storyGuestId == null || storyGuestId.isBlank()) {
            return null;
        }
        return storyGuestId;
    }
}
