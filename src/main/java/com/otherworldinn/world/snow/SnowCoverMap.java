package com.otherworldinn.world.snow;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.level.ChunkPos;

/** 按列记录的覆雪位图：每个区块 256 位（16x16 列）。服务端权威数据与客户端镜像共用。 */
public final class SnowCoverMap {

    /** 区块 → 4 个 long（256 位）。 */
    private final Map<Long, long[]> chunks = new HashMap<>();

    private static int indexOf(int x, int z) {
        return (x & 15) + ((z & 15) << 4);
    }

    public boolean isCovered(int x, int z) {
        long[] bits = chunks.get(ChunkPos.asLong(x >> 4, z >> 4));
        if (bits == null) {
            return false;
        }
        int index = indexOf(x, z);
        long mask = 1L << (index & 63);
        return (bits[index >> 6] & mask) != 0L;
    }

    /** @return 状态是否发生变化 */
    public boolean setCovered(int x, int z, boolean covered) {
        long key = ChunkPos.asLong(x >> 4, z >> 4);
        int index = indexOf(x, z);
        long mask = 1L << (index & 63);
        long[] bits = chunks.get(key);
        if (covered) {
            if (bits == null) {
                bits = new long[4];
                chunks.put(key, bits);
            }
            if ((bits[index >> 6] & mask) != 0L) {
                return false;
            }
            bits[index >> 6] |= mask;
            return true;
        }
        if (bits == null) {
            return false;
        }
        if ((bits[index >> 6] & mask) == 0L) {
            return false;
        }
        bits[index >> 6] &= ~mask;
        return true;
    }

    /** 只读视图（不要改内容），用于"每列都要查一次"的热路径，避免逐列做 map 查找。 */
    public long[] view(int chunkX, int chunkZ) {
        return chunks.get(ChunkPos.asLong(chunkX, chunkZ));
    }

    /** 位图里某一列是否置位。{@code mask} 可以为 null（表示该区块没有数据）。 */
    public static boolean isSet(long[] mask, int x, int z) {
        if (mask == null) {
            return false;
        }
        int index = (x & 15) + ((z & 15) << 4);
        long bit = 1L << (index & 63);
        return (mask[index >> 6] & bit) != 0L;
    }

    public long[] copyMask(ChunkPos pos) {
        long[] bits = chunks.get(ChunkPos.asLong(pos.x, pos.z));
        return bits == null ? new long[4] : bits.clone();
    }

    public void putMask(ChunkPos pos, long[] mask) {
        if (mask.length != 4) {
            return;
        }
        long key = ChunkPos.asLong(pos.x, pos.z);
        boolean empty = true;
        for (long part : mask) {
            if (part != 0L) {
                empty = false;
                break;
            }
        }
        if (empty) {
            chunks.remove(key);
        } else {
            chunks.put(key, mask.clone());
        }
    }

    public java.util.Set<Long> chunkKeys() {
        return chunks.keySet();
    }

    public void clear() {
        chunks.clear();
    }
}
