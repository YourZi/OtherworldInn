package com.otherworldinn.entity.guest;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.world.storyguest.StoryGuestDefinition;
import com.otherworldinn.world.storyguest.StoryGuestRegistry;
import com.otherworldinn.world.storyguest.StoryGuestSavedData;
import javax.annotation.Nullable;
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
    private static final ResourceLocation FALLBACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/guest/ordinary_guest/1.png");

    private String storyGuestId = "";
    @Nullable private StoryGuestDefinition definition;

    public StoryGuestEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public void setStoryGuestId(String storyGuestId) {
        this.storyGuestId = storyGuestId == null ? "" : storyGuestId;
        this.definition = this.storyGuestId.isBlank() ? null : StoryGuestRegistry.get(this.storyGuestId);
        applyDefinitionState();
    }

    @Nullable
    public String getStoryGuestId() {
        return this.storyGuestId.isBlank() ? null : this.storyGuestId;
    }

    @Nullable
    public StoryGuestDefinition getStoryGuestDefinition() {
        if (this.definition == null && !this.storyGuestId.isBlank()) {
            this.definition = StoryGuestRegistry.get(this.storyGuestId);
        }
        return this.definition;
    }

    public void refreshAssignedDialogue(ServerLevel level) {
        StoryGuestDefinition storyDefinition = getStoryGuestDefinition();
        if (storyDefinition == null) {
            return;
        }
        int storyStage =
                StoryGuestSavedData.get(level)
                        .getOrCreateProgress(storyDefinition.id())
                        .getStoryStage();
        this.setAssignedDialogueId(storyDefinition.resolveDialogueId(storyStage));
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
        if (!this.storyGuestId.isBlank()) {
            compound.putString(TAG_STORY_GUEST_ID, this.storyGuestId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains(TAG_STORY_GUEST_ID)) {
            this.storyGuestId = compound.getString(TAG_STORY_GUEST_ID);
        }
        this.definition = this.storyGuestId.isBlank() ? null : StoryGuestRegistry.get(this.storyGuestId);
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
        this.setCustomName(Component.translatable(storyDefinition.nameKey()));
        this.setSkinVariant(storyDefinition.fixedSkinVariant());
        this.setBudget(randomInRange(storyDefinition.guestProfile().budgetRange()));
    }

    private int randomInRange(com.otherworldinn.world.inn.GuestData.IntRange range) {
        if (range.min() >= range.max()) {
            return range.min();
        }
        return range.min() + this.getRandom().nextInt(range.max() - range.min() + 1);
    }
}
