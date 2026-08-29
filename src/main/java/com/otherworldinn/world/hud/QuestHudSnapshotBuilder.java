package com.otherworldinn.world.hud;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.quest.QuestCategory;
import com.otherworldinn.world.quest.QuestDefinition;
import com.otherworldinn.world.quest.QuestNodeDef;
import com.otherworldinn.world.quest.QuestObjectiveDef;
import com.otherworldinn.world.quest.QuestObjectiveType;
import com.otherworldinn.world.quest.QuestRegistry;
import com.otherworldinn.world.quest.QuestRewardDef;
import com.otherworldinn.world.quest.QuestService;
import com.otherworldinn.world.quest.TeamQuestData;
import com.otherworldinn.world.team.TeamData;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * 任务系统 HUD 条目构建（主 mod 自渲染管线，与 OtherworldInnHud 无关）。
 *
 * <p>kind=quest：每个已接取未完成的任务一条，需求行 = 全部活跃节点的目标。
 * 复用 {@link TaskHudSnapshotBuilder} 的条目格式与显示文案助手，保证与委托/故事委托条目同构。
 */
final class QuestHudSnapshotBuilder {
    private static final String KIND_QUEST = "quest";
    private static final String TYPE_ITEM = "item";
    private static final String TYPE_COIN = "coin";
    private static final String TYPE_ENTITY_INTERACT = "interact_entity";
    private static final String TYPE_BLOCK_INTERACT = "interact_block";
    private static final String TYPE_ADVANCEMENT = "advancement";
    private static final String TYPE_REGION = "region";
    private static long lastMarkerDebugLogAt;

    private QuestHudSnapshotBuilder() {}

    /** 追加全部活跃任务条目（排在调用方已有条目之前即为主线置顶）。 */
    static void addQuestTasks(ListTag tasks, ServerPlayer player, TeamData team) {
        for (QuestDefinition quest : QuestRegistry.all()) {
            TeamQuestData.QuestProgress progress = team.getQuestData().getProgress(quest.id());
            if (progress == null || progress.isCompleted() || progress.getActiveNodeIds() == null) {
                continue;
            }

            CompoundTag task = TaskHudSnapshotBuilder.createTask(
                    "quest:" + quest.id(),
                    KIND_QUEST,
                    "",
                    QuestService.textFor(player, quest.name()),
                    false,
                    false,
                    progress.getAcceptedAt());
            // 节点推进动效签名：活跃节点集合变化时客户端做交叉淡变
            List<String> stageIds = new java.util.ArrayList<>(progress.getActiveNodeIds());
            java.util.Collections.sort(stageIds);
            task.putString("Stage", String.join("|", stageIds));
            task.putString("Category", quest.category() == QuestCategory.MAIN ? "main" : "side");

            ListTag requirements = new ListTag();
            for (QuestNodeDef node : quest.nodes()) {
                if (!progress.getActiveNodeIds().contains(node.id())) {
                    continue;
                }
                for (QuestObjectiveDef objective : node.objectives()) {
                    boolean done = progress.isObjectiveCompleted(node.id(), objective.id());
                    requirements.add(createQuestRequirement(player, objective, done));
                }
            }
            task.put("Requirements", requirements);
            task.put("Rewards", activeNodeRewards(quest, progress));
            task.put("Markers", activeNodeMarkers(player, quest, progress));
            tasks.add(task);
        }
    }

