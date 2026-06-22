package com.wdiscute.starcatcher.registry.fishing.compat;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.fishrestrictions.DaytimeRestriction;
import com.wdiscute.starcatcher.registry.FishProperties;

public class DGSpawnFishes extends FishingPropertiesRegistry
{
    public static void bootstrap()
    {

        //
        // ,---.
        //'   .-'   ,---.   ,--,--. ,--.   ,--. ,--,--,
        //`.  `-.  | .-. | ' ,-.  | |  |.'.|  | |      \
        //.-'    | | '-' ' \ '-'  | |   .'.   | |  ||  |
        //`-----'  |  |-'   `--`--' '--'   '--' `--''--'
        //         `--'

        register(fish(U.holderItem("spawn", "angler_fish"))
                .withBucketedFish(U.holderItem("spawn", "angler_fish_bucket"))
                .withEntityToSpawn(U.holderEntity("spawn", "angler_fish"))
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
                .addRestrictions(FishProperties.WorldRestrictions.OVERWORLD_DEEP_OCEAN)
                .withRarity(FishProperties.Rarity.RARE)
                .withDaytimeRestriction(DaytimeRestriction.MIDNIGHT)
                .withDifficulty(FishProperties.Difficulty.FOUR_AQUA)
                .withBaseChance(20)
        );

        register(fish(U.holderItem("spawn", "tuna_egg_bucket"))
                .withBucketedFish(U.holderItem("spawn", "tuna_egg_bucket"))
                .withEntityToSpawn(U.holderEntity("spawn", "tuna"))
                .withAlwaysSpawnEntity()
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
                .addRestrictions(FishProperties.WorldRestrictions.OVERWORLD_ALL_OCEANS)
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM_MOVING)
        );

        register(fish(U.holderItem("spawn", "baby_sunfish_bucket"))
                .withBucketedFish(U.holderItem("spawn", "baby_sunfish_bucket"))
                .withEntityToSpawn(U.holderEntity("spawn", "sunfish"))
                .withAlwaysSpawnEntity()
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
                .addRestrictions(FishProperties.WorldRestrictions.OVERWORLD_WARM_OCEAN)
                .withRarity(FishProperties.Rarity.EPIC)
                .withDaytimeRestriction(DaytimeRestriction.NOON)
                .withBaseChance(20)
                .withDifficulty(FishProperties.Difficulty.TWO_THIN.vanishing())
        );

        register(fish(U.holderItem("spawn", "captured_octopus"))
                .withEntityToSpawn(U.holderEntity("spawn", "octopus"))
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
                .addRestrictions(FishProperties.WorldRestrictions.OVERWORLD_ALL_OCEANS)
                .withBaseChance(1)
                .withRarity(FishProperties.Rarity.RARE)
                .withDifficulty(FishProperties.Difficulty.MEDIUM_VANISHING_MOVING)
        );

        register(fish(U.holderItem("spawn", "herring"))
                .withBucketedFish(U.holderItem("spawn", "herring_bucket"))
                .withEntityToSpawn(U.holderEntity("spawn", "herring"))
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
                .addRestrictions(FishProperties.WorldRestrictions.OVERWORLD_ALL_OCEANS)
                .withRarity(FishProperties.Rarity.COMMON)
                .withDifficulty(FishProperties.Difficulty.EASY_MOVING)
        );

    }
}
