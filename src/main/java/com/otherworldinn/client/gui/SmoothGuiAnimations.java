package com.otherworldinn.client.gui;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;

/**
 * GUI 平滑动画工具：缓动函数、居中缩放、控件入场错峰与槽位级联。
 * 移植自 Subtle GUI Tweaks（MIT），署名见 {@code NOTICE-smooth-gui.md}。
 */
public final class SmoothGuiAnimations {

    /** 单帧时间步上限，避免卡顿/切屏后动画跳变。 */
    public static final float MAX_DT = 0.1F;
    public static final float WIDGET_APPEAR_DURATION = 0.22F;
    public static final float WIDGET_APPEAR_STAGGER = 0.12F;
    public static final float HOTBAR_SHAKE_DURATION = 0.3F;

    private static Screen trackedScreen;
    private static long screenOpenedAt;
    private static long hotbarShakeAt;
    private static long slotCascadeAt;
    private static boolean panelTransformPushed;

    private SmoothGuiAnimations() {}

    public static long now() {
        return Util.getMillis() * 1_000_000L;
    }

    public static float deltaSeconds(long nowNanos, long lastNanos) {
        return lastNanos == 0L ? 0.0F : Mth.clamp((float) (nowNanos - lastNanos) / 1.0E9F, 0.0F, MAX_DT);
    }

    public static float ageSeconds(long nowNanos, long startNanos) {
        return startNanos == 0L ? Float.MAX_VALUE : (float) (nowNanos - startNanos) / 1.0E9F;
    }

    public static float expLerp(float speed, float current, float target, float dt) {
        return dt <= 0.0F ? current : Mth.lerp(1.0F - (float) Math.exp(-speed * dt), current, target);
    }

    public static float easeOutCubic(float t) {
        float inv = 1.0F - Mth.clamp(t, 0.0F, 1.0F);
        return 1.0F - inv * inv * inv;
    }

    public static float easeOutBack(float t) {
        float x = Mth.clamp(t, 0.0F, 1.0F) - 1.0F;
        return 1.0F + 2.70158F * x * x * x + 1.70158F * x * x;
    }

    /** 触发一次热键栏抖动（丢弃物品失败时调用）。 */
    public static void triggerHotbarShake(long nowNanos) {
        hotbarShakeAt = nowNanos;
    }

    /** 热键栏抖动的当前 Y 偏移（像素）：0.3s 内按正弦衰减，之后归零。 */
    public static float hotbarShakeOffset(long nowNanos) {
        if (hotbarShakeAt == 0L) {
            return 0.0F;
        }
        float age = ageSeconds(nowNanos, hotbarShakeAt);
        if (age >= HOTBAR_SHAKE_DURATION) {
            hotbarShakeAt = 0L;
            return 0.0F;
        }
        float t = age / HOTBAR_SHAKE_DURATION;
        float decay = 1.0F - t;
        return Mth.sin(t * (float) Math.PI * 6.0F) * decay * decay * 4.0F;
    }

    /** 标记"面板展开变换已压栈"，由屏幕最外层统一弹出。 */
    public static void markPanelTransformPushed() {
        panelTransformPushed = true;
    }

    /** 取走标记；返回 true 表示调用方需要执行一次 {@code popPose()}。 */
    public static boolean consumePanelTransformPushed() {
        boolean pushed = panelTransformPushed;
        panelTransformPushed = false;
        return pushed;
    }

    public static boolean hasTransform(float scale, float lift) {        return Math.abs(scale - 1.0F) > 0.001F || Math.abs(lift) > 0.01F;
    }

    /** 以屏幕坐标 (centerX, centerY) 为中心压入缩放变换；调用方必须成对 popPose()。 */
    public static void pushCenteredScale(GuiGraphics guiGraphics, float centerX, float centerY, float scale) {
        pushCenteredTransform(guiGraphics, centerX, centerY, scale, 0.0F);
    }

    /** 以 (centerX, centerY) 为中心压入"缩放 + 旋转"变换（角度为度）；调用方必须成对 popPose()。 */
    public static void pushCenteredTransform(
            GuiGraphics guiGraphics, float centerX, float centerY, float scale, float angleDegrees) {
        float safeScale = Math.max(scale, 0.02F);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0.0F);
        guiGraphics.pose().scale(safeScale, safeScale, 1.0F);
        if (angleDegrees != 0.0F) {
            guiGraphics.pose().mulPose(com.mojang.math.Axis.ZP.rotationDegrees(angleDegrees));
        }
        guiGraphics.pose().translate(-centerX, -centerY, 0.0F);
    }

    /** 点击/掉落类"弹一下"的衰减曲线：0→1 过程中一次完整正弦脉冲，两端归零。 */
    public static float punch(float t) {
        if (t <= 0.0F || t >= 1.0F) {
            return 0.0F;
        }
        float decay = 1.0F - t;
        return Mth.sin(t * (float) Math.PI * 2.5F) * decay * decay;
    }

    /** 槽位级联：界面打开后按槽位坐标错峰，用 easeOutBack 从 0 弹到 1。 */
    public static float slotCascade(int slotX, int slotY, long nowNanos) {
        float t = (ageSeconds(nowNanos, slotCascadeAt) - Math.max(0, slotX + slotY) * 0.0006F) / 0.2F;
        return t >= 1.0F ? 1.0F : easeOutBack(t);
    }

    /** 重新开始槽位级联（切换创造模式标签等场景）。 */
    public static void restartSlotCascade(long nowNanos) {
        slotCascadeAt = nowNanos;
    }

    /** 登记当前界面开始时间；由 {@code MixinScreenTrackOpen} 在 {@code renderWithTooltip} HEAD 调用，使第一帧就按 open≈0 计算。 */
    public static void trackScreen(Screen screen, long nowNanos) {
        if (screen == null) {
            trackedScreen = null;
            screenOpenedAt = 0L;
            return;
        }
        if (screen != trackedScreen) {
            trackedScreen = screen;
            screenOpenedAt = nowNanos;
            slotCascadeAt = nowNanos;
        }
    }

    /** 容器界面"面板展开"进度（0→1）：刚打开时从 0 增长，之后恒为 1；未登记过的界面返回 1（不动画）。 */
    public static float panelOpen(long nowNanos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null || mc.screen != trackedScreen || screenOpenedAt == 0L) {
            return 1.0F;
        }
        float t = ageSeconds(nowNanos, screenOpenedAt) / WIDGET_APPEAR_DURATION;
        return t >= 1.0F ? 1.0F : easeOutCubic(t);
    }

    /** 控件入场进度（0→1）：按控件纵坐标错峰，容器界面不参与；未登记过的界面返回 1（不动画）。 */
    public static float widgetAppear(int widgetY, long nowNanos) {
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.screen;
        if (screen == null || screen != trackedScreen || screenOpenedAt == 0L) {
            return 1.0F;
        }
        if (screen instanceof AbstractContainerScreen) {
            return 1.0F;
        }
        float delay = Mth.clamp((float) widgetY / Math.max(1, screen.height), 0.0F, 1.0F) * WIDGET_APPEAR_STAGGER;
        float t = (ageSeconds(nowNanos, screenOpenedAt) - delay) / WIDGET_APPEAR_DURATION;
        return t >= 1.0F ? 1.0F : easeOutCubic(t);
    }
}
