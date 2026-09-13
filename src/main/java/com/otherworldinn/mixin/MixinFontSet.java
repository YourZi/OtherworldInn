package com.otherworldinn.mixin;

import com.mojang.blaze3d.font.GlyphInfo;
import com.otherworldinn.client.util.OwBoldOffset;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 在 {@link FontSet#getGlyphInfo} 返回字形信息时套一层包装：
 * 仅当“旅社物语”资源包启用且配置值偏离原版时，让加粗偏移改用 {@link OwBoldOffset#configuredOffset}。
 */
@Mixin(FontSet.class)
public abstract class MixinFontSet {

    @Inject(method = "getGlyphInfo", at = @At("RETURN"), cancellable = true)
    private void ow$wrapGlyphInfo(int character, boolean filterFishyGlyphs, CallbackInfoReturnable<GlyphInfo> cir) {
        cir.setReturnValue(OwBoldOffset.wrapIfNeeded(cir.getReturnValue()));
    }
}
