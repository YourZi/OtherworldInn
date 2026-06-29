package com.otherworldinn.world.commission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

@Data
public class TeamCommissionData {
    private final List<CommissionEntry> boardEntries = new ArrayList<>();
    private final Map<String, Integer> killProgress = new HashMap<>();
    private final Set<String> photoProgress = new HashSet<>();
    private int acceptedIndex = -1;
    private long acceptedDay = -1L;
    private long acceptedOrder = Long.MAX_VALUE;
    private long expireDay = -1L;
    private long nextAutoRefreshDay = 0L;
    private long refreshSequence = 0L;
    private boolean rewardClaimed = false;
    private int completedCount = 0;

    public boolean hasAccepted() {
        return acceptedIndex >= 0 && acceptedIndex < boardEntries.size();
    }

    public CommissionEntry getAcceptedEntry() {
        if (!hasAccepted()) {
            return null;
        }
        return boardEntries.get(acceptedIndex);
    }

    public void resetAcceptedState() {
        this.acceptedIndex = -1;
        this.acceptedDay = -1L;
        this.acceptedOrder = Long.MAX_VALUE;
        this.expireDay = -1L;
        this.rewardClaimed = false;
        this.killProgress.clear();
        this.photoProgress.clear();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        ListTag boardTag = new ListTag();
        for (CommissionEntry entry : boardEntries) {
            boardTag.add(entry.save());
        }
        tag.put("BoardEntries", boardTag);

        ListTag killProgressTag = new ListTag();
        for (Map.Entry<String, Integer> entry : killProgress.entrySet()) {
            CompoundTag progressTag = new CompoundTag();
            progressTag.putString("EntityId", entry.getKey());
            progressTag.putInt("Count", entry.getValue());
            killProgressTag.add(progressTag);
        }
        tag.put("KillProgress", killProgressTag);

        ListTag photoProgressTag = new ListTag();
        for (String objectiveId : photoProgress) {
            photoProgressTag.add(StringTag.valueOf(objectiveId));
        }
        tag.put("PhotoProgress", photoProgressTag);

        tag.putInt("AcceptedIndex", acceptedIndex);
        tag.putLong("AcceptedDay", acceptedDay);
        tag.putLong("AcceptedOrder", acceptedOrder);
        tag.putLong("ExpireDay", expireDay);
        tag.putLong("NextAutoRefreshDay", nextAutoRefreshDay);
        tag.putLong("RefreshSequence", refreshSequence);
        tag.putBoolean("RewardClaimed", rewardClaimed);
        tag.putInt("CompletedCount", Math.max(0, completedCount));
        return tag;
    }

    public void load(CompoundTag tag) {
        boardEntries.clear();
        if (tag.contains("BoardEntries", Tag.TAG_LIST)) {
            ListTag boardTag = tag.getList("BoardEntries", Tag.TAG_COMPOUND);
            for (Tag t : boardTag) {
                if (t instanceof CompoundTag entryTag) {
                    boardEntries.add(CommissionEntry.load(entryTag));
                }
            }
        }

        killProgress.clear();
        if (tag.contains("KillProgress", Tag.TAG_LIST)) {
            ListTag killProgressTag = tag.getList("KillProgress", Tag.TAG_COMPOUND);
            for (Tag t : killProgressTag) {
                if (t instanceof CompoundTag progressTag) {
                    String entityId = progressTag.getString("EntityId");
                    int count = progressTag.getInt("Count");
                    if (!entityId.isBlank() && count > 0) {
                        killProgress.put(entityId, count);
                    }
                }
            }
        }

        photoProgress.clear();
        if (tag.contains("PhotoProgress", Tag.TAG_LIST)) {
            ListTag photoProgressTag = tag.getList("PhotoProgress", Tag.TAG_STRING);
            for (Tag t : photoProgressTag) {
                String objectiveId = t.getAsString();
                if (!objectiveId.isBlank()) {
                    photoProgress.add(objectiveId);
                }
            }
        }

        acceptedIndex = tag.contains("AcceptedIndex", Tag.TAG_INT) ? tag.getInt("AcceptedIndex") : -1;
        acceptedDay = tag.contains("AcceptedDay", Tag.TAG_LONG) ? tag.getLong("AcceptedDay") : -1L;
        acceptedOrder = tag.contains("AcceptedOrder", Tag.TAG_LONG) ? tag.getLong("AcceptedOrder") : Long.MAX_VALUE;
        expireDay = tag.contains("ExpireDay", Tag.TAG_LONG) ? tag.getLong("ExpireDay") : -1L;
        nextAutoRefreshDay =
                tag.contains("NextAutoRefreshDay", Tag.TAG_LONG)
                        ? tag.getLong("NextAutoRefreshDay")
                        : 0L;
        refreshSequence =
                tag.contains("RefreshSequence", Tag.TAG_LONG)
                        ? tag.getLong("RefreshSequence")
                        : 0L;
        rewardClaimed = tag.getBoolean("RewardClaimed");
        completedCount = tag.contains("CompletedCount", Tag.TAG_INT) ? Math.max(0, tag.getInt("CompletedCount")) : 0;

        if (!hasAccepted()) {
            resetAcceptedState();
        }
    }
}
