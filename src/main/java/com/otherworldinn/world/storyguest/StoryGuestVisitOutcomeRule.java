package com.otherworldinn.world.storyguest;

public record StoryGuestVisitOutcomeRule(String requiredFlag, boolean continueVisiting) {
    public StoryGuestVisitOutcomeRule {
        requiredFlag = requiredFlag == null ? "" : requiredFlag.trim();
    }

    public boolean isValid() {
        return !requiredFlag.isBlank();
    }
}
