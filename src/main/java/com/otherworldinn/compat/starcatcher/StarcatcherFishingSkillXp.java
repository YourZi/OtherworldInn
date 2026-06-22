package com.otherworldinn.compat.starcatcher;

import com.otherworldinn.compat.ReskillableCompat;
import com.wdiscute.starcatcher.registry.FishProperties;
import net.minecraft.server.level.ServerPlayer;

public final class StarcatcherFishingSkillXp {
    private static final int FIRST_CATCH_BONUS = 2;
    private static final int PERFECT_CATCH_BONUS = 1;
    private static final int GOLDEN_CATCH_BONUS = 2;
    private static final int TREASURE_BONUS = 1;

    private StarcatcherFishingSkillXp() {}

    public static void awardFishingSkillExperience(
            ServerPlayer player,
            FishProperties fishProperties,
            boolean firstCatch,
            boolean perfectCatch,
            boolean completedTreasure,
            boolean goldenCatch) {
        if (player == null
                || fishProperties == null
                || !ReskillableCompat.isLoaded()
                || player.level().isClientSide) {
            return;
        }

        int totalXp = getBaseSkillXp(fishProperties);
        if (firstCatch) {
            totalXp += FIRST_CATCH_BONUS;
        }
        if (perfectCatch) {
            totalXp += PERFECT_CATCH_BONUS;
        }
        if (completedTreasure) {
            totalXp += TREASURE_BONUS;
        }
        if (goldenCatch) {
            totalXp += GOLDEN_CATCH_BONUS;
        }

        if (totalXp > 0) {
            ReskillableCompat.addSkillExperience(
                    player, StarcatcherFishingSkillScaling.FISHING_SKILL_ID, totalXp);
        }
    }

    private static int getBaseSkillXp(FishProperties fishProperties) {
        int rarityXp = fishProperties.rarity().getXp();
        if (rarityXp <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(rarityXp / 4.0D));
    }
}
