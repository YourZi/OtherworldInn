package com.otherworldinn.world.dialogue;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record DialogueRequirementDef(
        DialogueRequirementType type,
        @Nullable ResourceLocation itemId,
        int count,
        @Nullable String storyFlag,
        int stageValue) {
    public static DialogueRequirementDef hasItem(ResourceLocation itemId, int count) {
        return new DialogueRequirementDef(
                DialogueRequirementType.HAS_ITEM,
                itemId,
                Math.max(1, count),
                null,
                0);
    }

    public static DialogueRequirementDef hasStoryFlag(String storyFlag) {
        return new DialogueRequirementDef(
                DialogueRequirementType.STORY_FLAG_PRESENT, null, 0, storyFlag, 0);
    }

    public static DialogueRequirementDef missingStoryFlag(String storyFlag) {
        return new DialogueRequirementDef(
                DialogueRequirementType.STORY_FLAG_ABSENT, null, 0, storyFlag, 0);
    }

    public static DialogueRequirementDef storyStageEquals(int stageValue) {
        return new DialogueRequirementDef(
                DialogueRequirementType.STORY_STAGE_EQUALS, null, 0, null, stageValue);
    }
}
