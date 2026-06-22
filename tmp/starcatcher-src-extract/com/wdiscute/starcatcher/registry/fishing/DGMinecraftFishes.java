package com.wdiscute.starcatcher.registry.fishing;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.SCItems;
import com.wdiscute.starcatcher.registry.fishrestrictions.*;
import com.wdiscute.starcatcher.registry.FishProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

import java.util.Map;

import static com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry.*;


public class DGMinecraftFishes
{
    public static void bootstrap()
    {

        //ocean
        register(overworldOceanFish(U.holderItem("minecraft", "cod"))
                .withBucketedFish(U.holderItem("minecraft", "cod_bucket"))
                .withEntityToSpawn(U.holderEntity("minecraft", "cod"))
                .withDifficulty(FishProperties.Difficulty.EASY_MOVING)
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
        );

        register(overworldOceanFish(U.holderItem("minecraft", "pufferfish"))
                .withBucketedFish(U.holderItem("minecraft", "pufferfish_bucket"))
                .withEntityToSpawn(U.holderEntity("minecraft", "pufferfish"))
                .withSizeAndWeight(FishProperties.sizeWeight(70, 20, 10000, 3000))
                .withDifficulty(FishProperties.Difficulty.MEDIUM_VANISHING)
                .withRarity(FishProperties.Rarity.UNCOMMON)
        );


        //warm ocean
        register(overworldWarmOceanFish(U.holderItem("minecraft", "tropical_fish"))
                .withBucketedFish(U.holderItem("minecraft", "tropical_fish_bucket"))
                .withEntityToSpawn(U.holderEntity("minecraft", "tropical_fish"))
                .withSizeAndWeight(FishProperties.sizeWeight(70, 20, 10000, 3000))
                .withDifficulty(FishProperties.Difficulty.MEDIUM_VANISHING)
                .withRarity(FishProperties.Rarity.RARE)
                .withDifficulty(FishProperties.Difficulty.FOUR_AQUA)
        );

        //river
        register(overworldRiverFish(U.holderItem("minecraft", "salmon"))
                .withBucketedFish(U.holderItem("minecraft", "salmon_bucket"))
                .withEntityToSpawn(U.holderEntity("minecraft", "salmon"))
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 10000, 8000))
        );

        //mobs
        register(fish(BuiltInRegistries.ITEM.wrapAsHolder(Items.NETHER_STAR))
                .withAlwaysSpawnEntity(true)
                .withEntityToSpawn(U.holderEntity("minecraft", "wither"))
                .withBaseChance(0)
                .addRestrictions(new BaitRestriction(Map.of(U.rl("wither_skeleton_skull"), 200), ""))
                .withDifficulty(FishProperties.Difficulty.WITHER)
                .withItemToOverrideWith(SCItems.UNKNOWN_FISH)
                .withRarity(FishProperties.Rarity.LEGENDARY)
        );

        register(fish(BuiltInRegistries.ITEM.wrapAsHolder(Items.CREEPER_HEAD))
                .withAlwaysSpawnEntity(true)
                .withEntityToSpawn(U.holderEntity("minecraft", "creeper"))
                .withBaseChance(0)
                .addRestrictions(DimensionRestriction.OVERWORLD)
                .addRestrictions(new BaitRestriction(Map.of(Starcatcher.rl("gunpowder_bait"), 200), ""))
                .withDifficulty(FishProperties.Difficulty.CREEPER)
                .withItemToOverrideWith(SCItems.UNKNOWN_FISH)
                .withRarity(FishProperties.Rarity.EPIC)
        );

        register(overworldSurfaceFish(U.holderItem("minecraft", "rotten_flesh"))
                .withEntityToSpawn(U.holderEntity("minecraft", "drowned"))
                .withBaseChance(1)
                .withDaytimeRestriction(DaytimeRestriction.NIGHT)
                .withWeather(WeatherRestriction.RAIN)
                .withHasGuideEntry(false)
                .withAlwaysSpawnEntity()
                .withSkipMinigame(true));
    }
}
