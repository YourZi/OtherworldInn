package com.otherworldinn.world.festival;

import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;

/**
 * 节日触发窗口
 *
 * <p>季节绑定：指定主季节（春/夏/秋/冬）内的日期区间 [startDayInSeason, endDayInSeason)，
 * 每年同季同期重复触发。窗口为纯时间函数，可重入计算。
 */
public record FestivalTrigger(Season season, int startDayInSeason, int endDayInSeason) {

    public FestivalTrigger {
        if (season == null) {
            throw new IllegalArgumentException("Festival season cannot be null.");
        }
        if (startDayInSeason < 0 || endDayInSeason <= startDayInSeason) {
            throw new IllegalArgumentException(
                    "Festival window must satisfy 0 <= startDayInSeason < endDayInSeason.");
        }
    }

    /** 当前季节状态是否落在窗口内 */
    public boolean isActive(ISeasonState state) {
        if (state.getSeason() != season) {
            return false;
        }
        int dayInSeason = state.getDay() % daysPerSeason(state);
        return dayInSeason >= startDayInSeason && dayInSeason < endDayInSeason;
    }

    /** 本季窗口起始的绝对世界日，用于跨年区分事件（每年同窗口起始日不同） */
    public long windowStartDay(ISeasonState state, long currentDay) {
        int dayInSeason = state.getDay() % daysPerSeason(state);
        return currentDay - dayInSeason + startDayInSeason;
    }

    /** 当前日期在窗口内的第几天（0 起） */
    public int dayIndex(ISeasonState state) {
        return state.getDay() % daysPerSeason(state) - startDayInSeason;
    }

    /** 一个主季节的天数（= 3 子季节，动态取自季节配置） */
    public static int daysPerSeason(ISeasonState state) {
        int dayDuration = state.getDayDuration();
        return dayDuration <= 0 ? 1 : state.getSeasonDuration() / dayDuration;
    }
}
