package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 槽位 tooltip 弹出动效：手上没拿东西且悬停槽位有物品时，tooltip 从鼠标位置下方 3px、
 * 90% 尺寸在 0.12s 内平滑弹出。移植自 Subtle GUI Tweaks（MIT），署名见 {@code NOTICE-smooth-gui.md}。
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreenTooltipPop {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Unique
    private boolean otherworldinn$tooltipPushed;

    @Unique
    @Nullable
    private Slot otherworldinn$tooltipSlot;

    @Unique
    private long otherworldinn$tooltipAt;

    @Inject(method = "renderTooltip", at = @At("HEAD"))
    private void otherworldinn$pushTooltipPop(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        otherworldinn$tooltipPushed = false;
        if (!com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            return;
        }
        boolean emptyHand = menu().getCarried().isEmpty();
        Slot slot = emptyHand && hoveredSlot != null && hoveredSlot.hasItem() ? hoveredSlot : null;
        long now = SmoothGuiAnimations.now();
        if (slot != otherworldinn$tooltipSlot) {
            otherworldinn$tooltipSlot = slot;
            otherworldinn$tooltipAt = now;
        }
        if (slot == null) {
            return;
        }
        float t = SmoothGuiAnimations.ageSeconds(now, otherworldinn$tooltipAt) / 0.12F;
        if (t >= 1.0F) {
            return;
        }
        float eased = SmoothGuiAnimations.easeOutCubic(t);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, (1.0F - eased) * 3.0F, 0.0F);
        SmoothGuiAnimations.pushCenteredScale(guiGraphics, mouseX, mouseY, 0.9F + 0.1F * eased);
        otherworldinn$tooltipPushed = true;
    }

    @Inject(method = "renderTooltip", at = @At("RETURN"))
    private void otherworldinn$popTooltipPop(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (!otherworldinn$tooltipPushed) {
            return;
        }
        guiGraphics.pose().popPose();
        guiGraphics.pose().popPose();
        otherworldinn$tooltipPushed = false;
    }

    @Unique
    private net.minecraft.world.inventory.AbstractContainerMenu menu() {
        return ((AbstractContainerScreen<?>) (Object) this).getMenu();
    }
}
