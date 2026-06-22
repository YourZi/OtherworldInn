package com.wdiscute.starcatcher.registry.sweetspotbehaviour;

import com.wdiscute.starcatcher.minigame.ActiveSweetSpot;
import com.wdiscute.starcatcher.minigame.FishingMinigameScreen;

public class AquaSweetSpot extends NormalSweetSpotBehaviour
{
    @Override
    public void onAdd(FishingMinigameScreen instance, ActiveSweetSpot ass)
    {
        super.onAdd(instance, ass);
        ass.shouldSudokuOnVanish = true;
        ass.vanishingRate = 0.018f;
    }

    @Override
    public void tick()
    {
        super.tick();
        if(ass.shouldSudokuOnVanish && ass.alpha <= 0)
            instance.addParticles(ass.pos, 10, ass.particleColor);
    }
}
