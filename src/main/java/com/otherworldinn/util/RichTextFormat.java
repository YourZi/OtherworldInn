package com.otherworldinn.util;

/**
 * 对话系统富文本工具：基于 §（U+00A7）格式码的常量与快捷构造，
 * 可直接嵌入翻译文本（lang JSON）。格式码可叠加，r 重置所有样式。
 *
 * @see net.minecraft.ChatFormatting
 */
public final class RichTextFormat {

    public static final char SECTION_SIGN = '\u00a7';

    // ── 颜色码 ──────────────────────────────────

    public static final String BLACK = "\u00a70";
    public static final String DARK_BLUE = "\u00a71";
    public static final String DARK_GREEN = "\u00a72";
    public static final String DARK_AQUA = "\u00a73";
    public static final String DARK_RED = "\u00a74";
    public static final String DARK_PURPLE = "\u00a75";
    public static final String GOLD = "\u00a76";
    public static final String GRAY = "\u00a77";
    public static final String DARK_GRAY = "\u00a78";
    public static final String BLUE = "\u00a79";
    public static final String GREEN = "\u00a7a";
    public static final String AQUA = "\u00a7b";
    public static final String RED = "\u00a7c";
    public static final String LIGHT_PURPLE = "\u00a7d";
    public static final String YELLOW = "\u00a7e";
    public static final String WHITE = "\u00a7f";

    // ── 样式码 ──────────────────────────────────

    public static final String OBFUSCATED = "\u00a7k";
    public static final String BOLD = "\u00a7l";
    public static final String STRIKETHROUGH = "\u00a7m";
    public static final String UNDERLINE = "\u00a7n";
    public static final String ITALIC = "\u00a7o";
    public static final String RESET = "\u00a7r";

    // ── 快捷方法 ────────────────────────────────

    public static String color(String text, String colorCode) {
        return colorCode + text + RESET;
    }

    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    public static String italic(String text) {
        return ITALIC + text + RESET;
    }

    public static String underline(String text) {
        return UNDERLINE + text + RESET;
    }

    public static String strikethrough(String text) {
        return STRIKETHROUGH + text + RESET;
    }

    public static String coloredBold(String text, String colorCode) {
        return colorCode + BOLD + text + RESET;
    }

    public static String styled(String text, String... codes) {
        StringBuilder sb = new StringBuilder();
        for (String code : codes) {
            sb.append(code);
        }
        sb.append(text);
        sb.append(RESET);
        return sb.toString();
    }

    private RichTextFormat() {
        throw new UnsupportedOperationException("utility class");
    }
}
