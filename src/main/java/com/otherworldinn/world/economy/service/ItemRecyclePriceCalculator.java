package com.otherworldinn.world.economy.service;

import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 通用物品回收价计算器
 */
public final class ItemRecyclePriceCalculator {

    /** 通用回收价上限 */
    private static final int MAX_RECYCLE_PRICE = 16;

    /**
     * 材料底价表 — 矿锭 / 宝石 / 稀有掉落物等原料的单个回收价值。
     * <p>不在表中的物品按稀有度兜底计算。
     */
    private static final Map<ResourceLocation, Integer> MATERIAL_BASE = Map.ofEntries(
            Map.entry(rl("minecraft:coal"), 1),
            Map.entry(rl("minecraft:charcoal"), 1),
            Map.entry(rl("minecraft:raw_iron"), 2),
            Map.entry(rl("minecraft:iron_ingot"), 2),
            Map.entry(rl("minecraft:raw_copper"), 1),
            Map.entry(rl("minecraft:copper_ingot"), 1),
            Map.entry(rl("minecraft:raw_gold"), 3),
            Map.entry(rl("minecraft:gold_ingot"), 4),
            Map.entry(rl("minecraft:gold_nugget"), 1),
            Map.entry(rl("minecraft:diamond"), 8),
            Map.entry(rl("minecraft:netherite_ingot"), 12),
            Map.entry(rl("minecraft:netherite_scrap"), 3),
            Map.entry(rl("minecraft:emerald"), 6),
            Map.entry(rl("minecraft:redstone"), 2),
            Map.entry(rl("minecraft:lapis_lazuli"), 2),
            Map.entry(rl("minecraft:quartz"), 2),
            Map.entry(rl("minecraft:amethyst_shard"), 3),
            Map.entry(rl("minecraft:obsidian"), 3),
            Map.entry(rl("minecraft:crying_obsidian"), 5),
            Map.entry(rl("minecraft:ender_pearl"), 5),
            Map.entry(rl("minecraft:blaze_rod"), 5),
            Map.entry(rl("minecraft:blaze_powder"), 3),
            Map.entry(rl("minecraft:ghast_tear"), 6),
            Map.entry(rl("minecraft:nether_star"), 12),
            Map.entry(rl("minecraft:dragon_breath"), 8),
            Map.entry(rl("minecraft:dragon_head"), 14),
            Map.entry(rl("minecraft:echo_shard"), 8),
            Map.entry(rl("minecraft:phantom_membrane"), 4),
            Map.entry(rl("minecraft:shulker_shell"), 6),
            Map.entry(rl("minecraft:spider_eye"), 1),
            Map.entry(rl("minecraft:rotten_flesh"), 1),
            Map.entry(rl("minecraft:bone"), 1),
            Map.entry(rl("minecraft:gunpowder"), 2),
            Map.entry(rl("minecraft:string"), 1),
            Map.entry(rl("minecraft:slime_ball"), 3),
            Map.entry(rl("minecraft:leather"), 2),
            Map.entry(rl("minecraft:rabbit_hide"), 1),
            Map.entry(rl("minecraft:feather"), 1),
            Map.entry(rl("minecraft:ink_sac"), 1),
            Map.entry(rl("minecraft:glow_ink_sac"), 2),
            Map.entry(rl("minecraft:honey_bottle"), 2),
            Map.entry(rl("minecraft:totem_of_undying"), 12),
            Map.entry(rl("minecraft:nautilus_shell"), 5),
            Map.entry(rl("minecraft:heart_of_the_sea"), 10),
            Map.entry(rl("minecraft:trident"), 10),
            Map.entry(rl("minecraft:elytra"), 12),
            Map.entry(rl("minecraft:saddle"), 4),
            Map.entry(rl("minecraft:name_tag"), 4),
            Map.entry(rl("minecraft:lead"), 3),
            Map.entry(rl("minecraft:music_disc_5"), 8),
            Map.entry(rl("minecraft:music_disc_13"), 6),
            Map.entry(rl("minecraft:music_disc_cat"), 6),
            Map.entry(rl("minecraft:music_disc_blocks"), 6),
            Map.entry(rl("minecraft:music_disc_chirp"), 6),
            Map.entry(rl("minecraft:music_disc_far"), 6),
            Map.entry(rl("minecraft:music_disc_mall"), 6),
            Map.entry(rl("minecraft:music_disc_mellohi"), 6),
            Map.entry(rl("minecraft:music_disc_stal"), 6),
            Map.entry(rl("minecraft:music_disc_strad"), 6),
            Map.entry(rl("minecraft:music_disc_ward"), 6),
            Map.entry(rl("minecraft:music_disc_11"), 6),
            Map.entry(rl("minecraft:music_disc_wait"), 6),
            Map.entry(rl("minecraft:music_disc_otherside"), 8),
            Map.entry(rl("minecraft:music_disc_pigstep"), 8),
            Map.entry(rl("minecraft:music_disc_relic"), 8),
            Map.entry(rl("minecraft:music_disc_creator"), 8),
            Map.entry(rl("minecraft:music_disc_creator_music_box"), 8),
            Map.entry(rl("minecraft:music_disc_precipice"), 8),
            Map.entry(rl("minecraft:sculk_catalyst"), 6),
            Map.entry(rl("minecraft:sculk_sensor"), 4),
            Map.entry(rl("minecraft:sculk_shrieker"), 5),
            Map.entry(rl("minecraft:sculk"), 2),
            Map.entry(rl("minecraft:disc_fragment_5"), 4),
            Map.entry(rl("minecraft:goat_horn"), 4),
            Map.entry(rl("minecraft:sniffer_egg"), 5),
            Map.entry(rl("minecraft:brushes"), 1),
            Map.entry(rl("minecraft:armadillo_scute"), 4),
            Map.entry(rl("minecraft:breeze_rod"), 6),
            Map.entry(rl("minecraft:heavy_core"), 10)
    );

