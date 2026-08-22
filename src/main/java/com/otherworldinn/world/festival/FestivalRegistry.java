package com.otherworldinn.world.festival;

import com.otherworldinn.OtherworldInn;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import sereneseasons.api.season.Season;

/**
 * 节日注册表
 *
 * <p>代码内联注册节日定义（仿 FacilityRegistry）。{{@link #registerDefaults()} 内含一个
 * 覆盖全部效果类型的示例节日，可直接复制改造成正式节日。
 */
public final class FestivalRegistry {
    /** 节日装饰效果：通用喷泉（全部节日共用），快照对待导出至 structure/festival/common/ */
    private static final FestivalDecorationEffect DECORATION_EFFECT =
            new FestivalDecorationEffect(
                    List.of(
                            new FestivalDecorationService.DecorationRegion(
                                    ResourceLocation.fromNamespaceAndPath(
                                            OtherworldInn.MODID, "festival/common/fountain"),
                                    ResourceLocation.fromNamespaceAndPath(
                                            OtherworldInn.MODID, "festival/common/fountain_base"),
                                    new BlockPos(-4, 70, -4))));

    private static final Map<String, FestivalDefinition> FESTIVALS = new LinkedHashMap<>();

    static {
        registerDefaults();
    }

    private FestivalRegistry() {}

    /**
     * 内置节日注册（实装版，见 festival-design.md）。
     *
     * <p>窗口按主季天数（0 起）换算：早/仲/晚季分别对应主季 0-7 / 8-15 / 16-23 天，
     * 子季内的 "day 1-N"（1 起）换算为 0 起的索引区间。
     */
    private static void registerDefaults() {
        // 春祭：早春 day 1-3，农民种子 8 折
        registerFestival(
                "spring_festival",
                "Spring Festival",
                "春祭",
                new FestivalTrigger(Season.SPRING, 0, 3), // 早春 day 1-3
                List.of(new ShopSaleEffect(Map.of("farmer", 0.2D)), DECORATION_EFFECT));

        // 仲夏夜：仲夏 day 5-7，故事客人刷新 +100%
        registerFestival(
                "midsummer_night",
                "Midsummer Night",
                "仲夏夜",
                new FestivalTrigger(Season.SUMMER, 12, 15), // 仲夏 day 5-7
                List.of(new GuestSpawnBoostEffect(Map.of("", 1.0D)), DECORATION_EFFECT));

        // 秋收祭：晚秋 day 1-4，餐饮收益 +30%、顾客好感度 +25%
        registerFestival(
                "harvest_festival",
                "Harvest Festival",
                "秋收祭",
                new FestivalTrigger(Season.AUTUMN, 16, 20), // 晚秋 day 1-4
                List.of(
                        new InnAttributeBoostEffect(
                                Map.of(
                                        InnAttributeBoostEffect.ATTR_DINING_INCOME, 0.3D,
                                        InnAttributeBoostEffect.ATTR_REPUTATION_GAIN, 0.25D)),
                        DECORATION_EFFECT));

        // 隆冬节：隆冬 day 1-3，入住费 +50%、矿井产出 +30%、商店全场 85 折
        registerFestival(
                "deep_winter_festival",
                "Deep Winter Festival",
                "隆冬节",
                new FestivalTrigger(Season.WINTER, 16, 19), // 隆冬 day 1-3
                List.of(
                        new InnAttributeBoostEffect(
                                Map.of(InnAttributeBoostEffect.ATTR_LODGING_INCOME, 0.5D)),
                        new FacilityYieldBoostEffect(Map.of("mine", 0.3D)),
                        new ShopSaleEffect(Map.of("", 0.15D)),
                        DECORATION_EFFECT));
    }

    /**
     * 注册节日定义。
     *
     * @throws IllegalArgumentException id 为空/重复，或 trigger 为空
     */
    public static FestivalDefinition registerFestival(
            String id, String enName, String zhName, FestivalTrigger trigger, List<FestivalEffect> effects) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Festival id cannot be blank.");
        }
        if (trigger == null) {
            throw new IllegalArgumentException("Festival trigger cannot be null.");
        }
        if (FESTIVALS.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate festival id: " + id);
        }
        FestivalDefinition definition =
                new FestivalDefinition(
                        id,
                        enName == null || enName.isBlank() ? id : enName,
                        zhName == null || zhName.isBlank() ? enName : zhName,
                        trigger,
                        effects == null ? List.of() : List.copyOf(effects),
                        "festival." + OtherworldInn.MODID + "." + id);
        FESTIVALS.put(id, definition);
        return definition;
    }

    public static Optional<FestivalDefinition> get(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(FESTIVALS.get(id));
    }

    public static Collection<FestivalDefinition> getAll() {
        return FESTIVALS.values();
    }
}
