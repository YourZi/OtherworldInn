package com.otherworldinn.compat;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.compat.exposure.ExposurePhotoTaskService;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

public final class ExposureCompat {
    private static final String MOD_ID = "exposure";
    private static boolean initialized = false;

    private ExposureCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void init() {
        if (!isLoaded() || initialized) {
            return;
        }
        NeoForge.EVENT_BUS.register(ExposurePhotoTaskService.class);
        initialized = true;
        OtherworldInn.LOGGER.info("Exposure photo task compatibility enabled");
    }
}
