package com.otherworldinn.world.storyguest;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class StoryGuestSavedData extends SavedData {
    private static final String DATA_NAME = "otherworldinn_story_guests";

    private final Map<String, StoryGuestProgress> progressById = new HashMap<>();

    public static StoryGuestSavedData get(ServerLevel level) {
        return level.getServer()
                .overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new SavedData.Factory<>(StoryGuestSavedData::new, StoryGuestSavedData::load, null),
                        DATA_NAME);
    }

    public static StoryGuestSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        StoryGuestSavedData data = new StoryGuestSavedData();
        if (tag.contains("StoryGuests", Tag.TAG_LIST)) {
            ListTag guests = tag.getList("StoryGuests", Tag.TAG_COMPOUND);
            for (Tag guestTag : guests) {
                if (!(guestTag instanceof CompoundTag entryTag)) {
                    continue;
                }
                String id = entryTag.getString("Id");
                if (id.isBlank()) {
                    continue;
                }
                data.progressById.put(id, StoryGuestProgress.load(entryTag.getCompound("Progress")));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag guests = new ListTag();
        for (Map.Entry<String, StoryGuestProgress> entry : this.progressById.entrySet()) {
            CompoundTag guestTag = new CompoundTag();
            guestTag.putString("Id", entry.getKey());
            guestTag.put("Progress", entry.getValue().save());
            guests.add(guestTag);
        }
        tag.put("StoryGuests", guests);
        return tag;
    }

    public StoryGuestProgress getOrCreateProgress(String storyGuestId) {
        return this.progressById.computeIfAbsent(storyGuestId, ignored -> {
            this.setDirty();
            return new StoryGuestProgress();
        });
    }

    public Map<String, StoryGuestProgress> getAllProgress() {
        return this.progressById;
    }

    public void clearAllProgress() {
        if (this.progressById.isEmpty()) {
            return;
        }
        this.progressById.clear();
        this.setDirty();
    }
}
