package com.otherworldinn.util;

/**
 * 对话系统多样式字体工具类。
 *
 * <p>Minecraft 通过 §（U+00A7）格式码支持内联样式，可直接嵌入翻译文本（lang JSON）中。
 * 此处提供格式码常量和方法式快捷构造，方便在代码中拼接富文本。
 *
 * <h3>在 lang JSON 中使用（推荐）</h3>
 * 直接在翻译字符串中嵌入 § 格式码：
 * <pre>{@code
 * "dialogue.otherworldinn.blacksmith.node.root": "\u00a7l\u2026\u2026\u00a7r\n\u00a77打铁趁热，要点什么？"
 * }</pre>
 *
 * <h3>在 Java 代码中使用</h3>
 * <pre>{@code
 * String text = RichTextFormat.color("红色警告", RichTextFormat.RED)
 *     + RichTextFormat.bold("重要");
 * }</pre>
 *
 * <p>支持的格式码（可叠加）：</p>
 * <ul>
 *   <li>颜色：0-9 a-f（与 ChatFormatting 一致）</li>
 *   <li>样式：l=粗体, o=斜体, n=下划线, m=删除线, k=随机字符</li>
 *   <li>特殊：r=重置所有样式</li>
 * </ul>
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

    /** 给文本加颜色 */
    public static String color(String text, String colorCode) {
        return colorCode + text + RESET;
    }

    /** 粗体 */
    public static String bold(String text) {
        return BOLD + text + RESET;
    }

    /** 斜体 */
    public static String italic(String text) {
        return ITALIC + text + RESET;
    }

    /** 下划线 */
    public static String underline(String text) {
        return UNDERLINE + text + RESET;
    }

    /** 删除线 */
    public static String strikethrough(String text) {
        return STRIKETHROUGH + text + RESET;
    }

    /** 彩色 + 粗体 */
    public static String coloredBold(String text, String colorCode) {
        return colorCode + BOLD + text + RESET;
    }

    /** 给整个文本应用指定的样式码列表 */
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
