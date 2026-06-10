package com.otherworldinn.item;

import com.otherworldinn.world.expedition.ChartComponentType;
import com.otherworldinn.world.expedition.ExpeditionDimensions;
import com.otherworldinn.world.expedition.ExpeditionService;
import com.otherworldinn.world.expedition.ExpeditionSession;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CExpeditionTimerPacket;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import com.otherworldinn.world.expedition.ExpeditionNbtHelper;

public class ExpeditionChartItem extends Item {

    private static final int BASE_TIME_MINUTES = 45;
    private static final int TIME_PER_SLOT = 5;
    private static final int MIN_TIME_MINUTES = 15;
    private static final int BASE_FEE = 10;
    private static final int FEE_PER_PERSON = 5;

    public ExpeditionChartItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResultHolder.fail(stack);

        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        player.getCooldowns().addCooldown(this, 2400);

        String state = getChartState(stack);
        if ("recruiting".equals(state)) {
            if (player.isShiftKeyDown()) {
                return activateExpedition(serverPlayer, stack);
            } else {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.otherworldinn.expedition.sneak_to_confirm")
                                .withStyle(ChatFormatting.YELLOW), true);
                return InteractionResultHolder.success(stack);
            }
        }

        if (!"idle".equals(state)) return InteractionResultHolder.fail(stack);

        TeamData team = TeamManager.getInstance().getPlayerTeam(serverPlayer);
        if (team == null) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.otherworldinn.expedition.no_team"), true);
            return InteractionResultHolder.fail(stack);
        }

        int onlinePlayerCount = serverPlayer.getServer().getPlayerList().getPlayerCount();
        if (onlinePlayerCount <= 1) {
            return startSolo(serverPlayer, stack, team);
        } else {
            return startRecruiting(serverPlayer, stack, team);
        }
    }

    private InteractionResultHolder<ItemStack> startSolo(ServerPlayer player,
            ItemStack stack, TeamData team) {
        int fee = calculateFee(stack, 1);
        if (team.getCoins() < fee) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.expedition.not_enough_coins", fee)
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        UUID chartUuid = UUID.randomUUID();
        Set<UUID> members = Set.of(player.getUUID());
        int timeMinutes = calculateTimeLimit(stack);
        long deadlineTick = player.getServer().getTickCount() + timeMinutes * 60L * 20L;

        ResourceKey<net.minecraft.world.level.Level> dimKey = ExpeditionDimensions.createChartKey(chartUuid);
        ExpeditionSession session = new ExpeditionSession(dimKey, members, getComponentIds(stack), deadlineTick);

        if (ExpeditionService.getActiveSession() != null) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.expedition.already_active")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel expeditionLevel = ExpeditionService.ensureExpeditionLevel(
                player.getServer(), dimKey, getComponentIds(stack), chartUuid.getLeastSignificantBits(),
                getChartDimension(stack), session);
        if (expeditionLevel == null) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.expedition.create_failed")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        team.removeCoins(fee, player.getServer());
        TeamManager.getInstance().syncTeam(team, player.getServer());
        session.markDimensionCreated();
        if (!ExpeditionService.registerSession(session)) {
            team.addCoins(fee, player.getServer());
            TeamManager.getInstance().syncTeam(team, player.getServer());
            ExpeditionService.restoreTemplateDimension(player.getServer());
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.expedition.create_failed")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        ExpeditionService.registerLevel(player.getServer(), dimKey, expeditionLevel, session);

        for (UUID id : members) {
            ServerPlayer mp = player.getServer().getPlayerList().getPlayer(id);
            if (mp != null) {
                teleportToSafeSurface(mp, expeditionLevel, session);
                long remaining = session.deadlineTick() - player.getServer().getTickCount();
                ModMessages.sendToPlayer(
                        new S2CExpeditionTimerPacket(remaining), mp);
            }
        }

        player.displayClientMessage(
                Component.translatable("message.otherworldinn.expedition.solo_started")
                        .withStyle(ChatFormatting.GREEN), false);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        
        player.getCooldowns().removeCooldown(this);
        return InteractionResultHolder.success(stack);
    }

    private InteractionResultHolder<ItemStack> startRecruiting(ServerPlayer player,
            ItemStack stack, TeamData team) {
        int estimatedFee = calculateFee(stack, 1);

        UUID chartUuid = UUID.randomUUID();
        setChartUuid(stack, chartUuid);
        setChartState(stack, "recruiting");
        setLeaderUuid(stack, player.getUUID());

        ListTag memberList = new ListTag();
        memberList.add(StringTag.valueOf(player.getUUID().toString()));
        ExpeditionNbtHelper.updateTag(stack, tag -> tag.put("members", memberList));

        MutableComponent msg = Component.translatable("message.otherworldinn.expedition.recruit_broadcast",
                        player.getName().copy().withStyle(ChatFormatting.YELLOW),
                        buildComponentSummary(stack),
                        Component.literal(String.valueOf(estimatedFee)).withStyle(ChatFormatting.GREEN),
                        Component.translatable("message.otherworldinn.expedition.click_to_join")
                                .withStyle(style -> style
                                        .withColor(ChatFormatting.GREEN)
                                        .withUnderlined(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                "/expedition join " + chartUuid))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                Component.translatable("message.otherworldinn.expedition.click_to_join_hover")))))
                .withStyle(ChatFormatting.GOLD);

        player.getServer().getPlayerList().broadcastSystemMessage(msg, false);
        player.getCooldowns().removeCooldown(this);
        return InteractionResultHolder.success(stack);
    }

    private MutableComponent buildComponentSummary(ItemStack stack) {
        CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
        ListTag compList = tag.getList("components", Tag.TAG_COMPOUND);
        MutableComponent summary = Component.literal("");
        boolean first = true;
        for (Tag t : compList) {
            if (!first) summary.append(Component.literal(" "));
            first = false;
            CompoundTag ct = (CompoundTag) t;
            String typeId = ct.getString("type");
            ChartComponentType ctype = ChartComponentType.byId(typeId);
            if (ctype != null) {
                summary.append(Component.literal(ctype.displayName(false) + "×1")
                        .withStyle(ctype.rarityColor()));
            }
        }
        return summary;
    }

    private InteractionResultHolder<ItemStack> activateExpedition(ServerPlayer player,
            ItemStack stack) {
        UUID leader = getLeaderUuid(stack);
        if (leader == null || !leader.equals(player.getUUID())) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.expedition.not_leader")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) return InteractionResultHolder.fail(stack);

        List<UUID> members = getMembers(stack);
        Set<UUID> onlineMembers = new HashSet<>();
        for (UUID id : members) {
            ServerPlayer mp = player.getServer().getPlayerList().getPlayer(id);
            if (mp != null) onlineMembers.add(id);
        }

        int fee = calculateFee(stack, onlineMembers.size());
        if (team.getCoins() < fee) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.expedition.not_enough_coins", fee)
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        UUID chartUuid = getChartUuid(stack);
        int timeMinutes = calculateTimeLimit(stack);
        long deadlineTick = player.getServer().getTickCount() + timeMinutes * 60L * 20L;

        ResourceKey<net.minecraft.world.level.Level> dimKey = ExpeditionDimensions.createChartKey(chartUuid);
        ExpeditionSession session = new ExpeditionSession(dimKey, onlineMembers, getComponentIds(stack), deadlineTick);

        if (ExpeditionService.getActiveSession() != null) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.expedition.already_active")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel expeditionLevel = ExpeditionService.ensureExpeditionLevel(
                player.getServer(), dimKey, getComponentIds(stack), chartUuid.getLeastSignificantBits(),
                getChartDimension(stack), session);
        if (expeditionLevel == null) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.expedition.create_failed")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }

        team.removeCoins(fee, player.getServer());
        TeamManager.getInstance().syncTeam(team, player.getServer());
        session.markDimensionCreated();
        if (!ExpeditionService.registerSession(session)) {
            team.addCoins(fee, player.getServer());
            TeamManager.getInstance().syncTeam(team, player.getServer());
            ExpeditionService.restoreTemplateDimension(player.getServer());
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.expedition.create_failed")
                            .withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        ExpeditionService.registerLevel(player.getServer(), dimKey, expeditionLevel, session);

        for (UUID id : onlineMembers) {
            ServerPlayer mp = player.getServer().getPlayerList().getPlayer(id);
            if (mp != null) {
                teleportToSafeSurface(mp, expeditionLevel, session);
                long remaining = session.deadlineTick() - player.getServer().getTickCount();
                ModMessages.sendToPlayer(
                        new S2CExpeditionTimerPacket(remaining), mp);
            }
        }

        player.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.otherworldinn.expedition.started",
                        onlineMembers.size()).withStyle(ChatFormatting.GREEN), false);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        
        player.getCooldowns().removeCooldown(this);
        return InteractionResultHolder.success(stack);
    }

    public static void onLeftClickCancel(ServerPlayer player, ItemStack stack) {
        if (!"recruiting".equals(getChartState(stack))) return;
        UUID leader = getLeaderUuid(stack);
        if (leader == null || !leader.equals(player.getUUID())) return;

        setChartState(stack, "idle");
        ExpeditionNbtHelper.updateTag(stack, tag -> {
            tag.remove("leader_uuid");
            tag.remove("members");
            tag.remove("chart_uuid");
        });

        player.displayClientMessage(
                Component.translatable("message.otherworldinn.expedition.cancelled")
                        .withStyle(ChatFormatting.YELLOW), false);
    }

    private int calculateFee(ItemStack stack, int playerCount) {
        List<String> componentIds = getComponentIds(stack);
        if (componentIds.isEmpty()) return 0;

        int total = BASE_FEE;
        for (String id : componentIds) {
            ChartComponentType ct = ChartComponentType.byId(id);
            if (ct != null) total += ct.fee();
        }
        total += (playerCount - 1) * FEE_PER_PERSON;
        return total;
    }

    private int calculateTimeLimit(ItemStack stack) {
        List<String> componentIds = getComponentIds(stack);
        int slots = componentIds.size();
        return Math.max(MIN_TIME_MINUTES, BASE_TIME_MINUTES - slots * TIME_PER_SLOT);
    }

    private static void teleportToSafeSurface(ServerPlayer player, ServerLevel level, ExpeditionSession session) {
        final int centerX = session.getCenterX();
        final int centerZ = session.getCenterZ();

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                centerX, centerZ);
                
        if (level.dimension() == com.otherworldinn.world.dimension.TownDimensions.EXPEDITION_TEMPLATE_NETHER) {
            surfaceY = 90;
            for (int y = 90; y > 32; y--) {
                BlockPos check = new BlockPos(centerX, y, centerZ);
                if (level.getBlockState(check).isAir() && level.getBlockState(check.below()).isSolid() && !level.getBlockState(check.below()).is(Blocks.BEDROCK)) {
                    surfaceY = y;
                    break;
                }
            }
        } else {
            if (surfaceY <= level.getMinBuildHeight()) {
                surfaceY = 64;
            }

            for (int y = surfaceY; y >= surfaceY - 5 && y > level.getMinBuildHeight(); y--) {
                BlockPos check = new BlockPos(centerX, y, centerZ);
                if (level.getBlockState(check).isSolid()) {
                    surfaceY = y;
                    break;
                }
            }
        }

        BlockPos basePos = new BlockPos(centerX, surfaceY + 1, centerZ);
        buildGlassChamber(level, basePos);

        double x = centerX + 0.5;
        double z = centerZ + 0.5;
        double y = surfaceY + 2.0;
        player.teleportTo(level, x, y, z, player.getYRot(), player.getXRot());
    }

    private static void buildGlassChamber(ServerLevel level, BlockPos base) {
        BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy <= 4; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    boolean isSurface = dx == -2 || dx == 2
                            || dy == 0 || dy == 4
                            || dz == -2 || dz == 2;
                    BlockPos p = base.offset(dx, dy, dz);
                    level.setBlock(p, isSurface ? glass : air, 3);
                }
            }
        }
    }

    public static List<String> getComponentIds(ItemStack stack) {
        CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
        ListTag compList = tag.getList("components", Tag.TAG_COMPOUND);
        List<String> ids = new ArrayList<>();
        for (Tag t : compList) {
            CompoundTag ct = (CompoundTag) t;
            ids.add(ct.getString("type"));
        }
        return ids;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
            List<Component> lines, TooltipFlag flag) {
        CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
        String state = getChartState(stack);
        int maxSlots = tag.getInt("max_slots");
        if (maxSlots == 0) maxSlots = 1;

        List<String> componentIds = getComponentIds(stack);
        int timeMinutes = calculateTimeLimit(stack);

        if ("recruiting".equals(state)) {
            lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.recruiting")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
            lines.add(Component.empty());

            UUID leader = getLeaderUuid(stack);
            if (leader != null) {
                lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.leader",
                        leader.toString().substring(0, 8)).withStyle(ChatFormatting.WHITE));
            }

            List<UUID> members = getMembers(stack);
            lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.joined_count",
                    members.size(), maxSlots).withStyle(ChatFormatting.GRAY));
            for (UUID id : members) {
                lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.member_entry",
                        id.toString().substring(0, 8)).withStyle(ChatFormatting.GRAY));
            }
            for (int i = members.size(); i < maxSlots; i++) {
                lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.waiting_slot")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            if (componentIds.isEmpty()) {
                lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.no_components")
                        .withStyle(ChatFormatting.GRAY));
                lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.available_slots", maxSlots)
                        .withStyle(ChatFormatting.GRAY));
            } else {
                lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.attached_components",
                        componentIds.size(), maxSlots).withStyle(ChatFormatting.GRAY));
                for (String id : componentIds) {
                    ChartComponentType ct = ChartComponentType.byId(id);
                    if (ct != null) {
                        lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.component_entry",
                                ct.displayComponent(true)).withStyle(ChatFormatting.GRAY));
                    }
                }
                for (int i = componentIds.size(); i < maxSlots; i++) {
                    lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.empty_slot")
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            lines.add(Component.empty());
            addDimensionTooltip(stack, lines);
            lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.time_limit", timeMinutes)
                    .withStyle(ChatFormatting.YELLOW));
            lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.max_players", maxSlots)
                    .withStyle(ChatFormatting.GRAY));
            int fee = calculateFee(stack, 1);
            lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.fee", fee)
                    .withStyle(ChatFormatting.GOLD));
        }

        if ("recruiting".equals(state)) {
            int fee = calculateFee(stack, getMembers(stack).size());
            lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.time_limit", timeMinutes)
                    .withStyle(ChatFormatting.YELLOW));
            lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.fee_with_count",
                    fee, getMembers(stack).size()).withStyle(ChatFormatting.GOLD));
        }
    }

    public static ChartComponentType.DimensionCategory getChartDimension(ItemStack stack) {
        CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
        String dimName = tag.getString("dimension_category");
        if (dimName.isEmpty()) return ChartComponentType.DimensionCategory.MAIN_WORLD;
        return ChartComponentType.fromNbtName(dimName);
    }

    public static void setChartDimension(ItemStack stack,
            ChartComponentType.DimensionCategory category) {
        ExpeditionNbtHelper.updateTag(stack,
                tag -> tag.putString("dimension_category", category.name()));
    }

    public static String getDimensionDisplayName(
            ChartComponentType.DimensionCategory category) {
        return switch (category) {
            case NETHER -> "下界";
            default -> "主世界";
        };
    }

    public void addDimensionTooltip(ItemStack stack, List<Component> lines) {
        ChartComponentType.DimensionCategory dim = getChartDimension(stack);
        ChatFormatting dimColor = switch (dim) {
            case NETHER -> ChatFormatting.RED;
            default -> ChatFormatting.GREEN;
        };
        lines.add(Component.translatable("tooltip.otherworldinn.expedition_chart.dimension",
                Component.literal(getDimensionDisplayName(dim)).withStyle(dimColor))
                .withStyle(ChatFormatting.GRAY));
    }

    public static void giveRecallScroll(ServerPlayer player) {
        ItemStack scroll = new ItemStack(ModItems.RECALL_SCROLL.get());
        if (!player.getInventory().add(scroll)) {
            player.drop(scroll, false);
        }
    }

    public static String getChartState(ItemStack stack) {
        CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
        return tag.contains("chart_state") ? tag.getString("chart_state") : "idle";
    }

    public static void setChartState(ItemStack stack, String state) {
        ExpeditionNbtHelper.updateTag(stack, tag -> tag.putString("chart_state", state));
    }

    public static UUID getChartUuid(ItemStack stack) {
        CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
        return tag.contains("chart_uuid") ? tag.getUUID("chart_uuid") : null;
    }

    public static void setChartUuid(ItemStack stack, UUID uuid) {
        ExpeditionNbtHelper.updateTag(stack, tag -> tag.putUUID("chart_uuid", uuid));
    }

    public static UUID getLeaderUuid(ItemStack stack) {
        CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
        return tag.contains("leader_uuid") ? tag.getUUID("leader_uuid") : null;
    }

    public static void setLeaderUuid(ItemStack stack, UUID uuid) {
        ExpeditionNbtHelper.updateTag(stack, tag -> tag.putUUID("leader_uuid", uuid));
    }

    public static List<UUID> getMembers(ItemStack stack) {
        CompoundTag tag = ExpeditionNbtHelper.readTag(stack);
        ListTag list = tag.getList("members", Tag.TAG_STRING);
        List<UUID> members = new ArrayList<>();
        for (Tag t : list) {
            try {
                members.add(UUID.fromString(t.getAsString()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return members;
    }
}
