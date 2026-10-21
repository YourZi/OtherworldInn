package com.otherworldinn.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.otherworldinn.world.event.listener.TownZonePolicyService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 创造模式玩家在城镇维度内的破坏/交互：把 {@code destroyBlock} 与 {@code useItemOn} 整个方法
 * 包进受控的保护旁路作用域（{@code TownZonePolicyService.ProtectionBypassScope}）。
 *
 * <p>旁路栈按线程保存，栈非空时会取消 {@code Block.popResource}，因此统一用 {@code @WrapMethod} +
 * try-with-resources 包住，保证正常返回、抛异常、嵌套调用三种情况下作用域都会被关闭。
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class MixinServerPlayerGameMode {

    @Shadow protected ServerLevel level;

    @Shadow protected ServerPlayer player;

    @WrapMethod(method = "destroyBlock")
    private boolean otherworldinn$wrapCreativeDestroyBypass(BlockPos pos, Operation<Boolean> original) {
        try (TownZonePolicyService.ProtectionBypassScope ignored =
                TownZonePolicyService.beginCreativePlayerBypass(level, player)) {
            return original.call(pos);
        }
    }

    @WrapMethod(method = "useItemOn")
    private InteractionResult otherworldinn$wrapCreativeUseBypass(
            ServerPlayer player,
            Level level,
            ItemStack stack,
            InteractionHand hand,
            BlockHitResult hitResult,
            Operation<InteractionResult> original) {
        try (TownZonePolicyService.ProtectionBypassScope ignored =
                TownZonePolicyService.beginCreativePlayerBypass(level, player)) {
            return original.call(player, level, stack, hand, hitResult);
        }
    }
}
