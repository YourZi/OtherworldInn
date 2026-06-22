package com.wdiscute.starcatcher.registry.sweetspotbehaviour;

import com.wdiscute.starcatcher.minigame.ActiveSweetSpot;
import com.wdiscute.starcatcher.minigame.FishingMinigameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;

public class TreasureSweetSpotBehaviour extends AbstractSweetSpotBehaviour
{

    @Override
    public void onHit()
    {
        super.onHit();
        //spawn particles before repositioning
        instance.addParticles(ass.pos, 30, 0x00ff00);
        //reposition
        ass.pos = instance.getRandomFreePosition(ass.thickness);
        //flip if ass is flip
        if(ass.isFlip) instance.currentRotation *= -1;
        //award treasure progress based on ass
        instance.treasureProgress += ass.reward;
        //playsound
        Minecraft.getInstance().player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.4f, 1f);
    }

    @Override
    public void onAdd(FishingMinigameScreen instance, ActiveSweetSpot ass)
    {
        super.onAdd(instance, ass);
        //if no treasure item is set, doesnt add it
        if(instance.treasureIS.isEmpty())
        {
            ass.removed = true;
            return;
        }
        if(!instance.treasureActive) instance.treasureActive = true;
        if(instance.treasureProgress >= 100) ass.removed = true;
    }

    @Override
    public void tick()
    {
        super.tick();
        if(instance.treasureProgress >= 100) ass.removed = true;
    }
}
