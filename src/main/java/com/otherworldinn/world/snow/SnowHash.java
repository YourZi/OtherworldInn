package com.otherworldinn.world.snow;

import net.minecraft.core.BlockPos;

/** 覆雪用到的稳定哈希：厚度、风雪渗透、融雪掷骰都从这里取，保证同一位置每次结果一致。 */
public final class SnowHash {

    private static final long GOLDEN = 0x9E3779B97F4A7C15L;

    private SnowHash() {}

    /** murmur3 尾混。 */
    public static long mix(long value) {
        long hash = value;
        hash ^= hash >>> 33;
        hash *= 0xFF51AFD7ED558CCDL;
        hash ^= hash >>> 33;
        hash *= 0xC4CEB9FE1A85EC53L;
        hash ^= hash >>> 33;
        return hash;
    }

    /** 按列（x,z）取哈希。 */
    public static long column(int x, int z) {
        return mix(((long) x << 32) ^ (z & 0xFFFFFFFFL));
    }

    /** 按"方块位置 + 表面高度"取哈希（同一块雪面永远同一个值）。 */
    public static long surface(BlockPos pos, float surfaceY) {
        return mix(pos.asLong() ^ (Math.round(surfaceY * 16.0F) * GOLDEN));
    }
}
