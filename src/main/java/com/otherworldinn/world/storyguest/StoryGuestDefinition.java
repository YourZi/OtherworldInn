package com.otherworldinn.world.storyguest;

import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.world.dialogue.DialogueDefinition;
import com.otherworldinn.world.dialogue.LocalizedText;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record StoryGuestDefinition(
        String id,
        LocalizedText displayName,
        ResourceLocation skinTexture,
        String modelType,
        int fixedSkinVariant,
        GuestEntity.GuestProfile guestProfile,
        int minReturnIntervalDays,
        int maxReturnIntervalDays,
        int minInnRating,
        int spawnWeight,
        Map<Integer, String> dialogueIdsByStage,
        List<DialogueDefinition> dialogues) {
    public String nameKey() {
        return "story_guest.otherworldinn." + this.id;
    }

    public String resolveDialogueId(int storyStage) {
        if (this.dialogueIdsByStage.containsKey(storyStage)) {
            return this.dialogueIdsByStage.get(storyStage);
        }
        String fallback = this.dialogueIdsByStage.get(0);
        int nearestStage = Integer.MIN_VALUE;
        for (Map.Entry<Integer, String> entry : this.dialogueIdsByStage.entrySet()) {
            if (entry.getKey() <= storyStage && entry.getKey() > nearestStage) {
                nearestStage = entry.getKey();
                fallback = entry.getValue();
            }
        }
        return fallback;
    }
}
