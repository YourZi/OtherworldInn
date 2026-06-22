package com.wdiscute.starcatcher.registry.fishing.compat;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.fishrestrictions.BaitRestriction;
import com.wdiscute.starcatcher.registry.fishrestrictions.DaytimeRestriction;
import com.wdiscute.starcatcher.registry.fishrestrictions.SeasonRestriction;
import com.wdiscute.starcatcher.registry.fishrestrictions.WeatherRestriction;
import com.wdiscute.starcatcher.registry.FishProperties;

public class DGAquacultureFishes extends FishingPropertiesRegistry
{
    public static void bootstrap()
    {

        //
        //                                                   ,--.   ,--.                                 ,---.
        // ,--,--.  ,---.  ,--.,--.  ,--,--.  ,---. ,--.,--. |  | ,-'  '-. ,--.,--. ,--.--.  ,---.      '.-.  \
        //' ,-.  | | .-. | |  ||  | ' ,-.  | | .--' |  ||  | |  | '-.  .-' |  ||  | |  .--' | .-. :      .-' .'
        //\ '-'  | ' '-' | '  ''  ' \ '-'  | \ `--. '  ''  ' |  |   |  |   '  ''  ' |  |    \   --.     /   '-.
        // `--`--'  `-|  |  `----'   `--`--'  `---'  `----'  `--'   `--'    `----'  `--'     `----'     '-----'
        //            `--'

        //freshwater
        register(overworldRiverFish(U.holderItem("aquaculture", "smallmouth_bass"))
                .withBucketedFish(U.holderItem("aquaculture", "smallmouth_bass_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "smallmouth_bass"))
                .withSeasons(SeasonRestriction.NOT_WINTER)
                .withSizeAndWeight(FishProperties.sizeWeight(30, 10, 1500, 500))
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
                .withDaytimeRestriction(DaytimeRestriction.DAY)
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        register(overworldRiverFish(U.holderItem("aquaculture", "bluegill"))
                .withBucketedFish(U.holderItem("aquaculture", "smallmouth_bass_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "smallmouth_bass"))
                .withSeasons(SeasonRestriction.SUMMER_AUTUMN)
                .withSizeAndWeight(FishProperties.sizeWeight(15, 3, 300, 200))
                .withTreasure(U.rl("aquaculture", "box"))
        );

        register(overworldRiverFish(U.holderItem("aquaculture", "brown_trout"))
                .withBucketedFish(U.holderItem("aquaculture", "brown_trout_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "brown_trout"))
                .withSeasons(SeasonRestriction.NOT_SPRING)
                .withSizeAndWeight(FishProperties.sizeWeight(45, 15, 3000, 2000))
                .withDaytimeRestriction(DaytimeRestriction.NIGHT)
                .withWeather(WeatherRestriction.CLEAR)
                .withTreasure(U.rl("aquaculture", "box"))
        );

        register(overworldRiverFish(U.holderItem("aquaculture", "carp"))
                .withBucketedFish(U.holderItem("aquaculture", "carp_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "carp"))
                .withSeasons(SeasonRestriction.SPRING_WINTER)
                .withSizeAndWeight(FishProperties.sizeWeight(60, 20, 10000, 4000))
                .withDifficulty(FishProperties.Difficulty.HARD)
                .withRarity(FishProperties.Rarity.RARE)
                .withWeather(WeatherRestriction.RAIN)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        register(overworldMountainFish(U.holderItem("aquaculture", "catfish"))
                .withBucketedFish(U.holderItem("aquaculture", "catfish_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "catfish"))
                .withSizeAndWeight(FishProperties.sizeWeight(150, 40, 100000, 25000))
                .withDifficulty(FishProperties.Difficulty.THIN_NO_DECAY_NOT_FORGIVING)
                .withRarity(FishProperties.Rarity.EPIC)
                .withWeather(WeatherRestriction.RAIN)
                .withTreasure(U.rl("aquaculture", "treasure_chest"))
        );

        register(overworldMountainFish(U.holderItem("aquaculture", "gar"))
                .withBucketedFish(U.holderItem("aquaculture", "gar_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "gar"))
                .withSeasons(SeasonRestriction.SPRING_WINTER)
                .withSizeAndWeight(FishProperties.sizeWeight(160, 30, 160000, 20000))
                .withTreasure(U.rl("aquaculture", "box"))
        );

        register(overworldLakeFish(U.holderItem("aquaculture", "minnow"))
                .withBucketedFish(U.holderItem("aquaculture", "minnow_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "minnow"))
                .withSizeAndWeight(FishProperties.sizeWeight(6, 4, 10, 4))
                .withTreasure(U.rl("aquaculture", "box"))
        );

        register(overworldLakeFish(U.holderItem("aquaculture", "muskellunge"))
                .withBucketedFish(U.holderItem("aquaculture", "muskellunge_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "muskellunge"))
                .withSizeAndWeight(FishProperties.sizeWeight(100, 10, 7000, 3000))
                .withRarity(FishProperties.Rarity.RARE)
                .withDaytimeRestriction(DaytimeRestriction.MIDNIGHT)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        register(overworldLakeFish(U.holderItem("aquaculture", "perch"))
                .withBucketedFish(U.holderItem("aquaculture", "perch_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "perch"))
                .withSeasons(SeasonRestriction.SUMMER_AUTUMN)
                .withSizeAndWeight(FishProperties.sizeWeight(20, 5, 500, 200))
                .withTreasure(U.rl("aquaculture", "box"))
        );

        //arid
        register(overworldWarmMountainFish(U.holderItem("aquaculture", "bayad"))
                .withBucketedFish(U.holderItem("aquaculture", "bayad_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "bayad"))
                .withSeasons(SeasonRestriction.SUMMER_AUTUMN)
                .withSizeAndWeight(FishProperties.sizeWeight(170, 30, 150000, 20000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
                .withDaytimeRestriction(DaytimeRestriction.NIGHT)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        register(overworldWarmLakeFish(U.holderItem("aquaculture", "boulti"))
                .withBucketedFish(U.holderItem("aquaculture", "boulti_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "boulti"))
                .withSeasons(SeasonRestriction.SUMMER)
                .withSizeAndWeight(FishProperties.sizeWeight(40, 10, 4000, 300))
                .withRarity(FishProperties.Rarity.RARE)
                .withDaytimeRestriction(DaytimeRestriction.DAY)
                .withDifficulty(FishProperties.Difficulty.HARD)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        register(overworldWarmMountainFish(U.holderItem("aquaculture", "capitaine"))
                .withBucketedFish(U.holderItem("aquaculture", "capitaine_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "capitaine"))
                .withSeasons(SeasonRestriction.SPRING_SUMMER)
                .withSizeAndWeight(FishProperties.sizeWeight(130, 50, 12000, 3000))
                .withTreasure(U.rl("aquaculture", "box"))
        );

        register(overworldWarmMountainFish(U.holderItem("aquaculture", "synodontis"))
                .withBucketedFish(U.holderItem("aquaculture", "synodontis_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "synodontis"))
                .withSizeAndWeight(FishProperties.sizeWeight(35, 15, 1000, 300))
                .withDifficulty(FishProperties.Difficulty.HARD_MOVING)
                .withRarity(FishProperties.Rarity.EPIC)
                .withTreasure(U.rl("aquaculture", "treasure_chest"))
        );

        //arctic ocean
        register(overworldColdOceanFish(U.holderItem("aquaculture", "atlantic_cod"))
                .withBucketedFish(U.holderItem("aquaculture", "atlantic_cod_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "atlantic_cod"))
                .withSeasons(SeasonRestriction.WINTER)
                .withSizeAndWeight(FishProperties.sizeWeight(100, 50, 15000, 10000))
                .withDaytimeRestriction(DaytimeRestriction.DAY)
                .withTreasure(U.rl("aquaculture", "box"))
        );

        register(overworldColdOceanFish(U.holderItem("aquaculture", "blackfish"))
                .withBucketedFish(U.holderItem("aquaculture", "blackfish_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "blackfish"))
                .withSeasons(SeasonRestriction.WINTER)
                .withSizeAndWeight(FishProperties.sizeWeight(50, 20, 5000, 3000))
                .withDaytimeRestriction(DaytimeRestriction.NIGHT)
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        register(overworldColdOceanFish(U.holderItem("aquaculture", "pacific_halibut"))
                .withBucketedFish(U.holderItem("aquaculture", "pacific_halibut_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "pacific_halibut"))
                .withSeasons(SeasonRestriction.NOT_SUMMER)
                .withSizeAndWeight(FishProperties.sizeWeight(150, 50, 80000, 5000))
                .withTreasure(U.rl("aquaculture", "box"))
        );

        register(overworldColdOceanFish(U.holderItem("aquaculture", "atlantic_halibut"))
                .withBucketedFish(U.holderItem("aquaculture", "atlantic_halibut_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "atlantic_halibut"))
                .withSizeAndWeight(FishProperties.sizeWeight(200, 80, 150000, 10000))
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
                .withWeather(WeatherRestriction.RAIN)
                .withTreasure(U.rl("aquaculture", "box"))
        );

        register(overworldColdOceanFish(U.holderItem("aquaculture", "atlantic_herring"))
                .withBucketedFish(U.holderItem("aquaculture", "atlantic_herring_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "atlantic_herring"))
                .withSeasons(SeasonRestriction.SPRING_WINTER)
                .withSizeAndWeight(FishProperties.sizeWeight(25, 5, 200, 100))
                .withDifficulty(FishProperties.Difficulty.HARD_MOVING)
                .withRarity(FishProperties.Rarity.RARE)
                .withDaytimeRestriction(DaytimeRestriction.DAY)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        register(overworldColdOceanFish(U.holderItem("aquaculture", "pink_salmon"))
                .withBucketedFish(U.holderItem("aquaculture", "pink_salmon_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "pink_salmon"))
                .withSeasons(SeasonRestriction.SPRING_WINTER)
                .withSizeAndWeight(FishProperties.sizeWeight(50, 10, 2000, 1000))
                .withRarity(FishProperties.Rarity.EPIC)
                .withWeather(WeatherRestriction.THUNDER)
                .withDifficulty(FishProperties.Difficulty.HARD)
                .withTreasure(U.rl("aquaculture", "treasure_chest"))
        );

        register(overworldColdOceanFish(U.holderItem("aquaculture", "pollock"))
                .withBucketedFish(U.holderItem("aquaculture", "pollock_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "pollock"))
                .withSizeAndWeight(FishProperties.sizeWeight(70, 30, 5000, 4000))
                .withTreasure(U.rl("aquaculture", "box"))
        );

        register(overworldColdOceanFish(U.holderItem("aquaculture", "rainbow_trout"))
                .withBucketedFish(U.holderItem("aquaculture", "rainbow_trout_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "rainbow_trout"))
                .withSeasons(SeasonRestriction.WINTER)
                .withSizeAndWeight(FishProperties.sizeWeight(60, 20, 2000, 1500))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDaytimeRestriction(DaytimeRestriction.DAY)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        //saltwater
        register(overworldOceanFish(U.holderItem("aquaculture", "jellyfish"))
                .withBucketedFish(U.holderItem("aquaculture", "jellyfish_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "jellyfish"))
                .withSeasons(SeasonRestriction.SUMMER)
                .withSizeAndWeight(FishProperties.sizeWeight(100, 70, 50000, 40000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.HARD)
                .withBaseChance(3)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        register(overworldOceanFish(U.holderItem("aquaculture", "red_grouper"))
                .withBucketedFish(U.holderItem("aquaculture", "red_grouper_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "red_grouper"))
                .withSeasons(SeasonRestriction.AUTUMN)
                .withSizeAndWeight(FishProperties.sizeWeight(100, 50, 15000, 10000))
                .withTreasure(U.rl("aquaculture", "box"))
        );

        register(overworldOceanFish(U.holderItem("aquaculture", "tuna"))
                .withBucketedFish(U.holderItem("aquaculture", "tuna_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "tuna"))
                .withSizeAndWeight(FishProperties.sizeWeight(200, 100, 200000, 150000))
                .withTreasure(U.rl("aquaculture", "box"))
        );

        //jungle
        register(overworldJungleFish(U.holderItem("aquaculture", "arapaima"))
                .withBucketedFish(U.holderItem("aquaculture", "arapaima_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "arapaima"))
                .withSeasons(SeasonRestriction.AUTUMN_WINTER)
                .withSizeAndWeight(FishProperties.sizeWeight(250, 50, 50000, 150000))
                .withRarity(FishProperties.Rarity.RARE)
                .withDifficulty(FishProperties.Difficulty.HARD)
                .withWeather(WeatherRestriction.RAIN)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        register(overworldJungleFish(U.holderItem("aquaculture", "arrau_turtle"))
                //no bucketed version
                .withEntityToSpawn(U.holderEntity("aquaculture", "arrau_turtle"))
                .withSizeAndWeight(FishProperties.sizeWeight(100, 30, 80000, 150000))
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
                .withTreasure(U.rl("aquaculture", "box"))
        );


        register(overworldJungleFish(U.holderItem("aquaculture", "piranha"))
                .withBucketedFish(U.holderItem("aquaculture", "piranha_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "piranha"))
                .withSeasons(SeasonRestriction.NOT_WINTER)
                .withSizeAndWeight(FishProperties.sizeWeight(30, 10, 500, 300))
                .addRestrictions(BaitRestriction.LEGENDARY_BAIT)
                .withRarity(FishProperties.Rarity.LEGENDARY)
                .withDifficulty(FishProperties.Difficulty.FOUR_BIG)
                .withDaytimeRestriction(DaytimeRestriction.NOON)
                .withTreasure(U.rl("aquaculture", "treasure_chest"))
        );

        register(overworldJungleFish(U.holderItem("aquaculture", "tambaqui"))
                .withBucketedFish(U.holderItem("aquaculture", "tambaqui_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "tambaqui"))
                .withSizeAndWeight(FishProperties.sizeWeight(100, 30, 150000, 10000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        //swamp
        register(overworldSwampFish(U.holderItem("aquaculture", "leech"))
                //no bucketed version
                //no entity
                .withSeasons(SeasonRestriction.AUTUMN)
                .withSizeAndWeight(FishProperties.sizeWeight(10, 5, 5, 3))
                .withRarity(FishProperties.Rarity.RARE)
                .withDifficulty(FishProperties.Difficulty.HARD)
                .withTreasure(U.rl("aquaculture", "lockbox"))
        );

        register(overworldSwampFish(U.holderItem("aquaculture", "box_turtle"))
                //no bucketed version
                .withEntityToSpawn(U.holderEntity("aquaculture", "box_turtle"))
                .withSizeAndWeight(FishProperties.sizeWeight(20, 5, 1000, 500))
                .withRarity(FishProperties.Rarity.EPIC)
                .withDifficulty(FishProperties.Difficulty.HARD)
                .withWeather(WeatherRestriction.RAIN)
                .withTreasure(U.rl("aquaculture", "treasure_chest"))
        );

        //mushroom island
        register(overworldMushroomFieldsFish(U.holderItem("aquaculture", "brown_shrooma"))
                .withBucketedFish(U.holderItem("aquaculture", "brown_shrooma_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "brown_shrooma"))
                .withSizeAndWeight(FishProperties.sizeWeight(100, 20, 3000, 500))
                .withRarity(FishProperties.Rarity.EPIC)
                .withDifficulty(FishProperties.Difficulty.FOUR_BIG)
                .withTreasure(U.rl("aquaculture", "treasure_chest"))
        );

        register(overworldMushroomFieldsFish(U.holderItem("aquaculture", "red_shrooma"))
                .withBucketedFish(U.holderItem("aquaculture", "brown_shrooma_bucket"))
                .withEntityToSpawn(U.holderEntity("aquaculture", "brown_shrooma"))
                .withSizeAndWeight(FishProperties.sizeWeight(100, 20, 3000, 500))
                .withRarity(FishProperties.Rarity.EPIC)
                .withDifficulty(FishProperties.Difficulty.FOUR_BIG)
                .withTreasure(U.rl("aquaculture", "treasure_chest"))
        );

        //anywhere
        register(overworldOceanFish(U.holderItem("aquaculture", "goldfish"))
                //no bucketed version
                //no entity
                .withSeasons(SeasonRestriction.SUMMER)
                .withSizeAndWeight(FishProperties.sizeWeight(15, 5, 100, 5))
                .withBaseChance(1)
                .withTreasure(U.rl("aquaculture", "box"))
        );

    }
}
