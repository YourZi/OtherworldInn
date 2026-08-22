package com.otherworldinn.mixin;

import com.otherworldinn.world.event.listener.TownZonePolicyService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 结构贴图期间的掉落抑制。
 *
 * <p>Create 伪装方块在 onRemove 中手动 popResource 弹出内容物，setBlock 的
 * UPDATE_SUPPRESS_DROPS 只作用于 destroy 路径的战利品、对此无拦截能力。在
 * popResource 入口处按保护旁路窗口取消，同时覆盖 Copycat 手动弹出与
 * Containers.dropContents（箱子等容器内容）链路——两者最终都汇入该方法。
 * 旁路窗口仅在 TownStructurePlacer 结构贴图期间激活，窗口外行为不变。
 */
@Mixin(Block.class)
public abstract class MixinBlockPopResource {
    @Inject(
            method =
                    "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true)
    private static void otherworldinn$suppressDuringStructurePaste(
            Level level, BlockPos pos, ItemStack stack, CallbackInfo ci) {
        if (TownZonePolicyService.isProtectionBypassActive()) {
            ci.cancel();
        }
    }
}
