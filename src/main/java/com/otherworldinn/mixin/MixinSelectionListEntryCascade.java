package com.otherworldinn.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.otherworldinn.client.gui.SmoothGuiAnimations;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 列表条目入场级联：界面打开时按条目纵坐标错峰淡入并轻微上移（注入 {@code AbstractSelectionList.renderItem}）。
 * 移植自 Subtle GUI Tweaks（MIT），署名见 {@code NOTICE-smooth-gui.md}。
 */
@Mixin(AbstractSelectionList.class)
public abstract class MixinSelectionListEntryCascade {

    @Unique
    private boolean otherworldinn$entryPushed;

    @Inject(method = "renderItem", at = @At("HEAD"))
    private void otherworldinn$pushEntryAppear(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            int index,
            int left,
            int top,
            int width,
            int height,
            CallbackInfo ci) {
        otherworldinn$entryPushed = false;
        if (!com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            return;
        }
        float appear = SmoothGuiAnimations.widgetAppear(top, SmoothGuiAnimations.now());
        if (appear >= 1.0F) {
            return;
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, (1.0F - appear) * 6.0F, 0.0F);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, Math.max(0.05F, appear));
        RenderSystem.enableBlend();
        otherworldinn$entryPushed = true;
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    private void otherworldinn$popEntryAppear(
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            int index,
            int left,
            int top,
            int width,
            int height,
            CallbackInfo ci) {
        if (!otherworldinn$entryPushed) {
            return;
        }
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().popPose();
        otherworldinn$entryPushed = false;
    }
}
