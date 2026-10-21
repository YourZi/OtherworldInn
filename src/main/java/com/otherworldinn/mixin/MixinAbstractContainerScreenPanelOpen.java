package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 容器界面面板展开：在背景画完、{@code renderBg} 之前压入缩放变换（只负责压栈，弹出由 {@code MixinScreenTrackOpen} 统一处理）。 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreenPanelOpen {

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    protected int imageWidth;

    @Shadow
    protected int imageHeight;

    @Inject(
            method = "renderBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lnet/minecraft/client/gui/GuiGraphics;FII)V",
                    shift = At.Shift.BEFORE))
    private void otherworldinn$pushPanelOpen(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            return;
        }
        float open = SmoothGuiAnimations.panelOpen(SmoothGuiAnimations.now());
        if (open >= 1.0F) {
            return;
        }
        float scale = 0.97F + 0.03F * open;
        float centerX = leftPos + imageWidth / 2.0F;
        float centerY = topPos + imageHeight / 2.0F;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.pose().translate(-centerX, -centerY, 0.0F);
        SmoothGuiAnimations.markPanelTransformPushed();
    }
}
