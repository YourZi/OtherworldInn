package com.otherworldinn.compat.waystones;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.compat.waystones.client.TeamCoinsRequirementRenderer;

import net.blay09.mods.waystones.client.requirement.RequirementClientRegistry;

public final class WaystonesClientCompat {
    private WaystonesClientCompat() {}

    public static void init() {
        RequirementClientRegistry.registerRenderer(
                TeamCoinsRequirement.class, new TeamCoinsRequirementRenderer());
        OtherworldInn.LOGGER.info("Waystones client compatibility enabled");
    }
}