    private ItemRecyclePriceCalculator() {}

    private static ResourceLocation rl(String id) {
        return ResourceLocation.tryParse(id);
    }

    /**
     * 计算任意物品的回收价。
     *
     * @param stack 要回收的物品
     * @return 回收单价（0 表示不可回收或价值低于 1）
     */
    public static int getRecyclePrice(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());

        // 已配置明确价格的菜品 → 走 ItemSellPriceManager，不参与回收
        if (ItemSellPriceManager.BASE_PRICES.containsKey(id)) {
            return 0;
        }

        // ═══ 第一步：基础价值 ═══
        int base = MATERIAL_BASE.getOrDefault(id, rarityBase(stack.getRarity()));

        // ═══ 第二步：耐久折算 ═══
        if (stack.isDamageableItem()) {
            int maxDmg = stack.getMaxDamage();
            int dmg = stack.getDamageValue();
            if (maxDmg > 0) {
                double durabilityFraction = 1.0 - (double) dmg / maxDmg;
                base = Math.max(1, (int) Math.round(base * durabilityFraction));
            }
        }

        // ═══ 第三步：附魔加成 ═══
        int enchantBonus = 0;
        var enchantments = stack.getEnchantments();
        for (var holder : enchantments.keySet()) {
            Enchantment ench = holder.value();
            if (ench == null) continue;
            int level = enchantments.getLevel(holder);
            enchantBonus += level;
        }
        base += enchantBonus;

        // ═══ 第四步：封顶 ═══
        return Math.clamp(base, 0, MAX_RECYCLE_PRICE);
    }

    /**
     * 按稀有度兜底计算基础价值。
     *
     * <p>COMMON→1, UNCOMMON→3, RARE→6, EPIC→10
     */
    private static int rarityBase(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 1;
            case UNCOMMON -> 3;
            case RARE -> 6;
            case EPIC -> 10;
        };
    }
}
