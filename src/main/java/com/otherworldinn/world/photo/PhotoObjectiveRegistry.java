package com.otherworldinn.world.photo;

import com.otherworldinn.OtherworldInn;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class PhotoObjectiveRegistry {
    public static final ResourceLocation PLAINS_SCENE_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "plains_scene");
    public static final ResourceLocation COW_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "cow_snapshot");
    public static final ResourceLocation BEE_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "bee_snapshot");
    public static final ResourceLocation ZOMBIE_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "zombie_snapshot");
    public static final ResourceLocation FARM_ANIMAL_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "farm_animal_snapshot");
    public static final ResourceLocation WOODLAND_MANSION_SCENE_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "woodland_mansion_scene");
    public static final ResourceLocation NETHER_FORTRESS_SCENE_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "nether_fortress_scene");
    public static final ResourceLocation ANCIENT_TEMPLE_SCENE_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "ancient_temple_scene");
    public static final ResourceLocation UNDERGROUND_ORE_SCENE_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "underground_ore_scene");
    public static final ResourceLocation SNOWY_PLAINS_SCENE_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "snowy_plains_scene");
    public static final ResourceLocation GHAST_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "ghast_snapshot");
    public static final ResourceLocation OCEAN_BIOME_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "ocean_biome_snapshot");
    public static final ResourceLocation SEAHORSE_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "seahorse_snapshot");
    public static final ResourceLocation HERRING_SCHOOL_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "herring_school");
    public static final ResourceLocation SEA_COW_MEADOW_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "sea_cow_meadow");
    public static final ResourceLocation DODO_ISLAND_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "dodo_island");
    public static final ResourceLocation ANGLER_FISH_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "angler_fish_snapshot");
    public static final ResourceLocation SNAIL_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "snail_snapshot");
    public static final ResourceLocation HAMSTER_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "hamster_snapshot");
    public static final ResourceLocation ANT_GARDEN_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "ant_garden");
    public static final ResourceLocation OCTOPUS_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "octopus_snapshot");
    public static final ResourceLocation STRANDED_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "stranded_snapshot");
    public static final ResourceLocation FIREKEEPER_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "firekeeper_snapshot");
    public static final ResourceLocation COASTAL_CRAB_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "coastal_crab_snapshot");
    public static final ResourceLocation TUNA_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "tuna_snapshot");
    public static final ResourceLocation SPIDER_CRAB_SNAPSHOT_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "spider_crab_snapshot");

    private static final Map<ResourceLocation, PhotoObjective> OBJECTIVES = new LinkedHashMap<>();

    static {
        register(
                new PhotoObjective(
                        PLAINS_SCENE_ID,
                        "photo_objective.otherworldinn.plains_scene",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "plains")),
                        1,
                        true));
        register(
                new PhotoObjective(
                        COW_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.cow_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "cow")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        BEE_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.bee_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "bee")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        ZOMBIE_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.zombie_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "zombie")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        FARM_ANIMAL_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.farm_animal_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(
                                ResourceLocation.fromNamespaceAndPath("minecraft", "cow"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "pig"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "sheep"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "chicken")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        WOODLAND_MANSION_SCENE_ID,
                        "photo_objective.otherworldinn.woodland_mansion_scene",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(),
                        List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "mansion")),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        NETHER_FORTRESS_SCENE_ID,
                        "photo_objective.otherworldinn.nether_fortress_scene",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(),
                        List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "fortress")),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        ANCIENT_TEMPLE_SCENE_ID,
                        "photo_objective.otherworldinn.ancient_temple_scene",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(),
                        List.of(
                                ResourceLocation.fromNamespaceAndPath("minecraft", "desert_pyramid"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "jungle_temple")),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        UNDERGROUND_ORE_SCENE_ID,
                        "photo_objective.otherworldinn.underground_ore_scene",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(
                                ResourceLocation.fromNamespaceAndPath("minecraft", "dripstone_caves"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "lush_caves"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "deep_dark")),
                        1,
                        true));
        register(
                new PhotoObjective(
                        SNOWY_PLAINS_SCENE_ID,
                        "photo_objective.otherworldinn.snowy_plains_scene",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "snowy_plains")),
                        1,
                        true));
        register(
                new PhotoObjective(
                        GHAST_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.ghast_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "ghast")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "the_nether"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        OCEAN_BIOME_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.ocean_biome_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(
                                ResourceLocation.fromNamespaceAndPath("minecraft", "ocean"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "deep_ocean"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "cold_ocean"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "deep_cold_ocean"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "lukewarm_ocean"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "deep_lukewarm_ocean"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "warm_ocean"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "frozen_ocean"),
                                ResourceLocation.fromNamespaceAndPath("minecraft", "deep_frozen_ocean")),
                        1,
                        true));

        // ── 生机遍布（spawn）联动目标：实体/群系 ID 均来自该模组，引用方委托用 requiresMod 门控 ──

        register(
                new PhotoObjective(
                        SEAHORSE_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.seahorse_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "seahorse")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        HERRING_SCHOOL_ID,
                        "photo_objective.otherworldinn.herring_school",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "herring")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        3,
                        true));
        register(
                new PhotoObjective(
                        SEA_COW_MEADOW_ID,
                        "photo_objective.otherworldinn.sea_cow_meadow",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "sea_cow")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "seagrass_meadow")),
                        1,
                        true));
        register(
                new PhotoObjective(
                        DODO_ISLAND_ID,
                        "photo_objective.otherworldinn.dodo_island",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "dodo")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "dodo_island")),
                        1,
                        true));
        register(
                new PhotoObjective(
                        ANGLER_FISH_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.angler_fish_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "angler_fish")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));

        register(
                new PhotoObjective(
                        SNAIL_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.snail_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "snail")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        HAMSTER_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.hamster_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "hamster")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        ANT_GARDEN_ID,
                        "photo_objective.otherworldinn.ant_garden",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "ant")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "ant_gardens")),
                        1,
                        true));
        register(
                new PhotoObjective(
                        OCTOPUS_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.octopus_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "octopus")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        STRANDED_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.stranded_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "stranded")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        FIREKEEPER_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.firekeeper_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "firekeeper")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "volcanic_island")),
                        1,
                        true));
        register(
                new PhotoObjective(
                        COASTAL_CRAB_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.coastal_crab_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "coastal_crab")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        TUNA_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.tuna_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "tuna")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
        register(
                new PhotoObjective(
                        SPIDER_CRAB_SNAPSHOT_ID,
                        "photo_objective.otherworldinn.spider_crab_snapshot",
                        PhotoObjectiveMatchMode.ALL,
                        List.of(ResourceLocation.fromNamespaceAndPath("spawn", "spider_crab")),
                        List.of(),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),
                        List.of(),
                        1,
                        true));
    }

    private PhotoObjectiveRegistry() {}

    private static void register(PhotoObjective objective) {
        OBJECTIVES.put(objective.id(), objective);
    }

    @Nullable
    public static PhotoObjective get(ResourceLocation id) {
        return OBJECTIVES.get(id);
    }

    public static List<PhotoObjective> all() {
        return List.copyOf(OBJECTIVES.values());
    }

    public static Component getDisplayName(ResourceLocation id) {
        PhotoObjective objective = get(id);
        if (objective == null || objective.translationKey().isBlank()) {
            return Component.literal(id.toString());
        }
        return Component.translatable(objective.translationKey());
    }
}
