package com.otherworldinn.world.inn.listener;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.item.RoomKeyItem;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.inn.GuestData;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.RoomData;
import com.otherworldinn.world.inn.RoomData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.otherworldinn.world.team.TeamSavedData;
import com.simibubi.create.AllBlocks;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * 旅社事件处理器
 *
 * <p>处理与旅社运营相关的事件，例如方块更新触发的房间检查。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public class InnEventHandler {

    // 记录本 tick 需要检查的队伍及其变更位置
    private static final Map<UUID, Set<BlockPos>> pendingChecks = new HashMap<>();
    private static final String MESSY_BED_ITEM_KEY = "MessyBed";

    /**
     * 监听方块更新事件 (NeighborNotifyEvent)
     *
     * <p>当方块发生更新（放置、破坏、状态改变）时触发。 如果更新发生在旅社区域内，则标记该队伍在 tick 结束时进行房间检查。
     */
    @SubscribeEvent
    public static void onBlockUpdate(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != TownDimensions.TOWN_LEVEL) return;

        BlockPos pos = event.getPos();
        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());

        if (team != null) {
            synchronized (pendingChecks) {
                pendingChecks.computeIfAbsent(team.getTeamId(), k -> new HashSet<>()).add(pos);
            }
        }
    }

    /**
     * 监听方块放置事件
     *
     * <p>NeighborNotifyEvent 可能不覆盖所有情况（如直接放置），补充监听 PlaceEvent。 同时也负责检测剪贴板的放置，同步缓存的待办事项。
     */
    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        applyMessyBedOnPlace(event);
        if (level.dimension() != TownDimensions.TOWN_LEVEL) return;

        BlockPos pos = event.getPos();
        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());

        if (team != null) {
            synchronized (pendingChecks) {
                pendingChecks.computeIfAbsent(team.getTeamId(), k -> new HashSet<>()).add(pos);
            }

            // 检查是否放置了剪贴板
            BlockState state = event.getState();
            if (AllBlocks.CLIPBOARD.has(state)) {
                // 如果是剪贴板，尝试同步缓存的待办事项
                // 扫描范围只需包含该方块即可，syncTodosToClipboard 会调用 modifyClipboards，后者会检查 BlockEntity
                AABB area = new AABB(pos);
                team.getInnData().syncTodosToClipboard(level, area);
            }
        }
    }

    /** 监听方块破坏事件 */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (preserveMessyBedDrop(event, level)) {
            return;
        }
        if (level.dimension() != TownDimensions.TOWN_LEVEL) return;

        BlockPos pos = event.getPos();
        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());

        if (team != null) {
            synchronized (pendingChecks) {
                pendingChecks.computeIfAbsent(team.getTeamId(), k -> new HashSet<>()).add(pos);
            }
        }
    }

    private static boolean preserveMessyBedDrop(BlockEvent.BreakEvent event, ServerLevel level) {
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!isMessyBedState(state)) {
            return false;
        }
        if (event.getPlayer() != null && event.getPlayer().isCreative()) {
            return false;
        }
        event.setCanceled(true);
        BlockPos headPos =
                state.getValue(BedBlock.PART) == BedPart.HEAD
                        ? pos
                        : pos.relative(state.getValue(BedBlock.FACING));
        BlockPos footPos =
                state.getValue(BedBlock.PART) == BedPart.FOOT
                        ? pos
                        : pos.relative(state.getValue(BedBlock.FACING).getOpposite());
        BlockState headState = level.getBlockState(headPos);
        BlockState footState = level.getBlockState(footPos);
        if (isMessyBedState(headState)) {
            level.setBlock(
                    headPos,
                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                    Block.UPDATE_ALL);
        }
        if (isMessyBedState(footState)) {
            level.setBlock(
                    footPos,
                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                    Block.UPDATE_ALL);
        }
        ItemStack drop = createMessyBedStack(state);
        Block.popResource(level, headPos, drop);
        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());
        if (team != null) {
            synchronized (pendingChecks) {
                pendingChecks.computeIfAbsent(team.getTeamId(), k -> new HashSet<>()).add(pos);
            }
        }
        return true;
    }

    private static void applyMessyBedOnPlace(BlockEvent.EntityPlaceEvent event) {
        BlockState state = event.getState();
        if (!(state.getBlock() instanceof BedBlock)
                || !state.hasProperty(com.otherworldinn.foundation.ModBlockProperties.MESSY)) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        ItemStack source =
                isMessyBedItem(main) ? main : (isMessyBedItem(off) ? off : ItemStack.EMPTY);
        if (source.isEmpty()) {
            return;
        }
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }
        BlockPos pos = event.getPos();
        BlockPos otherPos =
                state.getValue(BedBlock.PART) == BedPart.HEAD
                        ? pos.relative(state.getValue(BedBlock.FACING).getOpposite())
                        : pos.relative(state.getValue(BedBlock.FACING));
        BlockState current = level.getBlockState(pos);
        BlockState other = level.getBlockState(otherPos);
        if (current.hasProperty(com.otherworldinn.foundation.ModBlockProperties.MESSY)) {
            level.setBlock(
                    pos,
                    current.setValue(com.otherworldinn.foundation.ModBlockProperties.MESSY, true),
                    Block.UPDATE_ALL);
        }
        if (other.getBlock() instanceof BedBlock
                && other.hasProperty(com.otherworldinn.foundation.ModBlockProperties.MESSY)) {
            level.setBlock(
                    otherPos,
                    other.setValue(com.otherworldinn.foundation.ModBlockProperties.MESSY, true),
                    Block.UPDATE_ALL);
        }
    }

    private static boolean isMessyBedState(BlockState state) {
        return state.getBlock() instanceof BedBlock
                && state.hasProperty(com.otherworldinn.foundation.ModBlockProperties.MESSY)
                && state.getValue(com.otherworldinn.foundation.ModBlockProperties.MESSY);
    }

    private static ItemStack createMessyBedStack(BlockState state) {
        ItemStack stack = new ItemStack(state.getBlock().asItem());
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(MESSY_BED_ITEM_KEY, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    private static boolean isMessyBedItem(ItemStack stack) {
        if (stack.isEmpty() || !(Block.byItem(stack.getItem()) instanceof BedBlock)) {
            return false;
        }
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getBoolean(MESSY_BED_ITEM_KEY);
    }

    /**
     * 在 Level Tick 结束时处理待定检查
     *
     * <p>根据记录的变更位置，找出受影响的房间并逐一进行合法性判定与属性更新。
     * 确保每个 tick 每个队伍最多只执行检查一次。
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != TownDimensions.TOWN_LEVEL) return;

        TeamManager teamManager = TeamManager.getInstance();

        TeamSavedData data = teamManager.getData(level.getServer());
        if (data != null) {
            for (TeamData team : data.getTeams().values()) {
                team.getInnData().tick(level, team);
            }
        }

        Map<UUID, Set<BlockPos>> teamsToCheck;
        synchronized (pendingChecks) {
            if (pendingChecks.isEmpty()) return;
            teamsToCheck = new HashMap<>(pendingChecks);
            pendingChecks.clear();
        }

        for (Map.Entry<UUID, Set<BlockPos>> entry : teamsToCheck.entrySet()) {
            UUID teamId = entry.getKey();
            Set<BlockPos> positions = entry.getValue();
            TeamData team = teamManager.getTeam(teamId, level.getServer());
            if (team != null) {
                InnData innData = team.getInnData();
                Set<Integer> affectedRoomIds = innData.findAffectedRoomIds(positions);

                if (affectedRoomIds.isEmpty()) {
                    continue;
                }

                for (int roomId : affectedRoomIds) {
                    innData.checkAndUpdateRoom(roomId, level, team);
                }

                innData.refreshClipboardTodos(level, team);
                teamManager.syncTeam(team, level.getServer());
            }
        }
    }

    /**
     * 监听装备变更事件 (服务器端)
     *
     * <p>当房间登记册进入副手时，播放翻页音效。 这通常发生在玩家将物品从主手切换到副手时。
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            // 避免登录时触发
            if (player.tickCount < 10) return;

            // 仅关注副手变化
            if (event.getSlot() == EquipmentSlot.OFFHAND) {
                ItemStack to = event.getTo();
                ItemStack from = event.getFrom();

                // 检查是否切换到了房间登记册，且之前不是房间登记册
                if (to.is(ModItems.ROOM_REGISTER.get()) && !from.is(ModItems.ROOM_REGISTER.get())) {
                    // 使用 null 作为 player 参数，确保包括触发者在内的所有附近玩家都能听到声音
                    player.level()
                            .playSound(
                                    null,
                                    player.blockPosition(),
                                    SoundEvents.BOOK_PAGE_TURN,
                                    SoundSource.PLAYERS,
                                    1.0F,
                                    1.0F);
                }
            }
        }
    }

    /**
     * 处理玩家左键点击方块事件 (服务器端)
     *
     * <p>1. 地契：清除选定范围 2. 房间登记册：删除房间（并阻止方块破坏）
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos relativePos = event.getPos().relative(event.getFace());

        // 1. 处理地契逻辑 (主手)
        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHandItem.is(ModItems.LAND_DEED.get())) {
            CustomData customData =
                    mainHandItem.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            if (tag.contains("Pos1")) {
                if (!level.isClientSide) {
                    tag.remove("Pos1");
                    tag.remove("Pos2");
                    mainHandItem.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    player.displayClientMessage(
                            Component.translatable(
                                            "message.otherworldinn.land_deed.selection_cleared")
                                    .withStyle(style -> style.withColor(ModColors.INFO)),
                            true);
                }
                event.setCanceled(true); // 取消方块破坏
                return;
            }
        }

        // 2. 处理房间钥匙逻辑 (主手)
        if (mainHandItem.is(ModItems.ROOM_KEY.get())) {
            Optional<Integer> roomId = RoomKeyItem.getBoundRoomId(mainHandItem);
            if (roomId.isPresent()) {
                if (!level.isClientSide) {
                    RoomKeyItem.unbindRoom(mainHandItem);
                    player.displayClientMessage(
                            Component.translatable("message.otherworldinn.room_key.unbound")
                                    .withStyle(style -> style.withColor(ModColors.INFO)),
                            true);
                }
                event.setCanceled(true); // 取消方块破坏
                return;
            }
        }

        // 3. 处理房间登记册逻辑 (副手)
        ItemStack offhandItem = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhandItem.is(ModItems.ROOM_REGISTER.get())) {
            // 只要副手持有房间登记册，就取消方块破坏，尝试执行删除房间逻辑
            event.setCanceled(true);
            handleLeftClick(player, relativePos, level);
        }
    }

    /**
     * 处理玩家右键点击方块事件 (服务器端)
     *
     * <p>1. 铃铛：手持工具切换装修模式
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) return;

        if (level.getBlockEntity(pos)
                instanceof com.simibubi.create.content.redstone.deskBell.DeskBellBlockEntity) {
            ItemStack heldItem = player.getItemInHand(event.getHand());

            if (heldItem.is(ModItems.INN_KEY.get())) {
                if (level instanceof ServerLevel serverLevel
                        && level.dimension() == TownDimensions.TOWN_LEVEL) {
                    TeamData team =
                            TeamManager.getInstance().getTeamAt(pos, serverLevel.getServer());

                    if (team != null) {
                        if (!team.hasMember(player.getUUID())) {
                            player.displayClientMessage(
                                    Component.translatable(
                                                    "message.otherworldinn.inn_key.no_permission")
                                            .withStyle(style -> style.withColor(ModColors.ERROR)),
                                    true);
                            event.setCanceled(true);
                            return;
                        }

                        InnData innData = team.getInnData();
                        InnData.InnState currentState = innData.getState();
                        InnData.InnState newState;
                        SoundEvent sound;
                        MutableComponent message;
                        int color;

                        if (currentState == InnData.InnState.OPEN) {
                            sound = SoundEvents.WOODEN_DOOR_OPEN;
                            newState = InnData.InnState.CLOSED;
                            message =
                                    Component.translatable("message.otherworldinn.inn_key.closed");
                            color = ModColors.RED;
                        } else {
                            sound = SoundEvents.WOODEN_DOOR_CLOSE;
                            newState = InnData.InnState.OPEN;
                            message = Component.translatable("message.otherworldinn.inn_key.open");
                            color = ModColors.GREEN;
                        }

                        innData.setState(newState);
                        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                        TeamManager.getInstance().syncTeam(team, serverLevel.getServer());
                        if (newState == InnData.InnState.OPEN) {
                            innData.setInitialChartsGiven(true);
                        }
                        player.swing(event.getHand(), true);
                        player.displayClientMessage(
                                message.copy().withStyle(style -> style.withColor(color)),
                                true);
                    }
                }
            }

            if (heldItem.is(Items.BOOK) || heldItem.is(Items.WRITABLE_BOOK) || heldItem.is(Items.WRITTEN_BOOK)) {
                if (level instanceof ServerLevel serverLevel
                        && level.dimension() == TownDimensions.TOWN_LEVEL) {
                    TeamData team =
                            TeamManager.getInstance().getTeamAt(pos, serverLevel.getServer());
                    if (team != null && team.hasMember(player.getUUID())) {
                        ItemStack roster = createGuestRoster(team, serverLevel);
                        if (roster != null) {
                            heldItem.shrink(1);
                            if (!player.getInventory().add(roster)) {
                                player.spawnAtLocation(roster);
                            }
                            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8F, 1.0F);
                            player.swing(event.getHand(), true);
                            player.displayClientMessage(
                                    Component.translatable("message.otherworldinn.guest_roster.created")
                                            .withStyle(style -> style.withColor(ModColors.SUCCESS)),
                                    true);
                        }
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @Nullable
    private static ItemStack createGuestRoster(TeamData team, ServerLevel level) {
        InnData innData = team.getInnData();
        List<RoomData> rooms = innData.getRooms().values().stream()
                .sorted(java.util.Comparator.comparingInt(RoomData::getId))
                .toList();

        if (rooms.isEmpty()) {
            return null;
        }

        List<MutableComponent> pageComponents = new java.util.ArrayList<>();
        MutableComponent currentPage = Component.empty();
        int lineCount = 0;
        final int MAX_LINES = 13;

        for (RoomData room : rooms) {
            MutableComponent header = Component.literal(" -   " + room.getId() + "号房 ")
                    .withStyle(ChatFormatting.GRAY);

            Set<UUID> guestUuids = room.getCurrentGuests();
            if (guestUuids.isEmpty()) {
                header.append(Component.literal("空闲")
                        .withStyle(ChatFormatting.GRAY));
                if (lineCount + 2 > MAX_LINES) {
                    pageComponents.add(currentPage);
                    currentPage = Component.empty();
                    lineCount = 0;
                }
                currentPage.append(header).append("\n\n");
                lineCount += 2;
                continue;
            }

            int needed = 1 + 1 + guestUuids.size() + 1;
            if (lineCount + needed > MAX_LINES && lineCount > 0) {
                pageComponents.add(currentPage);
                currentPage = Component.empty();
                lineCount = 0;
            }
            currentPage.append(header).append("\n");
            lineCount++;

            MutableComponent guestsLabel = Component.literal(" 已入住旅客：")
                    .withStyle(ChatFormatting.GRAY);
            currentPage.append(guestsLabel).append("\n");
            lineCount++;

            for (UUID uuid : guestUuids) {
                String guestName = getGuestName(team, uuid, level);
                MutableComponent nameLine = Component.literal("  - " + guestName);
                currentPage.append(nameLine).append("\n");
                lineCount++;
            }
            currentPage.append("\n");
            lineCount++;
        }

        pageComponents.add(currentPage);

        List<Filterable<Component>> rawPages = new java.util.ArrayList<>();
        for (MutableComponent comp : pageComponents) {
            rawPages.add(Filterable.passThrough(comp));
        }

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.set(DataComponents.WRITTEN_BOOK_CONTENT,
                new WrittenBookContent(
                        Filterable.passThrough("旅客名册"),
                        team.getName(),
                        0,
                        rawPages,
                        true));
        return book;
    }

    private static String getGuestName(TeamData team, UUID uuid, ServerLevel level) {
        Entity entity = level.getEntity(uuid);
        if (entity != null && entity.getCustomName() != null) {
            return entity.getCustomName().getString();
        }
        if (entity != null) {
            return entity.getName().getString();
        }
        return uuid.toString().substring(0, 8);
    }

    /**
     * 处理左键点击方块的公共逻辑
     *
     * <p>检查玩家副手是否持有房间登记册。 如果条件满足，则删除点击位置所在的房间。
     *
     * @param player 玩家实体
     * @param pos 点击的方块坐标
     * @param level 世界实例
     */
    private static void handleLeftClick(Player player, BlockPos pos, Level level) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ItemStack offhandItem = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!offhandItem.is(ModItems.ROOM_REGISTER.get())) {
            return;
        }

        if (level.dimension() != TownDimensions.TOWN_LEVEL) return;

        TeamData team = TeamManager.getInstance().getPlayerTeam(serverPlayer);
        if (team != null) {
            InnData innData = team.getInnData();

            RoomData room = innData.getRoomAt(pos);

            if (room != null) {
                innData.removeRoom(
                        room.getId(),
                        level,
                        team,
                        Component.translatable(
                                "message.otherworldinn.room_register.manual_removal"));

                TeamManager.getInstance().syncTeam(team, serverPlayer.getServer());
            }
        }
    }
}
