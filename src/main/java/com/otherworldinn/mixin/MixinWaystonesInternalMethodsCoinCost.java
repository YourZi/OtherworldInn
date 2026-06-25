package com.otherworldinn.mixin;

import com.otherworldinn.compat.waystones.WaystonesCoinCostHelper;
import net.blay09.mods.waystones.api.WaystoneTeleportContext;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.blay09.mods.waystones.InternalMethodsImpl", remap = false)
public abstract class MixinWaystonesInternalMethodsCoinCost {
    @Inject(method = "resolveRequirements", at = @At("RETURN"), cancellable = true, remap = false)
    private void otherworldinn$replaceWaystonesXpCost(
            WaystoneTeleportContext context, CallbackInfoReturnable<WarpRequirement> cir) {
        cir.setReturnValue(
                WaystonesCoinCostHelper.replaceExperienceRequirements(cir.getReturnValue(), context));
    }
}
