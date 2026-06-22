package com.wdiscute.starcatcher.registry.sweetspotbehaviour;

import com.wdiscute.starcatcher.registry.minigamemodifiers.FrozenPointerWhileActiveModifier;

public class FreezeSweetSpotBehaviour extends AbstractSweetSpotBehaviour
{
    @Override
    public void onHit()
    {
        super.onHit();
        instance.addParticles(ass.pos, 30, 0xADD8E6);

        instance.addUniqueModifier(new FrozenPointerWhileActiveModifier(40, 10));
    }
}
