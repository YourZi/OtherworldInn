package com.otherworldinn.world.photo;

import java.util.List;

public final class StoryGuestPhotoTaskRegistry {
    public static final String CARTOGRAPHER_OCEAN_PHOTO_REQUESTED_FLAG =
            "cartographer_ocean_photo_requested";
    public static final String CARTOGRAPHER_OCEAN_PHOTO_COMPLETED_FLAG =
            "cartographer_ocean_photo_completed";
    public static final String MINSTREL_PLAINS_PHOTO_REQUESTED_FLAG =
            "minstrel_plains_photo_requested";
    public static final String MINSTREL_PLAINS_PHOTO_COMPLETED_FLAG =
            "minstrel_plains_photo_completed";
    public static final String CHEF_FARM_PHOTO_REQUESTED_FLAG =
            "chef_farm_photo_requested";
    public static final String CHEF_FARM_PHOTO_COMPLETED_FLAG =
            "chef_farm_photo_completed";
    public static final String NOBLE_MANSION_PHOTO_REQUESTED_FLAG =
            "noble_mansion_photo_requested";
    public static final String NOBLE_MANSION_PHOTO_COMPLETED_FLAG =
            "noble_mansion_photo_completed";
    public static final String ALCHEMIST_FORTRESS_PHOTO_REQUESTED_FLAG =
            "alchemist_fortress_photo_requested";
    public static final String ALCHEMIST_FORTRESS_PHOTO_COMPLETED_FLAG =
            "alchemist_fortress_photo_completed";
    public static final String ARCHAEOLOGIST_TEMPLE_PHOTO_REQUESTED_FLAG =
            "archaeologist_temple_photo_requested";
    public static final String ARCHAEOLOGIST_TEMPLE_PHOTO_COMPLETED_FLAG =
            "archaeologist_temple_photo_completed";
    public static final String GEM_MERCHANT_ORE_PHOTO_REQUESTED_FLAG =
            "gem_merchant_ore_photo_requested";
    public static final String GEM_MERCHANT_ORE_PHOTO_COMPLETED_FLAG =
            "gem_merchant_ore_photo_completed";

    private static final List<StoryGuestPhotoTask> TASKS =
            List.of(
                    new StoryGuestPhotoTask(
                            "wandering_cartographer",
                            PhotoObjectiveRegistry.OCEAN_BIOME_SNAPSHOT_ID,
                            2,
                            CARTOGRAPHER_OCEAN_PHOTO_REQUESTED_FLAG,
                            CARTOGRAPHER_OCEAN_PHOTO_COMPLETED_FLAG),
                    new StoryGuestPhotoTask(
                            "wandering_minstrel",
                            PhotoObjectiveRegistry.PLAINS_SCENE_ID,
                            2,
                            MINSTREL_PLAINS_PHOTO_REQUESTED_FLAG,
                            MINSTREL_PLAINS_PHOTO_COMPLETED_FLAG),
                    new StoryGuestPhotoTask(
                            "wandering_chef",
                            PhotoObjectiveRegistry.FARM_ANIMAL_SNAPSHOT_ID,
                            2,
                            CHEF_FARM_PHOTO_REQUESTED_FLAG,
                            CHEF_FARM_PHOTO_COMPLETED_FLAG),
                    new StoryGuestPhotoTask(
                            "fallen_noble",
                            PhotoObjectiveRegistry.WOODLAND_MANSION_SCENE_ID,
                            2,
                            NOBLE_MANSION_PHOTO_REQUESTED_FLAG,
                            NOBLE_MANSION_PHOTO_COMPLETED_FLAG),
                    new StoryGuestPhotoTask(
                            "wandering_alchemist",
                            PhotoObjectiveRegistry.NETHER_FORTRESS_SCENE_ID,
                            2,
                            ALCHEMIST_FORTRESS_PHOTO_REQUESTED_FLAG,
                            ALCHEMIST_FORTRESS_PHOTO_COMPLETED_FLAG),
                    new StoryGuestPhotoTask(
                            "archaeologist",
                            PhotoObjectiveRegistry.ANCIENT_TEMPLE_SCENE_ID,
                            2,
                            ARCHAEOLOGIST_TEMPLE_PHOTO_REQUESTED_FLAG,
                            ARCHAEOLOGIST_TEMPLE_PHOTO_COMPLETED_FLAG),
                    new StoryGuestPhotoTask(
                            "gem_merchant",
                            PhotoObjectiveRegistry.UNDERGROUND_ORE_SCENE_ID,
                            2,
                            GEM_MERCHANT_ORE_PHOTO_REQUESTED_FLAG,
                            GEM_MERCHANT_ORE_PHOTO_COMPLETED_FLAG));

    private StoryGuestPhotoTaskRegistry() {}

    public static List<StoryGuestPhotoTask> all() {
        return TASKS;
    }
}
