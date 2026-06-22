package com.wdiscute.starcatcher.registry.catchmodifiers;

public class SkipMinigameIfVanillaLoot extends AbstractCatchModifier
{
    @Override
    public boolean forceSkipMinigame(Boolean enableMinigameConfig)
    {
        return instance.modifiers.stream().anyMatch(o -> o instanceof VanillaLootModifier);
    }
}
