package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModStats {
    public static final DeferredRegister<ResourceLocation> CUSTOM_STATS =
            DeferredRegister.create(Registries.CUSTOM_STAT, OtherworldInn.MODID);

    public static final DeferredHolder<ResourceLocation, ResourceLocation> GUESTS_CHECKED_OUT =
            CUSTOM_STATS.register(
                    "guests_checked_out",
                    () -> ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "guests_checked_out"));

    public static final DeferredHolder<ResourceLocation, ResourceLocation> MEALS_SOLD =
            CUSTOM_STATS.register(
                    "meals_sold",
                    () -> ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "meals_sold"));

    public static final DeferredHolder<ResourceLocation, ResourceLocation> COMMISSIONS_COMPLETED =
            CUSTOM_STATS.register(
                    "commissions_completed",
                    () -> ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "commissions_completed"));

    private ModStats() {}
}
