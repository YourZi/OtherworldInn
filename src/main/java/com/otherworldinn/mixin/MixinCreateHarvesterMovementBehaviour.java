package com.otherworldinn.mixin;

import com.otherworldinn.world.event.listener.TownProtectionHandler;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HarvesterMovementBehaviour.class)
public abstract class MixinCreateHarvesterMovementBehaviour {

    @Inject(method = "visitNewPosition", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$denyProtectedTownHarvest(
            MovementContext context, BlockPos pos, CallbackInfo ci) {
        if (!TownProtectionHandler.canCreateModifyBlockAt(context.world, pos)) {
            ci.cancel();
        }
    }
}
