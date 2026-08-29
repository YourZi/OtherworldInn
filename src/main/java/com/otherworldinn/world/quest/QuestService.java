package com.otherworldinn.world.quest;

import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.dialogue.LocalizedText;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * 任务服务（服务端权威状态机）。
 *
 * <p>判定模型：状态驱动——激活时 + 相关事件后 + 低频兜底轮询统一走 {@link #reevaluateTeam}；
 * 交互类目标不可轮询，由事件直接标记完成后触发重算。树推进写成"任意变化后重算整棵树"，
 * 不假设节点先后顺序；完成状态写入为幂等依据，防并发重复推进。
 */
public final class QuestService {
    private QuestService() {}

    /** 接取任务（幂等：已接取/已完成返回 false）。 */
    public static boolean startQuest(ServerPlayer player, String questId) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            return false;
        }
        return startQuest(team, questId, player.server);
    }

    public static boolean startQuest(TeamData team, String questId, MinecraftServer server) {
        QuestDefinition quest = QuestRegistry.get(questId);
        if (quest == null) {
            return false;
        }
        TeamQuestData questData = team.getQuestData();
        if (questData.isAccepted(questId)) {
            return false;
        }
        QuestNodeDef root = quest.getRootNode();
        if (root == null) {
            return false;
        }
        questData.accept(questId, root.id(), server.overworld().getGameTime());
        reevaluateTeam(team, server);
        // 接取本身必须推送 HUD：首个节点可能是状态判定且无推进，否则客户端不知道新任务存在
        com.otherworldinn.world.hud.QuestHudSync.syncTeam(team, server.overworld());
        return true;
    }

    // ---------- 调试：重置 ----------

    /** 清除单条任务进度（可重新接取）。 */
    public static boolean resetQuest(TeamData team, String questId, MinecraftServer server) {
        if (!team.getQuestData().reset(questId)) {
            return false;
        }
        markDirtyAndSyncHud(team, server);
        return true;
    }

    /** 清除全部任务进度（可重新接取）。 */
    public static boolean resetAllQuests(TeamData team, MinecraftServer server) {
        if (!team.getQuestData().resetAll()) {
            return false;
        }
        markDirtyAndSyncHud(team, server);
        return true;
    }

    private static void markDirtyAndSyncHud(TeamData team, MinecraftServer server) {
        TeamManager.getInstance().getData(server).markDirty();
        // 自有同步管线，不经 OtherworldInnHud 的 TaskHudSnapshotSync
        com.otherworldinn.world.hud.QuestHudSync.syncTeam(team, server.overworld());
    }

    // ---------- 状态评估 ----------

    /** 对所有队伍重算任务状态（兜底轮询入口）。 */
    public static void reevaluateAll(MinecraftServer server) {
        for (TeamData team : TeamManager.getInstance().getData(server).getTeams().values()) {
            reevaluateTeam(team, server);
        }
    }

    /** 重算一支队伍的全部任务状态；有任何推进则标记存档脏。 */
    public static void reevaluateTeam(TeamData team, MinecraftServer server) {
        boolean anyChange = false;
        for (QuestDefinition quest : QuestRegistry.all()) {
            TeamQuestData.QuestProgress progress = team.getQuestData().getProgress(quest.id());
            if (progress == null || progress.isCompleted()) {
                continue;
            }
            if (reevaluateQuest(quest, team, server, progress)) {
                anyChange = true;
            }
        }
        if (anyChange) {
            markDirtyAndSyncHud(team, server);
        }
    }

    private static boolean reevaluateQuest(
            QuestDefinition quest, TeamData team, MinecraftServer server, TeamQuestData.QuestProgress progress) {
        boolean anyChange = false;
        // 固定点迭代：节点完成会激活子节点，子节点可能立即满足（状态判定型），直到无变化
        boolean progressed = true;
        while (progressed) {
            progressed = false;
            for (String nodeId : new ArrayList<>(progress.getActiveNodeIds())) {
                QuestNodeDef node = quest.getNode(nodeId);
                if (node == null) {
                    continue;
                }
                if (reevaluateNode(quest, node, team, server, progress)) {
                    progressed = true;
                    anyChange = true;
                }
            }
        }
        if (!progress.isCompleted()
                && quest.getLeafNodeIds().stream().allMatch(progress.getCompletedNodeIds()::contains)) {
            progress.markCompleted(server.overworld().getGameTime());
            // 奖励已在各节点完成时发放；完成提示交给 HUD 淡出动效，不发聊天消息（委托系统原有提示不受影响）
            anyChange = true;
        }
        return anyChange;
    }

    /** 评估单个活跃节点：目标全部满足则完成节点、发放节点奖励并激活子节点。返回是否有推进。 */
    private static boolean reevaluateNode(
            QuestDefinition quest,
            QuestNodeDef node,
            TeamData team,
            MinecraftServer server,
            TeamQuestData.QuestProgress progress) {
        boolean changed = false;
        boolean allDone = true;
        for (QuestObjectiveDef objective : node.objectives()) {
            if (progress.isObjectiveCompleted(node.id(), objective.id())) {
                continue;
            }
            if (isSatisfied(objective, team, server)) {
                progress.completeObjective(node.id(), objective.id());
                changed = true;
            } else {
                allDone = false;
            }
        }
        if (allDone) {
            progress.completeNode(node.id());
            grantRewards(node.rewards(), team, server);
            for (QuestNodeDef child : quest.getChildren(node.id())) {
                progress.activateNode(child.id());
            }
            changed = true;
        }
        return changed;
    }

    /** 状态判定型目标的纯判定函数；交互型目标不可轮询，恒为 false（由事件完成）。 */
    private static boolean isSatisfied(QuestObjectiveDef objective, TeamData team, MinecraftServer server) {
        List<ServerPlayer> online = onlineMembers(team, server);
        if (online.isEmpty()) {
            return false;
        }
        return switch (objective.type()) {
            case HAS_ITEM -> online.stream().anyMatch(player -> countItem(player, objective.itemId()) >= objective.count());
            case ADVANCEMENT -> online.stream().anyMatch(player -> hasAdvancement(player, objective.advancementId()));
            case ENTER_REGION -> anyMemberInRegion(objective, online, server);
            case INTERACT_ENTITY, INTERACT_BLOCK -> false;
        };
    }

    // ---------- 事件入口（交互型目标） ----------

    public static void onEntityInteract(ServerPlayer player, net.minecraft.world.entity.Entity target) {
        tryCompleteInteractObjectives(player, objective -> {
            if (objective.type() != QuestObjectiveType.INTERACT_ENTITY) {
                return false;
            }
            return objective.matchesEntity(
                    BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()), target.getUUID());
        });
    }

    public static void onBlockInteract(ServerPlayer player, Level level, BlockPos pos, BlockState state) {
        tryCompleteInteractObjectives(player, objective -> {
            if (objective.type() != QuestObjectiveType.INTERACT_BLOCK) {
                return false;
            }
            // 特定坐标目标只在城镇维度判定；方块类型/标签目标不限制维度
            if (objective.blockPos() != null && !level.dimension().equals(TownDimensions.TOWN_LEVEL)) {
                return false;
            }
            return objective.matchesBlock(state, pos);
        });
    }

    private static void tryCompleteInteractObjectives(
            ServerPlayer player, java.util.function.Predicate<QuestObjectiveDef> matcher) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            return;
        }
        boolean changed = false;
        for (QuestDefinition quest : QuestRegistry.all()) {
            TeamQuestData.QuestProgress progress = team.getQuestData().getProgress(quest.id());
            if (progress == null || progress.isCompleted()) {
                continue;
            }
            for (String nodeId : progress.getActiveNodeIds()) {
                QuestNodeDef node = quest.getNode(nodeId);
                if (node == null) {
                    continue;
                }
                for (QuestObjectiveDef objective : node.objectives()) {
                    if (!progress.isObjectiveCompleted(nodeId, objective.id()) && matcher.test(objective)) {
                        progress.completeObjective(nodeId, objective.id());
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            reevaluateTeam(team, player.server);
        }
    }

    // ---------- 奖励与反馈 ----------

    private static void grantRewards(List<QuestRewardDef> rewards, TeamData team, MinecraftServer server) {
        List<ServerPlayer> online = onlineMembers(team, server);
        for (QuestRewardDef reward : rewards) {
            switch (reward.type()) {
                case ITEM -> {
                    for (ServerPlayer player : online) {
                        giveItem(player, reward.itemId(), reward.count());
                    }
                }
                case COINS -> TeamManager.getInstance().addCoins(team, reward.count(), server);
            }
        }
    }

    private static void giveItem(ServerPlayer player, @Nullable ResourceLocation itemId, int count) {
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
        if (item.isEmpty() || count <= 0) {
            return;
        }
        ItemStack stack = new ItemStack(item.get(), count);
        if (!player.getInventory().add(stack)) {
            ItemEntity drop =
                    new ItemEntity(player.level(), player.getX(), player.getY() + 0.5D, player.getZ(), stack);
            drop.setPickUpDelay(0);
            player.level().addFreshEntity(drop);
        }
        player.getInventory().setChanged();
    }

    // ---------- 工具 ----------

    public static List<ServerPlayer> onlineMembers(TeamData team, MinecraftServer server) {
        List<ServerPlayer> online = new ArrayList<>();
        for (UUID memberId : team.getMembers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(memberId);
            if (player != null) {
                online.add(player);
            }
        }
        return online;
    }

    private static int countItem(ServerPlayer player, @Nullable ResourceLocation itemId) {
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(itemId);
        if (item.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item.get())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static boolean hasAdvancement(ServerPlayer player, @Nullable ResourceLocation advancementId) {
        if (advancementId == null) {
            return false;
        }
        AdvancementHolder holder = player.server.getAdvancements().get(advancementId);
        if (holder == null) {
            return false;
        }
        return player.getAdvancements().getOrStartProgress(holder).isDone();
    }

    private static boolean anyMemberInRegion(
            QuestObjectiveDef objective, List<ServerPlayer> online, MinecraftServer server) {
        Level townLevel = server.getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null || objective.regionMin() == null || objective.regionMax() == null) {
            return false;
        }
        BlockPos min = objective.regionMin();
        BlockPos max = objective.regionMax();
        if (objective.relative()) {
            BlockPos spawn = townLevel.getSharedSpawnPos();
            min = min.offset(spawn);
            max = max.offset(spawn);
        }
        AABB box = new AABB(min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1);
        for (ServerPlayer player : online) {
            if (player.level() == townLevel && box.contains(player.position())) {
                return true;
            }
        }
        return false;
    }

    public static String textFor(ServerPlayer player, LocalizedText text) {
        String language = player.clientInformation().language();
        return language != null && language.startsWith("zh") ? text.zh() : text.en();
    }
}
