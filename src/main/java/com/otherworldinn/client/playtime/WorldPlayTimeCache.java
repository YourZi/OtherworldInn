package com.otherworldinn.client.playtime;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

/**
 * 存档累计游玩时长缓存：读取 {@code saves/<levelId>/stats/*.json} 里的
 * {@code stats["minecraft:custom"]["minecraft:play_time"]} 并求和（多人存档里每个玩家一个文件）。
 *
 * <p>异步读取 + 结果缓存，未读完时先返回 0；{@code ClientTickEvent} 里检测到刚进入
 * {@code SelectWorldScreen} 时清空缓存。
 */
public final class WorldPlayTimeCache {

    private static final Map<String, Integer> TICKS = new ConcurrentHashMap<>();

    private static final Set<String> PENDING = ConcurrentHashMap.newKeySet();

    private static Object lastScreen;

    private WorldPlayTimeCache() {}

    /** 由 {@code ClientTickEvent.Post} 调用：进入存档选择界面时清空，避免显示旧数据。 */
    public static void onClientTick() {
        Object screen = Minecraft.getInstance().screen;
        boolean worldSelect =
                screen instanceof net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
        if (worldSelect && screen != lastScreen) {
            TICKS.clear();
            PENDING.clear();
        }
        lastScreen = screen;
    }

    public static int get(String levelId) {
        Integer cached = TICKS.get(levelId);
        if (cached != null) {
            return cached;
        }
        if (PENDING.add(levelId)) {
            Path stats = Minecraft.getInstance()
                    .getLevelSource()
                    .getBaseDir()
                    .resolve(levelId)
                    .resolve("stats");
            Util.backgroundExecutor().execute(() -> TICKS.put(levelId, read(stats)));
        }
        return 0;
    }

    private static int read(Path stats) {
        if (!Files.isDirectory(stats)) {
            return 0;
        }
        List<Path> files;
        try (Stream<Path> stream = Files.list(stats)) {
            files = stream.filter(path -> path.getFileName().toString().endsWith(".json")).toList();
        } catch (Exception ignored) {
            return 0;
        }
        int total = 0;
        for (Path file : files) {
            total += readPlayTime(file);
        }
        return total;
    }

    private static int readPlayTime(Path file) {
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonObject custom =
                    child(child(root.isJsonObject() ? root.getAsJsonObject() : null, "stats"), "minecraft:custom");
            if (custom == null) {
                return 0;
            }
            JsonElement playTime = custom.get("minecraft:play_time");
            return playTime != null && playTime.isJsonPrimitive() ? playTime.getAsInt() : 0;
        } catch (Exception ignored) {
            return 0;
        }
    }

    @Nullable
    private static JsonObject child(@Nullable JsonObject parent, String key) {
        if (parent == null) {
            return null;
        }
        JsonElement element = parent.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }
}
