package com.otherworldinn.world.economy.service;

import com.github.ysbbbbbb.kaleidoscopecookery.item.quality.QualityUtils;
import com.github.ysbbbbbb.kaleidoscopetavern.item.BottleBlockItem;
import com.github.ysbbbbbb.kaleidoscopetavern.item.DrinkBlockItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 物品售价管理器
 *
 * <p>BASE_PRICES 存配置的基准价格。
 * floatedPrices 由服务端计算后通过 S2CPriceSyncPacket 同步到客户端。
 */
public class ItemSellPriceManager {
    private static final ResourceLocation COIN_ITEM_ID =
            ResourceLocation.fromNamespaceAndPath("otherworldinn", "coin");

    static final Map<ResourceLocation, Integer> BASE_PRICES = new HashMap<>();
    private static final Map<String, Integer> floatedPrices = new HashMap<>();

    static {
        // 原版水果
        addPrice("minecraft:apple", 3);
        addPrice("minecraft:melon_slice", 2);

        // 原版熟肉类
        addPrice("minecraft:cooked_beef", 6);
        addPrice("minecraft:cooked_porkchop", 6);
        addPrice("minecraft:cooked_chicken", 5);
        addPrice("minecraft:cooked_mutton", 5);
        addPrice("minecraft:cooked_rabbit", 6);
        addPrice("minecraft:cooked_cod", 5);
        addPrice("minecraft:cooked_salmon", 5);

        // 原版简易菜肴
        addPrice("minecraft:bread", 3);
        addPrice("minecraft:baked_potato", 3);
        addPrice("minecraft:pumpkin_pie", 5);
        addPrice("minecraft:cookie", 2);
        addPrice("minecraft:cake", 8);
        addPrice("minecraft:mushroom_stew", 5);
        addPrice("minecraft:beetroot_soup", 5);
        addPrice("minecraft:rabbit_stew", 7);

        //森罗厨房 锅具菜品
        addPrice("kaleidoscope_cookery:suspicious_stir_fry", 1); // 谜之炒菜
        addPrice("kaleidoscope_cookery:slime_ball_meal", 10); // 粘液饭
        addPrice("kaleidoscope_cookery:fondant_pie", 16); // 翻糖派
        addPrice("kaleidoscope_cookery:dongpo_pork", 14); // 东坡肉
        addPrice("kaleidoscope_cookery:fondant_spider_eye", 12); // 翻糖蛛眼
        addPrice("kaleidoscope_cookery:chorus_fried_egg", 16); // 荷包紫颂烧
        addPrice("kaleidoscope_cookery:braised_fish", 20); // 红烧鱼
        addPrice("kaleidoscope_cookery:golden_salad", 38); // 黄金沙拉
        addPrice("kaleidoscope_cookery:spicy_chicken", 16); // 辣子鸡
        addPrice("kaleidoscope_cookery:yakitori", 14); // 烧鸟串
        addPrice("kaleidoscope_cookery:crystal_lamb_chop", 22); // 水晶羊排
        addPrice("kaleidoscope_cookery:pan_seared_knight_steak", 22); // 香煎骑士牛排
        addPrice("kaleidoscope_cookery:stargazy_pie", 16); // 仰望星空派
        addPrice("kaleidoscope_cookery:sweet_and_sour_ender_pearls", 42); // 珍珠咕噜肉
        addPrice("kaleidoscope_cookery:blaze_lamb_chop", 26); // 烈焰羊排
        addPrice("kaleidoscope_cookery:frost_lamb_chop", 26); // 凛冬羊排
        addPrice("kaleidoscope_cookery:braised_pork_ribs", 14); // 红烧排骨
        addPrice("kaleidoscope_cookery:candied_potato", 10); // 拔丝土豆
        addPrice("kaleidoscope_cookery:donkey_burger", 18); // 驴肉火烧
        addPrice("kaleidoscope_cookery:spicy_rabbit_head", 16); // 麻辣兔头
        addPrice("kaleidoscope_cookery:fried_spring_roll", 28); // 炸春卷
        addPrice("kaleidoscope_cookery:stuffed_tiger_skin_pepper", 16); // 虎皮青椒酿肉
        addPrice("kaleidoscope_cookery:fish_flavored_shredded_pork", 18); // 鱼香肉丝
        addPrice("kaleidoscope_cookery:stir_fried_pork_with_peppers", 16); // 青椒炒肉
        addPrice("kaleidoscope_cookery:egg_fried_rice", 12); // 蛋炒饭
        addPrice("kaleidoscope_cookery:fried_caterpillar", 10); // 油炸猪儿虫
        addPrice("kaleidoscope_cookery:oil_splashed_fish", 16); // 油泼鱼
        addPrice("kaleidoscope_cookery:scramble_egg_with_tomatoes", 12); // 番茄炒蛋
        addPrice("kaleidoscope_cookery:sweet_and_sour_pork", 16); // 糖醋里脊
        addPrice("kaleidoscope_cookery:braised_beef", 16); // 红烧牛肉
        addPrice("kaleidoscope_cookery:sticky_candy", 8); // 粘糖
        addPrice("kaleidoscope_cookery:fried_egg", 8); // 煎蛋
        addPrice("kaleidoscope_cookery:stir_fried_beef_offal", 18); // 爆炒牛杂
        addPrice("kaleidoscope_cookery:country_style_mixed_vegetables", 14); // 田园杂蔬
        addPrice("kaleidoscope_cookery:delicious_egg_fried_rice", 18); // 美味蛋炒饭

        //森罗厨房 汤锅菜品
        addPrice("kaleidoscope_cookery:borscht", 18); // 罗宋汤
        addPrice("kaleidoscope_cookery:braised_beef_with_potatoes", 18); // 土豆炖牛肉
        addPrice("kaleidoscope_cookery:buddha_jumps_over_the_wall", 36); // 佛跳墙
        addPrice("kaleidoscope_cookery:chicken_and_mushroom_stew", 14); // 小鸡炖蘑菇
        addPrice("kaleidoscope_cookery:dough_drop_soup", 14); // 疙瘩汤
        addPrice("kaleidoscope_cookery:lamb_and_radish_soup", 16); // 萝卜羊肉汤
        addPrice("kaleidoscope_cookery:warped_fungus_pot_soup", 16); // 诡异菌瓦罐汤
        addPrice("kaleidoscope_cookery:seafood_miso_soup", 18); // 海鲜味噌汤
        addPrice("kaleidoscope_cookery:hui_noodle", 22); // 羊肉烩面
        addPrice("kaleidoscope_cookery:red_mushroom_pot_soup", 14); // 红蘑菇瓦罐汤
        addPrice("kaleidoscope_cookery:pork_bone_soup", 10); // 大骨汤
        addPrice("kaleidoscope_cookery:beef_noodle", 22); // 牛肉面
        addPrice("kaleidoscope_cookery:four_joy_meatball_soup", 18); // 四喜丸子汤
        addPrice("kaleidoscope_cookery:brown_mushroom_pot_soup", 14); // 棕蘑菇瓦罐汤
        addPrice("kaleidoscope_cookery:wild_mushroom_rabbit_soup", 18); // 野菌兔肉汤
        addPrice("kaleidoscope_cookery:beef_meatball_soup", 22); // 牛丸汤
        addPrice("kaleidoscope_cookery:udon_noodle", 24); // 乌冬面
        addPrice("kaleidoscope_cookery:pufferfish_soup", 34); // 河豚汤
        addPrice("kaleidoscope_cookery:tomato_beef_brisket_soup", 28); // 番茄牛腩汤
        addPrice("kaleidoscope_cookery:donkey_soup", 28); // 驴肉汤
        addPrice("kaleidoscope_cookery:crimson_fungus_pot_soup", 16); // 绯红菌瓦罐汤

        //森罗厨房 岩浆汤底菜品
        addPrice("kaleidoscope_cookery:spicy_blood_stew", 32); // 毛血旺
        addPrice("kaleidoscope_cookery:numbing_spicy_chicken", 22); // 椒麻鸡
        addPrice("kaleidoscope_cookery:hot_dry_noodles", 24); // 热干面
        addPrice("kaleidoscope_cookery:fearsome_thick_soup", 36); // 恐惧浓汤

        //森罗厨房 蒸笼菜品
        addPrice("kaleidoscope_cookery:mantou", 8); // 馒头
        addPrice("kaleidoscope_cookery:baozi", 12); // 包子
        addPrice("kaleidoscope_cookery:qingtuan", 10); // 青团
        addPrice("kaleidoscope_cookery:bamboo_tube_rice", 18); // 竹筒饭

        //森罗厨房 煮锅菜品
        addPrice("kaleidoscope_cookery:dumpling", 14); // 饺子
        addPrice("kaleidoscope_cookery:samsa", 16); // 烤包子
        addPrice("kaleidoscope_cookery:shengjian_mantou", 18); // 水煎包
        addPrice("kaleidoscope_cookery:zongzi", 14); // 粽子
        addPrice("kaleidoscope_cookery:laba_congee", 12); // 腊八粥
        addPrice("kaleidoscope_cookery:meat_pie", 16); // 馅饼
        addPrice("kaleidoscope_cookery:sticky_rice_cake", 10); // 年糕

        //森罗厨房 刺身系列
        addPrice("kaleidoscope_cookery:sashimi", 3); // 刺身
        addPrice("kaleidoscope_cookery:desert_style_sashimi", 24); // 沙漠风味刺身
        addPrice("kaleidoscope_cookery:tundra_style_sashimi", 26); // 苔原风味刺身
        addPrice("kaleidoscope_cookery:cold_style_sashimi", 28); // 寒带风味刺身
        addPrice("kaleidoscope_cookery:nether_style_sashimi", 34); // 下界风味刺身
        addPrice("kaleidoscope_cookery:end_style_sashimi", 36); // 末地风味刺身

        //森罗厨房 冷盘拼盘
        addPrice("kaleidoscope_cookery:cold_cut_ham_slices", 24); // 冷切火腿片
        addPrice("kaleidoscope_cookery:cold_roasted_meat", 22); // 冷肉炙
        addPrice("kaleidoscope_cookery:fruit_platter", 12); // 水果拼盘

        //森罗厨房 果盘系列
        addPrice("kaleidoscope_cookery:apple_platter", 14); // 苹果拼盘
        addPrice("kaleidoscope_cookery:berry_platter", 20); // 莓果拼盘
        addPrice("kaleidoscope_cookery:chorus_fruit_platter", 28); // 紫颂果拼盘
        addPrice("kaleidoscope_cookery:tomato_platter", 22); // 番茄拼盘
        addPrice("kaleidoscope_cookery:watermelon_platter", 10); // 西瓜拼盘

        //森罗厨房 拼盘组合
        addPrice("kaleidoscope_cookery:sticky_rice_cake_plate", 22); // 年糕拼盘
        addPrice("kaleidoscope_cookery:zongzi_plate", 22); // 粽子拼盘
        addPrice("kaleidoscope_cookery:qingtuan_plate", 18); // 青团拼盘
        addPrice("kaleidoscope_cookery:baozi_plate", 22); // 包子拼盘
        addPrice("kaleidoscope_cookery:shengjian_mantou_plate", 28); // 水煎包拼盘
        addPrice("kaleidoscope_cookery:sticky_candy_plate", 14); // 粘糖拼盘
        addPrice("kaleidoscope_cookery:dark_cuisine", 1); // 黑暗料理

        //森罗厨房 盖饭系列
        addPrice("kaleidoscope_cookery:scramble_egg_with_tomatoes_rice_bowl", 16); // 番茄炒蛋盖饭
        addPrice("kaleidoscope_cookery:stir_fried_pork_with_peppers_rice_bowl", 20); // 青椒炒肉盖饭
        addPrice("kaleidoscope_cookery:sweet_and_sour_pork_rice_bowl", 20); // 糖醋里脊盖饭
        addPrice("kaleidoscope_cookery:braised_beef_rice_bowl", 20); // 红烧牛肉盖饭
        addPrice("kaleidoscope_cookery:fish_flavored_shredded_pork_rice_bowl", 22); // 鱼香肉丝盖饭
        addPrice("kaleidoscope_cookery:spicy_chicken_rice_bowl", 20); // 辣子鸡盖饭
        addPrice("kaleidoscope_cookery:braised_fish_rice_bowl", 24); // 红烧鱼盖饭
        addPrice("kaleidoscope_cookery:stir_fried_beef_offal_rice_bowl", 22); // 爆炒牛杂盖饭
        addPrice("kaleidoscope_cookery:suspicious_stir_fry_rice_bowl", 2); // 谜之炒菜盖饭

        //森罗厨房 熟制半成品
        addPrice("kaleidoscope_cookery:cooked_lamb_chops", 10); // 熟羊排
        addPrice("kaleidoscope_cookery:cooked_cow_offal", 10); // 熟牛杂
        addPrice("kaleidoscope_cookery:cooked_pork_belly", 10); // 熟五花肉
        addPrice("kaleidoscope_cookery:cooked_cut_small_meats", 8); // 熟切制小肉
        addPrice("kaleidoscope_cookery:cooked_meatball", 10); // 熟丸子
        addPrice("kaleidoscope_cookery:cooked_donkey_meat", 12); // 熟驴肉
        addPrice("kaleidoscope_cookery:fruit_basket", 16); // 水果篮

        //森罗厨房 茶饮
        addPrice("kaleidoscope_cookery:barley_tea", 8); // 大麦茶
        addPrice("kaleidoscope_cookery:flower_tea", 8); // 花茶
        addPrice("kaleidoscope_cookery:tieguanyin", 12); // 铁观音
        addPrice("kaleidoscope_cookery:biluochun", 12); // 碧螺春
        addPrice("kaleidoscope_cookery:oolong", 12); // 乌龙
        addPrice("kaleidoscope_cookery:sakura_fubuki", 16); // 樱花吹雪

        //森罗酒馆
        addPrice("kaleidoscope_tavern:wine", 10); // 葡萄酒
        addPrice("kaleidoscope_tavern:molotov", 12); // 莫洛托夫鸡尾酒
        addPrice("kaleidoscope_tavern:champagne", 16); // 香槟
        addPrice("kaleidoscope_tavern:vodka", 14); // 伏特加
        addPrice("kaleidoscope_tavern:brandy", 15); // 白兰地
        addPrice("kaleidoscope_tavern:carignan", 17); // 佳丽酿
        addPrice("kaleidoscope_tavern:sakura_wine", 17); // 樱花葡萄酒
        addPrice("kaleidoscope_tavern:plum_wine", 12); // 梅酒
        addPrice("kaleidoscope_tavern:whiskey", 14); // 威士忌
        addPrice("kaleidoscope_tavern:ice_wine", 15); // 冰葡萄酒
        addPrice("kaleidoscope_tavern:polaris_sweet_white", 20); // 北极星甜白
        addPrice("kaleidoscope_tavern:honey_wine", 16); // 蜂蜜酒
        addPrice("kaleidoscope_tavern:red_queen", 22); // 红皇后
        addPrice("kaleidoscope_tavern:miners_star", 16); // 矿工之星
        addPrice("kaleidoscope_tavern:rum", 14); // 朗姆酒
        addPrice("kaleidoscope_tavern:riesling_dry_white", 18); // 雷司令干白
        addPrice("kaleidoscope_tavern:sunset_glow", 20); // 日落辉光
        addPrice("kaleidoscope_tavern:madame_shexiang", 22); // 麝香夫人
        addPrice("kaleidoscope_tavern:sweet_berry_wine", 14); // 甜浆果酒
        addPrice("kaleidoscope_tavern:sherry", 16); // 雪莉酒
        addPrice("kaleidoscope_tavern:mother_snow", 22); // 雪母
        addPrice("kaleidoscope_tavern:luminous_bride", 24); // 发光新娘
        addPrice("kaleidoscope_tavern:glowflower_brew", 22); // 发光花酿造
        addPrice("kaleidoscope_tavern:sauvignon_blanc_dry_white", 18); // 长相思干白
        addPrice("kaleidoscope_tavern:vinegar", 6); // 醋
        addPrice("kaleidoscope_tavern:watermelon_juice", 8); // 西瓜汁
    }

