package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 槽位交互手感：悬停槽位里的物品平滑放大到 1.12，点击时按 {@code punch} 曲线弹一下（0.3s），
 * 输出槽新出现/数量增加时轻微弹出，界面打开时按槽位坐标错峰做入场级联，
 * 手持物品按鼠标横向移动速度倾斜（±16°，指数平滑）并在刚拿起时弹一下（0.32s）。
 *
 * <p>{@code renderSlot} 阶段 pose 原点已是面板左上角，所以缩放中心用槽位局部坐标 {@code slot.x/y + 8}。
 * 全部效果受 {@code ClientConfig.itemAnimations()} 控制。
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreenSlotPop {

    @Unique
    private static final float OTHERWORLDINN_HOVER_SCALE = 1.12F;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int topPos;

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Unique
    private Map<Slot, Float> otherworldinn$slotScales;

    @Unique
    private long otherworldinn$lastFrameTime;

    @Unique
    private float otherworldinn$frameDt;

    @Unique
    private boolean otherworldinn$slotTransformPushed;

    @Unique
    @Nullable
    private Slot otherworldinn$previousHoveredSlot;

    @Unique
    @Nullable
    private Slot otherworldinn$clickedSlot;

    @Unique
    private long otherworldinn$clickedAt;

    @Unique
    private ItemStack otherworldinn$lastCarried = ItemStack.EMPTY;

    @Unique
    private long otherworldinn$carriedAt;

    @Unique
    private int otherworldinn$lastCarriedX = Integer.MIN_VALUE;

    @Unique
    private float otherworldinn$carriedTilt;

    @Unique
    private boolean otherworldinn$carriedTransformPushed;

    @Unique
    private ItemStack otherworldinn$hoverMatch = ItemStack.EMPTY;

    @Unique
    private long otherworldinn$hoverMatchAt;

    @Unique
    private net.minecraft.world.item.Item @Nullable [] otherworldinn$outputItems;

    @Unique
    private int[] otherworldinn$outputCounts = new int[0];

    @Unique
    private long[] otherworldinn$outputPopAt = new long[0];

    @Unique
    private float[] otherworldinn$outputPopStrength = new float[0];

    /** 每帧刚开始：刷新帧时长、上一帧的悬停槽位、手持物品变化检测。 */
    @Inject(method = "render", at = @At("HEAD"))
    private void otherworldinn$captureFrame(
            GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!com.otherworldinn.foundation.ClientConfig.itemAnimations()) {
            return;
        }
        long now = SmoothGuiAnimations.now();
        otherworldinn$frameDt = SmoothGuiAnimations.deltaSeconds(now, otherworldinn$lastFrameTime);
        otherworldinn$lastFrameTime = now;
        otherworldinn$previousHoveredSlot = hoveredSlot;

        ItemStack carried = menu() == null ? ItemStack.EMPTY : menu().getCarried();
        if (!ItemStack.matches(carried, otherworldinn$lastCarried)) {
            if (!carried.isEmpty()) {
                otherworldinn$carriedAt = now;
            }
            otherworldinn$lastCarried = carried;
        }
        if (carried.isEmpty()) {
            otherworldinn$lastCarriedX = Integer.MIN_VALUE;
            otherworldinn$carriedTilt = 0.0F;
        }

        // 空手悬停时记录的物品，供同物品匹配抖动使用
        ItemStack hovered = carried.isEmpty() && hoveredSlot != null ? hoveredSlot.getItem() : ItemStack.EMPTY;
        if (!ItemStack.isSameItemSameComponents(hovered, otherworldinn$hoverMatch)) {
            otherworldinn$hoverMatchAt = now;
        }
        otherworldinn$hoverMatch = hovered;
        otherworldinn$detectOutputChanges(now);
    }

    /**
     * 输出槽变化检测：槽位"新出现"或"数量变多"且不接受该物品（即不是输入槽）时记一次弹出。
     */
    @Unique
    private void otherworldinn$detectOutputChanges(long now) {
        if (((Object) this) instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen) {
            return;
        }
        net.minecraft.world.inventory.AbstractContainerMenu containerMenu = menu();
        if (containerMenu == null) {
            return;
        }
        java.util.List<Slot> slots = containerMenu.slots;
        int size = slots.size();
        boolean fresh = otherworldinn$outputItems == null || otherworldinn$outputItems.length != size;
        if (fresh) {
            otherworldinn$outputItems = new net.minecraft.world.item.Item[size];
            otherworldinn$outputCounts = new int[size];
            otherworldinn$outputPopAt = new long[size];
            otherworldinn$outputPopStrength = new float[size];
        }
        for (int i = 0; i < size; i++) {
            Slot slot = slots.get(i);
            ItemStack stack = slot.getItem();
            net.minecraft.world.item.Item item = stack.isEmpty() ? null : stack.getItem();
            int count = stack.getCount();
            if (!fresh && item != null && slot.isActive()) {
                boolean appeared = item != otherworldinn$outputItems[i];
                boolean grew = !appeared && count > otherworldinn$outputCounts[i];
                if ((appeared || grew) && !slot.mayPlace(stack)) {
                    otherworldinn$outputPopAt[i] = now;
                    otherworldinn$outputPopStrength[i] = appeared ? 0.3F : 0.15F;
                }
            }
            otherworldinn$outputItems[i] = item;
            otherworldinn$outputCounts[i] = count;
        }
    }

    /** 同物品匹配抖动：手持物（或刚悬停的物品）与槽位物品相同时，槽位轻微摆一下（±8°，0.4s 一次）。 */
    @Unique
    private float otherworldinn$matchingWiggle(Slot slot) {
        ItemStack carried = menu() == null ? ItemStack.EMPTY : menu().getCarried();
        boolean carrying = !carried.isEmpty();
        ItemStack reference = carrying ? carried : otherworldinn$hoverMatch;
        if (reference.isEmpty()
                || slot == otherworldinn$previousHoveredSlot
                || !ItemStack.isSameItemSameComponents(slot.getItem(), reference)) {
            return 0.0F;
        }
        long start = carrying ? otherworldinn$carriedAt : otherworldinn$hoverMatchAt;
        float delay = carrying ? 0.2F : 0.5F;
        float since = SmoothGuiAnimations.ageSeconds(SmoothGuiAnimations.now(), start)
                - delay
                - (slot.x + slot.y) * 0.0012F;
        if (since <= 0.0F) {
            return 0.0F;
        }
        float cycle = since % 1.6F;
        if (cycle >= 0.4F) {
            return 0.0F;
        }
        float p = cycle / 0.4F;
        return Mth.sin(p * (float) Math.PI * 3.0F) * (1.0F - p) * 8.0F;
    }

    @Unique
    @Nullable
    private net.minecraft.world.inventory.AbstractContainerMenu menu() {
        return ((AbstractContainerScreen<?>) (Object) this).getMenu();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void otherworldinn$rememberClickedSlot(
            double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (hoveredSlot != null) {
            otherworldinn$clickedSlot = hoveredSlot;
            otherworldinn$clickedAt = SmoothGuiAnimations.now();
        }
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void otherworldinn$pushSlotPop(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        otherworldinn$slotTransformPushed = false;
        if (!com.otherworldinn.foundation.ClientConfig.itemAnimations()) {
            return;
        }
        float scale = otherworldinn$hoverScale(slot);
        if (slot == otherworldinn$clickedSlot) {
            float t = SmoothGuiAnimations.ageSeconds(SmoothGuiAnimations.now(), otherworldinn$clickedAt) / 0.3F;
            if (t >= 1.0F) {
                otherworldinn$clickedSlot = null;
            } else {
                scale *= 1.0F + 0.22F * SmoothGuiAnimations.punch(t);
            }
        }
        int slotIndex = slot.index;
        if (slotIndex >= 0 && slotIndex < otherworldinn$outputPopAt.length) {
            float outputT = SmoothGuiAnimations.ageSeconds(SmoothGuiAnimations.now(), otherworldinn$outputPopAt[slotIndex]) / 0.32F;
            if (outputT < 1.0F) {
                scale *= 1.0F + otherworldinn$outputPopStrength[slotIndex] * SmoothGuiAnimations.punch(outputT);
            }
        }
        if (slot.hasItem()) {
            // 界面打开后的槽位入场级联：按槽位坐标错峰，easeOutBack 从 0 弹到 1
            scale *= SmoothGuiAnimations.slotCascade(slot.x, slot.y, SmoothGuiAnimations.now());
        }
        float angle = slot.hasItem() ? otherworldinn$matchingWiggle(slot) : 0.0F;
        if (Math.abs(scale - 1.0F) <= 0.001F && Math.abs(angle) < 0.05F) {
            return;
        }
        // pose 原点已是面板左上角，故用槽位局部坐标；加上 leftPos/topPos 会让缩放中心偏出去
        SmoothGuiAnimations.pushCenteredTransform(
                guiGraphics, slot.x + 8.0F, slot.y + 8.0F, scale, angle);
        otherworldinn$slotTransformPushed = true;
    }

    @Inject(method = "renderSlot", at = @At("RETURN"))
    private void otherworldinn$popSlotPop(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (otherworldinn$slotTransformPushed) {
            guiGraphics.pose().popPose();
            otherworldinn$slotTransformPushed = false;
        }
    }

    @Unique
    private float otherworldinn$hoverScale(Slot slot) {
        if (otherworldinn$slotScales == null) {
            otherworldinn$slotScales = new IdentityHashMap<>();
        }
        boolean hovered = slot == otherworldinn$previousHoveredSlot && slot.hasItem();
        Float existing = otherworldinn$slotScales.get(slot);
        if (existing == null && !hovered) {
            return 1.0F;
        }
        float current = SmoothGuiAnimations.expLerp(
                20.0F, existing != null ? existing : 1.0F, hovered ? OTHERWORLDINN_HOVER_SCALE : 1.0F, otherworldinn$frameDt);
        if (!hovered && Math.abs(current - 1.0F) < 0.001F) {
            otherworldinn$slotScales.remove(slot);
            return 1.0F;
        }
        otherworldinn$slotScales.put(slot, current);
        return current;
    }

    /** 手持物品：按鼠标横向移动速度倾斜，并在刚拿起时弹一下。 */
    @Inject(method = "renderFloatingItem", at = @At("HEAD"))
    private void otherworldinn$pushCarriedMotion(
            GuiGraphics guiGraphics, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        otherworldinn$carriedTransformPushed = false;
        if (!com.otherworldinn.foundation.ClientConfig.itemAnimations()) {
            return;
        }
        float dt = otherworldinn$frameDt;
        float targetTilt = 0.0F;
        if (otherworldinn$lastCarriedX != Integer.MIN_VALUE && dt > 0.0F) {
            targetTilt = Mth.clamp((x - otherworldinn$lastCarriedX) / dt * 0.02F, -16.0F, 16.0F);
        }
        otherworldinn$lastCarriedX = x;
        otherworldinn$carriedTilt =
                SmoothGuiAnimations.expLerp(12.0F, otherworldinn$carriedTilt, targetTilt, dt);
        float t = SmoothGuiAnimations.ageSeconds(SmoothGuiAnimations.now(), otherworldinn$carriedAt) / 0.32F;
        float scale = 1.0F + 0.25F * SmoothGuiAnimations.punch(t);
        if (Math.abs(otherworldinn$carriedTilt) < 0.05F && Math.abs(scale - 1.0F) <= 0.001F) {
            return;
        }
        SmoothGuiAnimations.pushCenteredTransform(
                guiGraphics, x + 8.0F, y + 8.0F, scale, otherworldinn$carriedTilt);
        otherworldinn$carriedTransformPushed = true;
    }

    @Inject(method = "renderFloatingItem", at = @At("RETURN"))
    private void otherworldinn$popCarriedMotion(
            GuiGraphics guiGraphics, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        if (otherworldinn$carriedTransformPushed) {
            guiGraphics.pose().popPose();
            otherworldinn$carriedTransformPushed = false;
        }
    }
}
