package com.otherworldinn.world.photo;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record PhotoObjective(
        ResourceLocation id,
        String translationKey,
        PhotoObjectiveMatchMode matchMode,
        List<ResourceLocation> entityIds,
        List<ResourceLocation> structureIds,
        @Nullable ResourceLocation dimension,
        List<ResourceLocation> biomeIds,
        int countMin,
        boolean requireSingleShot) {
    public PhotoObjective {
        translationKey = translationKey == null ? "" : translationKey;
        matchMode = matchMode == null ? PhotoObjectiveMatchMode.ALL : matchMode;
        entityIds = List.copyOf(entityIds == null ? List.of() : entityIds);
        structureIds = List.copyOf(structureIds == null ? List.of() : structureIds);
        biomeIds = List.copyOf(biomeIds == null ? List.of() : biomeIds);
        countMin = Math.max(1, countMin);
    }
}
