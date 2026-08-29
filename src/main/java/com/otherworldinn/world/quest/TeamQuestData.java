package com.otherworldinn.world.quest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * 队伍任务进度（挂在 {@link com.otherworldinn.world.team.TeamData}，队伍级共享）。
 *
 * <p>Map 中存在 questId 即视为已接取；完成状态单独标记，奖励只在完成瞬间发放一次。
 */
public class TeamQuestData {
    private final Map<String, QuestProgress> quests = new HashMap<>();

    public QuestProgress getProgress(String questId) {
        return quests.get(questId);
    }

    public boolean isAccepted(String questId) {
        return quests.containsKey(questId);
    }

    public boolean isCompleted(String questId) {
        QuestProgress progress = quests.get(questId);
        return progress != null && progress.completed;
    }

    /** 接取任务：激活根节点（幂等，重复接取无效果）。acceptedAt 供 HUD 排序（越早越靠前）。 */
    public void accept(String questId, String rootNodeId, long acceptedAt) {
        QuestProgress progress = quests.computeIfAbsent(questId, k -> new QuestProgress());
        progress.setAcceptedAt(acceptedAt);
        progress.activateNode(rootNodeId);
    }

    public Map<String, QuestProgress> all() {
        return quests;
    }

    /** 清除单条任务进度（调试用），存在则返回 true。 */
    public boolean reset(String questId) {
        return quests.remove(questId) != null;
    }

    /** 清除全部任务进度（调试用），原本有进度则返回 true。 */
    public boolean resetAll() {
        boolean hadProgress = !quests.isEmpty();
        quests.clear();
        return hadProgress;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (Map.Entry<String, QuestProgress> entry : quests.entrySet()) {
            CompoundTag questTag = new CompoundTag();
            questTag.putString("QuestId", entry.getKey());
            questTag.put("Progress", entry.getValue().save());
            listTag.add(questTag);
        }
        tag.put("Quests", listTag);
        return tag;
    }

    public void load(CompoundTag tag) {
        quests.clear();
        if (!tag.contains("Quests", Tag.TAG_LIST)) {
            return;
        }
        ListTag listTag = tag.getList("Quests", Tag.TAG_COMPOUND);
        for (Tag t : listTag) {
            if (t instanceof CompoundTag questTag) {
                String questId = questTag.getString("QuestId");
                if (!questId.isBlank()) {
                    quests.put(questId, QuestProgress.load(questTag.getCompound("Progress")));
                }
            }
        }
    }

    /** 单条任务的进度：活跃节点集合 + 已完成节点/目标集合。 */
    public static class QuestProgress {
        private final Set<String> activeNodeIds = new HashSet<>();
        private final Set<String> completedNodeIds = new HashSet<>();
        private final Map<String, Set<String>> completedObjectiveIds = new HashMap<>();
        private boolean completed = false;
        private long finishedAt = -1L;
        private long acceptedAt = Long.MAX_VALUE;

        public long getAcceptedAt() {
            return acceptedAt;
        }

        public void setAcceptedAt(long acceptedAt) {
            this.acceptedAt = acceptedAt;
        }

        public Set<String> getActiveNodeIds() {
            return activeNodeIds;
        }

        public Set<String> getCompletedNodeIds() {
            return completedNodeIds;
        }

        public boolean isObjectiveCompleted(String nodeId, String objectiveId) {
            Set<String> done = completedObjectiveIds.get(nodeId);
            return done != null && done.contains(objectiveId);
        }

        public void activateNode(String nodeId) {
            activeNodeIds.add(nodeId);
        }

        public void completeObjective(String nodeId, String objectiveId) {
            completedObjectiveIds.computeIfAbsent(nodeId, k -> new HashSet<>()).add(objectiveId);
        }

        public void completeNode(String nodeId) {
            activeNodeIds.remove(nodeId);
            completedNodeIds.add(nodeId);
        }

        public boolean isCompleted() {
            return completed;
        }

        public void markCompleted(long finishedAt) {
            this.completed = true;
            this.finishedAt = finishedAt;
        }

        public long getFinishedAt() {
            return finishedAt;
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.put("ActiveNodes", stringList(activeNodeIds));
            tag.put("CompletedNodes", stringList(completedNodeIds));
            CompoundTag objectivesTag = new CompoundTag();
            for (Map.Entry<String, Set<String>> entry : completedObjectiveIds.entrySet()) {
                objectivesTag.put(entry.getKey(), stringList(entry.getValue()));
            }
            tag.put("CompletedObjectives", objectivesTag);
            tag.putBoolean("Completed", completed);
            tag.putLong("FinishedAt", finishedAt);
            tag.putLong("AcceptedAt", acceptedAt);
            return tag;
        }

        private static QuestProgress load(CompoundTag tag) {
            QuestProgress progress = new QuestProgress();
            progress.activeNodeIds.addAll(readStringList(tag, "ActiveNodes"));
            progress.completedNodeIds.addAll(readStringList(tag, "CompletedNodes"));
            if (tag.contains("CompletedObjectives", Tag.TAG_COMPOUND)) {
                CompoundTag objectivesTag = tag.getCompound("CompletedObjectives");
                for (String nodeId : objectivesTag.getAllKeys()) {
                    progress.completedObjectiveIds
                            .computeIfAbsent(nodeId, k -> new HashSet<>())
                            .addAll(readStringList(objectivesTag, nodeId));
                }
            }
            progress.completed = tag.getBoolean("Completed");
            progress.finishedAt = tag.contains("FinishedAt", Tag.TAG_LONG) ? tag.getLong("FinishedAt") : -1L;
            progress.acceptedAt = tag.contains("AcceptedAt", Tag.TAG_LONG) ? tag.getLong("AcceptedAt") : Long.MAX_VALUE;
            return progress;
        }

        private static ListTag stringList(Set<String> values) {
            ListTag listTag = new ListTag();
            for (String value : values) {
                listTag.add(StringTag.valueOf(value));
            }
            return listTag;
        }

        private static Set<String> readStringList(CompoundTag owner, String key) {
            Set<String> result = new HashSet<>();
            if (owner.contains(key, Tag.TAG_LIST)) {
                ListTag listTag = owner.getList(key, Tag.TAG_STRING);
                for (Tag t : listTag) {
                    result.add(t.getAsString());
                }
            }
            return result;
        }
    }
}
