package com.wdiscute.starcatcher.registry.fishing.compat;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.fishrestrictions.BaitRestriction;
import com.wdiscute.starcatcher.registry.FishProperties;

import java.util.Map;

public class DGTerraFirmaCraftFishes extends FishingPropertiesRegistry
{
    public static void bootstrap()
    {

        //
        //   ,--.
        // ,-'  '-.  ,---.  ,--.--. ,--.--.  ,--,--.
        // '-.  .-' | .-. : |  .--' |  .--' ' ,-.  |
        //   |  |   \   --. |  |    |  |    \ '-'  |
        //   `--'    `----' `--'    `--'     `--`--'
        //  ,---. ,--.
        // /  .-' `--' ,--.--. ,--,--,--.  ,--,--.
        // |  `-, ,--. |  .--' |        | ' ,-.  |
        // |  .-' |  | |  |    |  |  |  | \ '-'  |
        // `--'   `--' `--'    `--`--`--'  `--`--'
        //                          ,---.   ,--.
        //  ,---. ,--.--.  ,--,--. /  .-' ,-'  '-.
        // | .--' |  .--' ' ,-.  | |  `-, '-.  .-'
        // \ `--. |  |    \ '-'  | |  .-'   |  |
        //  `---' `--'     `--`--' `--'     `--'
        //


        register(overworldLakeFish(U.holderItem("tfc", "food/bluegill"))
                .withBucketedFish(U.holderItem("tfc", "bucket/bluegill"))
                .withEntityToSpawn(U.holderEntity("tfc", "bluegill"))
                .withSizeAndWeight(FishProperties.sizeWeight(15, 5, 200, 20))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
        );

        register(overworldRiverFish(U.holderItem("tfc", "food/lake_trout"))
                .withBucketedFish(U.holderItem("tfc", "bucket/lake_trout"))
                .withEntityToSpawn(U.holderEntity("tfc", "lake_trout"))
                .withSizeAndWeight(FishProperties.sizeWeight(15, 5, 200, 20))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
        );

        register(overworldLakeFish(U.holderItem("tfc", "food/largemouth_bass"))
                .withBucketedFish(U.holderItem("tfc", "bucket/bluegill"))
                .withEntityToSpawn(U.holderEntity("tfc", "bluegill"))
                .withSizeAndWeight(FishProperties.sizeWeight(15, 5, 200, 20))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
        );

        register(overworldRiverFish(U.holderItem("tfc", "food/rainbow_trout"))
                .withBucketedFish(U.holderItem("tfc", "bucket/rainbow_trout"))
                .withEntityToSpawn(U.holderEntity("tfc", "rainbow_trout"))
                .withSizeAndWeight(FishProperties.sizeWeight(15, 5, 200, 20))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
        );

        register(overworldLakeFish(U.holderItem("tfc", "food/smallmouth_bass"))
                .withBucketedFish(U.holderItem("tfc", "bucket/smallmouth_bass"))
                .withEntityToSpawn(U.holderEntity("tfc", "smallmouth_bass"))
                .withSizeAndWeight(FishProperties.sizeWeight(15, 5, 200, 20))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
        );

        register(overworldLakeFish(U.holderItem("tfc", "food/salmon"))
                .withBucketedFish(U.holderItem("tfc", "bucket/salmon"))
                .withEntityToSpawn(U.holderEntity("tfc", "salmon"))
                .withSizeAndWeight(FishProperties.sizeWeight(15, 5, 200, 20))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
        );

        register(overworldOceanFish(U.holderItem("tfc", "food/cod"))
                .withBucketedFish(U.holderItem("tfc", "bucket/cod"))
                .withEntityToSpawn(U.holderEntity("tfc", "cod"))
                .withSizeAndWeight(FishProperties.sizeWeight(15, 5, 200, 20))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
        );


        //dolphin
        //todo change baits to use tag #tfc:large_fishing_bait and #tfc:small_fishing_bait
        register(overworldOceanFish(U.holderItem("tfc", "blubber"))
                .withEntityToSpawn(U.holderEntity("tfc", "dolphin"))
                .withBaseChance(0)
                .withAlwaysSpawnEntity(true)
                .withSkipMinigame(true)
                .addRestrictions(new BaitRestriction(Map.of(
                        U.rl("tfc", "food/bluegill"), 50,
                        U.rl("tfc", "food/cod"), 50,
                        U.rl("tfc", "food/salmon"), 50,
                        U.rl("tfc", "food/tropical_fish"), 50
                        ), ""))
                .withSizeAndWeight(FishProperties.sizeWeight(15, 5, 200, 20))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
                .withHasGuideEntry(false)
        );


        //trash
        register(overworldOceanFish(U.holderItem("tfc", "food/fresh_seaweed"))
                .withBaseChance(1)
                .withSkipMinigame(true)
                .withHasGuideEntry(false)
        );

        register(overworldRiverFish(U.holderItem("tfc", "food/shellfish"))
                .withEntityToSpawn(U.holderEntity("tfc", "crayfish"))
                .withBaseChance(1)
                .withSkipMinigame(true)
                .withHasGuideEntry(false)
        );

        register(overworldOceanFish(U.holderItem("tfc", "groundcover/sea_urchin"))
                .withBaseChance(1)
                .withSkipMinigame(true)
                .withHasGuideEntry(false)
        );

        register(overworldBeachFish(U.holderItem("tfc", "groundcover/clam"))
                .withBaseChance(1)
                .withSkipMinigame(true)
                .withHasGuideEntry(false)
        );

        register(overworldFish(U.holderItem("tfc", "groundcover/driftwood"))
                .withBaseChance(1)
                .withSkipMinigame(true)
                .withHasGuideEntry(false)
        );


    }
}
