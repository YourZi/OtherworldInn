package com.wdiscute.starcatcher.registry.fishing.compat;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.SCItems;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.fishrestrictions.BiomeRestriction;
import com.wdiscute.starcatcher.registry.fishrestrictions.DimensionRestriction;
import com.wdiscute.starcatcher.registry.fishrestrictions.SeasonRestriction;
import com.wdiscute.starcatcher.registry.FishProperties;

import java.util.List;

public class DGCollectorsReapFishes extends FishingPropertiesRegistry
{
    public static void bootstrap()
    {
        //
        // ,-----.         ,--. ,--.                  ,--.                   ,--.             ,------.
        //'  .--./  ,---.  |  | |  |  ,---.   ,---. ,-'  '-.  ,---.  ,--.--. |  |  ,---.      |  .--. '  ,---.   ,--,--.  ,---.
        //|  |     | .-. | |  | |  | | .-. : | .--' '-.  .-' | .-. | |  .--' `-'  (  .-'      |  '--'.' | .-. : ' ,-.  | | .-. |
        //'  '--'\ ' '-' ' |  | |  | \   --. \ `--.   |  |   ' '-' ' |  |         .-'  `)     |  |\  \  \   --. \ '-'  | | '-' '
        // `-----'  `---'  `--' `--'  `----'  `---'   `--'    `---'  `--'         `----'      `--' '--'  `----'  `--`--' |  |-'
        //                                                                                                               `--'

        register(fish(U.holderItem("collectorsreap", "platinum_bass"))
                .withBucketedFish(U.holderItem("collectorsreap", "platinum_bass_bucket"))
                .withEntityToSpawn(U.holderEntity("collectorsreap", "platinum_bass"))
                .withSizeAndWeight(FishProperties.sizeWeight(40, 12, 1600, 1100))
                .withSeasons(SeasonRestriction.AROUND_WINTER)
                .withDifficulty(FishProperties.Difficulty.EASY_MOVING)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        new BiomeRestriction(List.of(), List.of(U.rl("collectorsreap", "biome/has_spawn/platinum_bass")),
                                List.of(), List.of(), "")
                )
        );


        register(fish(U.holderItem("collectorsreap", "tiger_prawn"))
                .withBucketedFish(U.holderItem("collectorsreap", "tiger_prawn_bucket"))
                .withEntityToSpawn(U.holderEntity("collectorsreap", "tiger_prawn"))
                .withSizeAndWeight(FishProperties.sizeWeight(28, 8, 260, 60))
                .withDifficulty(FishProperties.Difficulty.MEDIUM_VANISHING)
                .withRarity(FishProperties.Rarity.RARE)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        new BiomeRestriction(List.of(), List.of(U.rl("collectorsreap", "biome/has_spawn/tiger_prawn")),
                                List.of(), List.of(), "")
                )
        );

        register(fish(U.holderItem("collectorsreap", "clam"))  //no mini game
                .withBucketedFish(U.holderItem("collectorsreap", "clam_bucket"))
                .withEntityToSpawn(U.holderEntity("collectorsreap", "clam"))
                .withSkipMinigame(true)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        new BiomeRestriction(List.of(), List.of(U.rl("collectorsreap", "biome/has_spawn/clam")),
                                List.of(), List.of(), "")
                )
        );

        register(fish(U.holderItem("collectorsreap", "urchin"))  //no mini game
                .withBucketedFish(U.holderItem("collectorsreap", "urchin_bucket"))
                .withEntityToSpawn(U.holderEntity("collectorsreap", "urchin"))
                .withHasGuideEntry(false)
                .withSkipMinigame(true)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        new BiomeRestriction(List.of(), List.of(U.rl("collectorsreap", "biome/has_spawn/urchin")),
                                List.of(), List.of(), "")
                )
        );


        register(fish(U.holderItem("collectorsreap", "chieftain_crab"))
                .withBucketedFish(U.holderItem("collectorsreap", "chieftain_crab_bucket"))
                .withEntityToSpawn(U.holderEntity("collectorsreap", "chieftain_crab"))
                .withSizeAndWeight(FishProperties.sizeWeight(28, 8, 260, 60))
                .withDifficulty(FishProperties.Difficulty.NETHER_CRAB)
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        new BiomeRestriction(List.of(), List.of(U.rl("collectorsreap", "biome/has_spawn/chieftain_crab")),
                                List.of(), List.of(), "")
                )
                .withAlwaysSpawnEntity(true)
                .withItemToOverrideWith(SCItems.UNKNOWN_FISH)
        );
    }
}
