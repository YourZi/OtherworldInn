package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 槽位 hover 高亮的平滑淡入/淡出 + 呼吸脉冲：取消原版 {@code renderSlotHighlight}，
 * 改由 {@code render} 的 TAIL 按 {@code hoveredSlot} 自绘（指数淡入，alpha 低于 0.01 才清除）。
 *
 * <p>填充 alpha 48、描边 alpha 108，都乘以 0.92~1.0 的正弦呼吸系数；
 * 关闭界面动效时直接放行原版实现。
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreenSlotHighlight {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Unique
    @Nullable
    private Slot otherworldinn$highlightSlot;

    @Unique
    private float otherworldinn$highlightAlpha;

    @Unique
    private long otherworldinn$lastHighlightTime;

    /** 接管原版高亮绘制：它会在 hoveredSlot 上被调用，这里取消掉，改由 render 的 TAIL 统一画。 */
    @Inject(
            method = "renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/inventory/Slot;IIF)V",
            at = @At("HEAD"),
            cancellable = true)
    private void otherworldinn$cancelVanillaHighlight(
            GuiGraphics guiGraphics, Slot slot, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        // 关闭界面动效时必须保留原版高亮，否则槽位会完全没有高亮反馈
        if (!com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            return;
        }
        ci.cancel();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void otherworldinn$renderSmoothHighlight(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            return;
        }
        Slot currentHoveredSlot = hoveredSlot;
        if (currentHoveredSlot != null && currentHoveredSlot != otherworldinn$highlightSlot) {
            otherworldinn$highlightSlot = currentHoveredSlot;
            otherworldinn$highlightAlpha = 0.0F;
        }

        long time = SmoothGuiAnimations.now();
        float dt = SmoothGuiAnimations.deltaSeconds(time, otherworldinn$lastHighlightTime);
        otherworldinn$lastHighlightTime = time;

        float target = currentHoveredSlot != null ? 1.0F : 0.0F;
        otherworldinn$highlightAlpha =
                SmoothGuiAnimations.expLerp(15.0F, otherworldinn$highlightAlpha, target, dt);

        if (currentHoveredSlot == null && otherworldinn$highlightAlpha < 0.01F) {
            otherworldinn$highlightSlot = null;
            return;
        }
        Slot slot = otherworldinn$highlightSlot;
        if (slot == null || otherworldinn$highlightAlpha <= 0.01F) {
            return;
        }

        int x = leftPos + slot.x;
        int y = topPos + slot.y;
        float pulse = 0.92F + 0.08F * (0.5F + 0.5F * Mth.sin((float) (Util.getMillis() % 314159L) * 0.02F));
        int fillAlpha = Mth.clamp((int) (otherworldinn$highlightAlpha * 48.0F * pulse), 0, 255);
        int borderAlpha = Mth.clamp((int) (otherworldinn$highlightAlpha * 108.0F * pulse), 0, 255);
        int fillColor = fillAlpha << 24 | 0xFFFFFF;
        int borderColor = borderAlpha << 24 | 0xFFFFFF;

        guiGraphics.fill(x, y, x + 16, y + 16, fillColor);
        guiGraphics.fill(x, y, x + 16, y + 1, borderColor);
        guiGraphics.fill(x, y + 16 - 1, x + 16, y + 16, borderColor);
        guiGraphics.fill(x, y, x + 1, y + 16, borderColor);
        guiGraphics.fill(x + 16 - 1, y, x + 16, y + 16, borderColor);
    }
}
