package com.otherworldinn.compat.exposure;

import com.otherworldinn.world.photo.PhotoObjective;
import com.otherworldinn.world.photo.PhotoObjectiveMatchMode;
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
            int matchedCount = 0;
            for (EntityInFrame entityInFrame : frame.entitiesInFrame()) {
                if (objective.entityIds().contains(entityInFrame.id())) {
                    matchedCount++;
                }
            }
            results.add(matchedCount >= objective.countMin());
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
            results.add(objective.dimension().equals(dimension));
        }

        if (!objective.biomeIds().isEmpty()) {
            ResourceLocation biome = frame.getExtraDataForReading().get(Frame.BIOME).orElse(null);
            results.add(biome != null && objective.biomeIds().contains(biome));
        }

        if (results.isEmpty()) {
            return false;
        }

        return objective.matchMode() == PhotoObjectiveMatchMode.ANY
                ? results.stream().anyMatch(Boolean::booleanValue)
                : results.stream().allMatch(Boolean::booleanValue);
    }
}
