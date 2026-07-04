package com.otherworldinn.mixin;

import com.otherworldinn.compat.xaero.XaeroCompatBridge;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "xaero.map.events.ClientEvents", remap = false)
public abstract class MixinXaeroWorldMapClientEvents {
    @Inject(method = "handleClientRunTickStart", at = @At("HEAD"), cancellable = true, remap = false)
    private void otherworldinn$blockXaeroWorldMapHotkeysInTown(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (!XaeroCompatBridge.isTownBlocked(mc)) {
            return;
        }
        XaeroCompatBridge.suppressWorldMapInputs();
        ci.cancel();
    }
}
