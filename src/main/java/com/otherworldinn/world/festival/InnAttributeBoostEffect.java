package com.otherworldinn.world.festival;

import java.util.Map;

/**
 * 旅社属性加成效果：节日期间旅社全局属性（住宿/餐饮收益、声望获取）提升。
 *
 * <p>属性加成（0.3 = 收益提升 30%）。键 {@code ""} 表示全部属性的全局加成，
 * 具体属性键优先于全局加成。
 */
public class InnAttributeBoostEffect implements FestivalEffect {

    /** 住宿收益属性键 */
    public static final String ATTR_LODGING_INCOME = "lodging_income";
    /** 餐饮收益属性键 */
    public static final String ATTR_DINING_INCOME = "dining_income";
    /** 声望获取属性键 */
    public static final String ATTR_REPUTATION_GAIN = "reputation_gain";

    /** attribute → 加成；"" 键表示全局加成 */
    private final Map<String, Double> attributeBoosts;

    public InnAttributeBoostEffect(Map<String, Double> attributeBoosts) {
        this.attributeBoosts = Map.copyOf(attributeBoosts);
    }

    @Override
    public double queryValue(String key, Object... args) {
        if (!KEY_INN_ATTRIBUTE_BOOST.equals(key) || args.length == 0 || args[0] == null) {
            return 0.0D;
        }
        String attribute = args[0].toString();
        Double specific = attributeBoosts.get(attribute);
        if (specific != null) {
            return specific;
        }
        return attributeBoosts.getOrDefault("", 0.0D);
    }
}
