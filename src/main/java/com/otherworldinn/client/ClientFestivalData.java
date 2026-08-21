package com.otherworldinn.client;

import java.util.Map;

/**
 * 客户端节日数据缓存（由服务端通过 S2C 包同步）。
 */
public final class ClientFestivalData {

    /** shopType → 折扣率（0~1）；"" 键表示全局折扣 */
    private static Map<String, Double> shopDiscounts = Map.of();

    private ClientFestivalData() {}

    public static void setShopDiscounts(Map<String, Double> discounts) {
        shopDiscounts = Map.copyOf(discounts);
    }

    /** 指定商店类型的节日折扣率（0 = 无折扣）；未匹配具体类型时回退全局折扣（"" 键） */
    public static double getShopDiscount(String shopType) {
        Double specific = shopDiscounts.get(shopType);
        if (specific != null) {
            return specific;
        }
        return shopDiscounts.getOrDefault("", 0.0D);
    }
}
