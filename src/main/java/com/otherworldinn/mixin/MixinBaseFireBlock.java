package com.otherworldinn.mixin;

import com.otherworldinn.world.dimension.TownDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseFireBlock.class)
public abstract class MixinBaseFireBlock {

    @Inject(method = "inPortalDimension", at = @At("HEAD"), cancellable = true)
    private static void otherworldinn$allowTownPortalActivation(
            Level level, CallbackInfoReturnable<Boolean> cir) {
        if (level.dimension() == TownDimensions.TOWN_LEVEL) {
            cir.setReturnValue(true);
        }
    }
}
