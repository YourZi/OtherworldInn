package com.wdiscute.starcatcher.registry.fishing.compat;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.fishrestrictions.BiomeRestriction;
import com.wdiscute.starcatcher.registry.fishrestrictions.DimensionRestriction;
import com.wdiscute.starcatcher.registry.FishProperties;

import java.util.List;

public class DGEnvironmentalFishes extends FishingPropertiesRegistry
{
    public static void bootstrap()
    {

        //
        //,------.                     ,--.                                                        ,--.            ,--.
        //|  .---' ,--,--,  ,--.  ,--. `--' ,--.--.  ,---.  ,--,--,  ,--,--,--.  ,---.  ,--,--,  ,-'  '-.  ,--,--. |  |
        //|  `--,  |      \  \  `'  /  ,--. |  .--' | .-. | |      \ |        | | .-. : |      \ '-.  .-' ' ,-.  | |  |
        //|  `---. |  ||  |   \    /   |  | |  |    ' '-' ' |  ||  | |  |  |  | \   --. |  ||  |   |  |   \ '-'  | |  |
        //`------' `--''--'    `--'    `--' `--'     `---'  `--''--' `--`--`--'  `----' `--''--'   `--'    `--`--' `--'
        //


        register(fish(U.holderItem("environmental", "koi"))
                .withBucketedFish(U.holderItem("environmental", "koi_bucket"))
                .withEntityToSpawn(U.holderEntity("environmental", "koi"))
                .withSizeAndWeight(FishProperties.sizeWeight(60, 20, 3000, 2000))
                .withDifficulty(FishProperties.Difficulty.EASY_FAST_FISH)
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        new BiomeRestriction(List.of(U.rl("environmental", "blossom_woods"), U.rl("environmental", "blossom_valleys")),
                                List.of(), List.of(), List.of(), "")
                )
        );

        register(fish(U.holderItem("environmental", "slabfish_bucket"))
                .withAlwaysSpawnEntity(true)
                .withBucketedFish(U.holderItem("environmental", "slabfish_bucket"))
                .withEntityToSpawn(U.holderEntity("environmental", "slabfish"))
                .withSizeAndWeight(FishProperties.sizeWeight(120, 40, 20000, 10000))
                .withDifficulty(FishProperties.Difficulty.HARD)
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        new BiomeRestriction(List.of(U.rl("environmental", "marsh")),
                                List.of(), List.of(), List.of(), "")
                )
        );

    }
}