    public static void addPrice(String itemId, int price) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl != null) {
            BASE_PRICES.put(rl, price);
        }
    }

    /** 设置浮动后价格表 */
    public static void setFloatedPrices(Map<String, Integer> prices) {
        floatedPrices.clear();
        floatedPrices.putAll(prices);
    }

    /** 清空浮动价格表 */
    public static void clearFloatedPrices() {
        floatedPrices.clear();
    }

    public static int getPrice(ItemStack itemStack) {
        if (itemStack.isEmpty()) return 0;
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        if (COIN_ITEM_ID.equals(rl)) {
            return 0;
        }
        if (BASE_PRICES.containsKey(rl)) {
            return 1;
        }
        int base = getBasePrice(rl);
        if (base <= 0) return 1;

        double qm = 1.0;
        if (QualityUtils.hasQuality(itemStack)) {
            qm = switch (QualityUtils.getQuality(itemStack)) {
                case SUPERB -> 1.2;
                case EXCELLENT -> 1.0;
                case STANDARD -> 0.6;
                case POOR -> 0.3;
            };
        }

        if (!(itemStack.getItem() instanceof DrinkBlockItem)) {
            return Math.max(1, (int) Math.round(base * qm));
        }
        int brewLevel = Math.max(1, Math.min(7, BottleBlockItem.getBrewLevel(itemStack)));
        double price = base;
        for (int level = 2; level <= brewLevel; level++) {
            price = Math.floor(price * 1.4d);
        }
        return Math.max(1, (int) Math.round(price * qm));
    }

    private static int getBasePrice(ResourceLocation rl) {
        Integer f = floatedPrices.get(rl.toString());
        if (f != null) return f;
        return BASE_PRICES.getOrDefault(rl, 0);
    }

    public static int getConfiguredPrice(ResourceLocation itemId) {
        if (itemId == null) return 0;
        return getBasePrice(itemId);
    }

    public static Map<ResourceLocation, Integer> getConfiguredPrices() {
        return Map.copyOf(BASE_PRICES);
    }

    public static List<ResourceLocation> getConfiguredItemsAbovePrice(int minPriceExclusive) {
        List<ResourceLocation> result = new ArrayList<>();
        for (var entry : BASE_PRICES.entrySet()) {
            if (entry.getValue() > minPriceExclusive) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public static List<ResourceLocation> getTopPricedItems(int count) {
        return BASE_PRICES.entrySet().stream()
                .sorted(Map.Entry.<ResourceLocation, Integer>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey)
                .toList();
    }
}
