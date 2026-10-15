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
 * 创造模式玩家在城镇维度内的破坏/交互：把整个方法包进受控的保护旁路窗口。
 *
 * <p>旧实现把作用域存在 {@code @Unique} 字段里、靠 {@code @At("RETURN")} 关闭，有两条泄漏路径：
 *
 * <ol>
 *   <li>方法内部抛异常时 {@code @At("RETURN")} 不会执行 → 作用域永不关闭；
 *   <li>{@code destroyBlock} 与 {@code useItemOn} 共用同一个字段 → 两者嵌套时会覆盖掉外层作用域。
 * </ol>
 *
 * <p>旁路栈是<b>按线程</b>保存的，一旦泄漏，该服务端线程的栈将永久非空： {@code MixinBlockPopResource}
 * 会因此持续取消所有 {@code Block.popResource}，表现为"联机时破坏方块没有掉落物、所有维度都如此、 只能重启才能恢复"。
 *
 * <p>改用 MixinExtras 的 {@code @WrapMethod} + try-with-resources：无论正常返回、异常还是嵌套， 作用域都会被关闭。
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
