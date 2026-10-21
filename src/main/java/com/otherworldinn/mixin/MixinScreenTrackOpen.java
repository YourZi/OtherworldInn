package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 屏幕渲染最外层：HEAD 登记界面打开时刻、补弹上一帧残留的面板变换，并压入非容器界面的入场下滑；
 * RETURN 统一弹出下滑与 {@code MixinAbstractContainerScreenPanelOpen} 压入的面板变换。
 *
 * <p>弹出放在这里（而非 {@code AbstractContainerScreen.render} 的 RETURN），是为了让子类
 * {@code render} 中 {@code super.render(...)} 之后的自定义绘制也落在同一个变换里。
 */
@Mixin(Screen.class)
public abstract class MixinScreenTrackOpen {

    @Unique
    private boolean otherworldinn$slidePushed;

    @Inject(method = "renderWithTooltip", at = @At("HEAD"))
    private void otherworldinn$trackScreenOpen(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // 上一帧若因异常/提前返回没弹栈，这里补一次，避免变换泄漏到后续所有绘制
        if (SmoothGuiAnimations.consumePanelTransformPushed()) {
            guiGraphics.pose().popPose();
        }
        SmoothGuiAnimations.trackScreen((Screen) (Object) this, SmoothGuiAnimations.now());
        otherworldinn$slidePushed = false;
    }

    /** 非容器界面的入场下滑，压栈点在 {@code renderBackground} 之后，使背景与蒙层静止、只有内容层滑入。 */
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
                    shift = At.Shift.AFTER),
            require = 0)
    private void otherworldinn$pushScreenSlide(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        otherworldinn$slidePushed = false;
        if (!com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            return;
        }
        if (((Object) this) instanceof AbstractContainerScreen) {
            return;
        }
        float open = SmoothGuiAnimations.panelOpen(SmoothGuiAnimations.now());
        if (open >= 1.0F) {
            return;
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, (1.0F - open) * 8.0F, 0.0F);
        otherworldinn$slidePushed = true;
    }

    @Inject(method = "renderWithTooltip", at = @At("RETURN"))
    private void otherworldinn$popScreenTransforms(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (otherworldinn$slidePushed) {
            guiGraphics.pose().popPose();
            otherworldinn$slidePushed = false;
        }
        if (SmoothGuiAnimations.consumePanelTransformPushed()) {
            guiGraphics.pose().popPose();
        }
    }
}
