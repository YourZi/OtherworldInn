package com.otherworldinn.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.entity.guest.StoryGuestEntity;
import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.commission.CommissionService;
import com.otherworldinn.world.event.TownStructurePlacer;
import com.otherworldinn.world.event.listener.PlayerFatigueHandler;
import com.otherworldinn.world.fatigue.FatigueCalculator;
import com.otherworldinn.world.fatigue.FatigueData;
import com.otherworldinn.world.festival.FestivalDefinition;
import com.otherworldinn.world.festival.FestivalRegistry;
import com.otherworldinn.world.festival.FestivalService;
import com.otherworldinn.world.inn.facility.FacilityRegistry;
import com.otherworldinn.world.storyguest.StoryGuestDefinition;
import com.otherworldinn.world.storyguest.StoryGuestProgress;
import com.otherworldinn.world.storyguest.StoryGuestSavedData;
import com.otherworldinn.world.storyguest.StoryGuestService;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.otherworldinn.world.inn.service.WanderingTraderManager;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
                                                        .executes(
                                                                AdminCommands::resetAllStoreNpcs))
                                        .then(
                                                Commands.literal("global_discount")
                                                        .executes(
                                                                AdminCommands::getGlobalDiscount)
                                                        .then(
                                                                Commands.argument(
                                                                                "rate",
                                                                                DoubleArgumentType
                                                                                        .doubleArg(
                                                                                                0.0D,
                                                                                                0.9D))
                                                                        .executes(
                                                                                AdminCommands
                                                                                        ::setGlobalDiscount))))
                        .then(
                                Commands.literal("festival")
                                        .executes(AdminCommands::getDebugFestival)
                                        .then(
                                                Commands.literal("set_active")
                                                        .then(
                                                                Commands.argument("id", StringArgumentType.word())
                                                                        .suggests(
                                                                                (context, builder) -> {
                                                                                    for (FestivalDefinition festival :
                                                                                            FestivalRegistry.getAll()) {
                                                                                        builder.suggest(
                                                                                                festival.id());
                                                                                    }
                                                                                    return builder.buildFuture();
                                                                                })
                                                                        .executes(
                                                                                AdminCommands::setDebugFestival)))
                                        .then(
                                                Commands.literal("clear")
                                                        .executes(AdminCommands::clearDebugFestival)))
                        .then(
                                Commands.literal("guests")
                                        .then(
                                                Commands.literal("clear_all")
                                                        .executes(AdminCommands::clearAllGuests)))
                        .then(
                                Commands.literal("story_guest")
                                        .then(
                                                Commands.literal("spawn")
                                                        .then(
                                                                Commands.argument("id", StringArgumentType.word())
                                                                        .suggests(
                                                                                (context, builder) -> {
                                                                                    for (String id : StoryGuestService
                                                                                            .getAllStoryGuestIds()) {
                                                                                        builder.suggest(id);
                                                                                    }
                                                                                    return builder.buildFuture();
                                                                                })
                                                                        .executes(AdminCommands::spawnStoryGuestHere)
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "pos",
                                                                                                Vec3Argument.vec3())
                                                                                        .executes(
                                                                                                AdminCommands
                                                                                                        ::spawnStoryGuestAtPos))))
                                        .then(
                                                Commands.literal("leave")
                                                        .then(
                                                                Commands.argument("id", StringArgumentType.word())
                                                                        .suggests(
                                                                                (context, builder) -> {
                                                                                    for (String id : StoryGuestService
                                                                                            .getAllStoryGuestIds()) {
                                                                                        builder.suggest(id);
                                                                                    }
                                                                                    return builder.buildFuture();
                                                                                })
                                                                        .executes(AdminCommands::leaveStoryGuest)))
                                        .then(Commands.literal("reset_all").executes(AdminCommands::resetAllStoryGuests))
                                        .then(
                                                Commands.literal("info")
                                                        .then(
                                                                Commands.argument("id", StringArgumentType.word())
                                                                        .suggests(
                                                                                (context, builder) -> {
                                                                                    for (String id : StoryGuestService
                                                                                            .getAllStoryGuestIds()) {
                                                                                        builder.suggest(id);
                                                                                    }
                                                                                    return builder.buildFuture();
                                                                                })
                                                                        .executes(AdminCommands::infoStoryGuest)))
                                        .then(
                                                Commands.literal("set_stage")
                                                        .then(
                                                                Commands.argument("id", StringArgumentType.word())
                                                                        .suggests(
                                                                                (context, builder) -> {
                                                                                    for (String id : StoryGuestService
                                                                                            .getAllStoryGuestIds()) {
                                                                                        builder.suggest(id);
                                                                                    }
                                                                                    return builder.buildFuture();
                                                                                })
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "stage",
                                                                                                IntegerArgumentType
                                                                                                        .integer(0))
                                                                                        .executes(AdminCommands::setStoryGuestStage))))
                                        .then(
                                                Commands.literal("clear_active")
                                                        .then(
                                                                Commands.argument("id", StringArgumentType.word())
                                                                        .suggests(
                                                                                (context, builder) -> {
                                                                                    for (String id : StoryGuestService
                                                                                            .getAllStoryGuestIds()) {
                                                                                        builder.suggest(id);
                                                                                    }
                                                                                    return builder.buildFuture();
                                                                                })
                                                                        .executes(AdminCommands::clearStoryGuestActive)))
                        )
                        .then(
                                Commands.literal("wandering_trader")
                                        .then(
                                                Commands.literal("arrive")
                                                        .executes(AdminCommands::forceTraderArrive))
                                        .then(
                                                Commands.literal("leave")
                                                        .executes(AdminCommands::forceTraderLeave)))
                        .then(
                                Commands.literal("fatigue")
                                        .then(
                                                Commands.literal("get")
                                                        .then(
                                                                Commands.argument("target", EntityArgument.player())
                                                                        .executes(AdminCommands::getFatigue)))
                                        .then(
                                                Commands.literal("set")
                                                        .then(
                                                                Commands.argument("target", EntityArgument.player())
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "value",
                                                                                                IntegerArgumentType.integer(
                                                                                                        0,
                                                                                                        (int) FatigueData.MAX_FATIGUE))
                                                                                        .executes(AdminCommands::setFatigue))))
                                        .then(
                                                Commands.literal("add")
                                                        .then(
                                                                Commands.argument("target", EntityArgument.player())
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "value",
                                                                                                IntegerArgumentType.integer(
                                                                                                        0,
                                                                                                        (int) FatigueData.MAX_FATIGUE))
                                                                                        .executes(AdminCommands::addFatigue))))
                                        .then(
                                                Commands.literal("reduce")
                                                        .then(
                                                                Commands.argument("target", EntityArgument.player())
                                                                        .then(
                                                                                Commands.argument(
                                                                                                "value",
                                                                                                IntegerArgumentType.integer(
                                                                                                        0,
                                                                                                        (int) FatigueData.MAX_FATIGUE))
                                                                                        .executes(AdminCommands::reduceFatigue))))
                        )
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

    /** 查看当前全局调试折扣 */
    private static int getGlobalDiscount(CommandContext<CommandSourceStack> context) {
        double rate = FestivalService.getDebugGlobalDiscount();
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        rate > 0.0D
                                                ? "全局调试折扣: " + (int) (rate * 100) + "%"
                                                : "全局调试折扣: 关闭"),
                        false);
        return 1;
    }

    /** 设置全局调试折扣（0~0.9，0 关闭；覆盖节日折扣，对所有商店生效并同步给在线玩家） */
    private static int setGlobalDiscount(CommandContext<CommandSourceStack> context) {
        double rate = DoubleArgumentType.getDouble(context, "rate");
        FestivalService.setDebugGlobalDiscount(rate);
        rate = FestivalService.getDebugGlobalDiscount(); // 读取钳制后的实际值
        ServerLevel townLevel =
                context.getSource().getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel != null) {
            FestivalService.syncShopDiscounts(townLevel);
        }
        final double finalRate = rate;
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        finalRate > 0.0D
                                                ? "已设置全局调试折扣: " + (int) (finalRate * 100) + "%"
                                                : "已关闭全局调试折扣"),
                        false);
        return 1;
    }

    /** 查看当前节日状态（调试强制激活的节日或季节驱动的真实节日） */
    private static int getDebugFestival(CommandContext<CommandSourceStack> context) {
        String debugId = FestivalService.getDebugActiveFestivalId();
        if (debugId != null) {
            context.getSource()
                    .sendSuccess(() -> Component.literal("调试强制激活节日: " + debugId), false);
            return 1;
        }
        ServerLevel townLevel =
                context.getSource().getServer().getLevel(TownDimensions.TOWN_LEVEL);
        String active = FestivalService.getActiveFestival(townLevel)
                .map(FestivalDefinition::id)
                .orElse("无");
        context.getSource().sendSuccess(() -> Component.literal("当前激活节日: " + active + "（季节驱动）"), false);
        return 1;
    }

    /** 强制激活指定节日（调试：限定商品/限定委托/效果加成全部生效，立即广播开始事件） */
    private static int setDebugFestival(CommandContext<CommandSourceStack> context) {
        String id = StringArgumentType.getString(context, "id");
        if (FestivalRegistry.get(id).isEmpty()) {
            context.getSource().sendFailure(Component.literal("未知节日: " + id));
            return 0;
        }
        ServerLevel townLevel =
                context.getSource().getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) {
            context.getSource().sendFailure(Component.literal("城镇维度不可用"));
            return 0;
        }
        FestivalService.setDebugActiveFestivalId(id, townLevel);
        context.getSource()
                .sendSuccess(
                        () -> Component.literal("已强制激活节日: " + id + "（调试，不写入节日窗口记录）"),
                        false);
        return 1;
    }

    /** 清除调试强制节日，恢复季节驱动（立即广播结束事件） */
    private static int clearDebugFestival(CommandContext<CommandSourceStack> context) {
        ServerLevel townLevel =
                context.getSource().getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) {
            context.getSource().sendFailure(Component.literal("城镇维度不可用"));
            return 0;
        }
        FestivalService.setDebugActiveFestivalId(null, townLevel);
        context.getSource()
                .sendSuccess(() -> Component.literal("已清除强制节日，恢复季节驱动"), false);
        return 1;
    }

    private static int clearAllGuests(CommandContext<CommandSourceStack> context) {
        ServerLevel townLevel = context.getSource().getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) {
            context.getSource()
                    .sendFailure(Component.literal("城镇维度不可用"));
            return 0;
        }

        List<GuestEntity> guests = townLevel.getEntitiesOfClass(
                GuestEntity.class, new AABB(-1024, -64, -1024, 1024, 384, 1024));
        for (GuestEntity guest : guests) {
            guest.kill();
        }

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

    private static int spawnStoryGuestHere(CommandContext<CommandSourceStack> context) {
        return spawnStoryGuest(context, context.getSource().getPosition());
    }

    private static int spawnStoryGuestAtPos(CommandContext<CommandSourceStack> context) {
        return spawnStoryGuest(context, Vec3Argument.getVec3(context, "pos"));
    }

    private static int spawnStoryGuest(CommandContext<CommandSourceStack> context, Vec3 position) {
        String storyGuestId = StringArgumentType.getString(context, "id");
        StoryGuestDefinition definition = StoryGuestService.getDefinition(storyGuestId);
        if (definition == null) {
            context.getSource().sendFailure(Component.literal("未知的故事旅客ID: " + storyGuestId));
            return 0;
        }
        ServerLevel level = context.getSource().getLevel();
        StoryGuestEntity guest = StoryGuestService.spawnDebugGuest(level, storyGuestId, position);
        if (guest == null) {
            context.getSource().sendFailure(Component.literal("生成失败，可能该故事旅客当前已经处于活跃状态"));
            return 0;
        }
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        "已生成故事旅客 "
                                                + definition.displayName().zh()
                                                + " (" + definition.id() + ") 于 "
                                                + String.format("%.2f %.2f %.2f", position.x, position.y, position.z)),
                        true);
        return 1;
    }

    private static int leaveStoryGuest(CommandContext<CommandSourceStack> context) {
        String storyGuestId = StringArgumentType.getString(context, "id");
        StoryGuestDefinition definition = StoryGuestService.getDefinition(storyGuestId);
        if (definition == null) {
            context.getSource().sendFailure(Component.literal("未知的故事旅客ID: " + storyGuestId));
            return 0;
        }
        ServerLevel baseLevel = context.getSource().getLevel();
        if (!StoryGuestService.forceLeaveActiveStoryGuest(baseLevel, storyGuestId)) {
            context.getSource().sendFailure(Component.literal("该故事旅客当前没有活跃实体可离开"));
            return 0;
        }
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        "已强制让故事旅客 "
                                                + definition.displayName().zh()
                                                + " 离开，并写回回访进度"),
                        true);
        return 1;
    }

    private static int resetAllStoryGuests(CommandContext<CommandSourceStack> context) {
        int removedEntities = StoryGuestService.resetAllStoryGuestProgress(context.getSource().getServer());
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        "已重置全部故事旅客进度，并清除 "
                                                + removedEntities
                                                + " 个故事旅客实体"),
                        true);
        return 1;
    }

    private static int infoStoryGuest(CommandContext<CommandSourceStack> context) {
        String storyGuestId = StringArgumentType.getString(context, "id");
        StoryGuestDefinition definition = StoryGuestService.getDefinition(storyGuestId);
        if (definition == null) {
            context.getSource().sendFailure(Component.literal("未知的故事旅客ID: " + storyGuestId));
            return 0;
        }
        ServerLevel baseLevel = context.getSource().getLevel();
        StoryGuestProgress progress = StoryGuestService.getOrCreateProgress(baseLevel, storyGuestId);
        StoryGuestEntity activeGuest = StoryGuestService.getActiveStoryGuest(baseLevel, storyGuestId);
        StoryGuestSavedData savedData = StoryGuestSavedData.get(baseLevel);
        context.getSource().sendSuccess(
                () -> Component.literal("故事旅客: " + definition.displayName().zh() + " (" + definition.id() + ")"),
                false);
        context.getSource().sendSuccess(
                () -> Component.literal("storyStage: " + progress.getStoryStage()),
                false);
        context.getSource().sendSuccess(
                () -> Component.literal("visitCount: " + progress.getVisitCount()),
                false);
        context.getSource().sendSuccess(
                () -> Component.literal("lastCheckoutDay: " + progress.getLastCheckoutDay()),
                false);
        context.getSource().sendSuccess(
                () -> Component.literal("nextEligibleVisitDay: " + progress.getNextEligibleVisitDay()),
                false);
        context.getSource().sendSuccess(
                () -> Component.literal("globalNextEligibleVisitDay: " + savedData.getGlobalNextEligibleVisitDay()),
                false);
        context.getSource().sendSuccess(
                () -> Component.literal("activeEntityUuid: " + progress.getActiveEntityUuid()),
                false);
        context.getSource().sendSuccess(
                () -> Component.literal("storyFlags: " + progress.getStoryFlags()),
                false);
        context.getSource().sendSuccess(
                () -> Component.literal("canVisitAgain: " + StoryGuestService.canVisitAgain(baseLevel, storyGuestId)),
                false);
        context.getSource().sendSuccess(
                () ->
                        Component.literal(
                                "activeEntity: "
                                        + (activeGuest == null
                                                ? "null"
                                                : activeGuest.getUUID() + " @ " + activeGuest.blockPosition())),
                false);
        return 1;
    }

    private static int setStoryGuestStage(CommandContext<CommandSourceStack> context) {
        String storyGuestId = StringArgumentType.getString(context, "id");
        int stage = IntegerArgumentType.getInteger(context, "stage");
        StoryGuestDefinition definition = StoryGuestService.getDefinition(storyGuestId);
        if (definition == null) {
            context.getSource().sendFailure(Component.literal("未知的故事旅客ID: " + storyGuestId));
            return 0;
        }
        if (!StoryGuestService.setStoryStage(context.getSource().getLevel(), storyGuestId, stage)) {
            context.getSource().sendFailure(Component.literal("设置阶段失败"));
            return 0;
        }
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        "已将 "
                                                + definition.displayName().zh()
                                                + " 的 storyStage 设置为 "
                                                + stage),
                        true);
        return 1;
    }

    private static int clearStoryGuestActive(CommandContext<CommandSourceStack> context) {
        String storyGuestId = StringArgumentType.getString(context, "id");
        StoryGuestDefinition definition = StoryGuestService.getDefinition(storyGuestId);
        if (definition == null) {
            context.getSource().sendFailure(Component.literal("未知的故事旅客ID: " + storyGuestId));
            return 0;
        }
        if (!StoryGuestService.clearActiveStoryGuest(context.getSource().getLevel(), storyGuestId)) {
            context.getSource().sendFailure(Component.literal("该故事旅客当前没有可清理的活跃占用"));
            return 0;
        }
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        "已清除 "
                                                + definition.displayName().zh()
                                                + " 的活跃实体占用"),
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

    private static int getFatigue(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        FatigueData data = target.getData(ModAttachments.PLAYER_FATIGUE);
        FatigueCalculator.FatigueStage stage = FatigueCalculator.getStage(data.getFatigue());
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        "玩家 "
                                                + target.getName().getString()
                                                + " 当前疲劳值: "
                                                + Math.round(data.getFatigue())
                                                + " / "
                                                + (int) FatigueData.MAX_FATIGUE
                                                + " (阶段 "
                                                + stage.level()
                                                + ")"),
                        false);
        return 1;
    }

    private static int setFatigue(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return updateFatigue(context, IntegerArgumentType.getInteger(context, "value"), FatigueUpdateMode.SET);
    }

    private static int addFatigue(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return updateFatigue(context, IntegerArgumentType.getInteger(context, "value"), FatigueUpdateMode.ADD);
    }

    private static int reduceFatigue(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        return updateFatigue(context, IntegerArgumentType.getInteger(context, "value"), FatigueUpdateMode.REDUCE);
    }

    private static int updateFatigue(
            CommandContext<CommandSourceStack> context, int value, FatigueUpdateMode mode)
            throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        FatigueData data = target.getData(ModAttachments.PLAYER_FATIGUE);
        double before = data.getFatigue();

        switch (mode) {
            case SET -> data.setFatigue(value);
            case ADD -> data.addFatigue(value);
            case REDUCE -> data.reduceFatigue(value);
        }

        double after = data.getFatigue();
        data.setLastNotifiedStage(FatigueCalculator.getStage(after).level());
        PlayerFatigueHandler.syncCurrentState(target);

        String actionText = switch (mode) {
            case SET -> "设置为";
            case ADD -> "增加了";
            case REDUCE -> "减少了";
        };
        context.getSource()
                .sendSuccess(
                        () ->
                                Component.literal(
                                        "已将玩家 "
                                                + target.getName().getString()
                                                + " 的疲劳值"
                                                + actionText
                                                + " "
                                                + Math.round(after)
                                                + " / "
                                                + (int) FatigueData.MAX_FATIGUE
                                                + " (原值 "
                                                + Math.round(before)
                                                + ")"),
                        true);
        return 1;
    }

    private enum FatigueUpdateMode {
        SET,
        ADD,
        REDUCE
    }
}
