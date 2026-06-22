package com.wdiscute.starcatcher.registry.catchmodifiers;

public class AdjustLureTimeModifier extends AbstractCatchModifier
{

    final float minTicks;
    final float maxTicks;
    final float randomness;

    public AdjustLureTimeModifier(float minTicks, float maxTicks, float randomness)
    {
        this.minTicks = minTicks;
        this.maxTicks = maxTicks;
        this.randomness = randomness;
    }

    @Override
    public int adjustMinTicksToFish(int minTicksToFish)
    {
        return (int) (minTicksToFish * minTicks);
    }

    @Override
    public int adjustMaxTicksToFish(int maxTicksToFish)
    {
        return (int) (maxTicksToFish * maxTicks);
    }

    @Override
    public float adjustChanceToFishEachTick(float chanceToFishEachTick)
    {
        return chanceToFishEachTick * randomness;
    }
}
