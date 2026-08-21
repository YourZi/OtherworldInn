package com.otherworldinn.world.data;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 城镇数据
 *
 * <p>存储城镇维度的状态，包括初始建筑生成标记和游商循环状态。
 */
public class TownSavedData extends SavedData {
    private static final String DATA_NAME = "otherworldinn_town";
    private boolean generated = false;

    // === 游商状态 ===
    /** 游商是否正在停留 */
    private boolean traderActive = false;
    /** 游商下次到达的游戏时间（tick） */
    private long nextTraderArrivalTime = 0;
    /** 游商离开的游戏时间（tick），仅在 traderActive 时有意义 */
    private long traderDepartureTime = 0;
    /** 当前游商实体的 UUID，用于服务器重启后恢复 */
    @Nullable
    private UUID traderEntityUuid = null;

    // === 矿井状态 ===
    /** 矿井最近一次填充产出的世界日，-1 表示从未填充 */
    private long lastMineFillDay = -1;

    // === 节日状态 ===
    /** 最近一次广播的节日窗口标识（festivalId@窗口起始世界日），null 表示当前无激活节日 */
    @Nullable
    private String lastFestivalWindow = null;

    public static TownSavedData get(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(TownSavedData::new, TownSavedData::load, null),
                        DATA_NAME);
    }

    public TownSavedData() {}

    public static TownSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        TownSavedData data = new TownSavedData();
        data.generated = tag.getBoolean("generated");
        data.traderActive = tag.getBoolean("traderActive");
        data.nextTraderArrivalTime = tag.getLong("nextTraderArrivalTime");
        data.traderDepartureTime = tag.getLong("traderDepartureTime");
        if (tag.hasUUID("traderEntityUuid")) {
            data.traderEntityUuid = tag.getUUID("traderEntityUuid");
        }
        data.lastMineFillDay =
                tag.contains("lastMineFillDay") ? tag.getLong("lastMineFillDay") : -1;
        data.lastFestivalWindow =
                tag.contains("lastFestivalWindow")
                        ? tag.getString("lastFestivalWindow")
                        : null;
        return data;
    }

    @Override
    public @NotNull CompoundTag save(
            @NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        tag.putBoolean("generated", generated);
        tag.putBoolean("traderActive", traderActive);
        tag.putLong("nextTraderArrivalTime", nextTraderArrivalTime);
        tag.putLong("traderDepartureTime", traderDepartureTime);
        if (traderEntityUuid != null) {
            tag.putUUID("traderEntityUuid", traderEntityUuid);
        }
        tag.putLong("lastMineFillDay", lastMineFillDay);
        if (lastFestivalWindow != null) {
            tag.putString("lastFestivalWindow", lastFestivalWindow);
        }
        return tag;
    }

    // === 建筑生成 ===

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
        this.setDirty();
    }

    // === 游商状态 ===

    public boolean isTraderActive() {
        return traderActive;
    }

    public long getNextTraderArrivalTime() {
        return nextTraderArrivalTime;
    }

    public long getTraderDepartureTime() {
        return traderDepartureTime;
    }

    @Nullable
    public UUID getTraderEntityUuid() {
        return traderEntityUuid;
    }

    public void setTraderEntityUuid(@Nullable UUID uuid) {
        this.traderEntityUuid = uuid;
        this.setDirty();
    }

    /** 游商到达：标记为活跃，记录离开时间 */
    public void setTraderActive(long departureTime) {
        this.traderActive = true;
        this.traderDepartureTime = departureTime;
        this.setDirty();
    }

    /** 游商离开：标记为非活跃，设置下次到达时间 */
    public void setTraderInactive(long nextArrivalTime) {
        this.traderActive = false;
        this.traderDepartureTime = 0;
        this.nextTraderArrivalTime = nextArrivalTime;
        this.setDirty();
    }

    // === 矿井状态 ===

    public long getLastMineFillDay() {
        return lastMineFillDay;
    }

    public void setLastMineFillDay(long lastMineFillDay) {
        this.lastMineFillDay = lastMineFillDay;
        this.setDirty();
    }

    // === 节日状态 ===

    @Nullable
    public String getLastFestivalWindow() {
        return lastFestivalWindow;
    }

    public void setLastFestivalWindow(@Nullable String lastFestivalWindow) {
        this.lastFestivalWindow = lastFestivalWindow;
        this.setDirty();
    }
}
