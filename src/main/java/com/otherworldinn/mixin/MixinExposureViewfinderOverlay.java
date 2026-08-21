package com.otherworldinn.mixin;

import com.otherworldinn.compat.exposure.client.PhotoTargetHintService;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在 Exposure 取景器 overlay 渲染尾部追加拍照任务目标提示。
 *
 * <p>Exposure 在 Gui.render 头部渲染取景器 overlay 并（按配置）取消原版 HUD 渲染，
 * NeoForge 的 RenderGuiEvent.Post 随之不触发，常规 HUD 事件钩子无法显示在取景器之上。
 * 借 overlay 自身的 GuiGraphics 在 TAIL 绘制，投影与层级天然正确。
 * Exposure 未安装时本 mixin 被 @Pseudo 静默跳过；PhotoTargetHintService 仅在此处
 * （即 Exposure 必然已加载时）被调用，无软依赖类加载风险。
 */
@Pseudo
@Mixin(
        targets = "io.github.mortuusars.exposure.client.camera.viewfinder.ViewfinderOverlay",
        remap = false)
public abstract class MixinExposureViewfinderOverlay {
    @Inject(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("TAIL"),
            remap = false)
    private void otherworldinn$renderPhotoTargetHint(
            GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        PhotoTargetHintService.renderHint(guiGraphics);
    }
}
