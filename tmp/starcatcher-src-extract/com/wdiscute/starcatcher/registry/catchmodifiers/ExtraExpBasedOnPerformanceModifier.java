package com.wdiscute.starcatcher.registry.catchmodifiers;

import net.minecraft.server.level.ServerPlayer;

public class ExtraExpBasedOnPerformanceModifier extends AbstractCatchModifier
{
    @Override
    public void onSuccessfulMinigameCompletion(ServerPlayer player, int time, boolean completedTreasure, boolean perfectCatch, int hits)
    {
        super.onSuccessfulMinigameCompletion(player, time, completedTreasure, perfectCatch, hits);
        int hitsNonCheated = Math.min(hits, 20);
        player.giveExperiencePoints(instance.fpToFish.rarity().getXp() * (hitsNonCheated / 3) + 1);
    }
}

