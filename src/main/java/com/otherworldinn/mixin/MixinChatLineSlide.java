package com.otherworldinn.mixin;

import com.otherworldinn.client.gui.SmoothGuiAnimations;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 聊天行入场滑入：新出现的聊天行在 250ms 内从左侧 12px 滑到原位（{@code easeOutCubic}）。
 *
 * <p>{@code render} 的 HEAD 清空本帧记录；逐行读取 {@code Line.addedTime()} 时先弹掉上一行变换、
 * 再按该行年龄压入平移，使背景与文字整行一起滑动；{@code render} 的 RETURN 兜底弹栈并剔除已滚出的行。
 *
 * <p>{@code @Unique} 字段的内联初始化器在此不生效，容器一律在注入点内惰性初始化、用前判空。
 */
@Mixin(ChatComponent.class)
public abstract class MixinChatLineSlide {

    @Unique
    private static final long OTHERWORLDINN_LINE_APPEAR_MS = 250L;

    @Unique
    private static final float OTHERWORLDINN_LINE_SLIDE = 12.0F;

    @Unique
    private Map<GuiMessage.Line, Long> otherworldinn$firstSeen;

    @Unique
    private Set<GuiMessage.Line> otherworldinn$seenThisFrame;

    @Unique
    private GuiGraphics otherworldinn$graphics;

    @Unique
    private boolean otherworldinn$linePushed;

    @Inject(method = "render", at = @At("HEAD"))
    private void otherworldinn$beginChatRender(
            GuiGraphics guiGraphics, int ticks, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        otherworldinn$graphics = guiGraphics;
        otherworldinn$linePushed = false;
        if (otherworldinn$seenThisFrame == null) {
            otherworldinn$seenThisFrame =
                    Collections.newSetFromMap(new IdentityHashMap<GuiMessage.Line, Boolean>());
        } else {
            otherworldinn$seenThisFrame.clear();
        }
    }

    /** 每行读取时间戳时处理"上一行弹栈 + 本行按年龄压栈"。 */
    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GuiMessage$Line;addedTime()I"),
            require = 0)
    private int otherworldinn$slideForLine(GuiMessage.Line line) {
        if (!com.otherworldinn.foundation.ClientConfig.interfaceAnimations()
                || otherworldinn$graphics == null
                || line == null) {
            return line == null ? 0 : line.addedTime();
        }
        if (otherworldinn$firstSeen == null) {
            otherworldinn$firstSeen = new IdentityHashMap<>();
        }
        if (otherworldinn$seenThisFrame == null) {
            otherworldinn$seenThisFrame =
                    Collections.newSetFromMap(new IdentityHashMap<GuiMessage.Line, Boolean>());
        }
        otherworldinn$seenThisFrame.add(line);

        long nowMs = SmoothGuiAnimations.now() / 1_000_000L;
        Long firstSeen = otherworldinn$firstSeen.get(line);
        if (firstSeen == null) {
            firstSeen = nowMs;
            otherworldinn$firstSeen.put(line, nowMs);
        }
        float appear = 1.0F;
        long elapsed = nowMs - firstSeen;
        if (elapsed < OTHERWORLDINN_LINE_APPEAR_MS) {
            appear = SmoothGuiAnimations.easeOutCubic((float) elapsed / OTHERWORLDINN_LINE_APPEAR_MS);
        }

        if (otherworldinn$linePushed) {
            otherworldinn$graphics.pose().popPose();
            otherworldinn$linePushed = false;
        }
        if (appear < 1.0F) {
            otherworldinn$graphics.pose().pushPose();
            otherworldinn$graphics.pose().translate((appear - 1.0F) * OTHERWORLDINN_LINE_SLIDE, 0.0F, 0.0F);
            otherworldinn$linePushed = true;
        }
        return line.addedTime();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void otherworldinn$endChatRender(
            GuiGraphics guiGraphics, int ticks, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        if (otherworldinn$linePushed) {
            guiGraphics.pose().popPose();
            otherworldinn$linePushed = false;
        }
        otherworldinn$graphics = null;
        if (otherworldinn$firstSeen != null && otherworldinn$seenThisFrame != null) {
            // 只保留本帧仍在显示的行，滚出视线的记录随之丢弃
            otherworldinn$firstSeen.keySet().retainAll(otherworldinn$seenThisFrame);
        }
    }
}
