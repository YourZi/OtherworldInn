package com.otherworldinn.compat.jei;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record NpcStoreJeiRecipe(
        String storeNameKey,
        ItemStack output,
        int minPrice,
        int maxPrice,
        int minStock,
        int maxStock,
        int requiredFavorLevel,
        @Nullable String requiredAdvancementTitleKey,
        boolean randomOffer,
        @Nullable String festivalId) {

    public NpcStoreJeiRecipe {
        minPrice = Math.max(1, minPrice);
        maxPrice = Math.max(minPrice, maxPrice);
        minStock = Math.max(-1, minStock);
        maxStock = Math.max(minStock, maxStock);
        requiredFavorLevel = Math.max(1, requiredFavorLevel);
        if (output == null || output.isEmpty()) {
            throw new IllegalArgumentException("output cannot be empty");
        }
    }
}
