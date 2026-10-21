package com.otherworldinn.client.playtime;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ClientConfig;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 服务器累计游玩时长：纯客户端本地统计，按服务器地址（{@code ServerData.ip}）逐 tick 累加并持久化：
 * 每个 {@code ClientTickEvent.Post} 累加 1，每 6000 tick（5 分钟）异步落盘一次，
 * 断线（{@code LoggingOut}）时立即落盘；存储于
 * {@code config/otherworldinn_server_playtime.properties}（key = 服务器地址，value = tick 数）。
 *
 * <p>不读取服务器数据。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public final class ServerPlayTimeTracker {

    private static final String FILE_NAME = "otherworldinn_server_playtime.properties";

    private static final int SAVE_INTERVAL_TICKS = 6000;

    private static final Map<String, Integer> TICKS = new HashMap<>();

    private static boolean loaded = false;

    private static int ticksSinceSave = 0;

    private ServerPlayTimeTracker() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        // 存档列表缓存随界面切换清理
        WorldPlayTimeCache.onClientTick();
        if (!ClientConfig.showServerPlayTime()) {
            return;
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return;
        }
        ServerData serverData = connection.getServerData();
        if (serverData == null || serverData.ip == null) {
            return;
        }
        load();
        TICKS.merge(serverData.ip, 1, Integer::sum);
        if (++ticksSinceSave >= SAVE_INTERVAL_TICKS) {
            ticksSinceSave = 0;
            saveAsync();
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        if (ticksSinceSave > 0) {
            ticksSinceSave = 0;
            saveAsync();
        }
    }

    public static int get(String serverIp) {
        if (serverIp == null) {
            return 0;
        }
        load();
        return TICKS.getOrDefault(serverIp, 0);
    }

    private static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    private static void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        Path path = path();
        if (!Files.exists(path)) {
            return;
        }
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        } catch (Exception ignored) {
            return;
        }
        for (String serverIp : properties.stringPropertyNames()) {
            try {
                TICKS.put(serverIp, Integer.parseInt(properties.getProperty(serverIp)));
            } catch (NumberFormatException ignored) {
                // 跳过无法解析的条目
            }
        }
    }

    private static void saveAsync() {
        Properties properties = new Properties();
        TICKS.forEach((serverIp, ticks) -> properties.setProperty(serverIp, String.valueOf(ticks)));
        Util.backgroundExecutor().execute(() -> {
            try (Writer writer = Files.newBufferedWriter(path())) {
                properties.store(writer, "Otherworld Inn Server Play Time");
            } catch (Exception ignored) {
                // 落盘失败不影响游玩
            }
        });
    }
}
