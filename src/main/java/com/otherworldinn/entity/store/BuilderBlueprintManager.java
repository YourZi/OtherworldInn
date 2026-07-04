package com.otherworldinn.entity.store;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.SchematicSurvivalPrintHelper;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.AllItems;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import com.simibubi.create.foundation.utility.CreatePaths;

/**
 * 为建筑工商店生成带不同结构数据和生存打印标记的 Create 蓝图商品。
 * 每日随机选取 4 个不重复的蓝图。
 */
public final class BuilderBlueprintManager {
    private static final String BUILDER_BLUEPRINT_ID_KEY = "otherworldinn:builder_blueprint_id";
    private static final String BUILDER_BLUEPRINT_DISPLAY_KEY = "otherworldinn:builder_blueprint_display";
    private static final String BLUEPRINT_OWNER = "otherworldinn_builder_shop";

    private static final String BLUEPRINT_PATH = "data/otherworldinn/builder_blueprints/%s.nbt";

    public record BlueprintEntry(
            String id,
            String displayName,
            String resourcePath,
            String schematicFileName,
            int minPrice,
            int maxPrice) {}

    private static final List<BlueprintEntry> POOL;

    static {
        List<BlueprintEntry> list = new ArrayList<>();

        list.add(load("large_inn_3f_by_xiao_zhan", "3层大型旅社_by_xiao_zhan", 240, 320));
        list.add(load("large_inn_3f_by_summyao", "大型旅社_summyao", 250, 300));
        list.add(load("small_flower_mountain", "小花房_mountain", 80, 120));
        list.add(load("sunflower_mianbao", "阳光花房_面包面包", 90, 130));
        list.add(load("old_windmill_mianbao", "破旧的风车磨坊_面包面包", 60, 120));
        list.add(load("sakura_mianbao", "樱花池_面包面包", 160, 240));

        POOL = List.copyOf(list);
    }

    private BuilderBlueprintManager() {}

    private static BlueprintEntry load(String id, String displayName, int minPrice, int maxPrice) {
        String resourcePath = String.format(BLUEPRINT_PATH, id);
        String fileName = id + ".nbt";
        try (InputStream in =
                BuilderBlueprintManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                OtherworldInn.LOGGER.warn("Missing builder blueprint resource: {}", resourcePath);
            }
        } catch (IOException e) {
            OtherworldInn.LOGGER.warn("Failed to inspect builder blueprint: {}", resourcePath, e);
        }
        return new BlueprintEntry(id, displayName, resourcePath, fileName, minPrice, maxPrice);
    }

    /**
     * 导出模组内置蓝图到 Create 原生 schematic 目录，供文件型蓝图物品读取。
     */
    public static void exportBlueprintFiles() {
        for (BlueprintEntry entry : POOL) {
            exportBlueprintFile(entry, CreatePaths.SCHEMATICS_DIR.resolve(entry.schematicFileName()));
            exportBlueprintFile(
                    entry,
                    CreatePaths.UPLOADED_SCHEMATICS_DIR
                            .resolve(BLUEPRINT_OWNER)
                            .resolve(entry.schematicFileName()));
        }
    }

    public static ItemStack createStorePreviewStack(BlueprintEntry entry) {
        ItemStack stack = new ItemStack(AllItems.EMPTY_SCHEMATIC.get());
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(BUILDER_BLUEPRINT_ID_KEY, entry.id());
        tag.putString(BUILDER_BLUEPRINT_DISPLAY_KEY, entry.displayName());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(entry.displayName()).withStyle(ChatFormatting.AQUA));
        return stack;
    }

    public static ItemStack createPurchasedStackFromPreview(Level level, ItemStack previewStack) {
        String blueprintId = getBlueprintId(previewStack);
        if (blueprintId == null) {
            return previewStack.copy();
        }
        BlueprintEntry entry = findById(blueprintId);
        return entry != null ? createFileBackedSchematicStack(level, entry) : previewStack.copy();
    }

    public static String getBlueprintId(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return tag.contains(BUILDER_BLUEPRINT_ID_KEY) ? tag.getString(BUILDER_BLUEPRINT_ID_KEY) : null;
    }

    private static ItemStack createFileBackedSchematicStack(Level level, BlueprintEntry entry) {
        ItemStack stack = SchematicItem.create(level, entry.schematicFileName(), BLUEPRINT_OWNER);
        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(entry.displayName()).withStyle(ChatFormatting.AQUA));
        SchematicSurvivalPrintHelper.enable(stack);
        return stack;
    }

    private static void exportBlueprintFile(BlueprintEntry entry, Path targetPath) {
        try (InputStream in =
                BuilderBlueprintManager.class.getClassLoader().getResourceAsStream(entry.resourcePath())) {
            if (in == null) {
                OtherworldInn.LOGGER.warn(
                        "Failed to export builder blueprint {} because resource is missing",
                        entry.resourcePath());
                return;
            }
            Files.createDirectories(targetPath.getParent());
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            OtherworldInn.LOGGER.warn(
                    "Failed to export builder blueprint {} to {}",
                    entry.resourcePath(),
                    targetPath,
                    e);
        }
    }

    private static BlueprintEntry findById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        for (BlueprintEntry entry : POOL) {
            if (entry.id().equals(id)) {
                return entry;
            }
        }
        return null;
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
