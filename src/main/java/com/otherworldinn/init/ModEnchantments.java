package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 本模组的附魔。1.21 的附魔是数据驱动条目（data/otherworldinn/enchantment/*.json），
 * 代码侧只需要 key 与查询工具。
 */
public final class ModEnchantments {
    /** 收获：手持带该附魔的剪刀右键养殖动物，采集一份它的击杀掉落。 */
    public static final ResourceKey<Enchantment> HARVEST =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "harvest"));

    private ModEnchantments() {}

    /** 手持物上「收获」的等级；没有该附魔返回 0。 */
    public static int harvestLevel(ServerLevel level, ItemStack stack) {
        Holder<Enchantment> holder =
                level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(HARVEST);
        return Math.max(0, stack.getEnchantments().getLevel(holder));
    }
}
