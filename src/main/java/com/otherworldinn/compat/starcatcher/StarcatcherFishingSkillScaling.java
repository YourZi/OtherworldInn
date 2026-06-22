package com.otherworldinn.compat.starcatcher;

import com.otherworldinn.compat.ReskillableCompat;
import net.minecraft.world.entity.player.Player;

public final class StarcatcherFishingSkillScaling {
    public static final String FISHING_SKILL_ID = "magic";
    private static final int EFFECTIVE_LEVEL_CAP = 30;
    private static final double MAX_WAIT_BONUS = 0.20D;
    private static final double MAX_MINIGAME_ASSIST = 0.15D;
    private static final double MAX_NEW_FISH_BIAS = 0.20D;
    private static final double MAX_TREASURE_BONUS = 0.08D;
    private static final double MAX_QUALITY_BONUS = 0.03D;

    private StarcatcherFishingSkillScaling() {}

    public static int getFishingSkillLevel(Player player) {
        if (player == null || !ReskillableCompat.isLoaded()) {
            return 0;
        }
        return Math.max(0, ReskillableCompat.getSkillLevel(player, FISHING_SKILL_ID));
    }

    public static double getWaitBonus(int skillLevel) {
        return MAX_WAIT_BONUS * normalized(skillLevel);
    }

    public static double getMinigameAssist(int skillLevel) {
        return MAX_MINIGAME_ASSIST * normalized(skillLevel);
    }

    public static double getNewFishBias(int skillLevel) {
        return MAX_NEW_FISH_BIAS * Math.pow(normalized(skillLevel), 1.35D);
    }

    public static double getTreasureBonus(int skillLevel) {
        return MAX_TREASURE_BONUS * Math.pow(normalized(skillLevel), 1.60D);
    }

    public static double getQualityBonus(int skillLevel) {
        return MAX_QUALITY_BONUS * Math.pow(normalized(skillLevel), 1.80D);
    }

    private static double normalized(int skillLevel) {
        int effectiveLevel = Math.max(0, Math.min(skillLevel, EFFECTIVE_LEVEL_CAP));
        return effectiveLevel / (double) EFFECTIVE_LEVEL_CAP;
    }
}
