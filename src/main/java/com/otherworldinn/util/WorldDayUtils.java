package com.otherworldinn.util;

import net.minecraft.world.level.Level;

/**
 * 世界日期工具：每日逻辑统一基于 DayTime（受睡眠跳时、/time set 影响），
 * 保证餐饮计划、商店补货、游商周期等使用同一时间源。
 */
public final class WorldDayUtils {

    /** 每个 Minecraft 日的刻数 */
    public static final long TICKS_PER_DAY = 24000L;

    private WorldDayUtils() {}

    /** 由 DayTime 得到当前是第几天（从 0 开始） */
    public static long currentDay(long dayTime) {
        return dayTime / TICKS_PER_DAY;
    }

    /** 当前世界日（从 0 开始） */
    public static long currentDay(Level level) {
        return currentDay(level.getDayTime());
    }

    /** 某日零时（该日第一刻的 DayTime） */
    public static long dayStart(long day) {
        return day * TICKS_PER_DAY;
    }

    /** 下一日零时 */
    public static long nextDayStart(long day) {
        return dayStart(day + 1);
    }

    /** 天数换算为刻数 */
    public static long daysToTicks(long days) {
        return days * TICKS_PER_DAY;
    }

    /** 给定 DayTime 对齐到下一个日出时刻（vanilla 睡眠跳时使用的对齐值） */
    public static long nextSunrise(long dayTime) {
        return nextDayStart(currentDay(dayTime));
    }
}
