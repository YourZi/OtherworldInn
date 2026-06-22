package com.otherworldinn.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.wdiscute.starcatcher.event.SCEvents", remap = false)
public abstract class MixinStarcatcherEvents {
    private static final String STARCATCHER_EMERALD_PACK =
            "built_in_datapacks/selling_bin_starcatcher_emeralds";

    @Redirect(
            method = "addPackFinders",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/neoforged/neoforge/event/AddPackFindersEvent;addPackFinders(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/server/packs/PackType;Lnet/minecraft/network/chat/Component;Lnet/minecraft/server/packs/repository/PackSource;ZLnet/minecraft/server/packs/repository/Pack$Position;)V"),
            remap = false)
    private static void otherworldinn$skipStarcatcherEmeraldPack(
            AddPackFindersEvent event,
            ResourceLocation packLocation,
            PackType packType,
            Component title,
            PackSource packSource,
            boolean fixedPosition,
            Pack.Position packPosition) {
        if (packLocation != null
                && "starcatcher".equals(packLocation.getNamespace())
                && STARCATCHER_EMERALD_PACK.equals(packLocation.getPath())) {
            return;
        }
        event.addPackFinders(packLocation, packType, title, packSource, fixedPosition, packPosition);
    }
}
