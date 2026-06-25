package com.otherworldinn.world.photo;

import net.minecraft.resources.ResourceLocation;

public record StoryGuestPhotoTask(
        String storyGuestId,
        ResourceLocation objectiveId,
        int requiredStage,
        String activationFlag,
        String completionFlag) {}
