package com.wdiscute.starcatcher.registry.fishing.compat;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.FishProperties;

public class DGMinersDelightFishes extends FishingPropertiesRegistry
{
    public static void bootstrap()
    {
        //
        //,--.   ,--. ,--.                          ,--.             ,------.           ,--. ,--.         ,--.        ,--.
        //|   `.'   | `--' ,--,--,   ,---.  ,--.--. |  |  ,---.      |  .-.  \   ,---.  |  | `--'  ,---.  |  ,---.  ,-'  '-.
        //|  |'.'|  | ,--. |      \ | .-. : |  .--' `-'  (  .-'      |  |  \  : | .-. : |  | ,--. | .-. | |  .-.  | '-.  .-'
        //|  |   |  | |  | |  ||  | \   --. |  |         .-'  `)     |  '--'  / \   --. |  | |  | ' '-' ' |  | |  |   |  |
        //`--'   `--' `--' `--''--'  `----' `--'         `----'      `-------'   `----' `--' `--' .`-  /  `--' `--'   `--'
        //                                                                                        `---'

        register(overworldSurfaceFish(U.holderItem("miners_delight", "squid"))
                .withEntityToSpawn(U.holderEntity("minecraft", "squid"))
                .withSizeAndWeight(FishProperties.sizeWeight(40, 20, 1300, 700))
                .withDifficulty(FishProperties.Difficulty.SINGLE_BIG_FAST_MOVING)
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .addRestrictions(FishProperties.WorldRestrictions.OVERWORLD_OCEAN)
        );

        register(overworldUndergroundFish(U.holderItem("miners_delight", "glow_squid"))
                .withEntityToSpawn(U.holderEntity("minecraft", "glow_squid"))
                .withSizeAndWeight(FishProperties.sizeWeight(40, 20, 1300, 700))
                .withDifficulty(FishProperties.Difficulty.SINGLE_BIG_FAST_MOVING)
                .withRarity(FishProperties.Rarity.COMMON)
        );
    }
}
