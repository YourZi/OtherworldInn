package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.SchematicSurvivalPrintHelper;
import com.simibubi.create.AllItems;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 为建筑工商店生成带不同结构数据和生存打印标记的 Create 蓝图商品。
 * 每日随机选取 4 个不重复的蓝图。
 */
public final class BuilderBlueprintManager {

    private static final String BLUEPRINT_PATH = "data/otherworldinn/builder_blueprints/%s.nbt";

    public record BlueprintEntry(
            String id, CompoundTag schematicNbt, int minPrice, int maxPrice) {}

    private static final List<BlueprintEntry> POOL;

    static {
        List<BlueprintEntry> list = new ArrayList<>();
        list.add(load("farmhouse", 32, 48));
        list.add(load("watchtower", 28, 42));
        list.add(load("bridge", 24, 36));
        list.add(load("well", 14, 20));
        list.add(load("market_stall", 18, 28));
        list.add(load("statue", 22, 34));
        list.add(load("fountain", 26, 40));
        list.add(load("gazebo", 30, 46));
        POOL = List.copyOf(list);
    }

    private BuilderBlueprintManager() {}

    private static BlueprintEntry load(String id, int minPrice, int maxPrice) {
        String path = String.format(BLUEPRINT_PATH, id);
        CompoundTag nbt = null;
        try (InputStream in = BuilderBlueprintManager.class.getClassLoader().getResourceAsStream(path)) {
            if (in != null) {
                nbt = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
            }
        } catch (Exception e) {
            OtherworldInn.LOGGER.warn("Failed to load builder blueprint: {}", path, e);
        }
        return new BlueprintEntry(id, nbt, minPrice, maxPrice);
    }

    /**
     * 创建带 NBT 和生存打印标记的 Create Schematic ItemStack
     */
    public static ItemStack createSchematicStack(BlueprintEntry entry) {
        ItemStack stack = new ItemStack(AllItems.SCHEMATIC.get());
        if (entry.schematicNbt() != null) {
            var tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            tag.put("Create:Schematic", entry.schematicNbt());
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.of(tag));
        }
        SchematicSurvivalPrintHelper.enable(stack);
        return stack;
    }

    /**
     * 每日随机选取不超过 count 个蓝图
     */
    public static List<BlueprintEntry> pickDailyRandom(int count, long daySeed) {
        List<BlueprintEntry> pool = new ArrayList<>(POOL);
        Collections.shuffle(pool, new java.util.Random(daySeed));
        int limit = Math.min(count, pool.size());
        return pool.subList(0, limit);
    }
}
