package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 创造模式物品栏的网格裁剪 + 悬停边界限制：绘制网格槽位（index &lt; 45）前开启裁剪，
 * 避免槽位缩放/级联/高亮溢出滚动可视区；离开网格槽位、绘制标签文字前、整帧结束时都关闭裁剪。
 * 同时重写 {@code isHovering(Slot, double, double)}，让滚出可视区的槽位无法被悬停。
 *
 * <p>网格可视区相对面板左上角为 (9, 18) → (171, 108)。
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinCreativeGridScissor {

    @Unique
    private static final int OTHERWORLDINN_GRID_LEFT = 9;

    @Unique
    private static final int OTHERWORLDINN_GRID_TOP = 18;

    @Unique
    private static final int OTHERWORLDINN_GRID_RIGHT = 171;

    @Unique
    private static final int OTHERWORLDINN_GRID_BOTTOM = 108;

    @Unique
    private static final int OTHERWORLDINN_CREATIVE_GRID_SLOT_COUNT = 45;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Unique
    private boolean otherworldinn$scissorActive;

    @Unique
    @Nullable
    private CreativeModeTab otherworldinn$selectedTab() {
        return MixinCreativeTabAccessor.otherworldinn$getSelectedTab();
    }

    @Unique
    private boolean otherworldinn$shouldSmoothGrid() {
        CreativeModeTab tab = otherworldinn$selectedTab();
        return tab != null && tab.hasSearchBar() && tab.getType() != CreativeModeTab.Type.INVENTORY;
    }

    @Unique
    private void otherworldinn$enableGridScissor(GuiGraphics guiGraphics) {
        // pose 原点已是面板左上角，故用面板局部坐标；加 leftPos/topPos 会让裁剪框偏出面板、把物品全裁掉
        PoseStack.Pose pose = guiGraphics.pose().last();
        Matrix4f matrix = pose.pose();
        Vector3f min = matrix.transformPosition(
                OTHERWORLDINN_GRID_LEFT, OTHERWORLDINN_GRID_TOP, 0.0F, new Vector3f());
        Vector3f max = matrix.transformPosition(
                OTHERWORLDINN_GRID_RIGHT, OTHERWORLDINN_GRID_BOTTOM, 0.0F, new Vector3f());
        int scissorLeft = Mth.floor(Math.min(min.x, max.x));
        int scissorTop = Mth.floor(Math.min(min.y, max.y));
        int scissorRight = Mth.ceil(Math.max(min.x, max.x));
        int scissorBottom = Mth.ceil(Math.max(min.y, max.y));
        if (scissorRight <= scissorLeft || scissorBottom <= scissorTop) {
            // 退化矩形：宁可不裁剪，也不能把画面裁没
            return;
        }
        guiGraphics.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom);
        otherworldinn$scissorActive = true;
    }

    @Unique
    private void otherworldinn$disableScissor(GuiGraphics guiGraphics) {
        if (otherworldinn$scissorActive) {
            guiGraphics.disableScissor();
            otherworldinn$scissorActive = false;
        }
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void otherworldinn$preRenderSlot(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (!com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            otherworldinn$disableScissor(guiGraphics);
            return;
        }
        if (!(((Object) this) instanceof CreativeModeInventoryScreen) || !otherworldinn$shouldSmoothGrid()) {
            otherworldinn$disableScissor(guiGraphics);
            return;
        }
        boolean isGridSlot = slot.index < OTHERWORLDINN_CREATIVE_GRID_SLOT_COUNT;
        if (isGridSlot && !otherworldinn$scissorActive) {
            otherworldinn$enableGridScissor(guiGraphics);
        } else if (!isGridSlot) {
            otherworldinn$disableScissor(guiGraphics);
        }
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void otherworldinn$closeScissorBeforeLabels(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        otherworldinn$disableScissor(guiGraphics);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void otherworldinn$closeScissorAtEnd(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        otherworldinn$disableScissor(guiGraphics);
    }

    /** 滚出可视区的槽位不应该还能被悬停。 */
    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$restrictHoverBounds(
            Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (!com.otherworldinn.foundation.ClientConfig.interfaceAnimations()) {
            return;
        }
        if (!(((Object) this) instanceof CreativeModeInventoryScreen)
                || slot.index >= OTHERWORLDINN_CREATIVE_GRID_SLOT_COUNT
                || !otherworldinn$shouldSmoothGrid()) {
            return;
        }
        int slotLeft = leftPos + slot.x;
        int slotTop = topPos + slot.y;
        int visibleLeft = Math.max(slotLeft, leftPos + OTHERWORLDINN_GRID_LEFT);
        int visibleTop = Math.max(slotTop, topPos + OTHERWORLDINN_GRID_TOP);
        int visibleRight = Math.min(slotLeft + 16, leftPos + OTHERWORLDINN_GRID_RIGHT);
        int visibleBottom = Math.min(slotTop + 16, topPos + OTHERWORLDINN_GRID_BOTTOM);
        boolean within = visibleLeft < visibleRight
                && visibleTop < visibleBottom
                && mouseX >= visibleLeft
                && mouseX < visibleRight
                && mouseY >= visibleTop
                && mouseY < visibleBottom;
        cir.setReturnValue(slot.isActive() && within);
    }
}
