package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.world.fatigue.FatigueData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(
        modid = OtherworldInn.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public final class FatigueHudOverlay {
    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "fatigue_hud_overlay");
    private static final int EXPERIENCE_BAR_WIDTH = 182;
    private static final int EXPERIENCE_BAR_HALF_WIDTH = EXPERIENCE_BAR_WIDTH / 2;
    private static final int EXPERIENCE_BAR_HEIGHT = 5;
    private static final int EXPERIENCE_BAR_BOTTOM_OFFSET = 29;
    private static final int FATIGUE_BAR_HEIGHT = 2;
    private static final int FATIGUE_BACKGROUND = 0x66000000;
    private static final int FATIGUE_STAGE_0_COLOR = 0xFFE2B34A;
    private static final int FATIGUE_STAGE_1_COLOR = 0xFFE0A33B;
    private static final int FATIGUE_STAGE_2_COLOR = 0xFFD7862F;
    private static final int FATIGUE_STAGE_3_COLOR = 0xFFC76A2A;
    private static final int FATIGUE_STAGE_4_COLOR = 0xFFB94A30;
    private static final int FATIGUE_ANIMATION_BASE_DURATION_MS = 180;
    private static final int FATIGUE_ANIMATION_EXTRA_DURATION_MS = 140;
    private static final double ANIMATION_EPSILON = 0.001D;

    private static double displayedFatigue = -1.0D;
    private static double animationStartFatigue = 0.0D;
    private static double animationTargetFatigue = 0.0D;
    private static long animationStartTimeMs = 0L;
    private static int animationDurationMs = 0;

    private FatigueHudOverlay() {}

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, LAYER_ID, FatigueHudOverlay::render);
    }

    private static void render(net.minecraft.client.gui.GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (!canRenderHud(mc)) {
            return;
        }

        FatigueData fatigueData = mc.player.getData(ModAttachments.PLAYER_FATIGUE);
        long currentTimeMs = System.currentTimeMillis();
        double targetFatigue = Mth.clamp(fatigueData.getClientFatigue(), 0.0D, FatigueData.MAX_FATIGUE);
        double fatigueValue = getAnimatedFatigue(targetFatigue, currentTimeMs);
        if (fatigueValue <= 0.0D) {
            return;
        }

        int barLeft = guiGraphics.guiWidth() / 2 - EXPERIENCE_BAR_HALF_WIDTH;
        int experienceBarTop = guiGraphics.guiHeight() - EXPERIENCE_BAR_BOTTOM_OFFSET;
        int fatigueBarTop = experienceBarTop + EXPERIENCE_BAR_HEIGHT - FATIGUE_BAR_HEIGHT;
        int fatigueBarBottom = fatigueBarTop + FATIGUE_BAR_HEIGHT;

        int fillWidth =
                Mth.clamp(
                        (int) Math.round(EXPERIENCE_BAR_WIDTH * (fatigueValue / FatigueData.MAX_FATIGUE)),
                        0,
                        EXPERIENCE_BAR_WIDTH);
        if (fillWidth <= 0) {
            return;
        }

        int fillLeft = barLeft + (EXPERIENCE_BAR_WIDTH - fillWidth) / 2;
        int fillRight = fillLeft + fillWidth;

        guiGraphics.fill(
                barLeft, fatigueBarTop, barLeft + EXPERIENCE_BAR_WIDTH, fatigueBarBottom, FATIGUE_BACKGROUND);
        guiGraphics.fill(
                fillLeft,
                fatigueBarTop,
                fillRight,
                fatigueBarBottom,
                getFatigueStageColor(fatigueData.getClientStage()));
        guiGraphics.fill(fillLeft, fatigueBarTop, fillRight, fatigueBarTop + 1, ModColors.WHITE_ALPHA_FULL);
    }

    private static boolean canRenderHud(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return false;
        }
        if (!mc.gameMode.hasExperience()) {
            return false;
        }
        if (mc.player.isCreative() || mc.player.isSpectator()) {
            return false;
        }
        if (mc.player.jumpableVehicle() != null) {
            return false;
        }
        return true;
    }

    private static double getAnimatedFatigue(double targetFatigue, long currentTimeMs) {
        if (displayedFatigue < 0.0D) {
            displayedFatigue = targetFatigue;
            animationStartFatigue = targetFatigue;
            animationTargetFatigue = targetFatigue;
            animationStartTimeMs = currentTimeMs;
            animationDurationMs = 0;
            return displayedFatigue;
        }

        advanceAnimation(currentTimeMs);
        if (Math.abs(targetFatigue - animationTargetFatigue) > ANIMATION_EPSILON) {
            animationStartFatigue = displayedFatigue;
            animationTargetFatigue = targetFatigue;
            animationStartTimeMs = currentTimeMs;
            animationDurationMs = calculateAnimationDurationMs(Math.abs(targetFatigue - displayedFatigue));
            advanceAnimation(currentTimeMs);
        }
        return displayedFatigue;
    }

    private static void advanceAnimation(long currentTimeMs) {
        if (animationDurationMs <= 0) {
            displayedFatigue = animationTargetFatigue;
            return;
        }

        double progress =
                Mth.clamp(
                        (double) (currentTimeMs - animationStartTimeMs) / (double) animationDurationMs,
                        0.0D,
                        1.0D);
        double easedProgress = easeInOutSine(progress);
        displayedFatigue = Mth.lerp(easedProgress, animationStartFatigue, animationTargetFatigue);
        if (progress >= 1.0D - ANIMATION_EPSILON) {
            displayedFatigue = animationTargetFatigue;
            animationDurationMs = 0;
        }
    }

    private static int calculateAnimationDurationMs(double delta) {
        double clampedRatio = Mth.clamp(delta / FatigueData.MAX_FATIGUE, 0.0D, 1.0D);
        return FATIGUE_ANIMATION_BASE_DURATION_MS
                + (int) Math.round(clampedRatio * FATIGUE_ANIMATION_EXTRA_DURATION_MS);
    }

    private static double easeInOutSine(double progress) {
        return -(Math.cos(Math.PI * progress) - 1.0D) / 2.0D;
    }

    private static int getFatigueStageColor(int stageLevel) {
        return switch (Mth.clamp(stageLevel, 0, 4)) {
            case 1 -> FATIGUE_STAGE_1_COLOR;
            case 2 -> FATIGUE_STAGE_2_COLOR;
            case 3 -> FATIGUE_STAGE_3_COLOR;
            case 4 -> FATIGUE_STAGE_4_COLOR;
            default -> FATIGUE_STAGE_0_COLOR;
        };
    }
}
