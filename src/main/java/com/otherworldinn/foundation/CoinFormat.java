package com.otherworldinn.foundation;

import net.minecraft.network.chat.Component;

/**
 * 队伍金币的显示格式化与溢出保护：单位切换点取本单位的 10 倍，带单位时保留 4 位有效数字，
 * 小数位一律截断（永不四舍五入）：{@code < 10000} 原样；{@code 1e4 ~ 1e7} 用 k（999999 → 999.9k）；
 * {@code >= 1e7} 用 M 且不再升单位（2147483647 → 2147M）。
 */
public final class CoinFormat {

    /** 金币硬上限（int 可表示的最大值）。达到后不再增加。 */
    public static final int MAX_COINS = Integer.MAX_VALUE;

    public static final int K_THRESHOLD = 10_000;

    public static final int M_THRESHOLD = 10_000_000;

    public static final int SIGNIFICANT_DIGITS = 4;

    private CoinFormat() {}

    public static String format(long coins) {
        if (coins <= 0L) {
            return "0";
        }
        if (coins < K_THRESHOLD) {
            return Long.toString(coins);
        }
        if (coins < M_THRESHOLD) {
            return scale(coins, 1_000L, "k");
        }
        return scale(coins, 1_000_000L, "M");
    }

    public static String format(int coins) {
        return format((long) coins);
    }

    public static Component display(int coins) {
        return Component.literal(format(coins));
    }

    /** 整数运算缩放 + 截断，避免浮点误差；小数位数 = 4 - 整数位数。 */
    private static String scale(long coins, long unit, String suffix) {
        long whole = coins / unit;
        int integerDigits = whole < 10L ? 1 : whole < 100L ? 2 : whole < 1000L ? 3 : 4;
        int decimals = Math.max(0, SIGNIFICANT_DIGITS - integerDigits);
        if (decimals == 0) {
            return whole + suffix;
        }
        long factor = 1L;
        for (int i = 0; i < decimals; i++) {
            factor *= 10L;
        }
        long scaledTimes = coins * factor / unit;
        String frac = Long.toString(scaledTimes % factor);
        StringBuilder padded = new StringBuilder();
        for (int i = frac.length(); i < decimals; i++) {
            padded.append('0');
        }
        return (scaledTimes / factor) + "." + padded + frac + suffix;
    }

    /** 饱和加法：超过 {@link #MAX_COINS} 时停在上限，为负时夹到 0。 */
    public static int saturatingAdd(int current, int delta) {
        long result = (long) current + (long) delta;
        if (result > MAX_COINS) {
            return MAX_COINS;
        }
        if (result < 0L) {
            return 0;
        }
        return (int) result;
    }

    public static boolean isAtCap(int coins) {
        return coins >= MAX_COINS;
    }
}
