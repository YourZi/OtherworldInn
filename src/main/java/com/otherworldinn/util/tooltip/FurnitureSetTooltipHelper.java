package com.otherworldinn.util.tooltip;

import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.util.ClientServices;
import com.otherworldinn.world.inn.service.RoomThemeManager;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Block;

public final class FurnitureSetTooltipHelper {
    private FurnitureSetTooltipHelper() {}

    public static void appendSetTooltip(Block block, List<Component> tooltip, boolean showDetails) {
        RoomThemeManager.ThemeMembership membership = RoomThemeManager.findThemeMembership(block);
        if (membership == null) {
            return;
        }

        if (!showDetails) {
            tooltip.add(
                    Component.translatable("tooltip.otherworldinn.furniture.set.hold_shift")
                            .withStyle(style -> style.withColor(ModColors.INFO)));
            return;
        }

        RoomThemeManager.RoomTheme theme = membership.theme();
        tooltip.add(
                Component.translatable("tooltip.otherworldinn.furniture.set.name")
                        .withStyle(style -> style.withColor(ModColors.FURNITURE_SET_HEADER)));
        tooltip.add(
                createIndentedLine(Component.literal(getLocalizedThemeName(theme)))
                        .withStyle(style -> style.withColor(getThemeNameColor(theme.id()))));
        tooltip.add(
                Component.translatable("tooltip.otherworldinn.furniture.set.part")
                        .withStyle(style -> style.withColor(ModColors.FURNITURE_SET_HEADER)));
        tooltip.add(createIndentedLine(getPartComponent(membership.part()))
                .withStyle(style -> style.withColor(ModColors.GRAY_LIGHT)));
        tooltip.add(
                Component.translatable("tooltip.otherworldinn.furniture.set.effect")
                        .withStyle(style -> style.withColor(ModColors.FURNITURE_SET_HEADER)));
        appendEffectLines(tooltip, theme.statModifiers());
    }

    private static String getLocalizedThemeName(RoomThemeManager.RoomTheme theme) {
        return isChineseLocale() ? theme.zhName() : theme.enName();
    }

    private static boolean isChineseLocale() {
        return ClientServices.isChineseLocale();
    }

    private static Component getPartComponent(RoomThemeManager.ThemePart part) {
        return switch (part) {
            case SHELL -> Component.translatable("tooltip.otherworldinn.furniture.set.part.shell");
            case INTERIOR -> Component.translatable("tooltip.otherworldinn.furniture.set.part.interior");
            case BOTH -> Component.translatable("tooltip.otherworldinn.furniture.set.part.both");
        };
    }

    private static int getThemeNameColor(String themeId) {
        if (themeId == null) {
            return ModColors.INFO;
        }
        return switch (themeId) {
            case "ocean" -> ModColors.FURNITURE_SET_OCEAN;
            case "nether" -> ModColors.FURNITURE_SET_NETHER;
            case "end" -> ModColors.FURNITURE_SET_END;
            default -> ModColors.INFO;
        };
    }

    private static void appendEffectLines(
            List<Component> tooltip, RoomThemeManager.StatModifiers modifiers) {
        if (modifiers.comfort() != 0) {
            tooltip.add(createIndentedLine(
                    Component.translatable(
                                    "tooltip.otherworldinn.furniture.comfort",
                                    formatSigned(modifiers.comfort()))
                            .withStyle(style -> style.withColor(ModColors.COMFORT))));
        }
        if (modifiers.light() != 0) {
            tooltip.add(createIndentedLine(
                    Component.translatable(
                                    "tooltip.otherworldinn.furniture.light",
                                    formatSigned(modifiers.light()))
                            .withStyle(style -> style.withColor(ModColors.LIGHT))));
        }
        if (modifiers.humidity() != 0) {
            tooltip.add(createIndentedLine(
                    Component.translatable(
                                    "tooltip.otherworldinn.furniture.humidity",
                                    formatSigned(modifiers.humidity()))
                            .withStyle(style -> style.withColor(ModColors.HUMIDITY))));
        }
    }

    private static MutableComponent createIndentedLine(Component content) {
        MutableComponent line =
                Component.literal("  - ").withStyle(style -> style.withColor(ModColors.GRAY_LIGHT));
        line.append(content);
        return line;
    }

    private static String formatSigned(int value) {
        return String.format("%+d", value);
    }
}
