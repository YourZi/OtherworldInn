package com.otherworldinn.world.commission;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

@Getter
public class CommissionEntry {
    private final String id;
    private final String descriptionKey;
    private final int stars;
    private final long durationDays;
    private final List<ItemRequirement> submitRequirements;
    private final List<KillRequirement> killRequirements;
    private final List<PhotoRequirement> photoRequirements;
    private final List<ItemReward> itemRewards;
    private final int coinReward;
    private final List<NpcFavorReward> npcFavorRewards;

    public CommissionEntry(
            String id,
            String descriptionKey,
            int stars,
            long durationDays,
            List<ItemRequirement> submitRequirements,
            List<KillRequirement> killRequirements,
            List<PhotoRequirement> photoRequirements,
            List<ItemReward> itemRewards,
            int coinReward,
            List<NpcFavorReward> npcFavorRewards) {
        this.id = id;
        this.descriptionKey = descriptionKey == null ? "" : descriptionKey;
        this.stars = Math.max(1, Math.min(5, stars));
        this.durationDays = Math.max(1L, durationDays);
        this.submitRequirements = List.copyOf(submitRequirements);
        this.killRequirements = List.copyOf(killRequirements);
        this.photoRequirements = List.copyOf(photoRequirements);
        this.itemRewards = List.copyOf(itemRewards);
        this.coinReward = Math.max(0, coinReward);
        this.npcFavorRewards = List.copyOf(npcFavorRewards);
    }

    public boolean hasSubmitRequirement() {
        return !submitRequirements.isEmpty();
    }

    public boolean hasKillRequirement() {
        return !killRequirements.isEmpty();
    }

    public boolean hasPhotoRequirement() {
        return !photoRequirements.isEmpty();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Id", id);
        tag.putString("DescriptionKey", descriptionKey);
        tag.putInt("Stars", stars);
        tag.putLong("DurationDays", durationDays);
        tag.putInt("CoinReward", coinReward);

        ListTag submitTag = new ListTag();
        for (ItemRequirement req : submitRequirements) {
            submitTag.add(req.save());
        }
        tag.put("SubmitRequirements", submitTag);

        ListTag killTag = new ListTag();
        for (KillRequirement req : killRequirements) {
            killTag.add(req.save());
        }
        tag.put("KillRequirements", killTag);

        ListTag photoTag = new ListTag();
        for (PhotoRequirement req : photoRequirements) {
            photoTag.add(req.save());
        }
        tag.put("PhotoRequirements", photoTag);

        ListTag itemRewardTag = new ListTag();
        for (ItemReward reward : itemRewards) {
            itemRewardTag.add(reward.save());
        }
        tag.put("ItemRewards", itemRewardTag);

        ListTag favorRewardTag = new ListTag();
        for (NpcFavorReward reward : npcFavorRewards) {
            favorRewardTag.add(reward.save());
        }
        tag.put("NpcFavorRewards", favorRewardTag);
        return tag;
    }

