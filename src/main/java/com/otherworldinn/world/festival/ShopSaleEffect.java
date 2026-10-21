package com.otherworldinn.world.festival;

import java.util.Map;

/** 商店折扣：配置值为折扣率 0~1（0.2 = 打 8 折），键 {@code ""} 为全部商店的全局折扣，具体商店类型优先（类型见 {@code StoreEntity#shopTypeKey()}）。 */
public class ShopSaleEffect implements FestivalEffect {

    /** shopType → 折扣率（0~1）；"" 键表示全局折扣 */
    private final Map<String, Double> shopDiscountRates;

    public ShopSaleEffect(Map<String, Double> shopDiscountRates) {
        this.shopDiscountRates = Map.copyOf(shopDiscountRates);
    }

    /** 完整折扣表（shopType → 折扣率，含 "" 全局键），供服务端同步给客户端展示 */
    public Map<String, Double> getDiscountRates() {
        return shopDiscountRates;
    }

    @Override
    public double queryValue(String key, Object... args) {
        if (!KEY_SHOP_DISCOUNT.equals(key) || args.length == 0 || args[0] == null) {
            return 0.0D;
        }
        String shopType = args[0].toString();
        Double specific = shopDiscountRates.get(shopType);
        if (specific != null) {
            return specific;
        }
        return shopDiscountRates.getOrDefault("", 0.0D);
    }
}
