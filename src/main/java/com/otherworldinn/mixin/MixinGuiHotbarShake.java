package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 空手/满背包时丢物品失败的"热键栏抖动"（0.3s 正弦衰减，幅度 4px）。
 * 注入点用 {@code require = 0}，目标方法变动时静默失效而不致启动失败。
 * 移植自 Subtle GUI Tweaks（MIT），署名见 {@code NOTICE-smooth-gui.md}。
 */
@Mixin(Gui.class)
public abstract class MixinGuiHotbarShake {

    @Unique
    private boolean otherworldinn$shakePushed;

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void otherworldinn$pushHotbarShake(
            GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        otherworldinn$shakePushed = false;
        if (!com.otherworldinn.foundation.ClientConfig.itemAnimations()) {
            return;
        }
        float offset = SmoothGuiAnimations.hotbarShakeOffset(SmoothGuiAnimations.now());
        if (Math.abs(offset) < 0.01F) {
            return;
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, offset, 0.0F);
        otherworldinn$shakePushed = true;
    }

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    private void otherworldinn$popHotbarShake(
            GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (otherworldinn$shakePushed) {
            guiGraphics.pose().popPose();
            otherworldinn$shakePushed = false;
        }
    }
}
