package com.otherworldinn.client.snow;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ClientConfig;
import com.otherworldinn.world.snow.SnowCoverState;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * 真实覆雪的客户端开关与刷新入口。
 *
 * <p>两个配置项：{@code snowCover.enabled}（总开关）与 {@code snowCover.force}（强制覆雪）。
 * 游戏内可用模组配置界面或 {@code /innsnow} 切换，切换后重建视野内的相关区块段。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class SnowCoverController {

    /** 刷新时从列顶往下失效多少格，与 {@code SnowCoverRenderer#MAX_DEPTH} 保持一致。 */
    private static final int REFRESH_DEPTH = 24;

    private SnowCoverController() {}

    /** 当前是否开启真实覆雪（配置尚未加载时按默认开启）。 */
    public static boolean isEnabled() {
        return ClientConfig.snowCover();
    }

    /** 是否强制覆雪：开启时无视天气；关闭时由服务端同步过来的数据决定。 */
    public static boolean isForced() {
        return ClientConfig.snowCoverForce();
    }

    /** 切换总开关：写回配置并刷新画面。 */
    public static void setEnabled(boolean enabled) {
        ClientConfig.setSnowCover(enabled);
        SnowCoverState.clear();
        refresh();
    }

    /** 切换强制覆雪：写回配置并刷新画面。 */
    public static void setForced(boolean force) {
        ClientConfig.setSnowCoverForce(force);
        SnowCoverState.clear();
        refresh();
    }

    /** 退出世界/切换维度：清空客户端镜像，等新的一批同步。 */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            SnowCoverState.clear();
        }
    }

    /** 收到服务端新数据后，让某个区块里可能覆雪的段失效。 */
    public static void markChunkDirty(int chunkX, int chunkZ) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LevelRenderer levelRenderer = minecraft.levelRenderer;
        if (level == null || levelRenderer == null) {
            return;
        }
        LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
        if (chunk == null) {
            return;
        }
        int[] range = topRange(chunk, chunkX, chunkZ);
        if (range == null) {
            return;
        }
        int from = SectionPos.blockToSectionCoord(range[0] - REFRESH_DEPTH);
        int to = SectionPos.blockToSectionCoord(range[1]);
        for (int sectionY = from; sectionY <= to; sectionY++) {
            levelRenderer.setSectionDirty(chunkX, sectionY, chunkZ);
        }
    }

    /** 让视野内的段失效（开关、强制模式切换时用）。 */
    public static void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LevelRenderer levelRenderer = minecraft.levelRenderer;
        if (level == null || levelRenderer == null) {
            return;
        }
        ChunkPos center = minecraft.player != null ? minecraft.player.chunkPosition() : ChunkPos.ZERO;
        int radius = Math.min(minecraft.options.getEffectiveRenderDistance(), 12);
        LongOpenHashSet dirty = new LongOpenHashSet();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int chunkX = center.x + dx;
                int chunkZ = center.z + dz;
                LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
                if (chunk == null) {
                    continue;
                }
                int[] range = topRange(chunk, chunkX, chunkZ);
                if (range == null) {
                    continue;
                }
                int from = SectionPos.blockToSectionCoord(range[0] - REFRESH_DEPTH);
                int to = SectionPos.blockToSectionCoord(range[1]);
                for (int sectionY = from; sectionY <= to; sectionY++) {
                    dirty.add(SectionPos.asLong(chunkX, sectionY, chunkZ));
                }
            }
        }
        for (long packed : dirty.toLongArray()) {
            levelRenderer.setSectionDirty(SectionPos.x(packed), SectionPos.y(packed), SectionPos.z(packed));
        }
    }

    /** 区块内各列最高方块的最小/最大 y；雪可能出现在列顶往下好几格，所以按这个范围失效。 */
    private static int[] topRange(LevelChunk chunk, int chunkX, int chunkZ) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int top = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, chunkX * 16 + x, chunkZ * 16 + z);
                min = Math.min(min, top);
                max = Math.max(max, top);
            }
        }
        return min > max ? null : new int[] { min, max };
    }
}
