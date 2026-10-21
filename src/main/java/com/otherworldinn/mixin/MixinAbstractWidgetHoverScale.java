package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 按钮悬停放大 / 按下回弹 + 控件入场错峰淡入。
 * 注入点为 {@link AbstractWidget#render}（final）；{@code renderWidget} 是各子类实现的抽象方法。
 * 移植自 Subtle GUI Tweaks（MIT），署名见 {@code NOTICE-smooth-gui.md}。
 */
@Mixin(AbstractWidget.class)
public abstract class MixinAbstractWidgetHoverScale {

    @Shadow
    protected float alpha;

    @Shadow
    public boolean visible;

    @Shadow
    public boolean active;

    @Shadow
    public abstract int getX();

    @Shadow
    public abstract int getY();

    @Shadow
    public abstract int getWidth();

    @Shadow
    public abstract int getHeight();

    @Shadow
    public abstract boolean isHovered();

    @Shadow
    public abstract boolean isHoveredOrFocused();

    @Unique
    private float otherworldinn$currentScale = 1.0F;

    @Unique
    private float otherworldinn$currentLift;

    @Unique
    private long otherworldinn$lastTime;

    @Unique
    private boolean otherworldinn$transformPushed;

    @Unique
    private float otherworldinn$savedAlpha = Float.NaN;

    @Inject(method = "render", at = @At("HEAD"))
    private void otherworldinn$pushHoverScale(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        otherworldinn$transformPushed = false;
        otherworldinn$savedAlpha = Float.NaN;
        if (!com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            return;
        }
        if (!visible) {
            return;
        }

        long time = SmoothGuiAnimations.now();
        float dt = SmoothGuiAnimations.deltaSeconds(time, otherworldinn$lastTime);
        otherworldinn$lastTime = time;

        float scale = 1.0F;
        float lift = 0.0F;
        if (((Object) this) instanceof Button) {
            boolean hoveredOrFocused = isHoveredOrFocused();
            boolean pressed = isHovered() && active && Minecraft.getInstance().mouseHandler.isLeftPressed();
            float targetScale = pressed ? 0.97F : (hoveredOrFocused ? 1.05F : 1.0F);
            float targetLift = pressed ? 0.25F : (hoveredOrFocused ? -0.75F : 0.0F);
            float responsiveness = pressed ? 30.0F : 18.0F;
            otherworldinn$currentScale =
                    SmoothGuiAnimations.expLerp(responsiveness, otherworldinn$currentScale, targetScale, dt);
            otherworldinn$currentLift =
                    SmoothGuiAnimations.expLerp(16.0F, otherworldinn$currentLift, targetLift, dt);
            scale = otherworldinn$currentScale;
            lift = otherworldinn$currentLift;
        }

        float appear = SmoothGuiAnimations.widgetAppear(getY(), time);
        if (appear < 1.0F) {
            otherworldinn$savedAlpha = alpha;
            alpha = otherworldinn$savedAlpha * Math.max(0.05F, appear);
            lift += (1.0F - appear) * 6.0F;
            scale *= 0.96F + 0.04F * appear;
        }

        if (SmoothGuiAnimations.hasTransform(scale, lift)) {
            float centerX = getX() + getWidth() / 2.0F;
            float centerY = getY() + getHeight() / 2.0F + lift;
            SmoothGuiAnimations.pushCenteredScale(guiGraphics, centerX, centerY, scale);
            otherworldinn$transformPushed = true;
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void otherworldinn$popHoverScale(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (otherworldinn$transformPushed) {
            guiGraphics.pose().popPose();
            otherworldinn$transformPushed = false;
        }
        if (!Float.isNaN(otherworldinn$savedAlpha)) {
            alpha = otherworldinn$savedAlpha;
            otherworldinn$savedAlpha = Float.NaN;
        }
    }
}
