package com.otherworldinn.world.economy.service;

import com.github.ysbbbbbb.kaleidoscopecookery.item.quality.Quality;
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
 * <p>用于管理玩家向旅社出售物品的价格。 使用静态 Map 存储
 */
public class ItemSellPriceManager {

    private static final Map<ResourceLocation, Integer> PRICES = new HashMap<>();

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

		//森罗厨房
        addPrice("kaleidoscope_cookery:suspicious_stir_fry", 1); // 谜之炒菜
        addPrice("kaleidoscope_cookery:slime_ball_meal", 22); // 黏液饭
        addPrice("kaleidoscope_cookery:fondant_pie", 24); // 翻糖派
        addPrice("kaleidoscope_cookery:dongpo_pork", 26); // 东坡肉
        addPrice("kaleidoscope_cookery:fondant_spider_eye", 26); // 翻糖蛛眼
        addPrice("kaleidoscope_cookery:chorus_fried_egg", 28); // 荷包紫颂烧
        addPrice("kaleidoscope_cookery:braised_fish", 20); // 红烧鱼
        addPrice("kaleidoscope_cookery:golden_salad", 32); // 黄金沙拉
        addPrice("kaleidoscope_cookery:spicy_chicken", 20); // 辣子鸡
        addPrice("kaleidoscope_cookery:yakitori", 16); // 烧鸟串
        addPrice("kaleidoscope_cookery:crystal_lamb_chop", 30); // 水晶羊排
        addPrice("kaleidoscope_cookery:nether_style_sashimi", 34); // 下界风味刺身
        addPrice("kaleidoscope_cookery:pan_seared_knight_steak", 34); // 香煎骑士牛排
        addPrice("kaleidoscope_cookery:stargazy_pie", 28); // 仰望星空派
        addPrice("kaleidoscope_cookery:sweet_and_sour_ender_pearls", 38); // 珍珠咕噜肉
        addPrice("kaleidoscope_cookery:blaze_lamb_chop", 34); // 烈焰羊排
        addPrice("kaleidoscope_cookery:frost_lamb_chop", 34); // 凛冬羊排
        addPrice("kaleidoscope_cookery:braised_pork_ribs", 26); // 红烧排骨
        addPrice("kaleidoscope_cookery:buddha_jumps_over_the_wall", 46); // 佛跳墙
        addPrice("kaleidoscope_cookery:brown_mushroom_pot_soup", 22); // 棕蘑菇瓦罐汤
        addPrice("kaleidoscope_cookery:candied_potato", 16); // 拔丝土豆
        addPrice("kaleidoscope_cookery:cold_cut_ham_slices", 24); // 冷切火腿片
        addPrice("kaleidoscope_cookery:cold_roasted_meat", 20); // 冷肉炙
        addPrice("kaleidoscope_cookery:crimson_fungus_pot_soup", 26); // 绯红菌瓦罐汤
        addPrice("kaleidoscope_cookery:dough_drop_soup", 18); // 疙瘩汤
        addPrice("kaleidoscope_cookery:four_joy_meatball_soup", 30); // 四喜丸子汤
        addPrice("kaleidoscope_cookery:fried_caterpillar", 22); // 油炸猪儿虫
        addPrice("kaleidoscope_cookery:fried_spring_roll", 24); // 炸春卷
        addPrice("kaleidoscope_cookery:fruit_platter", 14); // 水果拼盘
        addPrice("kaleidoscope_cookery:numbing_spicy_chicken", 28); // 椒麻鸡
        addPrice("kaleidoscope_cookery:oil_splashed_fish", 30); // 油泼鱼
        addPrice("kaleidoscope_cookery:red_mushroom_pot_soup", 22); // 红蘑菇瓦罐汤
        addPrice("kaleidoscope_cookery:spicy_blood_stew", 34); // 毛血旺
        addPrice("kaleidoscope_cookery:spicy_rabbit_head", 24); // 麻辣兔头
        addPrice("kaleidoscope_cookery:stuffed_tiger_skin_pepper", 28); // 虎皮青椒酿肉
        addPrice("kaleidoscope_cookery:warped_fungus_pot_soup", 26); // 诡异菌瓦罐汤
        addPrice("kaleidoscope_cookery:end_style_sashimi", 36); // 末地风味刺身
        addPrice("kaleidoscope_cookery:desert_style_sashimi", 28); // 沙漠风味刺身
        addPrice("kaleidoscope_cookery:tundra_style_sashimi", 30); // 苔原风味刺身
        addPrice("kaleidoscope_cookery:cold_style_sashimi", 32); // 寒带风味刺身
        addPrice("kaleidoscope_cookery:shengjian_mantou", 26); // 水煎包
        addPrice("kaleidoscope_cookery:fried_egg", 8); // 煎蛋
        addPrice("kaleidoscope_cookery:scramble_egg_with_tomatoes", 14); // 番茄炒蛋
        addPrice("kaleidoscope_cookery:scramble_egg_with_tomatoes_rice_bowl", 18); // 番茄炒蛋盖饭
        addPrice("kaleidoscope_cookery:stir_fried_beef_offal", 28); // 爆炒牛杂
        addPrice("kaleidoscope_cookery:stir_fried_beef_offal_rice_bowl", 32); // 爆炒牛杂盖饭
        addPrice("kaleidoscope_cookery:braised_beef", 26); // 红烧牛肉
        addPrice("kaleidoscope_cookery:braised_beef_rice_bowl", 30); // 红烧牛肉盖饭
        addPrice("kaleidoscope_cookery:stir_fried_pork_with_peppers", 20); // 青椒炒肉
        addPrice("kaleidoscope_cookery:stir_fried_pork_with_peppers_rice_bowl", 24); // 青椒炒肉盖饭
        addPrice("kaleidoscope_cookery:sweet_and_sour_pork", 22); // 糖醋里脊
        addPrice("kaleidoscope_cookery:sweet_and_sour_pork_rice_bowl", 26); // 糖醋里脊盖饭
        addPrice("kaleidoscope_cookery:country_style_mixed_vegetables", 16); // 田园杂蔬
        addPrice("kaleidoscope_cookery:fish_flavored_shredded_pork", 24); // 鱼香肉丝
        addPrice("kaleidoscope_cookery:fish_flavored_shredded_pork_rice_bowl", 28); // 鱼香肉丝盖饭
        addPrice("kaleidoscope_cookery:braised_fish_rice_bowl", 24); // 红烧鱼盖饭
        addPrice("kaleidoscope_cookery:spicy_chicken_rice_bowl", 24); // 辣子鸡盖饭
        addPrice("kaleidoscope_cookery:suspicious_stir_fry_rice_bowl", 2); // 谜之炒菜盖饭
        addPrice("kaleidoscope_cookery:egg_fried_rice", 14); // 蛋炒饭
        addPrice("kaleidoscope_cookery:delicious_egg_fried_rice", 22); // 美味蛋炒饭
        addPrice("kaleidoscope_cookery:pork_bone_soup", 24); // 大骨汤
        addPrice("kaleidoscope_cookery:seafood_miso_soup", 30); // 海鲜味噌汤
        addPrice("kaleidoscope_cookery:fearsome_thick_soup", 40); // 恐惧浓汤
        addPrice("kaleidoscope_cookery:lamb_and_radish_soup", 26); // 萝卜羊肉汤
        addPrice("kaleidoscope_cookery:braised_beef_with_potatoes", 30); // 土豆炖牛肉
        addPrice("kaleidoscope_cookery:wild_mushroom_rabbit_soup", 28); // 野菌兔肉汤
        addPrice("kaleidoscope_cookery:tomato_beef_brisket_soup", 30); // 番茄牛腩汤
        addPrice("kaleidoscope_cookery:pufferfish_soup", 40); // 河豚汤
        addPrice("kaleidoscope_cookery:borscht", 26); // 罗宋汤
        addPrice("kaleidoscope_cookery:beef_meatball_soup", 28); // 牛丸汤
        addPrice("kaleidoscope_cookery:chicken_and_mushroom_stew", 24); // 小鸡炖蘑菇
        addPrice("kaleidoscope_cookery:donkey_soup", 34); // 驴肉汤
        addPrice("kaleidoscope_cookery:cooked_lamb_chops", 12); // 熟羊排
        addPrice("kaleidoscope_cookery:cooked_cow_offal", 12); // 熟牛杂
        addPrice("kaleidoscope_cookery:cooked_pork_belly", 12); // 熟五花肉
        addPrice("kaleidoscope_cookery:cooked_cut_small_meats", 10); // 熟切制小肉
        addPrice("kaleidoscope_cookery:cooked_meatball", 12); // 熟丸子
        addPrice("kaleidoscope_cookery:cooked_donkey_meat", 14); // 熟驴肉
        addPrice("kaleidoscope_cookery:donkey_burger", 26); // 驴肉火烧
        addPrice("kaleidoscope_cookery:baozi", 14); // 包子
        addPrice("kaleidoscope_cookery:dumpling", 18); // 饺子
        addPrice("kaleidoscope_cookery:samsa", 18); // 烤包子
        addPrice("kaleidoscope_cookery:mantou", 10); // 馒头
        addPrice("kaleidoscope_cookery:meat_pie", 20); // 馅饼
        addPrice("kaleidoscope_cookery:beef_noodle", 30); // 牛肉面
        addPrice("kaleidoscope_cookery:hui_noodle", 30); // 羊肉烩面
        addPrice("kaleidoscope_cookery:udon_noodle", 24); // 乌冬面

