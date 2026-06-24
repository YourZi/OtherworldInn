package com.otherworldinn.world.storyguest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public class StoryGuestProgress {
    private int storyStage = 0;
    private long lastCheckoutDay = Long.MIN_VALUE;
    private long nextEligibleVisitDay = Long.MIN_VALUE;
    @Nullable private UUID activeEntityUuid;
    private int visitCount = 0;
    private final Set<String> storyFlags = new HashSet<>();
    private int pendingMinReturnDays = -1;
    private int pendingMaxReturnDays = -1;

    public static StoryGuestProgress load(CompoundTag tag) {
        StoryGuestProgress progress = new StoryGuestProgress();
        progress.storyStage = tag.getInt("StoryStage");
        if (tag.contains("LastCheckoutDay")) {
            progress.lastCheckoutDay = tag.getLong("LastCheckoutDay");
        }
        if (tag.contains("NextEligibleVisitDay")) {
            progress.nextEligibleVisitDay = tag.getLong("NextEligibleVisitDay");
        }
        if (tag.hasUUID("ActiveEntityUuid")) {
            progress.activeEntityUuid = tag.getUUID("ActiveEntityUuid");
        }
        progress.visitCount = Math.max(0, tag.getInt("VisitCount"));
        if (tag.contains("StoryFlags", Tag.TAG_LIST)) {
            ListTag flags = tag.getList("StoryFlags", Tag.TAG_STRING);
            for (Tag flagTag : flags) {
                progress.storyFlags.add(flagTag.getAsString());
            }
        }
        progress.pendingMinReturnDays = tag.contains("PendingMinReturnDays")
                ? tag.getInt("PendingMinReturnDays")
                : -1;
        progress.pendingMaxReturnDays = tag.contains("PendingMaxReturnDays")
                ? tag.getInt("PendingMaxReturnDays")
                : -1;
        return progress;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("StoryStage", this.storyStage);
        if (this.lastCheckoutDay != Long.MIN_VALUE) {
            tag.putLong("LastCheckoutDay", this.lastCheckoutDay);
        }
        if (this.nextEligibleVisitDay != Long.MIN_VALUE) {
            tag.putLong("NextEligibleVisitDay", this.nextEligibleVisitDay);
        }
        if (this.activeEntityUuid != null) {
            tag.putUUID("ActiveEntityUuid", this.activeEntityUuid);
        }
        tag.putInt("VisitCount", this.visitCount);
        if (!this.storyFlags.isEmpty()) {
            ListTag flags = new ListTag();
            for (String storyFlag : this.storyFlags) {
                flags.add(StringTag.valueOf(storyFlag));
            }
            tag.put("StoryFlags", flags);
        }
        if (this.pendingMinReturnDays >= 0) {
            tag.putInt("PendingMinReturnDays", this.pendingMinReturnDays);
        }
        if (this.pendingMaxReturnDays >= 0) {
            tag.putInt("PendingMaxReturnDays", this.pendingMaxReturnDays);
        }
        return tag;
    }

    public int getStoryStage() {
        return storyStage;
    }

    public void setStoryStage(int storyStage) {
        this.storyStage = Math.max(0, storyStage);
    }

    public long getLastCheckoutDay() {
        return lastCheckoutDay;
    }

    public void setLastCheckoutDay(long lastCheckoutDay) {
        this.lastCheckoutDay = lastCheckoutDay;
    }

    public long getNextEligibleVisitDay() {
        return nextEligibleVisitDay;
    }

    public void setNextEligibleVisitDay(long nextEligibleVisitDay) {
        this.nextEligibleVisitDay = nextEligibleVisitDay;
    }

    @Nullable
    public UUID getActiveEntityUuid() {
        return activeEntityUuid;
    }

    public void setActiveEntityUuid(@Nullable UUID activeEntityUuid) {
        this.activeEntityUuid = activeEntityUuid;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void incrementVisitCount() {
        this.visitCount++;
    }

    public boolean hasFlag(String storyFlag) {
        return this.storyFlags.contains(storyFlag);
    }

    public void addFlag(String storyFlag) {
        if (storyFlag != null && !storyFlag.isBlank()) {
            this.storyFlags.add(storyFlag);
        }
    }

    public Set<String> getStoryFlags() {
        return storyFlags;
    }

    public int getPendingMinReturnDays() {
        return pendingMinReturnDays;
    }

    public int getPendingMaxReturnDays() {
        return pendingMaxReturnDays;
    }

    public void setPendingReturnRange(int minDays, int maxDays) {
        this.pendingMinReturnDays = Math.max(0, minDays);
        this.pendingMaxReturnDays = Math.max(this.pendingMinReturnDays, maxDays);
    }

    public void clearPendingReturnRange() {
        this.pendingMinReturnDays = -1;
        this.pendingMaxReturnDays = -1;
    }
}
