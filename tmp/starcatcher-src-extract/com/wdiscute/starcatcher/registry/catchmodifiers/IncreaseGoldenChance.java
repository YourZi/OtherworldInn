package com.wdiscute.starcatcher.registry.catchmodifiers;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.io.FishCaughtCounter;
import net.minecraft.server.level.ServerPlayer;

public class IncreaseGoldenChance extends AbstractCatchModifier
{
    private final float increase;

    public IncreaseGoldenChance(float increase)
    {
        this.increase = increase;
    }


    @Override
    public boolean shouldBeGolden()
    {
        return FishCaughtCounter.canCatchGolden(instance.fpToFish, (ServerPlayer) instance.player) && U.r.nextFloat() < increase;
    }
}
