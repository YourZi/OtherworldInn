package com.otherworldinn.client.playtime;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

/**
 * 时长的格式化与绘制：{@code 72000 tick = 1 小时}、{@code 1200 tick = 1 分钟}，
 * 最大单位只到小时（超过一天也不换算成天），输出形如 {@code 3h30min} / {@code 12h} / {@code 45min}，
 * 颜色 {@code 0xFF808080}、不画阴影。
 */
public final class PlayTimeDisplay {

    private static final int COLOR = 0xFF808080;
    private static final int TICKS_PER_HOUR = 72000;
    private static final int TICKS_PER_MINUTE = 1200;

    private PlayTimeDisplay() {}

    @Nullable
    public static String format(int ticks) {
        if (ticks <= 0) {
            return null;
        }
        int totalMinutes = ticks / TICKS_PER_MINUTE;
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours <= 0) {
            return Math.max(1, minutes) + "min";
        }
        if (minutes <= 0) {
            return hours + "h";
        }
        return hours + "h" + minutes + "min";
    }

    public static int width(String text) {
        return Minecraft.getInstance().font.width(text);
    }

    public static void draw(GuiGraphics guiGraphics, String text, int x, int y) {
        guiGraphics.drawString(Minecraft.getInstance().font, text, x, y, COLOR, false);
    }
}
