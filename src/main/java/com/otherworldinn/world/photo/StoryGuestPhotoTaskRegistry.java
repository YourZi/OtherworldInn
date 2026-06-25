package com.otherworldinn.world.photo;

import java.util.List;

public final class StoryGuestPhotoTaskRegistry {
    public static final String CARTOGRAPHER_OCEAN_PHOTO_REQUESTED_FLAG =
            "cartographer_ocean_photo_requested";
    public static final String CARTOGRAPHER_OCEAN_PHOTO_COMPLETED_FLAG =
            "cartographer_ocean_photo_completed";

    private static final List<StoryGuestPhotoTask> TASKS =
            List.of(
                    new StoryGuestPhotoTask(
                            "wandering_cartographer",
                            PhotoObjectiveRegistry.OCEAN_BIOME_SNAPSHOT_ID,
                            2,
                            CARTOGRAPHER_OCEAN_PHOTO_REQUESTED_FLAG,
                            CARTOGRAPHER_OCEAN_PHOTO_COMPLETED_FLAG));

    private StoryGuestPhotoTaskRegistry() {}

    public static List<StoryGuestPhotoTask> all() {
        return TASKS;
    }
}
