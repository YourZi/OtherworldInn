package com.otherworldinn.client.util;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 控制《旅社物语》资源包启用时的加粗字形“二次绘制”偏移量。
 *
 * <p>原版把粗体文本近似为每个字形画两遍、第二遍右移 1px（{@link GlyphInfo#getBoldOffset()}），
 * 当使用自定义宽体字体时会在笔画间出现缝隙/重影。这里在 {@code FontSet#getGlyphInfo} 的返回值上
 * 做一层包装：只有“旅社物语”资源包被选中时，才让偏移使用下面的配置值；其余情况原样回退到原版 1.0。
 */
@OnlyIn(Dist.CLIENT)
public final class OwBoldOffset {

    /** 该资源包经 {@code AddPackFindersEvent#addPackFinders} 注册后的实际 pack id。 */
    public static final String OW_RESOURCE_PACK_ID = "mod/otherworldinn:resourcepacks/otherworldinn";

    /**
     * 资源包启用时使用的加粗偏移（像素）。
     * 默认 1.0F = 原版行为；需要消除缝隙/重影时可改为 0.0F，或调到需要的数值后自行微调。
     */
    public static float configuredOffset = 0.5F;

    private OwBoldOffset() {
    }

    /**
     * 仅在“已启用资源包”且“配置值偏离原版”时才包装，避免默认状态下的任何开销与行为差异。
     */
    public static GlyphInfo wrapIfNeeded(GlyphInfo glyph) {
        if (glyph == null || configuredOffset == 1.0F || !isOwPackSelected()) {
            return glyph;
        }
        return new AdjustedGlyphInfo(glyph);
    }

    private static boolean isOwPackSelected() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return false;
        }
        PackRepository packRepository = minecraft.getResourcePackRepository();
        if (packRepository == null) {
            return false;
        }
        for (Pack pack : packRepository.getSelectedPacks()) {
            if (OW_RESOURCE_PACK_ID.equals(pack.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 覆盖原版默认 {@code getBoldOffset()} 的委托包装。
     * 加粗时的额外布局宽度来自 {@link GlyphInfo#getAdvance(boolean)}（默认实现会读回本方法），
     * 因此只重写这一处即可让绘制间距与宽度测量保持一致。
     */
    private record AdjustedGlyphInfo(GlyphInfo delegate) implements GlyphInfo {

        @Override
        public float getAdvance() {
            return delegate.getAdvance();
        }

        @Override
        public float getBoldOffset() {
            return configuredOffset;
        }

        @Override
        public float getShadowOffset() {
            return delegate.getShadowOffset();
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> glyphProvider) {
            return delegate.bake(glyphProvider);
        }
    }
}
