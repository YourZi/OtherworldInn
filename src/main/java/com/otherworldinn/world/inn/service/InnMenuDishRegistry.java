package com.otherworldinn.world.inn.service;

import com.otherworldinn.world.economy.service.ItemSellPriceManager;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 菜单多样性统计用的菜品判定：售价大于 {@value #MENU_DISH_PRICE_THRESHOLD} 的展示物才计入多样性
 * （酒水、果汁、醋等够价也计入）。可出售判定见 {@link InnDiningDisplayHelper#isSellableItem}：
 * 售价不高于阈值的物品仍可出售，只是不计入多样性。
 */
public final class InnMenuDishRegistry {
    public static final int MENU_DISH_PRICE_THRESHOLD = 4;

    private InnMenuDishRegistry() {}

    public static boolean isMenuDish(ItemStack stack) {
        return !stack.isEmpty()
                && ItemSellPriceManager.getPrice(stack) > MENU_DISH_PRICE_THRESHOLD;
    }

    @Nullable
    public static ResourceLocation resolveMenuDishId(ItemStack stack) {
        if (!isMenuDish(stack)) {
            return null;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }
}
