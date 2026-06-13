package com.otherworldinn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/*
 * 队伍命令
 *
 * <p>
 * /innteam create <name> - 创建队伍 
 * /innteam invite <player> - 邀请玩家（简化：直接加入） 
 * /innteam join <player_in_team> - 加入某玩家所在的队伍 
 * /innteam leave - 离开队伍 
 * /innteam kick <player> - 踢出成员（仅队长） 
 * /innteam transfer <player> - 转让队长（仅队长） 
 * /innteam rename <name> - 重命名队伍（仅队长） 
 * /innteam info - 查看队伍信息 
 * /innteam teleport <enabled> - 开启/关闭队伍传送（管理员）
 */
public class TeamCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("innteam")
                        .then(
                                Commands.literal("create")
                                        .then(
                                                Commands.argument(
                                                                "name",
                                                                StringArgumentType.greedyString())
                                                        .executes(TeamCommands::createTeam)))
                        .then(
                                Commands.literal("join")
                                        .then(
                                                Commands.argument("target", EntityArgument.player())
                                                        .executes(TeamCommands::joinTeam)))
                        .then(Commands.literal("leave").executes(TeamCommands::leaveTeam))
                        .then(
                                Commands.literal("kick")
                                        .then(
                                                Commands.argument("target", EntityArgument.player())
                                                        .executes(TeamCommands::kickMember)))
                        .then(
                                Commands.literal("transfer")
                                        .then(
                                                Commands.argument("target", EntityArgument.player())
                                                        .executes(TeamCommands::transferLeader)))
                        .then(
                                Commands.literal("rename")
                                        .then(
                                                Commands.argument(
                                                                "name",
                                                                StringArgumentType.greedyString())
                                                        .executes(TeamCommands::renameTeam)))
                        .then(Commands.literal("info").executes(TeamCommands::teamInfo))
                        .then(
                                Commands.literal("teleport")
                                        .requires(s -> s.hasPermission(2)) // 需要管理员权限
                                        .then(
                                                Commands.argument(
                                                                "enabled", BoolArgumentType.bool())
                                                        .executes(TeamCommands::toggleTeleport)))
                        .then(
                                Commands.literal("state")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.argument(
                                                                "state", StringArgumentType.word())
                                                        .suggests(
                                                                (ctx, builder) -> {
                                                                    for (InnData.InnState state :
                                                                            InnData.InnState
                                                                                    .values()) {
                                                                        builder.suggest(
                                                                                state.name()
                                                                                        .toLowerCase());
                                                                    }
                                                                    return builder.buildFuture();
                                                                })
                                                        .executes(TeamCommands::setInnState)))
                        .then(
                                Commands.literal("rating")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.argument(
                                                                "value",
                                                                IntegerArgumentType.integer(0, 5))
                                                        .executes(
                                                                ctx ->
                                                                        setInnRating(ctx, null))
                                                        .then(
                                                                Commands.argument(
                                                                                "target",
                                                                                EntityArgument
                                                                                        .player())
                                                                        .executes(
                                                                                ctx ->
                                                                                        setInnRating(
                                                                                                ctx,
                                                                                                EntityArgument
                                                                                                        .getPlayer(
                                                                                                                ctx,
                                                                                                                "target"))))))
                        .then(
                                Commands.literal("unlockpoint")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.argument("target", EntityArgument.player())
                                                        .then(
                                                                Commands.argument(
                                                                                "pointId",
                                                                                ResourceLocationArgument
                                                                                        .id())
                                                                        .executes(
                                                                                TeamCommands
                                                                                        ::unlockMapPoint))))
                        .then(
                                Commands.literal("lockpoint")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.argument("target", EntityArgument.player())
                                                        .then(
                                                                Commands.argument(
                                                                                "pointId",
                                                                                ResourceLocationArgument
                                                                                        .id())
                                                                        .executes(
                                                                                TeamCommands
                                                                                        ::lockMapPoint))))
                        .then(
                                Commands.literal("coins")
                                        .requires(s -> s.hasPermission(2))
                                        .then(
                                                Commands.literal("set")
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                IntegerArgumentType
                                                                                        .integer(0))
                                                                        .executes(
                                                                                ctx ->
                                                                                        setCoins(
                                                                                                ctx,
                                                                                                null))
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "target",
                                                                                                EntityArgument
                                                                                                        .player())
                                                                                        .executes(
                                                                                                ctx ->
                                                                                                        setCoins(
                                                                                                                ctx,
                                                                                                                EntityArgument
                                                                                                                        .getPlayer(
                                                                                                                                ctx,
                                                                                                                                "target"))))))
                                        .then(
                                                Commands.literal("add")
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                IntegerArgumentType
                                                                                        .integer(1))
                                                                        .executes(
                                                                                ctx ->
                                                                                        addCoins(
                                                                                                ctx,
                                                                                                null))
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "target",
                                                                                                EntityArgument
                                                                                                        .player())
                                                                                        .executes(
                                                                                                ctx ->
                                                                                                        addCoins(
                                                                                                                ctx,
                                                                                                                EntityArgument
                                                                                                                        .getPlayer(
                                                                                                                                ctx,
                                                                                                                                "target"))))))
                                        .then(
                                                Commands.literal("remove")
                                                        .then(
                                                                Commands.argument(
                                                                                "amount",
                                                                                IntegerArgumentType
                                                                                        .integer(1))
                                                                        .executes(
                                                                                ctx ->
                                                                                        removeCoins(
                                                                                                ctx,
                                                                                                null))
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "target",
                                                                                                EntityArgument
                                                                                                        .player())
                                                                                        .executes(
                                                                                                ctx ->
                                                                                                        removeCoins(
                                                                                                                ctx,
                                                                                                                EntityArgument
                                                                                                                        .getPlayer(
                                                                                                                                ctx,
                                                                                                                                "target"))))))
                                        .then(
                                                Commands.literal("get")
                                                        .executes(ctx -> getCoins(ctx, null))
                                                        .then(
                                                                Commands.argument(
                                                                                "target",
                                                                                EntityArgument
                                                                                        .player())
                                                                        .executes(
                                                                                ctx ->
                                                                                        getCoins(
                                                                                                ctx,
                                                                                                EntityArgument
                                                                                                        .getPlayer(
                                                                                                                ctx,
                                                                                                                "target")))))));
    }

    private static int setCoins(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        try {
            if (target == null) target = context.getSource().getPlayerOrException();
            int amount = IntegerArgumentType.getInteger(context, "amount");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(target);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.target_no_team"));
                return 0;
            }

            team.setCoins(amount, context.getSource().getServer());
            manager.syncTeam(team, context.getSource().getServer());

            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.coins.set",
                                            team.getName(),
                                            amount),
                            true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int addCoins(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        try {
            if (target == null) target = context.getSource().getPlayerOrException();
            int amount = IntegerArgumentType.getInteger(context, "amount");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(target);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.target_no_team"));
                return 0;
            }

            team.addCoins(amount, context.getSource().getServer());
            manager.syncTeam(team, context.getSource().getServer());

            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.coins.add",
                                            team.getName(),
                                            amount,
                                            team.getCoins()),
                            true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int removeCoins(
            CommandContext<CommandSourceStack> context, ServerPlayer target) {
        try {
            if (target == null) target = context.getSource().getPlayerOrException();
            int amount = IntegerArgumentType.getInteger(context, "amount");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(target);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.target_no_team"));
                return 0;
            }

            if (team.removeCoins(amount, context.getSource().getServer())) {
                manager.syncTeam(team, context.getSource().getServer());
                context.getSource()
                        .sendSuccess(
                                () ->
                                        Component.translatable(
                                                "command.otherworldinn.team.coins.remove",
                                                team.getName(),
                                                amount,
                                                team.getCoins()),
                                true);
                return 1;
            } else {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.coins.remove_fail",
                                        team.getCoins()));
                return 0;
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int getCoins(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        try {
            if (target == null) target = context.getSource().getPlayerOrException();

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(target);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.target_no_team"));
                return 0;
            }

            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.coins.get",
                                            team.getName(),
                                            team.getCoins()),
                            false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int unlockMapPoint(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            ResourceLocation pointId = ResourceLocationArgument.getId(context, "pointId");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(target);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.target_no_team"));
                return 0;
            }

            manager.unlockMapPoint(team, pointId, context.getSource().getServer());
            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.point_unlocked",
                                            pointId.toString(),
                                            team.getName()),
                            true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int lockMapPoint(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            ResourceLocation pointId = ResourceLocationArgument.getId(context, "pointId");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(target);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.target_no_team"));
                return 0;
            }

            manager.lockMapPoint(team, pointId, context.getSource().getServer());
            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.point_locked",
                                            pointId.toString(),
                                            team.getName()),
                            true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setInnState(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String stateStr = StringArgumentType.getString(context, "state");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(player);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable("command.otherworldinn.team.not_in_team"));
                return 0;
            }

            try {
                InnData.InnState newState = InnData.InnState.valueOf(stateStr.toUpperCase());
                if (team.getInnData().setState(newState)) {
                    context.getSource()
                            .sendSuccess(
                                    () ->
                                            Component.translatable(
                                                    "command.otherworldinn.team.state_set",
                                                    newState.name()),
                                    true);
                    manager.syncTeam(team, player.getServer());
                    return 1;
                } else {
                    context.getSource()
                            .sendFailure(
                                    Component.translatable(
                                            "command.otherworldinn.team.state_set_fail"));
                    return 0;
                }
            } catch (IllegalArgumentException e) {
                context.getSource().sendFailure(Component.literal("Invalid state: " + stateStr));
                return 0;
            }
        } catch (CommandSyntaxException e) {
            context.getSource()
                    .sendFailure(Component.literal("Command Syntax Error: " + e.getMessage()));
            return 0;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int toggleTeleport(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            boolean enabled = BoolArgumentType.getBool(context, "enabled");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(player);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable("command.otherworldinn.team.not_in_team"));
                return 0;
            }

            manager.setTeamTeleportUnlocked(team, enabled, context.getSource().getServer());
            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.teleport_set", enabled),
                            true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setInnRating(
            CommandContext<CommandSourceStack> context, ServerPlayer target) {
        try {
            if (target == null) target = context.getSource().getPlayerOrException();
            int value = IntegerArgumentType.getInteger(context, "value");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(target);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.target_no_team"));
                return 0;
            }

            team.getInnData().setRating(value);
            manager.syncTeam(team, context.getSource().getServer());

            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.rating.set",
                                            team.getName(),
                                            team.getInnData().getRating()),
                            true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int createTeam(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(context, "name");

            TeamManager manager = TeamManager.getInstance();
            if (manager.getPlayerTeam(player) != null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.already_in_team"));
                return 0;
            }

            TeamData team = manager.createTeam(player, name);
            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.created", team.getName()),
                            true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int joinTeam(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "target");

            TeamManager manager = TeamManager.getInstance();
            TeamData targetTeam = manager.getPlayerTeam(target);

            if (targetTeam == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.target_no_team"));
                return 0;
            }

            // 简化逻辑：直接加入，无需同意
            manager.joinTeam(player, targetTeam.getTeamId());
            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.joined",
                                            targetTeam.getName()),
                            true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int leaveTeam(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            TeamManager manager = TeamManager.getInstance();

            if (manager.getPlayerTeam(player) == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable("command.otherworldinn.team.not_in_team"));
                return 0;
            }

            manager.leaveTeam(player);
            context.getSource()
                    .sendSuccess(
                            () -> Component.translatable("command.otherworldinn.team.left"), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int kickMember(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "target");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(player);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable("command.otherworldinn.team.not_in_team"));
                return 0;
            }

            if (!player.getUUID().equals(team.getLeaderId())) {
                context.getSource()
                        .sendFailure(
                                Component.translatable("command.otherworldinn.team.not_leader"));
                return 0;
            }

            if (!team.hasMember(target.getUUID())) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.target_not_in_team"));
                return 0;
            }

            if (player.getUUID().equals(target.getUUID())) {
                context.getSource()
                        .sendFailure(
                                Component.translatable("command.otherworldinn.team.kick_self"));
                return 0;
            }

            manager.leaveTeam(target);
            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.kicked",
                                            target.getName().getString()),
                            true);
            target.sendSystemMessage(
                    Component.translatable("command.otherworldinn.team.you_were_kicked"));

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int transferLeader(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ServerPlayer target = EntityArgument.getPlayer(context, "target");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(player);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable("command.otherworldinn.team.not_in_team"));
                return 0;
            }

            if (!player.getUUID().equals(team.getLeaderId())) {
                context.getSource()
                        .sendFailure(
                                Component.translatable("command.otherworldinn.team.not_leader"));
                return 0;
            }

            if (!team.hasMember(target.getUUID())) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.team.target_not_in_team"));
                return 0;
            }

            manager.transferLeader(team, target.getUUID(), context.getSource().getServer());
            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.transferred",
                                            target.getName().getString()),
                            true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int renameTeam(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            String name = StringArgumentType.getString(context, "name");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(player);

            if (team == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable("command.otherworldinn.team.not_in_team"));
                return 0;
            }

            if (!player.getUUID().equals(team.getLeaderId())) {
                context.getSource()
                        .sendFailure(
                                Component.translatable("command.otherworldinn.team.not_leader"));
                return 0;
            }

            manager.renameTeam(team, name, context.getSource().getServer());
            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.team.renamed", name),
                            true);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int teamInfo(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(player);

            if (team == null) {
                context.getSource()
                        .sendSuccess(
                                () ->
                                        Component.translatable(
                                                "command.otherworldinn.team.not_in_team"),
                                false);
                return 1;
            }

            context.getSource()
                    .sendSuccess(() -> Component.literal("Team: " + team.getName()), false);
            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.literal(
                                            "Leader: "
                                                    + (team.getLeaderId() != null
                                                            ? team.getLeaderId().toString()
                                                            : "None")),
                            false);
            context.getSource()
                    .sendSuccess(
                            () -> Component.literal("Members: " + team.getMembers().size()), false);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

}
