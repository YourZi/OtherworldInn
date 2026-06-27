package com.otherworldinn.mixin;

import com.otherworldinn.world.event.listener.TownZonePolicyService;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "dev.ftb.mods.ftbultimine.rightclick.CropHarvesting", remap = false)
public abstract class MixinFTBUltimineCropHarvesting {
    @Inject(method = "handleRightClickBlock", at = @At("HEAD"), cancellable = true, remap = false)
    private void otherworldinn$denyProtectedTownCropHarvest(
            @Coerce Object context,
            @Coerce Object hand,
            Collection<BlockPos> positions,
            CallbackInfoReturnable<Integer> cir) {
        ServerPlayer player = otherworldinn$getPlayer(context);
        if (player == null) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            if (!TownZonePolicyService.canPlayerHarvestCropAt(level, pos, state, player)) {
                cir.setReturnValue(0);
                return;
            }
        }
    }

    private static ServerPlayer otherworldinn$getPlayer(Object context) {
        try {
            Object player = context.getClass().getMethod("player").invoke(context);
            return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
            return null;
        }
    }
}
