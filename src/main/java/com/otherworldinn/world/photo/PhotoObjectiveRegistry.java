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
