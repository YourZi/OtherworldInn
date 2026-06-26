package com.otherworldinn.world.event.listener;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

final class ForcedDropBlacklist {
    private static final Set<ResourceLocation> BLOCKED_ITEM_IDS =
            Set.of(
                    ResourceLocation.fromNamespaceAndPath("touhou_little_maid", "smart_slab"),
                    ResourceLocation.fromNamespaceAndPath("otherworldinn", "room_register"));

    private ForcedDropBlacklist() {}

    static boolean isBlocked(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return BLOCKED_ITEM_IDS.contains(itemId);
    }
}
