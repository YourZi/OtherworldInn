package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 丢弃物品失败（空手/背包满）时触发热键栏抖动：{@code LocalPlayer.drop(boolean)} 返回 false 即视为丢不出去。
 * 移植自 Subtle GUI Tweaks（MIT），署名见 {@code NOTICE-smooth-gui.md}。
 */
@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayerEmptyDropShake {

    @Inject(method = "drop", at = @At("RETURN"), require = 0)
    private void otherworldinn$shakeOnEmptyDrop(boolean all, CallbackInfoReturnable<Boolean> cir) {
        if (!com.otherworldinn.foundation.ClientConfig.itemAnimations()) {
            return;
        }
        if (!cir.getReturnValueZ()) {
            SmoothGuiAnimations.triggerHotbarShake(SmoothGuiAnimations.now());
        }
    }
}
