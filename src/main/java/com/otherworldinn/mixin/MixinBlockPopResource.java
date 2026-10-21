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
 * 结构贴图期间抑制方块掉落：在 {@code Block.popResource} 入口按旁路窗口取消，
 * 可同时覆盖 Copycat 伪装方块的手动弹出与 {@code Containers.dropContents} 链路。
 *
 * <p>判定只认 {@link TownZonePolicyService#isDropSuppressingBypassActive()}：若改成"任意旁路激活"，
 * 旁路作用域一旦泄漏就会永久吞掉该服务端线程的所有掉落。
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
        if (TownZonePolicyService.isDropSuppressingBypassActive()) {
            ci.cancel();
        }
    }
}
