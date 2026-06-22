package com.otherworldinn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.commission.CommissionService;
import com.otherworldinn.world.event.TownStructurePlacer;
import com.otherworldinn.world.inn.facility.FacilityRegistry;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.otherworldinn.world.inn.service.WanderingTraderManager;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

/**
 * 管理员命令
 *
 */
public class AdminCommands {
    private static final AABB TOWN_STORE_SCAN_AREA = new AABB(-1024, -64, -1024, 1024, 384, 1024);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("innadmin")
                        .requires(s -> s.hasPermission(2))
                        .then(
                                Commands.literal("facility")
                                        .then(
                                                Commands.literal("set_level")
                                                        .then(
                                                                Commands.argument("target", EntityArgument.player())
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "facilityId",
                                                                                                StringArgumentType
                                                                                                        .word())
                                                                                        .suggests(
                                                                                                (ctx,
                                                                                                        builder) -> {
                                                                                                    for (FacilityRegistry.FacilityDefinition facility :
                                                                                                            FacilityRegistry.getAll()) {
                                                                                                        builder.suggest(
                                                                                                                facility
                                                                                                                        .id());
                                                                                                    }
                                                                                                    return builder
                                                                                                            .buildFuture();
                                                                                                })
                                                                                        .then(
                                                                                                Commands.argument(
                                                                                                                "level",
                                                                                                                IntegerArgumentType
                                                                                                                        .integer(
                                                                                                                                0))
                                                                                                        .executes(
                                                                                                                AdminCommands
                                                                                                                        ::setFacilityLevel))))))
                        .then(
                                Commands.literal("commission")
                                        .then(
                                                Commands.literal("complete_current")
                                                        .then(
                                                                Commands.argument("target", EntityArgument.player())
                                                                        .executes(
                                                                                AdminCommands
                                                                                        ::completeCurrentCommission)))
                                        .then(
                                                Commands.literal("refresh")
                                                        .then(
                                                                Commands.argument("target", EntityArgument.player())
                                                                        .executes(
                                                                                AdminCommands
                                                                                        ::refreshCommissionBoard))))
                        .then(
                                Commands.literal("store")
                                        .then(
                                                Commands.literal("reset_all_npcs")
                                                        .executes(AdminCommands::resetAllStoreNpcs)))
                        .then(
                                Commands.literal("guests")
                                        .then(
                                                Commands.literal("clear_all")
                                                        .executes(AdminCommands::clearAllGuests)))
                        .then(
                                Commands.literal("wandering_trader")
                                        .then(
                                                Commands.literal("arrive")
                                                        .executes(AdminCommands::forceTraderArrive))
                                        .then(
                                                Commands.literal("leave")
                                                        .executes(AdminCommands::forceTraderLeave)))
        );
    }

    private static int setFacilityLevel(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            String facilityId = StringArgumentType.getString(context, "facilityId");
            int targetLevel = IntegerArgumentType.getInteger(context, "level");

            TeamManager manager = TeamManager.getInstance();
            TeamData team = manager.getPlayerTeam(target);
            if (team == null) {
                context.getSource()
                        .sendFailure(Component.translatable("command.otherworldinn.team.target_no_team"));
                return 0;
            }

            FacilityRegistry.FacilityDefinition facility = FacilityRegistry.get(facilityId);
            if (facility == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.admin.facility.not_found", facilityId));
                return 0;
            }

            if (targetLevel > facility.maxLevel()) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.admin.facility.level_out_of_range",
                                        facility.maxLevel()));
                return 0;
            }

            ServerLevel townLevel = context.getSource().getServer().getLevel(TownDimensions.TOWN_LEVEL);
            if (townLevel == null) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.admin.facility.town_unavailable"));
                return 0;
            }

            FacilityRegistry.FacilityLevelDefinition levelDefinition = facility.getLevel(targetLevel);
            boolean placed =
                    TownStructurePlacer.placeStructureTemplate(
                            townLevel, levelDefinition.structureId(), facility.centerPos());
            if (!placed) {
                context.getSource()
                        .sendFailure(
                                Component.translatable(
                                        "command.otherworldinn.admin.facility.place_fail"));
                return 0;
            }

            team.getInnData().setFacilityLevel(facility.id(), levelDefinition.level());
            if (levelDefinition.level() == 0) {
                manager.lockMapPoint(team, facility.mapPointId(), context.getSource().getServer());
            } else {
                manager.unlockMapPoint(team, facility.mapPointId(), context.getSource().getServer());
            }

            int updatedLevel = levelDefinition.level();
            context.getSource()
                    .sendSuccess(
                            () ->
                                    Component.translatable(
                                            "command.otherworldinn.admin.facility.downgrade_success",
                                            team.getName(),
                                            Component.translatable(facility.translationKey()),
                                            updatedLevel),
                            true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int completeCurrentCommission(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        TeamData team = TeamManager.getInstance().getPlayerTeam(target);
        if (team == null) {
            context.getSource().sendFailure(Component.translatable("command.otherworldinn.team.target_no_team"));
            return 0;
        }
        boolean ok = CommissionService.adminCompleteCurrentCommission(target);
        if (!ok) {
            context.getSource().sendFailure(Component.literal("目标队伍当前没有可完成的委托"));
            return 0;
        }
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        "已完成 "
                                                + target.getName().getString()
                                                + " 所在队伍的当前委托并发放奖励"),
                        true);
        return 1;
    }

    private static int refreshCommissionBoard(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        TeamData team = TeamManager.getInstance().getPlayerTeam(target);
        if (team == null) {
            context.getSource().sendFailure(Component.translatable("command.otherworldinn.team.target_no_team"));
            return 0;
        }
        boolean ok = CommissionService.adminRefreshBoard(target);
        if (!ok) {
            context.getSource().sendFailure(Component.literal("刷新委托板失败"));
            return 0;
        }
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        "已刷新 "
                                                + target.getName().getString()
                                                + " 所在队伍的委托板"),
                        true);
        return 1;
    }

    private static int resetAllStoreNpcs(CommandContext<CommandSourceStack> context) {
        ServerLevel townLevel = context.getSource().getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) {
            context.getSource()
                    .sendFailure(
                            Component.translatable(
                                    "command.otherworldinn.admin.store.reset_all.town_unavailable"));
            return 0;
        }

        int resetCount = 0;
        for (StoreEntity storeEntity :
                townLevel.getEntitiesOfClass(StoreEntity.class, TOWN_STORE_SCAN_AREA)) {
            storeEntity.debugResetToCodeDefaults();
            resetCount++;
        }

        final int finalResetCount = resetCount;
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.translatable(
                                        "command.otherworldinn.admin.store.reset_all.success",
                                        finalResetCount),
                        true);
        return 1;
    }

    private static int clearAllGuests(CommandContext<CommandSourceStack> context) {
        ServerLevel townLevel = context.getSource().getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) {
            context.getSource()
                    .sendFailure(Component.literal("城镇维度不可用"));
            return 0;
        }

        // 1. 杀死所有旅客实体
        List<GuestEntity> guests = townLevel.getEntitiesOfClass(
                GuestEntity.class, new AABB(-1024, -64, -1024, 1024, 384, 1024));
        for (GuestEntity guest : guests) {
            guest.kill();
        }

        // 2. 清除所有队伍的旅客数据和待办事项
        TeamManager manager = TeamManager.getInstance();
        int teamCount = 0;
        for (TeamData team : manager.getAllTeams(context.getSource().getServer())) {
            team.getInnData().clearAllGuests(townLevel, team);
            teamCount++;
        }

        final int killed = guests.size();
        final int teams = teamCount;
        context.getSource()
                .sendSuccess(
                        () -> Component.literal(
                                "已清除 " + killed + " 个旅客实体，重置 " + teams + " 支队伍的入住数据"),
                        true);
        return 1;
    }

    private static int forceTraderArrive(CommandContext<CommandSourceStack> context) {
        ServerLevel townLevel = context.getSource().getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) {
            context.getSource().sendFailure(Component.literal("城镇维度不可用"));
            return 0;
        }
        WanderingTraderManager.forceArrive(townLevel);
        context.getSource().sendSuccess(
                () -> Component.literal("已强制使游商到达并刷新商品"), true);
        return 1;
    }

    private static int forceTraderLeave(CommandContext<CommandSourceStack> context) {
        ServerLevel townLevel = context.getSource().getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) {
            context.getSource().sendFailure(Component.literal("城镇维度不可用"));
            return 0;
        }
        boolean removed = WanderingTraderManager.forceLeave(townLevel);
        if (removed) {
            context.getSource().sendSuccess(
                    () -> Component.literal("已强制使游商离开，将在 2-5 天后再次出现"), true);
        } else {
            context.getSource().sendFailure(Component.literal("游商当前不在城镇中"));
        }
        return removed ? 1 : 0;
    }
}
