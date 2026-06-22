package com.wdiscute.starcatcher.registry.fishing.compat;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.fishrestrictions.BiomeRestriction;
import com.wdiscute.starcatcher.registry.fishrestrictions.DimensionRestriction;
import com.wdiscute.starcatcher.registry.FishProperties;

import java.util.List;

public class DGSullysModFishes extends FishingPropertiesRegistry
{
    public static void bootstrap()
    {
        //
        // ,---.            ,--. ,--.           ,--.             ,--.   ,--.            ,--.
        //'   .-'  ,--.,--. |  | |  | ,--. ,--. |  |  ,---.      |   `.'   |  ,---.   ,-|  |
        //`.  `-.  |  ||  | |  | |  |  \  '  /  `-'  (  .-'      |  |'.'|  | | .-. | ' .-. |
        //.-'    | '  ''  ' |  | |  |   \   '        .-'  `)     |  |   |  | ' '-' ' \ `-' |
        //`-----'   `----'  `--' `--' .-'  /         `----'      `--'   `--'  `---'   `---'
        //                            `---'

        register(fish(U.holderItem("sullysmod", "piranha"))
                .withBucketedFish(U.holderItem("sullysmod", "piranha_bucket"))
                .withEntityToSpawn(U.holderEntity("sullysmod", "piranha"))
                .withSizeAndWeight(FishProperties.sizeWeight(30, 10, 500, 300))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.HARD_MOVING)
                .addRestrictions(DimensionRestriction.OVERWORLD,
                        new BiomeRestriction(List.of(), List.of(U.rl("sullysmod", "biome/piranha_spawn_in")), List.of(), List.of(), ""))
        );

        register(fish(U.holderItem("sullysmod", "lanternfish"))
                .withBucketedFish(U.holderItem("sullysmod", "lanternfish_bucket"))
                .withEntityToSpawn(U.holderEntity("sullysmod", "lanternfish"))
                .withSizeAndWeight(FishProperties.sizeWeight(100, 50, 15000, 10000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.HARD)
                .addRestrictions(DimensionRestriction.OVERWORLD,
                        new BiomeRestriction(List.of(), List.of(U.rl("sullysmod", "biome/lanternfish_spawn_in")), List.of(), List.of(), ""))
        );
    }
}
