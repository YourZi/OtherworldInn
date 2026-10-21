package com.otherworldinn.mixin;

import com.otherworldinn.client.playtime.PlayTimeDisplay;
import com.otherworldinn.client.playtime.WorldPlayTimeCache;
import com.otherworldinn.foundation.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在存档选择列表的条目右上角显示累计游玩时长：注入 {@code WorldListEntry.render} 的 TAIL，
 * 绘制于 {@code x + width - textWidth - 4, y + 1}。
 */
@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class MixinWorldListEntryPlayTime {

    @Shadow
    @Final
    private LevelSummary summary;

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void otherworldinn$renderPlayTime(
            GuiGraphics guiGraphics,
            int index,
            int y,
            int x,
            int width,
            int height,
            int mouseX,
            int mouseY,
            boolean hovered,
            float partialTick,
            CallbackInfo ci) {
        if (!ClientConfig.showWorldPlayTime()) {
            return;
        }
        String text = playTime(WorldPlayTimeCache.get(this.summary.getLevelId()));
        if (text == null) {
            return;
        }
        PlayTimeDisplay.draw(
                guiGraphics, text, x + width - PlayTimeDisplay.width(text) - 4, y + 1);
    }

    @Nullable
    private static String playTime(int ticks) {
        return PlayTimeDisplay.format(ticks);
    }
}
