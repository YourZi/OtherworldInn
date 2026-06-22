package com.wdiscute.starcatcher.compat;

import com.wdiscute.starcatcher.registry.fishrestrictions.SeasonRestriction;
import net.minecraft.world.level.Level;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

public class SereneSeasonsCompat
{

    public static SeasonRestriction.Seasons getSeason(Level level)
    {
        Season.SubSeason season = SeasonHelper.getSeasonState(level).getSubSeason();
        return switch (season)
        {
            case EARLY_SPRING -> SeasonRestriction.Seasons.EARLY_SPRING;
            case MID_SPRING -> SeasonRestriction.Seasons.MID_SPRING;
            case LATE_SPRING -> SeasonRestriction.Seasons.LATE_SPRING;

            case EARLY_SUMMER -> SeasonRestriction.Seasons.EARLY_SUMMER;
            case MID_SUMMER -> SeasonRestriction.Seasons.MID_SUMMER;
            case LATE_SUMMER -> SeasonRestriction.Seasons.LATE_SUMMER;

            case EARLY_AUTUMN -> SeasonRestriction.Seasons.EARLY_AUTUMN;
            case MID_AUTUMN -> SeasonRestriction.Seasons.MID_AUTUMN;
            case LATE_AUTUMN -> SeasonRestriction.Seasons.LATE_AUTUMN;

            case EARLY_WINTER -> SeasonRestriction.Seasons.EARLY_WINTER;
            case MID_WINTER -> SeasonRestriction.Seasons.MID_WINTER;
            default -> SeasonRestriction.Seasons.LATE_WINTER;
        };
    }
}
