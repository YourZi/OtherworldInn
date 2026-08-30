package com.otherworldinn.world.dimension;

import com.otherworldinn.OtherworldInn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

/** 维度定义 */
public class TownDimensions {
    public static final ResourceKey<LevelStem> TOWN_LEVEL_STEM =
            ResourceKey.create(
                    Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "town"));
    public static final ResourceKey<Level> TOWN_LEVEL =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "town"));
    public static final ResourceKey<DimensionType> TOWN_DIM_TYPE =
            ResourceKey.create(
                    Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "town_type"));

    public static final ResourceKey<LevelStem> MAGIC_SPACE_STEM =
            ResourceKey.create(
                    Registries.LEVEL_STEM,
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "magic_space"));
    public static final ResourceKey<Level> MAGIC_SPACE_LEVEL =
            ResourceKey.create(
                    Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "magic_space"));
    public static final ResourceKey<DimensionType> MAGIC_SPACE_DIM_TYPE =
            ResourceKey.create(
                    Registries.DIMENSION_TYPE,
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "magic_space_type"));

    public static void register() {}
}
