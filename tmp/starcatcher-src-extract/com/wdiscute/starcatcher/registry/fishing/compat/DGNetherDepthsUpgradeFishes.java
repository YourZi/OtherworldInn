package com.wdiscute.starcatcher.registry.fishing.compat;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.fishrestrictions.BaitRestriction;
import com.wdiscute.starcatcher.registry.FishProperties;

public class DGNetherDepthsUpgradeFishes extends FishingPropertiesRegistry
{
    public static void bootstrap()
    {

        //
        //,--.  ,--.           ,--.   ,--.                          ,------.                     ,--.   ,--.
        //|  ,'.|  |  ,---.  ,-'  '-. |  ,---.   ,---.  ,--.--.     |  .-.  \   ,---.   ,---.  ,-'  '-. |  ,---.   ,---.
        //|  |' '  | | .-. : '-.  .-' |  .-.  | | .-. : |  .--'     |  |  \  : | .-. : | .-. | '-.  .-' |  .-.  | (  .-'
        //|  | `   | \   --.   |  |   |  | |  | \   --. |  |        |  '--'  / \   --. | '-' '   |  |   |  | |  | .-'  `)
        //`--'  `--'  `----'   `--'   `--' `--'  `----' `--'        `-------'   `----' |  |-'    `--'   `--' `--' `----'
        //                                                                             `--'
        //
        //,--. ,--.                                     ,--.
        //|  | |  |  ,---.   ,---.  ,--.--.  ,--,--.  ,-|  |  ,---.
        //|  | |  | | .-. | | .-. | |  .--' ' ,-.  | ' .-. | | .-. :
        //'  '-'  ' | '-' ' ' '-' ' |  |    \ '-'  | \ `-' | \   --.
        // `-----'  |  |-'  .`-  /  `--'     `--`--'  `---'   `----'
        //          `--'    `---'


        register(netherLavaFish(U.holderItem("netherdepthsupgrade", "bonefish"))
                .withBucketedFish(U.holderItem("netherdepthsupgrade", "bonefish_bucket"))
                .withEntityToSpawn(U.holderEntity("netherdepthsupgrade", "bonefish"))
                .withSizeAndWeight(FishProperties.sizeWeight(120, 40, 700, 200))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM_MOVING)
        );

        //TODO ADD STRUCTURE RESTRICTION
        register(netherLavaFish(U.holderItem("netherdepthsupgrade", "blazefish"))
                .withBucketedFish(U.holderItem("netherdepthsupgrade", "blazefish_bucket"))
                .withEntityToSpawn(U.holderEntity("netherdepthsupgrade", "blazefish"))
                .withSizeAndWeight(FishProperties.sizeWeight(160, 29, 5200, 1200))
                .withRarity(FishProperties.Rarity.LEGENDARY)
                .addRestrictions(BaitRestriction.LEGENDARY_BAIT)
                .withDifficulty(FishProperties.Difficulty.HARD_VANISHING)
        );

        register(netherLavaCrimsonForestFish(U.holderItem("netherdepthsupgrade", "eyeball_fish"))
                .withBucketedFish(U.holderItem("netherdepthsupgrade", "eyeball_fish_bucket"))
                .withEntityToSpawn(U.holderEntity("netherdepthsupgrade", "eyeball_fish"))
                .withSizeAndWeight(FishProperties.sizeWeight(70, 40, 700, 200))
                .withRarity(FishProperties.Rarity.RARE)
                .withDifficulty(FishProperties.Difficulty.HARD_MOVING)
        );

        register(netherLavaFish(U.holderItem("netherdepthsupgrade", "glowdine"))
                .withBucketedFish(U.holderItem("netherdepthsupgrade", "glowdine_bucket"))
                .withEntityToSpawn(U.holderEntity("netherdepthsupgrade", "glowdine"))
                .withSizeAndWeight(FishProperties.sizeWeight(130, 30, 3400, 900))
                .withRarity(FishProperties.Rarity.RARE)
                .withDifficulty(FishProperties.Difficulty.MEDIUM_VANISHING_MOVING)
        );

        register(netherLavaWarpedForestFish(U.holderItem("netherdepthsupgrade", "lava_pufferfish"))
                .withBucketedFish(U.holderItem("netherdepthsupgrade", "lava_pufferfish_bucket"))
                .withEntityToSpawn(U.holderEntity("netherdepthsupgrade", "lava_pufferfish"))
                .withSizeAndWeight(FishProperties.sizeWeight(90, 30, 3700, 900))
                .withRarity(FishProperties.Rarity.EPIC)
                .withDifficulty(FishProperties.Difficulty.HARD_MOVING)
        );

        register(netherLavaBasaltDeltasFish(U.holderItem("netherdepthsupgrade", "magmacubefish"))
                .withBucketedFish(U.holderItem("netherdepthsupgrade", "magmacubefish_bucket"))
                .withEntityToSpawn(U.holderEntity("netherdepthsupgrade", "magmacubefish"))
                .withSizeAndWeight(FishProperties.sizeWeight(120, 40, 3000, 400))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.EASY_MOVING)
        );

        register(netherLavaBasaltDeltasFish(U.holderItem("netherdepthsupgrade", "obsidianfish"))
                .withBucketedFish(U.holderItem("netherdepthsupgrade", "obsidianfish_bucket"))
                .withEntityToSpawn(U.holderEntity("netherdepthsupgrade", "obsidianfish"))
                .withSizeAndWeight(FishProperties.sizeWeight(200, 50, 500000, 68000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.FOUR_STONE_SPOTS)
        );

        register(netherLavaFish(U.holderItem("netherdepthsupgrade", "searing_cod"))
                .withBucketedFish(U.holderItem("netherdepthsupgrade", "searing_cod_bucket"))
                .withEntityToSpawn(U.holderEntity("netherdepthsupgrade", "searing_cod"))
                .withSizeAndWeight(FishProperties.sizeWeight(500, 50, 80000, 20000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.EASY_FAST_FISH)
        );

        register(netherLavaSoulSandValleyFish(U.holderItem("netherdepthsupgrade", "soulsucker"))
                .withBucketedFish(U.holderItem("netherdepthsupgrade", "soulsucker_bucket"))
                .withEntityToSpawn(U.holderEntity("netherdepthsupgrade", "soulsucker"))
                .withSizeAndWeight(FishProperties.sizeWeight(140, 30, 12000, 3000))
                .withRarity(FishProperties.Rarity.EPIC)
                .withDifficulty(FishProperties.Difficulty.MEDIUM_VANISHING)
        );

        register(netherLavaSoulSandValleyFish(U.holderItem("netherdepthsupgrade", "wither_bonefish"))
                .withBucketedFish(U.holderItem("netherdepthsupgrade", "wither_bonefish_bucket"))
                .withEntityToSpawn(U.holderEntity("netherdepthsupgrade", "wither_bonefish"))
                .withSizeAndWeight(FishProperties.sizeWeight(400, 100, 32000, 7000))
                .withRarity(FishProperties.Rarity.EPIC)
                .withDifficulty(FishProperties.Difficulty.HARD_MOVING)
        );
    }
}
