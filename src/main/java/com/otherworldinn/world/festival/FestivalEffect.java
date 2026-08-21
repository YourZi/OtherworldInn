package com.otherworldinn.world.festival;

import net.minecraft.server.level.ServerLevel;

/**
 * 节日效果接口（扩展点）
 *
 * <p>新增节日效果只需实现本接口并注册到节日定义，无需改动引擎。
 * 效果分两类：
 * <ul>
 *   <li>事件式：onFestivalStart / onFestivalEnd，一次性副作用（如放置装饰）；</li>
 *   <li>查询式：queryValue，由各系统在运行时主动查询加成（默认返回 0 表示无加成）。</li>
 * </ul>
 */
public interface FestivalEffect {

    /** 商店折扣查询键（args[0] 为商店类型标识） */
    String KEY_SHOP_DISCOUNT = "shop_discount";
    /** 客人刷新加成查询键（args[0] 为客人类型标识） */
    String KEY_GUEST_SPAWN_BOOST = "guest_spawn_boost";
    /** 设施产出加成查询键（args[0] 为设施 ID） */
    String KEY_FACILITY_YIELD_BOOST = "facility_yield_boost";
    /** 旅社属性加成查询键（args[0] 为属性维度标识） */
    String KEY_INN_ATTRIBUTE_BOOST = "inn_attribute_boost";

    /** 节日开始时调用 */
    default void onFestivalStart(ServerLevel townLevel, FestivalContext ctx) {}

    /** 节日结束时调用 */
    default void onFestivalEnd(ServerLevel townLevel, FestivalContext ctx) {}

    /** 通用数值查询：返回 key 对应参数下的加成值，无匹配返回 0 */
    default double queryValue(String key, Object... args) {
        return 0.0;
    }
}
