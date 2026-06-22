package com.wdiscute.starcatcher.registry.minigamemodifiers;

public abstract class AbstractTimedModifier extends AbstractMinigameModifier {
    int length = -1;

    public AbstractTimedModifier(int length) {
        this.length = length;
    }

    public AbstractTimedModifier() {

    }

    public int getLength(){
        return length;
    }

    @Override
    public void tick() {
        if (length > 0 && tickCount >= length) {
            removed = true;
        }
        super.tick();
    }
}
