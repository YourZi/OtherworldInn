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
@Mixin(targets = "com.wdiscute.sellingbin.event.SBevents", remap = false)
public abstract class MixinSellingBinEvents {
    private static final String SELLING_BIN_EMERALD_PACK =
            "built_in_datapacks/selling_bin_currency_emeralds";

    @Redirect(
            method = "addPackFinders",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/neoforged/neoforge/event/AddPackFindersEvent;addPackFinders(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/server/packs/PackType;Lnet/minecraft/network/chat/Component;Lnet/minecraft/server/packs/repository/PackSource;ZLnet/minecraft/server/packs/repository/Pack$Position;)V"),
            remap = false)
    private static void otherworldinn$skipSellingBinEmeraldPack(
            AddPackFindersEvent event,
            ResourceLocation packLocation,
            PackType packType,
            Component title,
            PackSource packSource,
            boolean fixedPosition,
            Pack.Position packPosition) {
        if (packLocation != null
                && "selling_bin".equals(packLocation.getNamespace())
                && SELLING_BIN_EMERALD_PACK.equals(packLocation.getPath())) {
            return;
        }
        event.addPackFinders(packLocation, packType, title, packSource, fixedPosition, packPosition);
    }
}
