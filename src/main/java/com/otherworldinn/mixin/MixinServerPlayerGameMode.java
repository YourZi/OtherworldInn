package com.otherworldinn.mixin;

import com.otherworldinn.world.event.listener.TownZonePolicyService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {

    @Shadow protected ServerLevel level;

    @Shadow protected ServerPlayer player;

    @Unique
    private TownZonePolicyService.ProtectionBypassScope otherworldinn$creativeUseScope;

    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void otherworldinn$beginCreativeDestroyBypass(
            BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        otherworldinn$creativeUseScope =
                TownZonePolicyService.beginCreativePlayerBypass(level, player);
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void otherworldinn$endCreativeDestroyBypass(
            BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (otherworldinn$creativeUseScope != null) {
            otherworldinn$creativeUseScope.close();
            otherworldinn$creativeUseScope = null;
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void otherworldinn$beginCreativeUseBypass(
            ServerPlayer player,
            Level level,
            ItemStack stack,
            net.minecraft.world.InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir) {
        otherworldinn$creativeUseScope =
                TownZonePolicyService.beginCreativePlayerBypass(level, player);
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void otherworldinn$endCreativeUseBypass(
            ServerPlayer player,
            Level level,
            ItemStack stack,
            net.minecraft.world.InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (otherworldinn$creativeUseScope != null) {
            otherworldinn$creativeUseScope.close();
            otherworldinn$creativeUseScope = null;
        }
    }
}
