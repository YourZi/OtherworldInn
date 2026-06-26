package com.otherworldinn.world.inn.service;

import com.otherworldinn.world.economy.service.ItemSellPriceManager;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class InnMenuDishRegistry {
    private static final Set<String> INCLUDED_NAMESPACES =
            Set.of("minecraft", "kaleidoscope_cookery");
    private static final String EXCLUDED_NAMESPACE = "kaleidoscope_tavern";

    private InnMenuDishRegistry() {}

    public static boolean isMenuDish(ItemStack stack) {
        return resolveMenuDishId(stack) != null;
    }

    @Nullable
    public static ResourceLocation resolveMenuDishId(ItemStack stack) {
        if (stack.isEmpty() || ItemSellPriceManager.getPrice(stack) <= 0) {
            return null;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) {
            return null;
        }
        if (EXCLUDED_NAMESPACE.equals(itemId.getNamespace())) {
            return null;
        }
        return INCLUDED_NAMESPACES.contains(itemId.getNamespace()) ? itemId : null;
    }
}
