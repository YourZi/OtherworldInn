package com.otherworldinn.compat.jei;

import com.otherworldinn.world.inn.service.FurnitureManager;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/**
 * JEI"家具属性"配方：一组家具方块 + 相同的家具属性（舒适度/光照度/湿度）。
 * 属性组合相同的方块合并进同一条配方，避免类目页数爆炸。
 */
record FurnitureAttributeJeiRecipe(List<ItemStack> blockStacks, FurnitureManager.FurnitureStats stats) {}
