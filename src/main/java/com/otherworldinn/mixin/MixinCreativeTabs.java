package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 创造模式标签栏的悬停/选中缩放：选中标签放到 1.11、悬停 1.06，并上移 1.0 / 0.5px，
 * 以 18 / 16 的指数速度平滑；切换标签时重开槽位入场级联。
 *
 * <p>标签坐标由 Redirect {@code GuiGraphics.blit} 取得；注入均 {@code require = 0}，
 * 目标签名变动时静默失效而不崩溃。
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeTabs {

    @Shadow
    private static CreativeModeTab selectedTab;

    @Unique
    private Map<CreativeModeTab, Float> otherworldinn$tabScales;

    @Unique
    private Map<CreativeModeTab, Float> otherworldinn$tabLifts;

    @Unique
    private long otherworldinn$lastTabTime;

    @Unique
    private float otherworldinn$tabDt;

    @Unique
    private int otherworldinn$mouseX;

    @Unique
    private int otherworldinn$mouseY;

    @Unique
    @Nullable
    private CreativeModeTab otherworldinn$currentTab;

    @Unique
    private boolean otherworldinn$tabMatrixPushed;

    @Inject(method = "render", at = @At("HEAD"))
    private void otherworldinn$captureMouse(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        otherworldinn$mouseX = mouseX;
        otherworldinn$mouseY = mouseY;
        long now = SmoothGuiAnimations.now();
        otherworldinn$tabDt = SmoothGuiAnimations.deltaSeconds(now, otherworldinn$lastTabTime);
        otherworldinn$lastTabTime = now;
    }

    @Inject(method = "renderTabButton", at = @At("HEAD"), require = 0)
    private void otherworldinn$startTabRender(GuiGraphics guiGraphics, CreativeModeTab tab, CallbackInfo ci) {
        otherworldinn$currentTab = tab;
        otherworldinn$tabMatrixPushed = false;
    }

    @Redirect(
            method = "renderTabButton",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            require = 0)
    private void otherworldinn$wrapTabWithScale(
            GuiGraphics guiGraphics,
            ResourceLocation texture,
            int x,
            int y,
            int uOffset,
            int vOffset,
            int uWidth,
            int vHeight) {
        if (otherworldinn$tabMatrixPushed
                || otherworldinn$currentTab == null
                || !com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            guiGraphics.blit(texture, x, y, uOffset, vOffset, uWidth, vHeight);
            return;
        }
        if (otherworldinn$tabScales == null) {
            otherworldinn$tabScales = new IdentityHashMap<>();
            otherworldinn$tabLifts = new IdentityHashMap<>();
        }
        boolean isSelected = otherworldinn$currentTab == selectedTab;
        boolean isHovered = otherworldinn$mouseX >= x
                && otherworldinn$mouseX < x + uWidth
                && otherworldinn$mouseY >= y
                && otherworldinn$mouseY < y + vHeight;
        float targetScale = isSelected ? 1.11F : (isHovered ? 1.06F : 1.0F);
        float targetLift = isSelected ? -1.0F : (isHovered ? -0.5F : 0.0F);

        float currentScale = otherworldinn$tabScales.getOrDefault(otherworldinn$currentTab, 1.0F);
        float currentLift = otherworldinn$tabLifts.getOrDefault(otherworldinn$currentTab, 0.0F);
        currentScale = SmoothGuiAnimations.expLerp(18.0F, currentScale, targetScale, otherworldinn$tabDt);
        currentLift = SmoothGuiAnimations.expLerp(16.0F, currentLift, targetLift, otherworldinn$tabDt);
        otherworldinn$tabScales.put(otherworldinn$currentTab, currentScale);
        otherworldinn$tabLifts.put(otherworldinn$currentTab, currentLift);

        if (Math.abs(currentScale - 1.0F) > 0.001F || Math.abs(currentLift) > 0.01F) {
            float centerX = x + uWidth / 2.0F;
            float centerY = y + vHeight / 2.0F + currentLift;
            SmoothGuiAnimations.pushCenteredScale(guiGraphics, centerX, centerY, currentScale);
            otherworldinn$tabMatrixPushed = true;
        }
        guiGraphics.blit(texture, x, y, uOffset, vOffset, uWidth, vHeight);
    }

    @Inject(method = "renderTabButton", at = @At("RETURN"), require = 0)
    private void otherworldinn$endTabRender(GuiGraphics guiGraphics, CreativeModeTab tab, CallbackInfo ci) {
        if (otherworldinn$tabMatrixPushed) {
            guiGraphics.pose().popPose();
            otherworldinn$tabMatrixPushed = false;
        }
        otherworldinn$currentTab = null;
    }

    /** 切换标签时重开槽位入场级联，让新网格再"弹"一次。 */
    @Inject(method = "selectTab", at = @At("HEAD"), require = 0)
    private void otherworldinn$onTabSelected(CreativeModeTab tab, CallbackInfo ci) {
        if (com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            SmoothGuiAnimations.restartSlotCascade(SmoothGuiAnimations.now());
        }
    }
}
