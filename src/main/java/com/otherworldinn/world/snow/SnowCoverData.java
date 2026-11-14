package com.otherworldinn.world.snow;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 服务端保存的覆雪数据：哪些列已经由"真实覆雪"接住（原版雪层放不下的那些）。
 *
 * <p>它是原版降雪的**副产物**——只有在 {@code ServerLevel#tickPrecipitation} 抽中某一列、
 * 且判定失败的原因是"托不住一整个雪层"时才会写下一位，所以时序天然和原版一致（随机刻驱动、循序渐进）。
 */
public class SnowCoverData extends SavedData {

    private static final String NAME = "otherworldinn_snow_cover";

    private final SnowCoverMap map = new SnowCoverMap();
    /** 这一 tick 里变化过、还没推给客户端的区块。 */
    private final Set<Long> pendingSync = new HashSet<>();

    /** 按维度缓存一层：这个调用在随机刻热路径上，{@code computeIfAbsent} 每次都要新建 Factory。 */
    private static final Map<ServerLevel, SnowCoverData> CACHE = new WeakHashMap<>();

    public static SnowCoverData get(ServerLevel level) {
        SnowCoverData cached = CACHE.get(level);
        if (cached != null) {
            return cached;
        }
        SnowCoverData data = level.getDataStorage()
                .computeIfAbsent(new SavedData.Factory<>(SnowCoverData::new, SnowCoverData::load), NAME);
        CACHE.put(level, data);
        return data;
    }

    private static SnowCoverData load(CompoundTag tag, HolderLookup.Provider registries) {
        SnowCoverData data = new SnowCoverData();
        ListTag list = tag.getList("chunks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ChunkPos pos = new ChunkPos(entry.getInt("x"), entry.getInt("z"));
            data.map.putMask(pos, entry.getLongArray("mask"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (long key : map.chunkKeys()) {
            ChunkPos pos = new ChunkPos(key);
            long[] mask = map.copyMask(pos);
            CompoundTag entry = new CompoundTag();
            entry.putInt("x", pos.x);
            entry.putInt("z", pos.z);
            entry.putLongArray("mask", mask);
            list.add(entry);
        }
        tag.put("chunks", list);
        return tag;
    }

    public SnowCoverMap map() {
        return map;
    }

    public boolean isCovered(int x, int z) {
        return map.isCovered(x, z);
    }

    public void setCovered(int x, int z, boolean covered) {
        if (map.setCovered(x, z, covered)) {
            pendingSync.add(ChunkPos.asLong(x >> 4, z >> 4));
            setDirty();
        }
    }

    /** 取出本 tick 变化过的区块（清空）。 */
    public Set<Long> drainPendingSync() {
        if (pendingSync.isEmpty()) {
            return Set.of();
        }
        Set<Long> copy = new HashSet<>(pendingSync);
        pendingSync.clear();
        return copy;
    }
}
