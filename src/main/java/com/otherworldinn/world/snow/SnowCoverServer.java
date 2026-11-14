package com.otherworldinn.world.snow;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.network.packet.S2CSnowCoverPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 真实覆雪的服务端权威部分。
 *
 * <p>只做三件事：
 * <ol>
 *   <li><b>记录</b>：{@link #tryCover} 由 {@code ServerLevel#tickPrecipitation} 的注入调用，
 *       在"原版雪层放不下"的列上记一位。节奏完全等于原版随机刻；</li>
 *   <li><b>同步</b>：变化过的区块把 256 位位图推给正在跟踪它的玩家，玩家开始跟踪时补发一次；</li>
 *   <li><b>融化</b>：季节融化交给 Serene Seasons（{@link #meltColumn}，由
 *       {@code MixinRandomUpdateHandlerSnowMelt} 调用）；这里只保留原版雪层自己的
 *       "方块光 &gt; 11 才化"这条规则。</li>
 * </ol>
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class SnowCoverServer {

    /** 与原版 {@code SnowLayerBlock.randomTick} 一致：方块光 > 11 才化。 */
    private static final int MELT_BLOCK_LIGHT = 11;
    /** 每轮融雪检查最多扫几个区块，摊开开销。 */
    private static final int MELT_CHUNKS_PER_PASS = 32;

    private static int meltCursor;

    private SnowCoverServer() {}

    /** 原版随机刻抽中某一列、判定"能不能托住一个完整雪层"时调用。 */
    public static void tryCover(ServerLevel level, BlockPos columnPos) {
        BlockPos snowPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, columnPos);
        if (SnowFallConditions.decide(level, snowPos) != SnowFallConditions.Decision.COVER) {
            return;
        }
        SnowCoverData.get(level).setCovered(snowPos.getX(), snowPos.getZ(), true);
    }

    /** 玩家开始跟踪某个区块：把该区块的位图发过去。积雪本身只来自随机刻，这里不补雪。 */
    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Watch event) {
        ServerLevel level = event.getLevel();
        ChunkPos pos = event.getPos();
        long[] mask = SnowCoverData.get(level).map().copyMask(pos);
        for (long part : mask) {
            if (part != 0L) {
                PacketDistributor.sendToPlayer(event.getPlayer(), new S2CSnowCoverPacket(pos, mask));
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        for (ServerLevel level : server.getAllLevels()) {
            flush(level);
        }
        if (server.getTickCount() % 20 == 0) {
            for (ServerLevel level : server.getAllLevels()) {
                melt(level);
            }
        }
    }

    /** 把本 tick 变化过的区块推给跟踪它的玩家。没人跟踪时发送是空操作。 */
    private static void flush(ServerLevel level) {
        SnowCoverData data = SnowCoverData.get(level);
        Set<Long> changed = data.drainPendingSync();
        for (long key : changed) {
            ChunkPos pos = new ChunkPos(key);
            PacketDistributor.sendToPlayersTrackingChunk(
                    level, pos, new S2CSnowCoverPacket(pos, data.map().copyMask(pos)));
        }
    }

    /**
     * 由 Serene Seasons 的融雪流程调用：它选中某一列要融化时，我们的覆雪也一起化。
     *
     * <p>概率、频率、季节开关全部来自 SS 的 {@code melt_rolls}/{@code melt_percent}，
     * 这里不做任何自己的判定，所以节奏和原版雪层完全一致。
     */
    public static void meltColumn(ServerLevel level, BlockPos snowPos) {
        if (snowPos == null) {
            return;
        }
        SnowCoverData data = SnowCoverData.get(level);
        data.setCovered(snowPos.getX(), snowPos.getZ(), false);
    }

    /**
     * 原版雪层自己的融化规则：方块光 &gt; 11。再按"位置 + 游戏时间"掷 1/8，
     * 避免一整片同时化掉。季节融化不在这里。
     */
    private static void melt(ServerLevel level) {
        SnowCoverData data = SnowCoverData.get(level);
        List<Long> keys = new ArrayList<>(data.map().chunkKeys());
        if (keys.isEmpty()) {
            return;
        }
        int count = Math.min(keys.size(), MELT_CHUNKS_PER_PASS);
        for (int i = 0; i < count; i++) {
            ChunkPos pos = new ChunkPos(keys.get(Math.floorMod(meltCursor + i, keys.size())));
            if (!level.hasChunk(pos.x, pos.z)) {
                continue;
            }
            long[] mask = data.map().view(pos.x, pos.z);
            if (mask == null) {
                continue;
            }
            for (int index = 0; index < 256; index++) {
                long bit = 1L << (index & 63);
                if ((mask[index >> 6] & bit) == 0L) {
                    continue;
                }
                int x = pos.getMinBlockX() + (index & 15);
                int z = pos.getMinBlockZ() + (index >> 4);
                BlockPos snowPos = new BlockPos(
                        x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) + 1, z);
                if (level.getBrightness(LightLayer.BLOCK, snowPos) <= MELT_BLOCK_LIGHT) {
                    continue;
                }
                long roll = SnowHash.column(x, z) ^ (level.getGameTime() >>> 4);
                if ((roll & 7L) != 0L) {
                    continue;
                }
                // setCovered 会记进 pendingSync，下一 tick 推给客户端
                data.setCovered(x, z, false);
            }
        }
        meltCursor += count;
    }
}
