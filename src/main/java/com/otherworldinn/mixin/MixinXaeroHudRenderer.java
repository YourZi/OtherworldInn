package com.otherworldinn.mixin;

import com.otherworldinn.compat.xaero.XaeroCompatBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "xaero.hud.render.HudRenderer", remap = false)
public abstract class MixinXaeroHudRenderer {
    @Inject(
            method = "renderModule(Lxaero/hud/module/HudModule;Lxaero/hud/Hud;Lxaero/hud/render/module/ModuleRenderContext;Lnet/minecraft/client/gui/GuiGraphics;F)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false)
    private void otherworldinn$cancelXaeroMinimapRenderInTown(
            @Coerce Object module,
            @Coerce Object hud,
            @Coerce Object renderContext,
            GuiGraphics guiGraphics,
            float partialTicks,
            CallbackInfo ci) {
        if (XaeroCompatBridge.isTownBlocked(Minecraft.getInstance())
                && XaeroCompatBridge.isMinimapModule(module)) {
            ci.cancel();
        }
    }
}
