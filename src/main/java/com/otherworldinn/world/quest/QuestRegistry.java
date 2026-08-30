package com.otherworldinn.world.quest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.dialogue.LocalizedText;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/**
 * 任务注册表（参照原版进度的文件组织）：一个节点一个 JSON 文件，位于 {@code data/<命名空间>/quests/} 下，文件路径即节点 id。
 * 根节点额外承担任务元数据（{@code name} 必填、{@code desc}、{@code category} 默认 side）。
 * 解析或校验失败（单根、parent 存在且同任务、无环、全可达）只记日志并跳过该文件/任务。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class QuestRegistry {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private static final Map<String, QuestDefinition> QUESTS_BY_ID = new HashMap<>();

    private QuestRegistry() {}

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(RELOADER);
    }

    public static final SimpleQuestReloader RELOADER = new SimpleQuestReloader();

    @Nullable
    public static QuestDefinition get(String questId) {
        return QUESTS_BY_ID.get(questId);
    }

    public static List<QuestDefinition> all() {
        return List.copyOf(QUESTS_BY_ID.values());
    }

    public static List<QuestDefinition> byCategory(QuestCategory category) {
        List<QuestDefinition> result = new ArrayList<>();
        for (QuestDefinition quest : QUESTS_BY_ID.values()) {
            if (quest.category() == category) {
                result.add(quest);
            }
        }
        return result;
    }

    public static class SimpleQuestReloader extends net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener {
        public SimpleQuestReloader() {
            super(new Gson(), "quests");
        }

        @Override
        protected void apply(
                Map<ResourceLocation, JsonElement> files,
                ResourceManager resourceManager,
                ProfilerFiller profiler) {
            Map<String, Map<String, NodeFile>> nodesByQuest = new LinkedHashMap<>();
            for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
                String nodeId = entry.getKey().toString();
                try {
                    JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "quest node");
                    NodeFile file = parseNodeFile(json, nodeId);
                    Map<String, NodeFile> group =
                            nodesByQuest.computeIfAbsent(file.questId, k -> new LinkedHashMap<>());
                    if (group.containsKey(nodeId)) {
                        LOGGER.warn("[Quest] 重复节点 id '{}'，跳过后者", nodeId);
                        continue;
                    }
                    group.put(nodeId, file);
                } catch (Exception e) {
                    LOGGER.error("[Quest] 解析任务节点 {} 失败：{}", nodeId, e.getMessage());
                }
            }

            Map<String, QuestDefinition> parsed = new HashMap<>();
            for (Map.Entry<String, Map<String, NodeFile>> group : nodesByQuest.entrySet()) {
                try {
                    QuestDefinition quest = buildQuest(group.getKey(), group.getValue());
                    parsed.put(quest.id(), quest);
                } catch (Exception e) {
                    LOGGER.error("[Quest] 组装任务 {} 失败：{}", group.getKey(), e.getMessage());
                }
            }
            QUESTS_BY_ID.clear();
            QUESTS_BY_ID.putAll(parsed);
            LOGGER.info("[Quest] 已加载 {} 个任务定义（共 {} 个节点）", parsed.size(), countNodes(nodesByQuest));
        }

        private static int countNodes(Map<String, Map<String, NodeFile>> nodesByQuest) {
            int total = 0;
            for (Map<String, NodeFile> group : nodesByQuest.values()) {
                total += group.size();
            }
            return total;
        }
    }

    // ---------- 单文件解析 ----------

    private static final class NodeFile {
        final String questId;
        final QuestNodeDef node;
        final boolean root;
        final QuestCategory category;
        final LocalizedText name;
        final LocalizedText desc;

        NodeFile(
                String questId,
                QuestNodeDef node,
                boolean root,
                QuestCategory category,
                LocalizedText name,
                LocalizedText desc) {
            this.questId = questId;
            this.node = node;
            this.root = root;
            this.category = category;
            this.name = name;
            this.desc = desc;
        }
    }

    private static NodeFile parseNodeFile(JsonObject json, String nodeId) {
        String questId = GsonHelper.getAsString(json, "quest");
        String parent = null;
        if (json.has("parent") && !json.get("parent").isJsonNull()) {
            // 必须是完整节点 id（namespace:path），与原版进度的 parent 一致
            parent = ResourceLocation.parse(GsonHelper.getAsString(json, "parent")).toString();
        }
        LocalizedText label = json.has("label") ? parseText(GsonHelper.getAsJsonObject(json, "label")) : null;

        List<QuestObjectiveDef> objectives = new ArrayList<>();
        int index = 0;
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "objectives")) {
            JsonObject objJson = GsonHelper.convertToJsonObject(element, "objective");
            objectives.add(parseObjective(objJson, nodeId, index));
            index++;
        }
        if (objectives.isEmpty()) {
            throw new IllegalArgumentException("节点 " + nodeId + " 的 objectives 不能为空");
        }

        List<QuestRewardDef> rewards = new ArrayList<>();
        if (json.has("rewards")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "rewards")) {
                rewards.add(parseReward(GsonHelper.convertToJsonObject(element, "reward")));
            }
        }

        BlockPos marker = parsePos(json, "marker");
        boolean markerRelative = json.has("relative") && GsonHelper.getAsBoolean(json, "relative");

        boolean root = parent == null;
        QuestCategory category = QuestCategory.SIDE;
        LocalizedText name = null;
        LocalizedText desc = new LocalizedText("", "");
        if (root) {
            category = parseEnum(GsonHelper.getAsString(json, "category", "side"), QuestCategory.SIDE, QuestCategory.class);
            name = parseText(GsonHelper.getAsJsonObject(json, "name"));
            if (json.has("desc")) {
                desc = parseText(GsonHelper.getAsJsonObject(json, "desc"));
            }
        }
        return new NodeFile(
                questId,
                new QuestNodeDef(nodeId, parent, label, objectives, rewards, marker, markerRelative),
                root,
                category,
                name,
                desc);
    }

    private static QuestObjectiveDef parseObjective(JsonObject json, String nodeId, int index) {
        String typeStr = GsonHelper.getAsString(json, "type");
        QuestObjectiveType type;
        try {
            type = QuestObjectiveType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知目标类型 '" + typeStr + "'");
        }
        String id = GsonHelper.getAsString(json, "id", "obj" + index);
        LocalizedText label = json.has("label") ? parseText(GsonHelper.getAsJsonObject(json, "label")) : null;

        ResourceLocation itemId = parseId(json, "itemId");
        int count = json.has("count") ? GsonHelper.getAsInt(json, "count") : 1;
        ResourceLocation entityType = parseId(json, "entityType");
        UUID entityUuid = null;
        if (json.has("entityUuid") && !json.get("entityUuid").isJsonNull()) {
            entityUuid = UUID.fromString(GsonHelper.getAsString(json, "entityUuid"));
        }
        ResourceLocation blockId = null;
        TagKey<Block> blockTag = null;
        if (json.has("block") && !json.get("block").isJsonNull()) {
            String blockStr = GsonHelper.getAsString(json, "block");
            if (blockStr.startsWith("#")) {
                blockTag = TagKey.create(Registries.BLOCK, ResourceLocation.parse(blockStr.substring(1)));
            } else {
                blockId = ResourceLocation.parse(blockStr);
            }
        }
        BlockPos blockPos = parsePos(json, "pos");
        ResourceLocation advancementId = parseId(json, "advancementId");
        BlockPos regionMin = parsePos(json, "min");
        BlockPos regionMax = parsePos(json, "max");
        boolean relative = json.has("relative") && GsonHelper.getAsBoolean(json, "relative");

        switch (type) {
            case HAS_ITEM -> {
                require(itemId != null, "has_item 需要 itemId");
            }
            case INTERACT_ENTITY -> {
                require((entityType == null) != (entityUuid == null), "interact_entity 的 entityType / entityUuid 必须二选一");
            }
            case INTERACT_BLOCK -> {
                require((blockId == null && blockTag == null) != (blockPos == null), "interact_block 的 block / pos 必须二选一");
            }
            case ADVANCEMENT -> {
                require(advancementId != null, "advancement 需要 advancementId");
            }
            case ENTER_REGION -> {
                require(regionMin != null && regionMax != null, "enter_region 需要 min / max");
            }
        }
        return new QuestObjectiveDef(
                nodeId + "/" + id,
                type,
                label,
                itemId,
                Math.max(1, count),
                entityType,
                entityUuid,
                blockId,
                blockTag,
                blockPos,
                advancementId,
                regionMin,
                regionMax,
                relative);
    }

    private static QuestRewardDef parseReward(JsonObject json) {
        String typeStr = GsonHelper.getAsString(json, "type");
        QuestRewardDef.QuestRewardType type;
        try {
            type = QuestRewardDef.QuestRewardType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知奖励类型 '" + typeStr + "'");
        }
        ResourceLocation itemId = parseId(json, "itemId");
        int count = json.has("count") ? GsonHelper.getAsInt(json, "count") : 1;
        if (type == QuestRewardDef.QuestRewardType.ITEM) {
            require(itemId != null, "item 奖励需要 itemId");
        }
        return new QuestRewardDef(type, itemId, Math.max(1, count));
    }

    private static LocalizedText parseText(JsonObject json) {
        return new LocalizedText(GsonHelper.getAsString(json, "zh", ""), GsonHelper.getAsString(json, "en", ""));
    }

    @Nullable
    private static ResourceLocation parseId(JsonObject json, String key) {
        if (!json.has(key) || json.get(key).isJsonNull()) {
            return null;
        }
        return ResourceLocation.parse(GsonHelper.getAsString(json, key));
    }

    @Nullable
    private static BlockPos parsePos(JsonObject json, String key) {
        if (!json.has(key) || !json.get(key).isJsonArray()) {
            return null;
        }
        int[] coords = new int[3];
        com.google.gson.JsonArray array = GsonHelper.getAsJsonArray(json, key);
        if (array.size() != 3) {
            throw new IllegalArgumentException(key + " 必须是 [x, y, z]");
        }
        for (int i = 0; i < 3; i++) {
            coords[i] = array.get(i).getAsInt();
        }
        return new BlockPos(coords[0], coords[1], coords[2]);
    }

    private static <T extends Enum<T>> T parseEnum(String value, T fallback, Class<T> enumClass) {
        for (T constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(value)) {
                return constant;
            }
        }
        return fallback;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    // ---------- 按任务聚合与树校验 ----------

    private static QuestDefinition buildQuest(String questId, Map<String, NodeFile> group) {
        NodeFile root = null;
        int rootCount = 0;
        for (NodeFile file : group.values()) {
            if (file.root) {
                root = file;
                rootCount++;
            }
        }
        if (rootCount != 1) {
            throw new IllegalArgumentException("必须有且只有一个根节点（无 parent），实际 " + rootCount + " 个");
        }
        for (NodeFile file : group.values()) {
            if (file.node.parent() != null && !group.containsKey(file.node.parent())) {
                throw new IllegalArgumentException(
                        "节点 " + file.node.id() + " 的 parent '" + file.node.parent() + "' 不存在或属于其它任务");
            }
        }

        // 无环 + 全部可达：从根出发 DFS
        Set<String> visited = new HashSet<>();
        dfs(group, root.node.id(), new HashSet<>(), visited, questId);
        if (visited.size() != group.size()) {
            List<String> unreachable = group.keySet().stream().filter(id -> !visited.contains(id)).toList();
            throw new IllegalArgumentException("存在不可达节点或环：" + unreachable);
        }

        List<QuestNodeDef> nodes = new ArrayList<>();
        for (String nodeId : group.keySet()) {
            nodes.add(group.get(nodeId).node);
        }
        // 任务完成的展示奖励 = 叶子节点奖励（发放时机为节点完成时）
        List<QuestRewardDef> leafRewards = new ArrayList<>();
        for (QuestNodeDef node : nodes) {
            if (!hasChildren(group, node.id())) {
                leafRewards.addAll(node.rewards());
            }
        }
        return new QuestDefinition(questId, root.category, root.name, root.desc, nodes, leafRewards);
    }

    private static boolean hasChildren(Map<String, NodeFile> group, String nodeId) {
        for (NodeFile file : group.values()) {
            if (nodeId.equals(file.node.parent())) {
                return true;
            }
        }
        return false;
    }

    private static void dfs(
            Map<String, NodeFile> group,
            String nodeId,
            Set<String> onPath,
            Set<String> visited,
            String questId) {
        if (onPath.contains(nodeId)) {
            throw new IllegalArgumentException("检测到环：节点 '" + nodeId + "'（任务 " + questId + "）");
        }
        if (!visited.add(nodeId)) {
            return;
        }
        onPath.add(nodeId);
        for (NodeFile file : group.values()) {
            if (nodeId.equals(file.node.parent())) {
                dfs(group, file.node.id(), onPath, visited, questId);
            }
        }
        onPath.remove(nodeId);
    }
}
