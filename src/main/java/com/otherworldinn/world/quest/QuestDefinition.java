package com.otherworldinn.world.quest;

import com.otherworldinn.world.dialogue.LocalizedText;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * 任务定义（JSON 解析产物）：节点树 + 奖励列表。
 * 树的派生结构（节点索引、根、子节点、叶子）在构造时一次性算好，加载期完成校验。
 */
public final class QuestDefinition {
    private final String id;
    private final QuestCategory category;
    private final LocalizedText name;
    private final LocalizedText desc;
    private final List<QuestNodeDef> nodes;
    private final List<QuestRewardDef> rewards;

    private final Map<String, QuestNodeDef> nodesById;
    private final Map<String, List<QuestNodeDef>> childrenByParent;
    private final QuestNodeDef rootNode;
    private final List<String> leafNodeIds;

    public QuestDefinition(
            String id,
            QuestCategory category,
            LocalizedText name,
            LocalizedText desc,
            List<QuestNodeDef> nodes,
            List<QuestRewardDef> rewards) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.desc = desc;
        this.nodes = List.copyOf(nodes);
        this.rewards = List.copyOf(rewards);

        Map<String, QuestNodeDef> byId = new HashMap<>();
        for (QuestNodeDef node : this.nodes) {
            byId.put(node.id(), node);
        }
        this.nodesById = Collections.unmodifiableMap(byId);

        Map<String, List<QuestNodeDef>> children = new HashMap<>();
        QuestNodeDef root = null;
        for (QuestNodeDef node : this.nodes) {
            if (node.parent() == null) {
                root = node;
            } else {
                children.computeIfAbsent(node.parent(), k -> new ArrayList<>()).add(node);
            }
        }
        this.childrenByParent = Collections.unmodifiableMap(children);
        this.rootNode = root;

        List<String> leaves = new ArrayList<>();
        for (QuestNodeDef node : this.nodes) {
            if (!children.containsKey(node.id())) {
                leaves.add(node.id());
            }
        }
        this.leafNodeIds = List.copyOf(leaves);
    }

    public String id() {
        return id;
    }

    public QuestCategory category() {
        return category;
    }

    public LocalizedText name() {
        return name;
    }

    public LocalizedText desc() {
        return desc;
    }

    public List<QuestNodeDef> nodes() {
        return nodes;
    }

    public List<QuestRewardDef> rewards() {
        return rewards;
    }

    public QuestNodeDef getNode(String nodeId) {
        return nodesById.get(nodeId);
    }

    @Nullable
    public QuestNodeDef getRootNode() {
        return rootNode;
    }

    public List<QuestNodeDef> getChildren(String nodeId) {
        return childrenByParent.getOrDefault(nodeId, List.of());
    }

    public boolean isLeaf(String nodeId) {
        return !childrenByParent.containsKey(nodeId);
    }

    public List<String> getLeafNodeIds() {
        return leafNodeIds;
    }
}
