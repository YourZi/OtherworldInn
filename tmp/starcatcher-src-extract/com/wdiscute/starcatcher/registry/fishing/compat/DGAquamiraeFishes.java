package com.wdiscute.starcatcher.registry.fishing.compat;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.fishrestrictions.BiomeRestriction;
import com.wdiscute.starcatcher.registry.fishrestrictions.DimensionRestriction;
import com.wdiscute.starcatcher.registry.FishProperties;

import java.util.List;

public class DGAquamiraeFishes extends FishingPropertiesRegistry
{
    public static void bootstrap()
    {
        //
        //                                              ,--.
        // ,--,--.  ,---.  ,--.,--.  ,--,--. ,--,--,--. `--' ,--.--.  ,--,--.  ,---.
        //' ,-.  | | .-. | |  ||  | ' ,-.  | |        | ,--. |  .--' ' ,-.  | | .-. :
        //\ '-'  | ' '-' | '  ''  ' \ '-'  | |  |  |  | |  | |  |    \ '-'  | \   --.
        // `--`--'  `-|  |  `----'   `--`--' `--`--`--' `--' `--'     `--`--'  `----'
        //            `--'

        final BiomeRestriction ICE_MAZE = new BiomeRestriction(List.of(U.rl("aquamirae", "ice_maze")), List.of(), List.of(), List.of(), "");

        register(fish(U.holderItem("aquamirae", "spinefish"))
                .withBucketedFish(U.holderItem("aquamirae", "spinefish_bucket"))
                .withEntityToSpawn(U.holderEntity("aquamirae", "spinefish"))
                .withSizeAndWeight(FishProperties.sizeWeight(30, 10, 500, 300))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .withDifficulty(FishProperties.Difficulty.MEDIUM_VANISHING)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        ICE_MAZE
                )
        );
    }
}
