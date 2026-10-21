package com.otherworldinn.world.festival;

import java.util.Map;

/** 设施产出加成：把指定设施的每日产出提升配置值（0.3 = 提升 30%）；键 {@code ""} 为全局加成，具体设施 id 优先。 */
public class FacilityYieldBoostEffect implements FestivalEffect {

    /** facilityId → 产出加成；"" 键表示全局加成 */
    private final Map<String, Double> facilityYieldBoosts;

    public FacilityYieldBoostEffect(Map<String, Double> facilityYieldBoosts) {
        this.facilityYieldBoosts = Map.copyOf(facilityYieldBoosts);
    }

    @Override
    public double queryValue(String key, Object... args) {
        if (!KEY_FACILITY_YIELD_BOOST.equals(key) || args.length == 0 || args[0] == null) {
            return 0.0D;
        }
        String facilityId = args[0].toString();
        Double specific = facilityYieldBoosts.get(facilityId);
        if (specific != null) {
            return specific;
        }
        return facilityYieldBoosts.getOrDefault("", 0.0D);
    }
}
