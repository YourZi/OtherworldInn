package com.otherworldinn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.otherworldinn.world.quest.QuestDefinition;
import com.otherworldinn.world.quest.QuestRegistry;
import com.otherworldinn.world.quest.QuestService;
import com.otherworldinn.world.quest.TeamQuestData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/*
 * 任务调试命令
 *
 * /innquest list - 列出全部任务定义与本队接取/完成状态
 * /innquest state - 查看本队活跃任务的活跃节点
 * /innquest start <questId> - 接取任务（管理员，调试用；正式接取走对话选项）
 * /innquest reset <questId|all> - 清除任务进度以便反复测试（管理员）
 */
public class QuestCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("innquest")
                        .then(Commands.literal("list").executes(QuestCommands::list))
                        .then(Commands.literal("state").executes(QuestCommands::state))
                        .then(
                                Commands.literal("start")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.argument("questId", StringArgumentType.word())
                                                        .executes(QuestCommands::start)))
                        .then(
                                Commands.literal("reset")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.argument("questId", StringArgumentType.word())
                                                        .executes(QuestCommands::reset))));
    }

    private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!(context.getSource().getPlayerOrException() instanceof ServerPlayer player)) {
            return 0;
        }
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        for (QuestDefinition quest : QuestRegistry.all()) {
            String status;
            if (team == null) {
                status = "未组队";
            } else if (team.getQuestData().isCompleted(quest.id())) {
                status = "已完成";
            } else if (team.getQuestData().isAccepted(quest.id())) {
                status = "进行中";
            } else {
                status = "未接取";
            }
            context.getSource().sendSuccess(
                    () -> Component.literal(quest.id() + " [" + quest.category().name().toLowerCase() + "] "
                            + status),
                    false);
        }
        return 1;
    }

    private static int state(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            context.getSource().sendFailure(Component.literal("你还没有加入队伍"));
            return 0;
        }
        for (Map.Entry<String, TeamQuestData.QuestProgress> entry : team.getQuestData().all().entrySet()) {
            if (entry.getValue().isCompleted()) {
                context.getSource().sendSuccess(
                        () -> Component.literal(entry.getKey() + "：已完成"), false);
            } else {
                context.getSource().sendSuccess(
                        () -> Component.literal(
                                entry.getKey() + "：活跃节点 " + entry.getValue().getActiveNodeIds()),
                        false);
            }
        }
        return 1;
    }

    private static int start(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String questId = StringArgumentType.getString(context, "questId");
        if (QuestService.startQuest(player, questId)) {
            context.getSource().sendSuccess(() -> Component.literal("已接取任务 " + questId), true);
            return 1;
        }
        context.getSource().sendFailure(Component.literal("接取失败：任务不存在、已接取或未组队"));
        return 0;
    }

    /*
     * /innquest reset <questId|all> - 清除任务进度以便重新测试（管理员）
     */
    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String questId = StringArgumentType.getString(context, "questId");
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            context.getSource().sendFailure(Component.literal("你还没有加入队伍"));
            return 0;
        }
        if ("all".equalsIgnoreCase(questId)) {
            if (QuestService.resetAllQuests(team, player.server)) {
                context.getSource().sendSuccess(() -> Component.literal("已清除全部任务进度"), true);
                return 1;
            }
            context.getSource().sendFailure(Component.literal("当前没有任何任务进度"));
            return 0;
        }
        if (QuestRegistry.get(questId) == null) {
            context.getSource().sendFailure(Component.literal("任务不存在：" + questId));
            return 0;
        }
        if (QuestService.resetQuest(team, questId, player.server)) {
            context.getSource().sendSuccess(
                    () -> Component.literal("已清除任务进度：" + questId + "（可重新接取）"), true);
            return 1;
        }
        context.getSource().sendFailure(Component.literal("该任务没有进度记录"));
        return 0;
    }
}
