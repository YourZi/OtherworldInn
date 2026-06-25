package com.otherworldinn.mixin;

import com.otherworldinn.world.event.listener.TownProtectionHandler;
import com.simibubi.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBreakingMovementBehaviour.class)
public abstract class MixinCreateBlockBreakingMovementBehaviour {

    @Inject(method = "canBreak", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$denyProtectedTownBreak(
            Level world,
            BlockPos breakingPos,
            BlockState state,
            CallbackInfoReturnable<Boolean> cir) {
        if (!TownProtectionHandler.canCreateModifyBlockAt(world, breakingPos)) {
            cir.setReturnValue(false);
        }
    }
}
