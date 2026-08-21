package com.otherworldinn.compat.exposure;

import com.otherworldinn.world.photo.PhotoObjective;
import com.otherworldinn.world.photo.PhotoObjectiveEvaluator;
import io.github.mortuusars.exposure.world.camera.frame.EntityInFrame;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ExposurePhotoMatcher {
    private ExposurePhotoMatcher() {}

    public static boolean matches(PhotoObjective objective, Frame frame) {
        if (objective == null || frame == null) {
            return false;
        }

        List<Boolean> results = new ArrayList<>();

        if (!objective.entityIds().isEmpty()) {
            List<ResourceLocation> entityTypeIds = new ArrayList<>();
            for (EntityInFrame entityInFrame : frame.entitiesInFrame()) {
                entityTypeIds.add(entityInFrame.id());
            }
            results.add(PhotoObjectiveEvaluator.matchesEntityTypeIds(objective, entityTypeIds));
        }

        if (!objective.structureIds().isEmpty()) {
            List<ResourceLocation> structures =
                    frame.getExtraDataForReading().getOrDefault(Frame.STRUCTURES, List.of());
            Set<ResourceLocation> structureSet = new HashSet<>(structures);
            boolean matched =
                    objective.structureIds().stream().anyMatch(structureSet::contains);
            results.add(matched);
        }

        if (objective.dimension() != null) {
            ResourceLocation dimension =
                    frame.getExtraDataForReading().get(Frame.DIMENSION).orElse(null);
            results.add(PhotoObjectiveEvaluator.matchesDimension(objective, dimension));
        }

        if (!objective.biomeIds().isEmpty()) {
            ResourceLocation biome = frame.getExtraDataForReading().get(Frame.BIOME).orElse(null);
            results.add(PhotoObjectiveEvaluator.matchesBiome(objective, biome));
        }

        return PhotoObjectiveEvaluator.combine(objective, results);
    }
}
