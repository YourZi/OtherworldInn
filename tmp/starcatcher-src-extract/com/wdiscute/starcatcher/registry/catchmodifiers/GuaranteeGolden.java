package com.wdiscute.starcatcher.registry.catchmodifiers;

import com.wdiscute.starcatcher.io.FishCaughtCounter;
import net.minecraft.server.level.ServerPlayer;

public class GuaranteeGolden extends AbstractCatchModifier
{

    @Override
    public boolean shouldBeGolden()
    {
        return FishCaughtCounter.canCatchGolden(instance.fpToFish, (ServerPlayer) instance.player);
    }
}
