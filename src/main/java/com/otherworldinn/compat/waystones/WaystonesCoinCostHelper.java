package com.otherworldinn.compat.waystones;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import net.blay09.mods.waystones.requirement.CombinedRequirement;
import net.blay09.mods.waystones.requirement.ExperienceLevelRequirement;
import net.blay09.mods.waystones.requirement.ExperiencePointsRequirement;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class WaystonesCoinCostHelper {
    private static final int SAME_DIMENSION_BASE_COST = 5;
    private static final double SAME_DIMENSION_COST_PER_BLOCK = 0.02D;
    private static final int CROSS_DIMENSION_COST = 128;

    private WaystonesCoinCostHelper() {}

    public static WarpRequirement replaceExperienceRequirements(
            WarpRequirement original, WaystoneTeleportContext context) {
        List<WarpRequirement> flattened = new ArrayList<>();
        collectWithoutExperience(original, flattened);

        flattened.add(new TeamCoinsRequirement(calculateCost(context)));

        if (flattened.isEmpty()) {
            return new TeamCoinsRequirement(calculateCost(context));
        }
        if (flattened.size() == 1) {
            return flattened.getFirst();
        }
        return new CombinedRequirement(flattened);
    }

    public static int calculateCost(WaystoneTeleportContext context) {
        if (context == null || context.isDimensionalTeleport()) {
            return CROSS_DIMENSION_COST;
        }

        Entity entity = context.getEntity();
        if (entity == null) {
            return CROSS_DIMENSION_COST;
        }

        Vec3 targetCenter = Vec3.atCenterOf(context.getTargetWaystone().getPos());
        int distance = (int) entity.position().distanceTo(targetCenter);
        return (int) Math.ceil(SAME_DIMENSION_BASE_COST + (distance * SAME_DIMENSION_COST_PER_BLOCK));
    }

    private static void collectWithoutExperience(
            WarpRequirement requirement, Collection<WarpRequirement> output) {
        if (requirement == null || requirement.isEmpty()) {
            return;
        }

        if (requirement instanceof ExperiencePointsRequirement
                || requirement instanceof ExperienceLevelRequirement) {
            return;
        }

        if (requirement instanceof CombinedRequirement combinedRequirement) {
            for (WarpRequirement child : combinedRequirement.getRequirements()) {
                collectWithoutExperience(child, output);
            }
            return;
        }

        output.add(requirement);
    }
}
