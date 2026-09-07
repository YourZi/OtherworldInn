package com.otherworldinn.mixin;

import com.otherworldinn.block.HoneyCauldronService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeehiveBlockEntity.class)
public abstract class MixinBeehiveBlockEntityHoneyCauldron {

    @Inject(method = "serverTick", at = @At("TAIL"))
    private static void otherworldinn$fillHoneyCauldronFromHive(
            Level level, BlockPos pos, BlockState state, BeehiveBlockEntity beehive, CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (serverLevel.getGameTime() % HoneyCauldronService.DRIP_INTERVAL_TICKS != 0) {
            return;
        }
        if (!HoneyCauldronService.canAccumulateFromHive(state)) {
            return;
        }
        if (HoneyCauldronService.tryAccumulateBelowHive(serverLevel, pos)) {
            serverLevel.setBlockAndUpdate(
                    pos,
                    state.setValue(
                            BeehiveBlock.HONEY_LEVEL,
                            Integer.valueOf(state.getValue(BeehiveBlock.HONEY_LEVEL) - 1)));
        }
    }
}
