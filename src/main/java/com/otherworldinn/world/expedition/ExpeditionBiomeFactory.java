package com.otherworldinn.world.expedition;

import com.mojang.datafixers.util.Pair;
import com.otherworldinn.OtherworldInn;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class ExpeditionBiomeFactory {

    private static final Map<String, List<ResourceKey<Biome>>> BIOME_COMPONENT_MAP =
            new LinkedHashMap<>();

    static {
        BIOME_COMPONENT_MAP.put("plains_biome",
                List.of(b("plains"), b("meadow"), b("sunflower_plains")));
        BIOME_COMPONENT_MAP.put("forests_biome",
                List.of(b("forest"), b("flower_forest"), b("cherry_grove"),
                        b("birch_forest"), b("old_growth_birch_forest")));
        BIOME_COMPONENT_MAP.put("taigas_biome",
                List.of(b("taiga"), b("old_growth_pine_taiga"),
                        b("old_growth_spruce_taiga")));
        BIOME_COMPONENT_MAP.put("savannas_biome",
                List.of(b("savanna"), b("savanna_plateau"), b("windswept_savanna")));
        BIOME_COMPONENT_MAP.put("desert_biome",
                List.of(b("desert"), b("badlands"), b("eroded_badlands"),
                        b("wooded_badlands")));
        BIOME_COMPONENT_MAP.put("snowy_biome",
                List.of(b("snowy_plains"), b("ice_spikes"), b("frozen_peaks"),
                        b("snowy_slopes"), b("snowy_taiga"), b("grove")));
        BIOME_COMPONENT_MAP.put("jungle_biome",
                List.of(b("jungle"), b("sparse_jungle"), b("bamboo_jungle")));
        BIOME_COMPONENT_MAP.put("swamp_biome",
                List.of(b("swamp"), b("mangrove_swamp")));
        BIOME_COMPONENT_MAP.put("ocean_biome",
                List.of(b("ocean"), b("warm_ocean"), b("lukewarm_ocean"),
                        b("cold_ocean"), b("frozen_ocean"), b("deep_ocean"),
                        b("deep_warm_ocean"), b("deep_lukewarm_ocean"),
                        b("deep_cold_ocean"), b("deep_frozen_ocean")));
        BIOME_COMPONENT_MAP.put("mountain_biome",
                List.of(b("jagged_peaks"), b("frozen_peaks"), b("stony_peaks"),
                        b("meadow"), b("snowy_slopes")));
        BIOME_COMPONENT_MAP.put("mushroom_biome",
                List.of(b("mushroom_fields")));
        BIOME_COMPONENT_MAP.put("dark_forest_biome",
                List.of(b("dark_forest")));
        BIOME_COMPONENT_MAP.put("sculk_biome",
                List.of(b("deep_dark")));

        BIOME_COMPONENT_MAP.put("nether_wastes_biome",
                List.of(b("nether_wastes")));
        BIOME_COMPONENT_MAP.put("crimson_biome",
                List.of(b("crimson_forest")));
        BIOME_COMPONENT_MAP.put("warped_biome",
                List.of(b("warped_forest")));
        BIOME_COMPONENT_MAP.put("basalt_biome",
                List.of(b("basalt_deltas")));
        BIOME_COMPONENT_MAP.put("soul_valley_biome",
                List.of(b("soul_sand_valley")));
    }

    private static ResourceKey<Biome> b(String name) {
        return ResourceKey.create(Registries.BIOME,
                ResourceLocation.withDefaultNamespace(name));
    }

    private ExpeditionBiomeFactory() {}

    public static Set<ResourceKey<Biome>> collectTerrainBiomes(List<String> componentIds) {
        Set<ResourceKey<Biome>> result = new LinkedHashSet<>();
        for (String id : componentIds) {
            ChartComponentType ct = ChartComponentType.byId(id);
            if (ct == null || ct.componentCategory() != ChartComponentType.ComponentCategory.BIOME)
                continue;
            List<ResourceKey<Biome>> keys = BIOME_COMPONENT_MAP.get(id);
            if (keys != null) result.addAll(keys);
        }
        return result;
    }

    public static boolean hasStructureBoost(List<String> componentIds) {
        return componentIds.contains("thriving_realm");
    }

    public static BiomeSource createBiomeSource(MinecraftServer server,
            List<String> componentIds, ChartComponentType.DimensionCategory category) {
        HolderGetter<Biome> biomeRegistry =
                server.registryAccess().lookupOrThrow(Registries.BIOME);

        Set<ResourceKey<Biome>> terrainKeys = collectTerrainBiomes(componentIds);

        if (terrainKeys.isEmpty()) {
            return getDefaultBiomeSource(server, category);
        }

        if (terrainKeys.size() == 1) {
            ResourceKey<Biome> key = terrainKeys.iterator().next();
            if (key.location().getPath().equals("deep_dark")) {
                return getDefaultBiomeSource(server, category);
            }
            Optional<Holder.Reference<Biome>> holder = biomeRegistry.get(key);
            if (holder.isPresent()) {
                return new FixedBiomeSource(holder.get());
            }
            return getDefaultBiomeSource(server, category);
        }

        MultiNoiseBiomeSource refSource = getReferenceMultiNoiseSource(server, category);
        if (refSource == null) {
            return getDefaultBiomeSource(server, category);
        }

        Climate.ParameterList<Holder<Biome>> fullList = getParameters(refSource);
        if (fullList == null) {
            return getDefaultBiomeSource(server, category);
        }

        List<Holder<Biome>> targetHolders = new ArrayList<>();
        for (ResourceKey<Biome> key : terrainKeys) {
            biomeRegistry.get(key).ifPresent(targetHolders::add);
        }
        if (targetHolders.isEmpty()) {
            return getDefaultBiomeSource(server, category);
        }

        List<Pair<Climate.ParameterPoint, Holder<Biome>>> replaced = new ArrayList<>();
        int idx = 0;
        for (var entry : fullList.values()) {
            Holder<Biome> target = targetHolders.get(idx % targetHolders.size());
            replaced.add(Pair.of(entry.getFirst(), target));
            idx++;
        }

        Climate.ParameterList<Holder<Biome>> paramList = new Climate.ParameterList<>(replaced);
        return MultiNoiseBiomeSource.createFromList(paramList);
    }

    public static NoiseBasedChunkGenerator createNoiseGenerator(
            MinecraftServer server,
            List<String> componentIds,
            ChartComponentType.DimensionCategory category,
            BlockState stoneReplacement) {

        BiomeSource biomeSource = createBiomeSource(server, componentIds, category);

        Holder<NoiseGeneratorSettings> settingsHolder = getNoiseSettings(
                server, componentIds, category);
        if (settingsHolder == null) {
            OtherworldInn.LOGGER.error("Failed to get noise settings");
            return null;
        }

        NoiseGeneratorSettings settings = settingsHolder.value();

        BlockState defaultBlock = settings.defaultBlock();
        if (stoneReplacement != null && stoneReplacement.getBlock() != Blocks.STONE) {
            defaultBlock = stoneReplacement;
        }
        if (componentIds.contains("dry_land")) {
            settings = new NoiseGeneratorSettings(
                    settings.noiseSettings(), defaultBlock,
                    settings.defaultFluid(), settings.noiseRouter(),
                    settings.surfaceRule(), settings.spawnTarget(),
                    -64,
                    settings.disableMobGeneration(),
                    false,
                    settings.oreVeinsEnabled(),
                    settings.useLegacyRandomSource());
        } else if (componentIds.contains("water_world")) {
            settings = new NoiseGeneratorSettings(
                    settings.noiseSettings(), defaultBlock,
                    settings.defaultFluid(), settings.noiseRouter(),
                    settings.surfaceRule(), settings.spawnTarget(),
                    127,
                    settings.disableMobGeneration(),
                    settings.aquifersEnabled(),
                    settings.oreVeinsEnabled(),
                    settings.useLegacyRandomSource());
        } else if (stoneReplacement != null && stoneReplacement.getBlock() != Blocks.STONE) {
            settings = new NoiseGeneratorSettings(
                    settings.noiseSettings(), defaultBlock,
                    settings.defaultFluid(), settings.noiseRouter(),
                    settings.surfaceRule(), settings.spawnTarget(),
                    settings.seaLevel(),
                    settings.disableMobGeneration(),
                    settings.aquifersEnabled(),
                    settings.oreVeinsEnabled(),
                    settings.useLegacyRandomSource());
        }

        return new NoiseBasedChunkGenerator(biomeSource,
                net.minecraft.core.Holder.direct(settings));
    }

    private static Holder<NoiseGeneratorSettings> getNoiseSettings(
            MinecraftServer server,
            List<String> componentIds,
            ChartComponentType.DimensionCategory category) {

        ChartComponentType worldType = ChartComponentType.resolveWorldType(componentIds);

        ResourceKey<NoiseGeneratorSettings> presetKey = switch (worldType.id()) {
            case "amplified_world" -> NoiseGeneratorSettings.AMPLIFIED;
            case "floating_islands" -> NoiseGeneratorSettings.FLOATING_ISLANDS;
            case "cave_world" -> NoiseGeneratorSettings.CAVES;
            default -> null;
        };

        if (presetKey != null) {
            Optional<Holder.Reference<NoiseGeneratorSettings>> preset =
                    server.registryAccess()
                            .lookupOrThrow(Registries.NOISE_SETTINGS)
                            .get(presetKey);
            if (preset.isPresent()) {
                return preset.get();
            }
            OtherworldInn.LOGGER.warn(
                    "World type preset not found: {}, falling back to default",
                    presetKey.location());
        }

        NoiseBasedChunkGenerator referenceGen = null;
        if (category == ChartComponentType.DimensionCategory.NETHER) {
            var netherLevel = server.getLevel(Level.NETHER);
            if (netherLevel != null
                    && netherLevel.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator ng) {
                referenceGen = ng;
            }
        } else {
            if (server.overworld().getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator ng) {
                referenceGen = ng;
            }
        }

        if (referenceGen != null) {
            return referenceGen.generatorSettings();
        }

        OtherworldInn.LOGGER.error("Could not find NoiseGeneratorSettings for category: {}", category);
        return null;
    }

    private static MultiNoiseBiomeSource getReferenceMultiNoiseSource(
            MinecraftServer server, ChartComponentType.DimensionCategory category) {
        ResourceKey<Level> dimKey = switch (category) {
            case NETHER -> Level.NETHER;
            default -> Level.OVERWORLD;
        };
        ServerLevel level = server.getLevel(dimKey);
        if (level == null) return null;
        ChunkGenerator gen = level.getChunkSource().getGenerator();
        if (gen.getBiomeSource() instanceof MultiNoiseBiomeSource mns) {
            return mns;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Climate.ParameterList<Holder<Biome>> getParameters(
            MultiNoiseBiomeSource source) {
        try {
            Method m = MultiNoiseBiomeSource.class.getDeclaredMethod("parameters");
            m.setAccessible(true);
            return (Climate.ParameterList<Holder<Biome>>) m.invoke(source);
        } catch (Exception e) {
            OtherworldInn.LOGGER.error("Failed to reflect parameters()", e);
            return null;
        }
    }

    private static BiomeSource getDefaultBiomeSource(
            MinecraftServer server,
            ChartComponentType.DimensionCategory category) {

        if (category == ChartComponentType.DimensionCategory.NETHER) {
            var netherLevel = server.getLevel(Level.NETHER);
            if (netherLevel != null) {
                return netherLevel.getChunkSource().getGenerator().getBiomeSource();
            }
        }
        return server.overworld().getChunkSource().getGenerator().getBiomeSource();
    }

    public static List<String> buildInfoLog(List<String> componentIds,
            ChartComponentType.DimensionCategory category) {
        List<String> info = new ArrayList<>();
        info.add("category=" + category);
        Set<ResourceKey<Biome>> terrains = collectTerrainBiomes(componentIds);
        info.add("terrains=" + terrains.size());
        info.add("worldType=" + ChartComponentType.resolveWorldType(componentIds).displayName(true));
        return info;
    }
}
