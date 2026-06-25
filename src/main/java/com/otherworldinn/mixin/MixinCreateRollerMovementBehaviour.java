package com.otherworldinn.mixin;

import com.otherworldinn.world.event.listener.TownProtectionHandler;
import com.simibubi.create.content.contraptions.actors.roller.RollerMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RollerMovementBehaviour.class)
public abstract class MixinCreateRollerMovementBehaviour {

    @Inject(method = "tryFill", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$denyProtectedTownPaving(
            MovementContext context,
            BlockPos targetPos,
            BlockState toPlace,
            CallbackInfoReturnable<Object> cir) {
        if (!TownProtectionHandler.canCreateModifyBlockAt(context.world, targetPos)) {
            cir.setReturnValue(resolveFailResult());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object resolveFailResult() {
        Class enumClass;
        try {
            enumClass =
                    Class.forName(
                            "com.simibubi.create.content.contraptions.actors.roller.RollerMovementBehaviour$PaveResult");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to resolve Create roller pave result enum", e);
        }
        return Enum.valueOf(enumClass, "FAIL");
    }
}