        addPrice("kaleidoscope_cookery:barley_tea", 8); // 大麦茶
        addPrice("kaleidoscope_cookery:flower_tea", 8); // 花茶
        addPrice("kaleidoscope_cookery:tieguanyin", 14); // 铁观音
        addPrice("kaleidoscope_cookery:biluochun", 14); // 碧螺春
        addPrice("kaleidoscope_cookery:oolong", 14); // 乌龙
        addPrice("kaleidoscope_cookery:sakura_fubuki", 18); // 樱花吹雪

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

    /**
     * 添加物品售价
     *
     * @param itemId 物品 ID (例如 "minecraft:apple")
     * @param price 售价
     */
    public static void addPrice(String itemId, int price) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl != null) {
            PRICES.put(rl, price);
        }
    }

    /**
     * 获取物品的单价
     *
     * @param itemStack 物品栈
     * @return 单价，如果未定义则返回 0
     */
    public static int getPrice(ItemStack itemStack) {
        if (itemStack.isEmpty()) return 0;
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        int basePrice = PRICES.getOrDefault(rl, 0);
        if (basePrice <= 0) {
            return 0;
        }

        // 根据菜品品质调整售价
        double qualityMultiplier = 1.0;
        if (QualityUtils.hasQuality(itemStack)) {
            Quality quality = QualityUtils.getQuality(itemStack);
            qualityMultiplier = switch (quality) {
                case SUPERB -> 1.2;
                case EXCELLENT -> 1.0;
                case STANDARD -> 0.6;
                case POOR -> 0.3;
            };
        }

        if (!(itemStack.getItem() instanceof DrinkBlockItem)) {
            return (int) Math.max(1, Math.round(basePrice * qualityMultiplier));
        }
        int brewLevel = Math.max(1, Math.min(7, BottleBlockItem.getBrewLevel(itemStack)));
        int price = basePrice;
        for (int level = 2; level <= brewLevel; level++) {
            price = (int) Math.floor(price * 1.4d);
        }
        return (int) Math.max(1, Math.round(price * qualityMultiplier));
    }

    public static int getConfiguredPrice(ResourceLocation itemId) {
        if (itemId == null) {
            return 0;
        }
        return PRICES.getOrDefault(itemId, 0);
    }

    public static List<ResourceLocation> getConfiguredItemsAbovePrice(int minPriceExclusive) {
        List<ResourceLocation> result = new ArrayList<>();
        for (Map.Entry<ResourceLocation, Integer> entry : PRICES.entrySet()) {
            if (entry.getValue() != null && entry.getValue() > minPriceExclusive) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public static List<ResourceLocation> getTopPricedItems(int count) {
        return PRICES.entrySet().stream()
                .sorted(Map.Entry.<ResourceLocation, Integer>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey)
                .toList();
    }
}
