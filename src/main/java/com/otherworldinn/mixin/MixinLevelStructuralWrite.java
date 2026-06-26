package com.otherworldinn.mixin;

import com.otherworldinn.world.event.listener.TownZonePolicyService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class MixinLevelStructuralWrite {
    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"),
            cancellable = true)
    private void otherworldinn$guardProtectedStructuralWrite(
            BlockPos pos,
            BlockState newState,
            int flags,
            int recursionLeft,
            CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockState oldState = serverLevel.getBlockState(pos);
        if (!TownZonePolicyService.canStructuralWriteAt(serverLevel, pos, oldState, newState)) {
            cir.setReturnValue(false);
        }
    }
}