    /** 活跃节点的指示点（绝对坐标）：relative 为真时相对城镇出生点偏移。 */
    private static ListTag activeNodeMarkers(ServerPlayer player, QuestDefinition quest, TeamQuestData.QuestProgress progress) {
        ListTag markers = new ListTag();
        net.minecraft.server.level.ServerLevel townLevel =
                player.server.getLevel(com.otherworldinn.world.dimension.TownDimensions.TOWN_LEVEL);
        for (QuestNodeDef node : quest.nodes()) {
            if (!progress.getActiveNodeIds().contains(node.id()) || node.marker() == null) {
                continue;
            }
            net.minecraft.core.BlockPos pos = node.marker();
            if (node.markerRelative() && townLevel != null) {
                pos = pos.offset(townLevel.getSharedSpawnPos());
            }
            CompoundTag markerTag = new CompoundTag();
            markerTag.putInt("X", pos.getX());
            markerTag.putInt("Y", pos.getY());
            markerTag.putInt("Z", pos.getZ());
            markers.add(markerTag);
            // #region debug-point B:quest-marker-server
            if (System.currentTimeMillis() - lastMarkerDebugLogAt >= 1000L) {
                lastMarkerDebugLogAt = System.currentTimeMillis();
                try {
                    java.net.http.HttpClient.newHttpClient()
                            .sendAsync(
                                    java.net.http.HttpRequest.newBuilder(java.net.URI.create(readDebugServerUrl()))
                                            .header("Content-Type", "application/json")
                                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                                                    "{\"sessionId\":\"" + readDebugSessionId()
                                                            + "\",\"runId\":\"post-fix\",\"hypothesisId\":\"B\",\"location\":\"QuestHudSnapshotBuilder.activeNodeMarkers\",\"msg\":\"[DEBUG] server marker snapshot\",\"data\":{\"quest\":\""
                                                            + escapeDebug(quest.id()) + "\",\"node\":\""
                                                            + escapeDebug(node.id()) + "\",\"markerRelative\":"
                                                            + node.markerRelative() + ",\"markerX\":"
                                                            + pos.getX() + ",\"markerY\":"
                                                            + pos.getY() + ",\"markerZ\":"
                                                            + pos.getZ() + ",\"player\":\""
                                                            + escapeDebug(player.getScoreboardName()) + "\"},\"ts\":"
                                                            + System.currentTimeMillis() + "}"))
                                            .build(),
                                    java.net.http.HttpResponse.BodyHandlers.discarding())
                            .exceptionally(ex -> null);
                } catch (Exception ignored) {
                }
            }
            // #endregion
        }
        return markers;
    }

    private static String readDebugServerUrl() {
        try {
            java.nio.file.Path path = java.nio.file.Path.of(".dbg", "quest-marker-position.env");
            for (String line : java.nio.file.Files.readAllLines(path)) {
                if (line.startsWith("DEBUG_SERVER_URL=")) {
                    return line.substring("DEBUG_SERVER_URL=".length()).trim();
                }
            }
        } catch (Exception ignored) {
        }
        return "http://127.0.0.1:7777/event";
    }

    private static String readDebugSessionId() {
        try {
            java.nio.file.Path path = java.nio.file.Path.of(".dbg", "quest-marker-position.env");
            for (String line : java.nio.file.Files.readAllLines(path)) {
                if (line.startsWith("DEBUG_SESSION_ID=")) {
                    return escapeDebug(line.substring("DEBUG_SESSION_ID=".length()).trim());
                }
            }
        } catch (Exception ignored) {
        }
        return "quest-marker-position";
    }

    private static String escapeDebug(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static CompoundTag createQuestRequirement(
            ServerPlayer player, QuestObjectiveDef objective, boolean done) {
        String type = switch (objective.type()) {
            case HAS_ITEM -> TYPE_ITEM;
            case INTERACT_ENTITY -> TYPE_ENTITY_INTERACT;
            case INTERACT_BLOCK -> TYPE_BLOCK_INTERACT;
            case ADVANCEMENT -> TYPE_ADVANCEMENT;
            case ENTER_REGION -> TYPE_REGION;
        };
        int required = 1;
        int current = 0;
        String typeKey = "";
        String typeText = "";
        switch (objective.type()) {
            case HAS_ITEM -> {
                required = Math.max(1, objective.count());
                if (objective.itemId() != null) {
                    String itemId = objective.itemId().toString();
                    typeKey = TaskHudSnapshotBuilder.itemDisplayKey(itemId);
                    typeText = TaskHudSnapshotBuilder.itemDisplayText(itemId);
                    current = TaskHudSnapshotBuilder.countMatchingItems(player, itemId, null);
                }
            }
            case INTERACT_ENTITY -> {
                if (objective.entityType() != null) {
                    typeKey = TaskHudSnapshotBuilder.entityDisplayKey(objective.entityType().toString());
                    typeText = TaskHudSnapshotBuilder.entityDisplayText(objective.entityType().toString());
                }
            }
            default -> {}
        }
        // JSON 显式 label 优先，否则用类型默认文案（物品/实体名可本地化），再兜底目标 id
        String displayKey = objective.label() != null ? "" : typeKey;
        String displayText = objective.label() != null
                ? QuestService.textFor(player, objective.label())
                : typeText;
        if (displayKey.isBlank() && displayText.isBlank()) {
            displayText = objective.id();
        }
        return TaskHudSnapshotBuilder.createRequirement(
                type, objective.id(), displayKey, displayText, required, current, done, null);
    }

    private static ListTag activeNodeRewards(QuestDefinition quest, TeamQuestData.QuestProgress progress) {
        ListTag rewards = new ListTag();
        for (QuestNodeDef node : quest.nodes()) {
            if (!progress.getActiveNodeIds().contains(node.id())) {
                continue;
            }
            appendRewards(rewards, node.rewards());
        }
        return rewards;
    }

    private static void appendRewards(ListTag rewards, List<QuestRewardDef> defs) {
        for (QuestRewardDef reward : defs) {
            switch (reward.type()) {
                case ITEM -> {
                    if (reward.itemId() == null) {
                        continue;
                    }
                    rewards.add(TaskHudSnapshotBuilder.createReward(
                            TYPE_ITEM,
                            reward.itemId().toString(),
                            TaskHudSnapshotBuilder.itemDisplayKey(reward.itemId().toString()),
                            TaskHudSnapshotBuilder.itemDisplayText(reward.itemId().toString()),
                            reward.count()));
                }
                case COINS -> {
                    String coinId = ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "coin").toString();
                    rewards.add(TaskHudSnapshotBuilder.createReward(
                            TYPE_COIN,
                            coinId,
                            TaskHudSnapshotBuilder.itemDisplayKey(coinId),
                            TaskHudSnapshotBuilder.itemDisplayText(coinId),
                            reward.count()));
                }
            }
        }
    }
}