    public static CommissionEntry load(CompoundTag tag) {
        String id = tag.getString("Id");
        String descriptionKey = tag.getString("DescriptionKey");
        int stars = tag.getInt("Stars");
        long durationDays = tag.contains("DurationDays") ? tag.getLong("DurationDays") : 3L;
        int coinReward = tag.getInt("CoinReward");

        List<ItemRequirement> submitRequirements = new ArrayList<>();
        if (tag.contains("SubmitRequirements", Tag.TAG_LIST)) {
            ListTag submitTag = tag.getList("SubmitRequirements", Tag.TAG_COMPOUND);
            for (Tag t : submitTag) {
                if (t instanceof CompoundTag reqTag) {
                    submitRequirements.add(ItemRequirement.load(reqTag));
                }
            }
        }

        List<KillRequirement> killRequirements = new ArrayList<>();
        if (tag.contains("KillRequirements", Tag.TAG_LIST)) {
            ListTag killTag = tag.getList("KillRequirements", Tag.TAG_COMPOUND);
            for (Tag t : killTag) {
                if (t instanceof CompoundTag reqTag) {
                    killRequirements.add(KillRequirement.load(reqTag));
                }
            }
        }

        List<PhotoRequirement> photoRequirements = new ArrayList<>();
        if (tag.contains("PhotoRequirements", Tag.TAG_LIST)) {
            ListTag photoTag = tag.getList("PhotoRequirements", Tag.TAG_COMPOUND);
            for (Tag t : photoTag) {
                if (t instanceof CompoundTag reqTag) {
                    photoRequirements.add(PhotoRequirement.load(reqTag));
                }
            }
        }

        List<ItemReward> itemRewards = new ArrayList<>();
        if (tag.contains("ItemRewards", Tag.TAG_LIST)) {
            ListTag rewardTag = tag.getList("ItemRewards", Tag.TAG_COMPOUND);
            for (Tag t : rewardTag) {
                if (t instanceof CompoundTag rewardEntryTag) {
                    itemRewards.add(ItemReward.load(rewardEntryTag));
                }
            }
        }

        List<NpcFavorReward> npcFavorRewards = new ArrayList<>();
        if (tag.contains("NpcFavorRewards", Tag.TAG_LIST)) {
            ListTag favorTag = tag.getList("NpcFavorRewards", Tag.TAG_COMPOUND);
            for (Tag t : favorTag) {
                if (t instanceof CompoundTag rewardEntryTag) {
                    npcFavorRewards.add(NpcFavorReward.load(rewardEntryTag));
                }
            }
        }

        return new CommissionEntry(
                id,
                descriptionKey,
                stars,
                durationDays,
                submitRequirements,
                killRequirements,
                photoRequirements,
                itemRewards,
                coinReward,
                npcFavorRewards);
    }

    public record ItemRequirement(String itemId, int count, @Nullable CompoundTag nbt) {
        public ItemRequirement(String itemId, int count) {
            this(itemId, count, null);
        }

        public ItemRequirement {
            count = Math.max(1, count);
            nbt = nbt == null || nbt.isEmpty() ? null : nbt.copy();
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("ItemId", itemId);
            tag.putInt("Count", count);
            if (nbt != null && !nbt.isEmpty()) {
                tag.put("Nbt", nbt.copy());
            }
            return tag;
        }

        private static ItemRequirement load(CompoundTag tag) {
            CompoundTag nbt =
                    tag.contains("Nbt", Tag.TAG_COMPOUND) ? tag.getCompound("Nbt").copy() : null;
            return new ItemRequirement(tag.getString("ItemId"), Math.max(1, tag.getInt("Count")), nbt);
        }
    }

    public record KillRequirement(String entityTypeId, int count) {
        public KillRequirement {
            count = Math.max(1, count);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("EntityTypeId", entityTypeId);
            tag.putInt("Count", count);
            return tag;
        }

        private static KillRequirement load(CompoundTag tag) {
            return new KillRequirement(
                    tag.getString("EntityTypeId"), Math.max(1, tag.getInt("Count")));
        }
    }

    public record PhotoRequirement(String objectiveId) {
        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("ObjectiveId", objectiveId);
            return tag;
        }

        private static PhotoRequirement load(CompoundTag tag) {
            return new PhotoRequirement(tag.getString("ObjectiveId"));
        }
    }

    public record ItemReward(String itemId, int count) {
        public ItemReward {
            count = Math.max(1, count);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("ItemId", itemId);
            tag.putInt("Count", count);
            return tag;
        }

        private static ItemReward load(CompoundTag tag) {
            return new ItemReward(tag.getString("ItemId"), Math.max(1, tag.getInt("Count")));
        }
    }

    public record NpcFavorReward(String npcEntityTypeId, int favorProgress) {
        public NpcFavorReward {
            favorProgress = Math.max(1, favorProgress);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("NpcEntityTypeId", npcEntityTypeId);
            tag.putInt("FavorProgress", favorProgress);
            return tag;
        }

        private static NpcFavorReward load(CompoundTag tag) {
            return new NpcFavorReward(
                    tag.getString("NpcEntityTypeId"), Math.max(1, tag.getInt("FavorProgress")));
        }
    }
}
