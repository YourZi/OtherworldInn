package com.otherworldinn.mixin;

import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldOpenFlows.class)
public abstract class MixinWorldOpenFlowsExperimentalWarning {
    @ModifyVariable(method = "confirmWorldCreation", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static boolean otherworldinn$alwaysBypassExperimentalWarning(boolean bypassWarnings) {
        return true;
    }
}
