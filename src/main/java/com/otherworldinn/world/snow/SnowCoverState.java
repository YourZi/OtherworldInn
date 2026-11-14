package com.otherworldinn.world.snow;

import net.minecraft.world.level.ChunkPos;

/**
 * 覆雪位图的镜像：服务端把"哪些列已经积上雪"同步过来，渲染时按列查这张表。
 *
 * <p>为什么要服务端说了算：原版积雪是服务端随机刻逐个长的，客户端看不到骰子结果。
 * 只有让服务端在同一处判定里写下结果，时序才会和原版一致（循序渐进、而不是一下雪瞬间全白），
 * 也才能持久化（雪停之后不会立刻消失）。
 *
 * <p>本类只持有数据、不碰渲染，因此放在 {@code world.snow} 包（服务端也会加载它）。
 * 收包后"重建对应区块段"属于客户端行为，由 {@code S2CSnowCoverPacket#handle} 自己调用，
 * 这样这里就不会 import 任何 {@code client.*} 的类。
 */
public final class SnowCoverState {

    private static final SnowCoverMap MAP = new SnowCoverMap();

    private SnowCoverState() {}

    public static boolean isCovered(int x, int z) {
        return MAP.isCovered(x, z);
    }

    /** 某个区块的只读位图（可空）。渲染时"一次取、逐列查"，避免每列都查 map。 */
    public static long[] view(int chunkX, int chunkZ) {
        return MAP.view(chunkX, chunkZ);
    }

    /** 收到服务端同步：整块覆盖（重建区块段由收包的客户端逻辑负责）。 */
    public static void put(ChunkPos pos, long[] mask) {
        MAP.putMask(pos, mask);
    }

    /** 有数据的区块数（调试用）。 */
    public static int coveredChunks() {
        return MAP.chunkKeys().size();
    }

    /** 某个区块里被覆盖的列数（调试用）。 */
    public static int coveredCount(int chunkX, int chunkZ) {
        int count = 0;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                if (MAP.isCovered(chunkX * 16 + dx, chunkZ * 16 + dz)) {
                    count++;
                }
            }
        }
        return count;
    }

    public static void clear() {
        MAP.clear();
    }
}
