package com.wdiscute.starcatcher.registry.fishing.compat;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.registry.SCItems;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.fishrestrictions.BiomeRestriction;
import com.wdiscute.starcatcher.registry.fishrestrictions.DimensionRestriction;
import com.wdiscute.starcatcher.registry.fishrestrictions.FluidRestriction;
import com.wdiscute.starcatcher.registry.FishProperties;

import java.util.List;

public class DGAlexsCavesFishes extends FishingPropertiesRegistry
{
    public static void bootstrap()
    {

        //
        //  ,---.   ,--.                    ,--.              ,-----.
        // /  O  \  |  |  ,---.  ,--.  ,--. |  |  ,---.      '  .--./  ,--,--. ,--.  ,--.  ,---.   ,---.
        //|  .-.  | |  | | .-. :  \  `'  /  `-'  (  .-'      |  |     ' ,-.  |  \  `'  /  | .-. : (  .-'
        //|  | |  | |  | \   --.  /  /.  \       .-'  `)     '  '--'\ \ '-'  |   \    /   \   --. .-'  `)
        //`--' `--' `--'  `----' '--'  '--'      `----'       `-----'  `--`--'    `--'     `----' `----'
        //

        final BiomeRestriction TOXIC_CAVES = new BiomeRestriction(List.of(U.rl("alexscaves", "toxic_caves")), List.of(), List.of(), List.of(), "");
        final BiomeRestriction CANDY_CAVITY = new BiomeRestriction(List.of(U.rl("alexscaves", "candy_cavity")), List.of(), List.of(), List.of(), "");
        final BiomeRestriction ABYSSAL_CHASM = new BiomeRestriction(List.of(U.rl("alexscaves", "abyssal_chasm")), List.of(), List.of(), List.of(), "");
        final BiomeRestriction PRIMORDIAL_CAVES = new BiomeRestriction(List.of(U.rl("alexscaves", "primordial_caves")), List.of(), List.of(), List.of(), "");


        register(fish(U.holderItem("alexscaves", "radgill"))
                .withBucketedFish(U.holderItem("alexscaves", "radgill_bucket"))
                .withEntityToSpawn(U.holderEntity("alexscaves", "radgill"))
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        TOXIC_CAVES,
                        FluidRestriction.ACID)
        );

        register(fish(U.holderItem("alexscaves", "sweetish_fish_blue"))
                .withBucketedFish(U.holderItem("alexscaves", "sweetish_fish_blue_bucket"))
                .withEntityToSpawn(U.holderEntity("alexscaves", "sweetish_fish"))
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        CANDY_CAVITY,
                        FluidRestriction.PURPLE_SODA
                )
        );

        register(fish(U.holderItem("alexscaves", "sweetish_fish_green"))
                .withBucketedFish(U.holderItem("alexscaves", "sweetish_fish_blue_bucket"))
                .withEntityToSpawn(U.holderEntity("alexscaves", "sweetish_fish"))
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        CANDY_CAVITY,
                        FluidRestriction.PURPLE_SODA
                )
        );

        register(fish(U.holderItem("alexscaves", "sweetish_fish_pink"))
                .withBucketedFish(U.holderItem("alexscaves", "sweetish_fish_pink_bucket"))
                .withEntityToSpawn(U.holderEntity("alexscaves", "sweetish_fish"))
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 70000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        CANDY_CAVITY,
                        FluidRestriction.PURPLE_SODA
                )
        );

        register(fish(U.holderItem("alexscaves", "sweetish_fish_red"))
                .withBucketedFish(U.holderItem("alexscaves", "sweetish_fish_red_bucket"))
                .withEntityToSpawn(U.holderEntity("alexscaves", "sweetish_fish"))
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        CANDY_CAVITY,
                        FluidRestriction.PURPLE_SODA
                )
        );

        register(fish(U.holderItem("alexscaves", "sweetish_fish_yellow"))
                .withBucketedFish(U.holderItem("alexscaves", "sweetish_fish_yellow_bucket"))
                .withEntityToSpawn(U.holderEntity("alexscaves", "sweetish_fish"))
                .withSizeAndWeight(FishProperties.sizeWeight(80, 40, 12000, 7000))
                .withRarity(FishProperties.Rarity.UNCOMMON)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        CANDY_CAVITY,
                        FluidRestriction.PURPLE_SODA
                )
        );


        register(fish(U.holderItem("alexscaves", "lanternfish"))
                .withBucketedFish(U.holderItem("alexscaves", "lanternfish_bucket"))
                .withEntityToSpawn(U.holderEntity("alexscaves", "lanternfish"))
                .withSizeAndWeight(FishProperties.sizeWeight(100, 50, 15000, 10000))
                .withRarity(FishProperties.Rarity.RARE)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        ABYSSAL_CHASM
                )
        );

        register(fish(U.holderItem("alexscaves", "tripodfish"))
                .withBucketedFish(U.holderItem("alexscaves", "tripodfish_bucket"))
                .withEntityToSpawn(U.holderEntity("alexscaves", "tripodfish"))
                .withSizeAndWeight(FishProperties.sizeWeight(30, 10, 1000, 5000))
                .withRarity(FishProperties.Rarity.RARE)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        ABYSSAL_CHASM
                )
        );


        register(fish(U.holderItem("alexscaves", "trilocaris_tail"))
                .withBucketedFish(U.holderItem("alexscaves", "trilocaris_bucket"))
                .withEntityToSpawn(U.holderEntity("alexscaves", "trilocaris"))
                .withSizeAndWeight(FishProperties.sizeWeight(30, 10, 1000, 5000))
                .withRarity(FishProperties.Rarity.RARE)
                .addRestrictions(
                        DimensionRestriction.OVERWORLD,
                        PRIMORDIAL_CAVES
                )
                .withAlwaysSpawnEntity(true)
                .withEntityToSpawn(U.holderEntity("alexscaves", "trilocaris"))
                .withItemToOverrideWith(SCItems.UNKNOWN_FISH)
        );
    }
}
