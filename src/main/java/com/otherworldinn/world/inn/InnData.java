package com.otherworldinn.world.inn;

import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.entity.base.VipGuestEntity;
import com.otherworldinn.entity.guest.StoryGuestEntity;
import com.otherworldinn.foundation.ModBlockProperties;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.util.EntityUtils;
import com.otherworldinn.world.inn.decoration.InnDecorationBuff;
import com.otherworldinn.world.inn.decoration.InnDecorationBuffType;
import com.otherworldinn.world.inn.decoration.InnDecorationDefinition;
import com.otherworldinn.world.inn.decoration.InnDecorationRegistry;
import com.otherworldinn.world.inn.service.ClipboardManager;
import com.otherworldinn.world.inn.service.FurnitureManager;
import com.otherworldinn.world.inn.service.RoomThemeManager;
import com.otherworldinn.world.storyguest.StoryGuestService;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.otherworldinn.world.team.TeamSavedData;
import java.util.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

/**
 * 旅社数据
 *
 * <p>存储旅社的运营状态、旅客列表等信息。
 */
@Data
public class InnData {
    private String name = "My Inn";

    @Setter(AccessLevel.NONE)
    private int rating = 0; // 旅社评级 (0-5)

    @Setter(AccessLevel.NONE)
    private int reputation = 0; // 旅社声望

    @Setter(AccessLevel.NONE)
    private int totalLodgingIncome = 0;
    @Setter(AccessLevel.NONE)
    private int totalCheckInCount = 0;

    @Setter(AccessLevel.NONE)
    private int totalDiningIncome = 0;

    @Setter(AccessLevel.NONE)
    private int totalOtherIncome = 0;

    @Setter(AccessLevel.NONE)
    private int todayLodgingIncome = 0;

    @Setter(AccessLevel.NONE)
    private int todayDiningIncome = 0;

    @Setter(AccessLevel.NONE)
    private int todayOtherIncome = 0;

    @Setter(AccessLevel.NONE)
    private int yesterdayLodgingIncome = 0;

    @Setter(AccessLevel.NONE)
    private int yesterdayDiningIncome = 0;

    @Setter(AccessLevel.NONE)
    private int yesterdayOtherIncome = 0;

    @Setter(AccessLevel.NONE)
    private long incomeStatDay = -1L;

    @Setter(AccessLevel.NONE)
    private boolean initialChartsGiven = false;

    @Setter(AccessLevel.NONE)
    private int lastChartDistributionWeek = -1;

    @Setter(AccessLevel.NONE)
    private InnState state = InnState.CLOSED; // 默认为歇业

    public enum InnState {
        CLOSED,
        OPEN
    }

    private final Set<UUID> guestIds = new HashSet<>();
    private final Map<Integer, RoomData> rooms = new HashMap<>();
    private final Map<String, Integer> facilityLevels = new HashMap<>();
    private final Map<String, List<DecorationPlacement>> activeDecorations = new HashMap<>();

    // 待办事项缓存列表
    private final List<String> todoList = new ArrayList<>();

    // 下一次生成旅客的时间 (GameTime)
    private long nextGuestSpawnTime = 0;
    private static final int BASE_GUEST_WAITING_TIMEOUT = 6000;
    private static final int WAITING_PATIENCE_BONUS_PER_STAR = 1200;
    private static final int MAX_GUEST_WAITING_TIMEOUT = 12000;
    private static final int MIN_AVERAGE_SPAWN_DELAY_TICKS = 1200;
    private static final int MAX_AVERAGE_SPAWN_DELAY_TICKS = 4800;
    private static final double SPAWN_DELAY_JITTER_RATIO = 0.20D;
    private static final int MIN_SPAWN_DELAY_TICKS = 600;
    private static final int MAX_SPAWN_DELAY_TICKS = 7200;
    private static final float CLUTTER_SPAWN_CHANCE = 0.65F;
    private static final int MIN_CLUTTER_PER_CHECKOUT = 1;
    private static final int MAX_CLUTTER_PER_CHECKOUT = 3;
    private static final int[] REPUTATION_REQUIREMENTS_BY_RATING = {200, 450, 850, 1300, 1800, 2500};
    private static final int[] ROOM_REQUIREMENTS_BY_RATING = {2, 4, 6, 8, 10, 12};
    private static final int[] TOTAL_INCOME_REQUIREMENTS_BY_RATING = {200, 500, 2000, 4500, 9000, 20000};

    public InnData() {}

    public void setRating(int rating) {
        this.rating = Math.max(0, Math.min(5, rating));
    }

    public void setReputation(int reputation) {
        this.reputation = Math.max(0, reputation);
    }

    /**
     * 获取当前等级升级所需的最大声望值
     *
     * <p>类似于 Minecraft 的经验值系统。 公式：100 * (rating + 1)
     *
     * @param rating 当前星级
     * @return 升级所需声望
     */
    public int getMaxReputation(int rating) {
        int clamped = Math.max(0, Math.min(5, rating));
        return REPUTATION_REQUIREMENTS_BY_RATING[clamped];
    }

    public int getRequiredRoomCount(int rating) {
        int clamped = Math.max(0, Math.min(5, rating));
        return ROOM_REQUIREMENTS_BY_RATING[clamped];
    }

    public int getRequiredTotalIncome(int rating) {
        int clamped = Math.max(0, Math.min(5, rating));
        return TOTAL_INCOME_REQUIREMENTS_BY_RATING[clamped];
    }

    /**
     * 增加声望
     *
     * @param amount 增加的数值
     */
    public void addReputation(int amount) {
        if (amount > 0) {
            amount = applyPositiveBuff(amount, InnDecorationBuffType.REPUTATION_GAIN_MULTIPLIER);
        }
        this.reputation += amount;
        if (this.reputation < 0) {
            this.reputation = 0;
        }
    }

    public int scaleGuestReputationDelta(GuestEntity guest, int rawAmount) {
        if (rawAmount == 0) {
            return 0;
        }
        double guestMultiplier = guest == null ? 1.0D : Math.max(0.0D, guest.getReputationMultiplier());
        double starMultiplier = getRatingReputationMultiplier();
        int scaled = (int) Math.round(rawAmount * guestMultiplier * starMultiplier);
        if (scaled == 0) {
            if (guestMultiplier <= 0.0D) {
                return 0;
            }
            return rawAmount > 0 ? 1 : -1;
        }
        return scaled;
    }

    private double getRatingReputationMultiplier() {
        int clampedRating = Math.max(0, Math.min(5, this.rating));
        return 1.0D + clampedRating * (2.0D / 5.0D);
    }

    public int getTotalIncome() {
        return totalLodgingIncome + totalDiningIncome + totalOtherIncome;
    }

    public int getYesterdayIncome() {
        return yesterdayLodgingIncome + yesterdayDiningIncome + yesterdayOtherIncome;
    }

    public void recordLodgingIncome(int amount, ServerLevel level) {
        if (amount <= 0) {
            return;
        }
        amount = applyPositiveBuff(amount, InnDecorationBuffType.LODGING_INCOME_MULTIPLIER);
        syncIncomeStatDay(level);
        totalLodgingIncome += amount;
        todayLodgingIncome += amount;
    }

    public void recordDiningIncome(int amount, ServerLevel level) {
        if (amount <= 0) {
            return;
        }
        amount = applyPositiveBuff(amount, InnDecorationBuffType.DINING_INCOME_MULTIPLIER);
        syncIncomeStatDay(level);
        totalDiningIncome += amount;
        todayDiningIncome += amount;
    }

    public void recordOtherIncome(int amount, ServerLevel level) {
        if (amount <= 0) {
            return;
        }
        syncIncomeStatDay(level);
        totalOtherIncome += amount;
        todayOtherIncome += amount;
    }

    private void syncIncomeStatDay(ServerLevel level) {
        long currentDay = level.getDayTime() / 24000L;
        if (incomeStatDay < 0L) {
            incomeStatDay = currentDay;
            return;
        }
        if (currentDay <= incomeStatDay) {
            return;
        }
        if (currentDay == incomeStatDay + 1L) {
            yesterdayLodgingIncome = todayLodgingIncome;
            yesterdayDiningIncome = todayDiningIncome;
            yesterdayOtherIncome = todayOtherIncome;
        } else {
            yesterdayLodgingIncome = 0;
            yesterdayDiningIncome = 0;
            yesterdayOtherIncome = 0;
        }
        todayLodgingIncome = 0;
        todayDiningIncome = 0;
        todayOtherIncome = 0;
        incomeStatDay = currentDay;
    }

    public boolean checkLevelUp() {
        if (this.rating >= 5) {
            return false;
        }

        int clampedRating = Math.max(0, Math.min(5, this.rating));
        int requiredRoomCount = getRequiredRoomCount(clampedRating);
        int requiredTotalIncome = getRequiredTotalIncome(clampedRating);
        int requiredReputation = getMaxReputation(clampedRating);

        if (getRoomCount() < requiredRoomCount) {
            return false;
        }
        if (getTotalIncome() < requiredTotalIncome) {
            return false;
        }
        if (this.reputation < requiredReputation) {
            return false;
        }

        this.rating = Math.min(5, this.rating + 1);
        return true;
    }

    /**
     * 尝试开启装修模式
     *
     * @return 如果成功开启返回 true，否则返回 false (例如正在营业或有客人)
     */
    public boolean setState(InnState newState) {
        if (this.state == newState) {
            return true;
        }
        boolean opening = (this.state == InnState.CLOSED && newState == InnState.OPEN);
        this.state = newState;
        if (opening) {
            onInnOpened();
        }
        return true;
    }

    private void onInnOpened() {
        if (!initialChartsGiven) {
            initialChartsGiven = true;
        }
    }

    public void setInitialChartsGiven(boolean value) { this.initialChartsGiven = value; }
    public boolean isInitialChartsGiven() { return initialChartsGiven; }
    public int getLastChartDistributionWeek() { return lastChartDistributionWeek; }
    public void setLastChartDistributionWeek(int week) { this.lastChartDistributionWeek = week; }

    public void addGuest(UUID guestId) {
        this.guestIds.add(guestId);
    }

    /**
     * 添加旅客（进入旅社范围）
     *
     * @param guest 旅客实体
     * @param team 队伍数据
     * @param level 世界
     */
    public void addGuest(GuestEntity guest, TeamData team, ServerLevel level) {
        if (!guestIds.contains(guest.getUUID())) {
            addGuest(guest.getUUID());

            // 设置状态为等待
            GuestData data = guest.getGuestData();
            data.setWaiting(true, level.getDayTime());

            // 添加待办事项
            String guestName =
                    guest.getCustomName() != null ? guest.getCustomName().getString() : "Guest";
            String todoText =
                    Component.translatable("todo.otherworldinn.guest_waiting", guestName)
                            .getString();
            addTodo(level, team, todoText);
        }
    }

    public void removeGuest(UUID uuid) {
        guestIds.remove(uuid);
    }

    /** 清除所有旅客数据并移除待入住待办事项（调试用） */
    public void clearAllGuests(Level level, TeamData team) {
        for (String todo : List.copyOf(todoList)) {
            if (todo.contains("guest_waiting")) {
                removeTodo(level, team, todo);
            }
        }
        guestIds.clear();
    }

    /**
     * 获取旅客数据
     *
     * <p>通过 UUID 在服务器等级中查找实体并获取数据。
     *
     * @param uuid 旅客 UUID
     * @param level 服务器等级
     * @return 旅客数据，如果找不到实体则返回 null
     */
    public GuestData getGuestData(UUID uuid, ServerLevel level) {
        if (!guestIds.contains(uuid)) {
            return null;
        }
        Entity entity = level.getEntity(uuid);
        if (entity instanceof GuestEntity guestEntity) {
            return guestEntity.getGuestData();
        }
        return null;
    }

    public void addRoom(RoomData room) {
        this.rooms.put(room.getId(), room);
    }

    public void removeRoom(int roomId) {
        removeRoom(roomId, null, null, null);
    }

    public void removeRoom(int roomId, Level level, TeamData team) {
        removeRoom(roomId, level, team, null);
    }

    public void removeRoom(int roomId, Level level, TeamData team, Component reason) {
        RoomData removed = this.rooms.remove(roomId);

        // 通知队伍所有成员
        if (level != null && team != null && removed != null) {
            String roomDisplay = RoomData.getDisplayName(removed);
            team.getMembers()
                    .forEach(
                            uuid -> {
                                Player player = level.getPlayerByUUID(uuid);
                                if (player != null) {
                                    if (reason != null) {
                                        player.displayClientMessage(
                                                Component.translatable(
                                                                "message.otherworldinn.room_register.remove_success_with_reason",
                                                                roomDisplay,
                                                                reason)
                                                        .withStyle(
                                                                style ->
                                                                        style.withColor(
                                                                                ModColors.ERROR)),
                                                false);
                                    } else {
                                        player.displayClientMessage(
                                                Component.translatable(
                                                                "message.otherworldinn.room_register.remove_success",
                                                                roomDisplay)
                                                        .withStyle(
                                                                style ->
                                                                        style.withColor(
                                                                                ModColors.ERROR)),
                                                false);
                                    }
                                }
                            });
        }
    }

    public RoomData getRoom(int roomId) {
        return this.rooms.get(roomId);
    }

    public RoomData getRoomAt(BlockPos pos) {
        for (RoomData room : rooms.values()) {
            BlockPos min = room.getMinPos();
            BlockPos max = room.getMaxPos();
            if (pos.getX() >= min.getX()
                    && pos.getX() <= max.getX()
                    && pos.getY() >= min.getY()
                    && pos.getY() <= max.getY()
                    && pos.getZ() >= min.getZ()
                    && pos.getZ() <= max.getZ()) {
                return room;
            }
        }
        return null;
    }

    /**
     * 计算并更新房间属性
     *
     * <p>遍历房间内的所有方块，查找已注册的家具，累加其属性值。
     * 每种方块最多计入两个，防止大量放置同一方块刷属性。
     *
     * @param roomId 房间ID
     * @param level 服务器等级 (用于获取方块状态)
     */
    public void calculateRoomStats(int roomId, Level level) {
        RoomData room = rooms.get(roomId);
        if (room == null) {
            return;
        }

        final int[] stats = new int[3]; // [0]: comfort, [1]: light, [2]: humidity

        BlockPos min = room.getMinPos();
        BlockPos max = room.getMaxPos();

        java.util.Map<net.minecraft.world.level.block.Block, Integer> blockCounts = new java.util.HashMap<>();

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            net.minecraft.world.level.block.Block block = state.getBlock();
            int count = blockCounts.getOrDefault(block, 0);
            if (count >= 3) {
                continue;
            }
            blockCounts.put(block, count + 1);
            FurnitureManager.getStats(block)
                    .ifPresent(
                            s -> {
                                stats[0] += s.comfort();
                                stats[1] += s.light();
                                stats[2] += s.humidity();
                            });
        }

        String detected = null;
        if (level instanceof ServerLevel serverLevel) {
            detected = RoomThemeManager.detectTheme(serverLevel, min, max);
            room.setTheme(detected == null ? "" : detected);
        }

        if (detected != null && !detected.isEmpty()) {
            RoomThemeManager.StatModifiers mod = RoomThemeManager.getThemeModifiers(detected);
            stats[0] += mod.comfort();
            stats[1] += mod.light();
            stats[2] += mod.humidity();
        }

        room.setComfort(stats[0]);
        room.setLight(stats[1]);
        room.setHumidity(stats[2]);
    }

    /**
     * 更新所有房间的属性数据
     *
     * @param level 服务器等级
     */
    public void updateAllRoomsStats(Level level) {
        for (Integer roomId : rooms.keySet()) {
            calculateRoomStats(roomId, level);
            // 更新房间内所有旅客的偏好分数
            RoomData room = rooms.get(roomId);
            if (room != null && level instanceof ServerLevel serverLevel) {
                // 更新房间整洁度（虽然不用于平均计算，但可能用于其他逻辑）
                int[] bedStats =
                        RoomData.calculateBedStats(room.getMinPos(), room.getMaxPos(), level);
                room.setMaxGuests(bedStats[0]); // 有效床位
                room.setCleanliness(bedStats[1]); // 整洁度
                room.setTotalBeds(bedStats[2]); // 物理床位总数

                for (UUID guestId : room.getCurrentGuests()) {
                    GuestData guest = getGuestData(guestId, serverLevel);
                    if (guest != null) {
                        guest.updatePreferenceScore(room);
                    }
                }
            }
        }
    }

    /**
     * 获取总房间数量
     *
     * @return 房间总数
     */
    public int getRoomCount() {
        return rooms.size();
    }

    /**
     * 获取旅社平均舒适度
     *
     * @return 平均舒适度 (0-100)，如果没有房间则返回 0
     */
    public int getAverageComfort() {
        if (rooms.isEmpty()) {
            return 0;
        }

        int totalComfort = 0;
        for (RoomData room : rooms.values()) {
            totalComfort += room.getComfort();
        }

        return totalComfort / rooms.size();
    }

    public int getFacilityLevel(String facilityId) {
        if (facilityId == null || facilityId.isBlank()) {
            return 0;
        }
        return Math.max(0, facilityLevels.getOrDefault(facilityId, 0));
    }

    public void setFacilityLevel(String facilityId, int level) {
        if (facilityId == null || facilityId.isBlank()) {
            return;
        }
        facilityLevels.put(facilityId, Math.max(0, level));
    }

    public record DecorationPlacement(BlockPos origin, net.minecraft.world.level.block.Rotation rotation) {
        public DecorationPlacement {
            origin = origin == null ? BlockPos.ZERO : origin.immutable();
            rotation =
                    rotation == null
                            ? net.minecraft.world.level.block.Rotation.NONE
                            : rotation;
        }
    }

    public Map<String, List<DecorationPlacement>> copyActiveDecorations() {
        Map<String, List<DecorationPlacement>> copy = new HashMap<>();
        for (Map.Entry<String, List<DecorationPlacement>> entry : activeDecorations.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return copy;
    }

    public void replaceActiveDecorations(Map<String, List<DecorationPlacement>> decorations) {
        activeDecorations.clear();
        if (decorations == null || decorations.isEmpty()) {
            return;
        }
        for (Map.Entry<String, List<DecorationPlacement>> entry : decorations.entrySet()) {
            String key = entry.getKey();
            List<DecorationPlacement> placements = entry.getValue();
            if (key == null || key.isBlank() || placements == null || placements.isEmpty()) {
                continue;
            }
            activeDecorations.put(key, List.copyOf(placements));
        }
    }

    public int getActiveDecorationCount(String decorationId) {
        if (decorationId == null || decorationId.isBlank()) {
            return 0;
        }
        return activeDecorations.getOrDefault(decorationId, List.of()).size();
    }

    public double getDecorationBuffValue(InnDecorationBuffType type) {
        if (type == null || activeDecorations.isEmpty()) {
            return 0.0D;
        }
        double total = 0.0D;
        for (Map.Entry<String, List<DecorationPlacement>> entry : activeDecorations.entrySet()) {
            InnDecorationDefinition definition = InnDecorationRegistry.get(entry.getKey());
            if (definition == null) {
                continue;
            }
            int instanceCount = entry.getValue() == null ? 0 : entry.getValue().size();
            if (instanceCount <= 0) {
                continue;
            }
            for (InnDecorationBuff buff : definition.buffs()) {
                if (buff.type() == type) {
                    total += buff.value() * instanceCount;
                }
            }
        }
        return total;
    }

    public double getDecorationBuffMultiplier(InnDecorationBuffType type) {
        return Math.max(0.0D, 1.0D + getDecorationBuffValue(type));
    }

    private int applyPositiveBuff(int amount, InnDecorationBuffType type) {
        if (amount <= 0) {
            return amount;
        }
        double multiplier = getDecorationBuffMultiplier(type);
        int scaled = (int) Math.round(amount * multiplier);
        return Math.max(amount, scaled);
    }

    /**
     * 检查所有房间的合法性
     *
     * <p>遍历所有房间，如果不符合合法性规则，则将其删除。
     *
     * @param level 服务器等级
     * @param team 所属队伍 (用于范围检查)
     * @return 被删除的房间ID列表
     */
    public List<Integer> checkAllRoomsValidity(Level level, TeamData team) {
        List<Integer> removedRooms = new ArrayList<>();
        // 收集需要删除的房间ID和原因，避免在遍历时修改集合
        Map<Integer, RoomData.ValidationResult> failureReasons = new HashMap<>();

        for (RoomData room : rooms.values()) {
            RoomData.ValidationResult result =
                    RoomData.validate(
                            room.getMinPos(), room.getMaxPos(), level, team, room.getId());
            boolean keepRoom = result.isSuccess();
            if (!keepRoom && result == RoomData.ValidationResult.MISSING_BED) {
                // 装修模式自动检测时：只要房间内仍有床（无论干净或脏乱）就保留房间。
                keepRoom = RoomData.countAllBeds(room.getMinPos(), room.getMaxPos(), level) > 0;
            }

            if (!keepRoom) {
                removedRooms.add(room.getId());
                failureReasons.put(room.getId(), result);
            } else {
                // 如果验证通过，更新床的数量和整洁度
                int[] bedStats =
                        RoomData.calculateBedStats(room.getMinPos(), room.getMaxPos(), level);
                room.setMaxGuests(bedStats[0]);
                room.setCleanliness(bedStats[1]);
                room.setTotalBeds(bedStats[2]);
            }
        }

        // 删除无效房间
        for (Integer roomId : removedRooms) {
            RoomData.ValidationResult reason = failureReasons.get(roomId);
            removeRoom(roomId, level, team, Component.translatable(reason.getTranslationKey()));
        }

        return removedRooms;
    }

    /**
     * 判断一个位置是否处于房间的范围内或其六面外壳上。
     *
     * <p>六面外壳包括：房间内部、地板（底面下方一层）、天花板（顶面上方一层）、
     * 以及四面外侧墙壁（东西南北各向外偏移一格）。
     */
    public static boolean isPosAffectingRoom(BlockPos pos, RoomData room) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int minX = room.getMinPos().getX();
        int minY = room.getMinPos().getY();
        int minZ = room.getMinPos().getZ();
        int maxX = room.getMaxPos().getX();
        int maxY = room.getMaxPos().getY();
        int maxZ = room.getMaxPos().getZ();

        if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
            return true;
        }

        if (y == minY - 1 && x >= minX && x <= maxX && z >= minZ && z <= maxZ) {
            return true;
        }

        if (y == maxY + 1 && x >= minX && x <= maxX && z >= minZ && z <= maxZ) {
            return true;
        }

        if (y >= minY && y <= maxY) {
            if (z == minZ - 1 && x >= minX && x <= maxX) return true;
            if (z == maxZ + 1 && x >= minX && x <= maxX) return true;
            if (x == minX - 1 && z >= minZ && z <= maxZ) return true;
            if (x == maxX + 1 && z >= minZ && z <= maxZ) return true;
        }

        return false;
    }

    /**
     * 查找该位置受影响的房间（包含内部和六面外壳）。
     *
     * @return 受该位置影响的房间，没有则返回 null
     */
    public RoomData getRoomAffectedBy(BlockPos pos) {
        for (RoomData room : rooms.values()) {
            if (isPosAffectingRoom(pos, room)) {
                return room;
            }
        }
        return null;
    }

    /**
     * 根据一组变更位置，找出受影响的房间 ID 集合。
     */
    public java.util.Set<Integer> findAffectedRoomIds(java.util.Set<BlockPos> positions) {
        java.util.Set<Integer> affected = new java.util.HashSet<>();
        for (BlockPos pos : positions) {
            for (RoomData room : rooms.values()) {
                if (isPosAffectingRoom(pos, room)) {
                    affected.add(room.getId());
                }
            }
        }
        return affected;
    }

    /**
     * 对单个房间进行合法性判定与属性更新。
     *
     * <p>如果房间不合法，则自动删除该房间。
     *
     * @return 房间是否被保留
     */
    public boolean checkAndUpdateRoom(int roomId, Level level, TeamData team) {
        RoomData room = rooms.get(roomId);
        if (room == null) {
            return false;
        }

        RoomData.ValidationResult result =
                RoomData.validate(room.getMinPos(), room.getMaxPos(), level, team, room.getId());
        boolean keepRoom = result.isSuccess();
        if (!keepRoom && result == RoomData.ValidationResult.MISSING_BED) {
            keepRoom = RoomData.countAllBeds(room.getMinPos(), room.getMaxPos(), level) > 0;
        }

        if (!keepRoom) {
            removeRoom(
                    roomId,
                    level,
                    team,
                    Component.translatable(result.getTranslationKey()));
            return false;
        }

        int[] bedStats =
                RoomData.calculateBedStats(room.getMinPos(), room.getMaxPos(), level);
        room.setMaxGuests(bedStats[0]);
        room.setCleanliness(bedStats[1]);
        room.setTotalBeds(bedStats[2]);

        calculateRoomStats(roomId, level);
        if (level instanceof ServerLevel serverLevel && team != null) {
            syncRoomCleaningTodo(serverLevel, team, room);
        }

        return true;
    }

    private void syncRoomCleaningTodo(ServerLevel level, TeamData team, RoomData room) {
        if (room == null || team == null) {
            return;
        }
        String todoText = getRoomCleaningTodoText(room);
        if (roomNeedsCleaning(room, level)) {
            addTodo(level, team, todoText);
        } else {
            removeTodo(level, team, todoText);
        }
    }

    private String getRoomCleaningTodoText(RoomData room) {
        return Component.translatable("todo.otherworldinn.room_cleaning", RoomData.getDisplayName(room))
                .getString();
    }

    private boolean roomNeedsCleaning(RoomData room, Level level) {
        return roomHasMessyBed(room, level) || roomHasClutter(room, level);
    }

    private boolean roomHasMessyBed(RoomData room, Level level) {
        BlockPos min = room.getMinPos();
        BlockPos max = room.getMaxPos();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof BedBlock
                    && state.hasProperty(ModBlockProperties.MESSY)
                    && state.getValue(ModBlockProperties.MESSY)
                    && state.hasProperty(BedBlock.PART)
                    && state.getValue(BedBlock.PART) == BedPart.HEAD) {
                return true;
            }
        }
        return false;
    }

    private boolean roomHasClutter(RoomData room, Level level) {
        return countRoomClutter(room, level) > 0;
    }

    private int countRoomClutter(RoomData room, Level level) {
        int count = 0;
        BlockPos min = room.getMinPos();
        BlockPos max = room.getMaxPos();
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (level.getBlockState(pos).is(ModBlocks.CLUTTER.get())) {
                count++;
            }
        }
        return count;
    }

    // --- 入住/退房 ---

    /**
     * 旅客入住
     *
     * <p>将旅客分配到指定房间。如果房间已满或不存在，返回 false。 如果旅客已在其他房间，会自动先执行退房。
     *
     * @param guestId 旅客 UUID
     * @param roomId 目标房间ID
     * @param level 服务器等级
     * @return 是否成功入住
     */
    public boolean checkIn(UUID guestId, int roomId, ServerLevel level) {
        RoomData room = rooms.get(roomId);
        if (room == null) {
            return false; // 房间不存在
        }

        // 检查房间是否已满
        if (room.getCurrentGuests().size() >= room.getMaxGuests()) {
            return false;
        }

        GuestData guest = getGuestData(guestId, level);
        if (guest == null) {
            return false; // 找不到旅客实体
        }

        // 检查旅客是否已在其他房间
        if (guest.getRoomId() != -1) {
            return false;
        }

        // 执行入住逻辑
        if (room.addGuest(guestId)) {
            guest.setRoomId(roomId);
            BlockPos assignedBedPos = claimUnassignedBedForGuest(room, guestId, level);
            if (assignedBedPos == null) {
                room.removeGuest(guestId);
                guest.setRoomId(-1);
                return false;
            }
            guest.setAssignedBedPos(assignedBedPos);
            // 更新偏好分数
            guest.updatePreferenceScore(room);
            this.addGuest(guestId);

            // 让实体寻路到房间
            Entity entity = level.getEntity(guestId);
            if (entity instanceof GuestEntity guestEntity) {
                BlockPos targetPos =
                        findBestRoomNavigationTarget(level, guestEntity, room, assignedBedPos);
                guestEntity.setNavigationTarget(targetPos);

                // 移除剪贴板 TODO
                String guestName =
                        entity.getCustomName() != null
                                ? entity.getCustomName().getString()
                                : "Guest";
                // 使用与生成时相同的 Key
                String todoText =
                        Component.translatable("todo.otherworldinn.guest_waiting", guestName)
                                .getString();

                // 获取当前队伍并移除 TODO
                TeamData team =
                        TeamManager.getInstance().getTeamAt(room.getMinPos(), level.getServer());
                if (team != null) {
                    removeTodo(level, team, todoText);
                    // 触发客户端同步，更新 Tooltip
                    TeamManager.getInstance().syncTeam(team, level.getServer());
                }
            }

            totalCheckInCount++;
            return true;
        }

        return false;
    }

    private BlockPos findBestRoomNavigationTarget(
            ServerLevel level, GuestEntity guestEntity, RoomData room, BlockPos assignedBedPos) {
        BlockPos bedApproach = findBedApproachTarget(level, assignedBedPos);
        if (bedApproach != null) {
            if (isReachable(guestEntity, bedApproach)) {
                return bedApproach;
            }
        }
        BlockPos min = room.getMinPos();
        BlockPos max = room.getMaxPos();
        int centerX = (min.getX() + max.getX()) / 2;
        int centerY = min.getY() + 1;
        int centerZ = (min.getZ() + max.getZ()) / 2;

        int searchX0 = min.getX() - 2;
        int searchX1 = max.getX() + 2;
        int searchZ0 = min.getZ() - 2;
        int searchZ1 = max.getZ() + 2;

        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int x = searchX0; x <= searchX1; x++) {
            for (int z = searchZ0; z <= searchZ1; z++) {
                for (int y = min.getY(); y <= Math.min(max.getY(), min.getY() + 2); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!isWalkableRoomTarget(level, pos)) {
                        continue;
                    }
                    if (!isReachable(guestEntity, pos)) {
                        continue;
                    }
                    double dist = guestEntity.distanceToSqr(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = pos;
                    }
                }
            }
        }
        if (best != null) {
            return best;
        }

        BlockPos fallback = new BlockPos(centerX, centerY, centerZ);
        if (!isWalkableRoomTarget(level, fallback)) {
            return fallback.above();
        }
        return fallback;
    }

    private static boolean isReachable(GuestEntity guestEntity, BlockPos pos) {
        Path path = guestEntity.getNavigation().createPath(pos, 0);
        return path != null && path.canReach();
    }

    private BlockPos claimUnassignedBedForGuest(RoomData room, UUID guestId, ServerLevel level) {
        List<BlockPos> cleanBedHeads = collectCleanBedHeads(room, level);
        if (cleanBedHeads.isEmpty()) {
            return null;
        }
        double centerX = (room.getMinPos().getX() + room.getMaxPos().getX()) / 2.0D;
        double centerY = room.getMinPos().getY() + 1.0D;
        double centerZ = (room.getMinPos().getZ() + room.getMaxPos().getZ()) / 2.0D;
        cleanBedHeads.sort(
                Comparator.comparingDouble(
                        pos -> {
                            double dx = pos.getX() - centerX;
                            double dy = pos.getY() - centerY;
                            double dz = pos.getZ() - centerZ;
                            return dx * dx + dy * dy + dz * dz;
                        }));
        for (BlockPos bedHead : cleanBedHeads) {
            if (!isBedClaimedByOtherGuest(room, guestId, bedHead, level)) {
                return bedHead.immutable();
            }
        }
        return null;
    }

    private List<BlockPos> collectCleanBedHeads(RoomData room, ServerLevel level) {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(room.getMinPos(), room.getMaxPos())) {
            BlockState state = level.getBlockState(pos);
            BlockPos headPos = toBedHeadPos(state, pos);
            if (headPos == null || !headPos.equals(pos)) {
                continue;
            }
            if (!isCleanBedState(state)) {
                continue;
            }
            result.add(headPos.immutable());
        }
        return result;
    }

    private boolean isBedClaimedByOtherGuest(
            RoomData room, UUID excludedGuestId, BlockPos bedHeadPos, ServerLevel level) {
        for (UUID roomGuestId : room.getCurrentGuests()) {
            if (roomGuestId.equals(excludedGuestId)) {
                continue;
            }
            GuestData roomGuestData = getGuestData(roomGuestId, level);
            if (roomGuestData == null) {
                continue;
            }
            BlockPos claimed = roomGuestData.getAssignedBedPos();
            if (claimed != null && claimed.equals(bedHeadPos)) {
                return true;
            }
        }
        return false;
    }

    private BlockPos findBedApproachTarget(ServerLevel level, BlockPos bedHeadPos) {
        if (bedHeadPos == null) {
            return null;
        }
        BlockState headState = level.getBlockState(bedHeadPos);
        BlockPos normalizedHead = toBedHeadPos(headState, bedHeadPos);
        if (normalizedHead == null) {
            return null;
        }
        BlockState normalizedHeadState = level.getBlockState(normalizedHead);
        if (!isCleanBedState(normalizedHeadState) && !isBedState(normalizedHeadState)) {
            return null;
        }
        Direction facing = normalizedHeadState.getValue(BedBlock.FACING);
        BlockPos footPos = normalizedHead.relative(facing.getOpposite());
        BlockPos[] candidates =
                new BlockPos[] {
                    footPos.relative(facing.getOpposite()),
                    footPos.relative(facing.getClockWise()),
                    footPos.relative(facing.getCounterClockWise()),
                    normalizedHead.relative(facing),
                    footPos
                };
        for (BlockPos candidate : candidates) {
            if (isWalkableRoomTarget(level, candidate)) {
                return candidate.immutable();
            }
        }
        return null;
    }

    private boolean isCleanBedState(BlockState state) {
        if (!isBedState(state)) {
            return false;
        }
        if (state.hasProperty(ModBlockProperties.MESSY) && state.getValue(ModBlockProperties.MESSY)) {
            return false;
        }
        return state.hasProperty(BedBlock.PART) && state.getValue(BedBlock.PART) == BedPart.HEAD;
    }

    private boolean isBedState(BlockState state) {
        return state.getBlock() instanceof BedBlock
                && state.hasProperty(BedBlock.PART)
                && state.hasProperty(BedBlock.FACING);
    }

    private BlockPos toBedHeadPos(BlockState state, BlockPos pos) {
        if (!isBedState(state)) {
            return null;
        }
        BedPart part = state.getValue(BedBlock.PART);
        if (part == BedPart.HEAD) {
            return pos.immutable();
        }
        Direction facing = state.getValue(BedBlock.FACING);
        BlockPos headPos = pos.relative(facing);
        return headPos.immutable();
    }

    private boolean isWalkableRoomTarget(ServerLevel level, BlockPos pos) {
        BlockState feet = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        BlockState ground = level.getBlockState(pos.below());
        return !feet.isSolid() && !head.isSolid() && ground.isSolid();
    }

    // --- 旅客生成 ---

    /**
     * 尝试生成新旅客
     *
     * @param level 服务器等级
     */
    private void trySpawnGuest(ServerLevel level) {
        long currentTime = level.getDayTime();

        // 1. 检查是否到达生成时间
        if (currentTime < nextGuestSpawnTime) {
            return;
        }

        // 2. 检查旅社是否开业
        if (this.state != InnState.OPEN) {
            return;
        }

        // 3. 检查是否有可用床位
        if (!hasAvailableBed()) {
            return;
        }

        // 4. 检查当前世界中等待入住的旅客数量
        if (getWaitingGuestCount(level) >= 3) {
            // 如果等待人数过多，推迟生成
            scheduleNextSpawn(level.getRandom(), currentTime);
            return;
        }

        // 5. 生成旅客
        spawnGuest(level);

        // 6. 安排下一次生成
        scheduleNextSpawn(level.getRandom(), currentTime);
    }

    /** 检查是否有可用床位 */
    private boolean hasAvailableBed() {
        for (RoomData room : rooms.values()) {
            if (room.getCurrentGuests().size() < room.getMaxGuests()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前世界中正在等待入住的旅客数量
     *
     * <p>统计所有处于 IDLE 或 WAITING 状态的旅客实体。
     */
    private int getWaitingGuestCount(ServerLevel level) {
        int count = 0;
        // 遍历所有加载的实体，筛选出 GuestEntity
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof GuestEntity guest) {
                GuestData.GuestState state = guest.getGuestData().getState();
                if (state == GuestData.GuestState.IDLE || state == GuestData.GuestState.WAITING) {
                    count++;
                }
            }
        }
        return count;
    }

    /** 生成旅客实体 */
    private void spawnGuest(ServerLevel level) {
        // 随机坐标范围：(20, 71, 2) ~ (5, 71, -2)
        // X: 5 ~ 20
        // Z: -2 ~ 2
        // Y: 71
        double x = 5 + level.random.nextDouble() * (20 - 5);
        double z = -2 + level.random.nextDouble() * (2 - (-2));
        double y = 71;

        // 检查该位置所在的区块是否加载
        if (!level.isLoaded(BlockPos.containing(x, y, z))) {
            return;
        }

        GuestEntity guest = StoryGuestService.createSpawnCandidate(level, this.rating, level.random);
        if (guest == null) {
            guest = GuestSpawnRules.createGuestForRating(this.rating, level.random, level);
        }
        if (guest != null) {
            guest.moveTo(x, y, z, level.random.nextFloat() * 360F, 0.0F);
            guest.finalizeSpawn(
                    level,
                    level.getCurrentDifficultyAt(guest.blockPosition()),
                    MobSpawnType.EVENT,
                    null);
            guest.setNoAi(false);
            guest.setPersistenceRequired();
            if (level.addFreshEntity(guest) && guest instanceof StoryGuestEntity storyGuest) {
                StoryGuestService.markGuestSpawned(storyGuest, level);
            }
        }
    }

    // 修改 scheduleNextSpawn 为返回 delay
    private int calculateNextSpawnDelay(RandomSource random) {
        int clampedRating = Math.max(0, Math.min(5, this.rating));
        double progress = clampedRating / 5.0D;
        double averageDelay =
                MAX_AVERAGE_SPAWN_DELAY_TICKS
                        - (MAX_AVERAGE_SPAWN_DELAY_TICKS - MIN_AVERAGE_SPAWN_DELAY_TICKS)
                                * progress;
        averageDelay /= Math.max(0.1D, getDecorationBuffMultiplier(
                InnDecorationBuffType.GUEST_ARRIVAL_SPEED_MULTIPLIER));
        double jitter = averageDelay * SPAWN_DELAY_JITTER_RATIO;
        int delay = (int) Math.round(averageDelay + random.nextGaussian() * jitter);
        return Math.max(MIN_SPAWN_DELAY_TICKS, Math.min(MAX_SPAWN_DELAY_TICKS, delay));
    }

    private int getGuestWaitingTimeoutTicks() {
        int clampedRating = Math.max(0, Math.min(5, this.rating));
        int timeout = BASE_GUEST_WAITING_TIMEOUT + clampedRating * WAITING_PATIENCE_BONUS_PER_STAR;
        return Math.min(MAX_GUEST_WAITING_TIMEOUT, timeout);
    }

    /**
     * 辅助方法：安排下一次生成
     *
     * @param random 随机源
     * @param currentTime 当前游戏时间
     */
    private void scheduleNextSpawn(RandomSource random, long currentTime) {
        this.nextGuestSpawnTime = currentTime + calculateNextSpawnDelay(random);
    }

    // --- 辅助方法 ---

    /**
     * 将房间内的一张干净的床设置为脏乱状态
     *
     * @param roomId 房间ID
     * @param level 服务器等级
     * @return 是否成功弄乱了一张床
     */
    private boolean setRoomBedMessy(int roomId, ServerLevel level, BlockPos preferredBedHeadPos) {
        RoomData room = rooms.get(roomId);
        if (room == null) return false;

        if (preferredBedHeadPos != null && setBedMessy(preferredBedHeadPos, level)) {
            return true;
        }

        BlockPos min = room.getMinPos();
        BlockPos max = room.getMaxPos();

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (setBedMessy(pos, level)) {
                return true;
            }
        }
        return false;
    }

    private boolean setBedMessy(BlockPos anyBedPartPos, ServerLevel level) {
        BlockState state = level.getBlockState(anyBedPartPos);
        BlockPos headPos = toBedHeadPos(state, anyBedPartPos);
        if (headPos == null) {
            return false;
        }
        BlockState headState = level.getBlockState(headPos);
        if (!isCleanBedState(headState) || !headState.hasProperty(ModBlockProperties.MESSY)) {
            return false;
        }
        level.setBlock(headPos, headState.setValue(ModBlockProperties.MESSY, true), 3);
        Direction facing = headState.getValue(BedBlock.FACING);
        BlockPos footPos = headPos.relative(facing.getOpposite());
        BlockState footState = level.getBlockState(footPos);
        if (isBedState(footState) && footState.hasProperty(ModBlockProperties.MESSY)) {
            level.setBlock(footPos, footState.setValue(ModBlockProperties.MESSY, true), 3);
        }
        return true;
    }

    private boolean trySpawnRoomClutter(RoomData room, ServerLevel level) {
        if (room == null || level.random.nextFloat() >= CLUTTER_SPAWN_CHANCE) {
            return false;
        }

        List<BlockPos> candidates = collectRoomClutterSpawnPositions(room, level);
        if (candidates.isEmpty()) {
            return false;
        }

        Collections.shuffle(candidates, new Random(level.random.nextLong()));
        int spawnCount =
                Math.min(
                        candidates.size(),
                        MIN_CLUTTER_PER_CHECKOUT
                                + level.random.nextInt(MAX_CLUTTER_PER_CHECKOUT - MIN_CLUTTER_PER_CHECKOUT + 1));

        boolean spawned = false;
        BlockState clutterState = ModBlocks.CLUTTER.get().defaultBlockState();
        for (int i = 0; i < spawnCount; i++) {
            BlockPos pos = candidates.get(i);
            if (!level.getBlockState(pos).isAir()) {
                continue;
            }
            level.setBlock(pos, clutterState, 3);
            spawned = true;
        }
        return spawned;
    }

    private List<BlockPos> collectRoomClutterSpawnPositions(RoomData room, ServerLevel level) {
        List<BlockPos> candidates = new ArrayList<>();
        int floorY = room.getMinPos().getY();

        for (int x = room.getMinPos().getX(); x <= room.getMaxPos().getX(); x++) {
            for (int z = room.getMinPos().getZ(); z <= room.getMaxPos().getZ(); z++) {
                BlockPos pos = new BlockPos(x, floorY, z);
                if (!isValidClutterSpawnPos(level, pos)) {
                    continue;
                }
                candidates.add(pos);
            }
        }
        return candidates;
    }

    private boolean isValidClutterSpawnPos(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return net.minecraft.world.level.block.Block.isFaceFull(
                belowState.getCollisionShape(level, belowPos), Direction.UP);
    }

    /**
     * 每 tick 更新
     *
     * <p>检查等待超时的旅客。
     *
     * @param level 世界
     * @param team 队伍数据
     */
    public void tick(ServerLevel level, TeamData team) {
        long currentTime = level.getDayTime();

        // 尝试生成旅客 (每 20 tick 检查一次，减少开销)
        if (currentTime % 20 == 0) {
            trySpawnGuest(level);
        }

        // 每 5 tick 检查一次
        if (currentTime % 5 != 0) return;

        // 遍历旅客检查状态

        List<UUID> guestsToDepart = new ArrayList<>();
        List<UUID> guestsToCheckOut = new ArrayList<>();
        int waitingTimeoutTicks = getGuestWaitingTimeoutTicks();

        for (UUID guestId : guestIds) {
            Entity entity = level.getEntity(guestId);
            if (entity instanceof GuestEntity guestEntity) {
                GuestData guestData = guestEntity.getGuestData();
                if (guestData.getState() == GuestData.GuestState.WAITING) {
                    if (currentTime - guestData.getWaitingSince() > waitingTimeoutTicks
                            || this.state != InnState.OPEN) {
                        guestsToDepart.add(guestId);
                    }
                } else if (guestData.getState() == GuestData.GuestState.CHECKED_IN) {
                    // 检查是否到达退房时间
                    if (currentTime >= guestData.getCheckoutTime()
                            && guestEntity.canCheckOutNow(currentTime)) {
                        guestsToCheckOut.add(guestId);
                    }
                }
            }
        }

        // 处理离开
        for (UUID guestId : guestsToDepart) {
            handleGuestDeparture(guestId, true, level, team);
            // handleGuestDeparture 内部不调用 removeGuest，所以这里手动移除
            removeGuest(guestId);
        }

        // 处理退房
        for (UUID guestId : guestsToCheckOut) {
            checkOut(guestId, level, true);
            // checkOut 内部会调用 removeGuest
        }
    }

    /**
     * 处理旅客离开
     *
     * @param guestId 旅客 ID
     * @param isAngry 是否生气离开
     * @param level 世界
     * @param team 队伍数据
     */
    public void handleGuestDeparture(
            UUID guestId, boolean isAngry, ServerLevel level, TeamData team) {
        Entity entity = level.getEntity(guestId);
        GuestEntity guestEntity = entity instanceof GuestEntity g ? g : null;

        // 1. 获取并移除待办事项 (如果是等待中离开)
        if (guestEntity != null) {
            GuestData guestData = guestEntity.getGuestData();
            if (guestData.getState() == GuestData.GuestState.WAITING) {
                String guestName =
                        entity.getCustomName() != null
                                ? entity.getCustomName().getString()
                                : "Guest";
                String todoText =
                        Component.translatable("todo.otherworldinn.guest_waiting", guestName)
                                .getString();
                removeTodo(level, team, todoText);
            }
        }

        if (isAngry) {
            int reputationLoss = 2 + level.random.nextInt(5);
            this.addReputation(scaleGuestReputationDelta(guestEntity, -reputationLoss));
            if (entity != null) {
                level.broadcastEntityEvent(entity, (byte) 13);
                level.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.ANGRY_VILLAGER,
                        entity.getX(),
                        entity.getY() + entity.getEyeHeight() + 0.5,
                        entity.getZ(),
                        10,
                        0.25,
                        0.25,
                        0.25,
                        0.02);
                level.playSound(
                        null,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        net.minecraft.sounds.SoundEvents.VILLAGER_NO,
                        SoundSource.NEUTRAL,
                        1.0f,
                        1.0f);
            }
        } else {
            // 正常退房由 checkOut 处理；此处处理异常离开
        }

        // 通用离开逻辑：移除占用并触发离场

        if (guestEntity != null) {
            StoryGuestService.handleGuestVisitEnded(guestEntity, level);
            guestEntity.setNavigationTarget(new BlockPos(10, 71, 0));
            EntityUtils.scheduleDisappear(guestEntity);

            // 更新状态
            guestEntity.getGuestData().setCheckedOut(true);
        }

        // 从列表移除：tick 内遍历场景由调用方处理
        if (!isAngry) { // 仅非 tick 调用的情况
            removeGuest(guestId);
        }
    }

    /**
     * 旅客退房
     *
     * <p>将旅客从当前房间移除，并从旅社旅客名单中删除。 如果旅客实体存在，会触发奖励物品掉落。 此外，会将房间内的一张床标记为脏乱。
     *
     * @param guestId 旅客UUID
     * @param level 服务器等级
     * @param isNormalCheckout 是否为正常退房（如果为 false，则不计算房费）
     */
    public void checkOut(UUID guestId, ServerLevel level, boolean isNormalCheckout) {
        Entity entity = level.getEntity(guestId);
        GuestEntity guestEntity = entity instanceof GuestEntity g ? g : null;
        GuestData guest = null;
        boolean vipGuest = false;
        if (guestEntity != null) {
            guest = guestEntity.getGuestData();
            vipGuest = guestEntity instanceof VipGuestEntity;
            if (guest != null) {
                guest.dropRewards(level, entity.blockPosition(), guest.getAssignedBedPos());
            }
        }
        RoomData targetRoom = null;
        if (guest != null && guest.getRoomId() != -1) {
            targetRoom = rooms.get(guest.getRoomId());
        }
        if (targetRoom == null) {
            for (RoomData room : rooms.values()) {
                if (room.hasGuest(guestId)) {
                    targetRoom = room;
                    break;
                }
            }
        }
        TeamData team = null;
        if (targetRoom != null) {
            BlockPos boundBedPos = guest == null ? null : guest.getAssignedBedPos();
            setRoomBedMessy(targetRoom.getId(), level, boundBedPos);
            trySpawnRoomClutter(targetRoom, level);
            targetRoom.removeGuest(guestId);
            team = TeamManager.getInstance().getTeamAt(targetRoom.getMinPos(), level.getServer());
            if (team == null) {
                TeamSavedData data = TeamManager.getInstance().getData(level.getServer());
                if (data != null) {
                    for (TeamData candidate : data.getTeams().values()) {
                        RoomData candidateRoom = candidate.getInnData().getRoom(targetRoom.getId());
                        if (candidateRoom != null
                                && candidateRoom.getUuid().equals(targetRoom.getUuid())) {
                            team = candidate;
                            break;
                        }
                    }
                }
            }
            if (isNormalCheckout && team != null) {
                int price = targetRoom.getBedPrice(this.rating);
                TeamManager.getInstance().addCoins(team, price, level.getServer());
                this.recordLodgingIncome(price, level);
            }
            if (isNormalCheckout && guest != null) {
                guest.updatePreferenceScore(targetRoom);
                int score = guest.getPreferenceScore();
                int baseReputationGain = Math.max(2, Math.min(10, score));
                if (!vipGuest) {
                    this.addReputation(scaleGuestReputationDelta(guestEntity, baseReputationGain));
                } else {
                    int mismatchCount = countPreferenceMismatch(guest, targetRoom);
                    if (mismatchCount == 0) {
                        int vipReputationGain = Math.max(1, Math.round(baseReputationGain * 1.7f));
                        this.addReputation(scaleGuestReputationDelta(guestEntity, vipReputationGain));
                    } else if (mismatchCount >= 2) {
                        int vipReputationLoss = 2 + level.random.nextInt(5);
                        this.addReputation(scaleGuestReputationDelta(guestEntity, -vipReputationLoss));
                    }
                }
            }
            if (team != null) {
                checkAndUpdateRoom(targetRoom.getId(), level, team);
            }
        }
        if (guest != null) {
            guest.setAssignedBedPos(null);
            guest.setRoomId(-1);
            guest.setCheckedOut(true);
        }
        if (guestEntity != null) {
            StoryGuestService.handleGuestVisitEnded(guestEntity, level);
        }
        removeGuest(guestId);
        if (team != null) {
            TeamManager.getInstance().syncTeam(team, level.getServer());
        }
        if (guestEntity != null) {
            guestEntity.setNavigationTarget(new BlockPos(10, 71, 0));
        }
        if (entity != null) {
            EntityUtils.scheduleDisappear(entity);
        }
    }

    private int countPreferenceMismatch(GuestData guest, RoomData room) {
        int mismatch = 0;
        if (!guest.getComfortPreference().contains(room.getComfort())) {
            mismatch++;
        }
        if (!guest.getLightPreference().contains(room.getLight())) {
            mismatch++;
        }
        if (!guest.getHumidityPreference().contains(room.getHumidity())) {
            mismatch++;
        }
        return mismatch;
    }

    /**
     * 添加待办事项
     *
     * <p>同时添加到缓存列表和实际剪贴板中。
     *
     * @param level 世界
     * @param team 队伍数据
     * @param todoText 待办事项文本
     * @return 是否成功添加到剪贴板（如果只添加到缓存也算处理成功，但返回 false 表示没有物理剪贴板更新）
     */
    public boolean addTodo(Level level, TeamData team, String todoText) {
        // 1. 仅在首次新增时继续同步，避免房间清扫过程中的重复提示
        if (todoList.contains(todoText)) {
            return false;
        }
        todoList.add(todoText);

        // 2. 尝试同步到剪贴板
        boolean addedToClipboard = false;
        for (TeamData.InnRegion region : team.getInnRegions()) {
            AABB area =
                    new AABB(region.minX(), -64, region.minZ(), region.maxX(), 320, region.maxZ());
            if (ClipboardManager.addTodo(level, area, todoText)) {
                addedToClipboard = true;
            }
        }

        // 3. 广播通知
        if (addedToClipboard && level instanceof ServerLevel serverLevel) {
            team.getMembers()
                    .forEach(
                            uuid -> {
                                ServerPlayer player =
                                        serverLevel.getServer().getPlayerList().getPlayer(uuid);
                                if (player != null) {
                                    player.displayClientMessage(
                                            Component.translatable(
                                                            "message.otherworldinn.todo.new_task",
                                                            todoText)
                                                    .withStyle(
                                                            style ->
                                                                    style.withColor(
                                                                            ModColors.INFO)),
                                            false);
                                    player.playNotifySound(
                                            SoundEvents.NOTE_BLOCK_BELL.value(),
                                            SoundSource.PLAYERS,
                                            1.0f,
                                            1.0f);
                                }
                            });
        }

        return addedToClipboard;
    }

    /**
     * 移除待办事项
     *
     * @param level 世界
     * @param team 队伍数据
     * @param todoText 待办事项文本
     */
    public void removeTodo(Level level, TeamData team, String todoText) {
        // 1. 从缓存移除
        todoList.remove(todoText);

        // 2. 从剪贴板移除
        for (TeamData.InnRegion region : team.getInnRegions()) {
            AABB area =
                    new AABB(region.minX(), -64, region.minZ(), region.maxX(), 320, region.maxZ());
            ClipboardManager.removeTodo(level, area, todoText);
        }
    }

    /**
     * 同步缓存的待办事项到指定区域的剪贴板
     *
     * <p>通常在放置新的剪贴板时调用。
     */
    public void syncTodosToClipboard(Level level, AABB area) {
        if (todoList.isEmpty()) return;

        for (String todo : todoList) {
            ClipboardManager.addTodo(level, area, todo);
        }
    }

    public void refreshClipboardTodos(Level level, TeamData team) {
        for (TeamData.InnRegion region : team.getInnRegions()) {
            AABB area =
                    new AABB(region.minX(), -64, region.minZ(), region.maxX(), 320, region.maxZ());
            ClipboardManager.clear(level, area);
            for (String todo : todoList) {
                ClipboardManager.addTodo(level, area, todo);
            }
        }
    }

    /**
     * 保存数据到 NBT
     *
     * @param tag 目标标签
     * @return 写入数据的标签
     */
    public CompoundTag save(CompoundTag tag) {
        tag.putString("Name", name);
        tag.putInt("Rating", rating);
        tag.putInt("Reputation", reputation);
        tag.putInt("TotalLodgingIncome", totalLodgingIncome);
        tag.putInt("TotalCheckInCount", totalCheckInCount);
        tag.putInt("TotalDiningIncome", totalDiningIncome);
        tag.putInt("TotalOtherIncome", totalOtherIncome);
        tag.putInt("TodayLodgingIncome", todayLodgingIncome);
        tag.putInt("TodayDiningIncome", todayDiningIncome);
        tag.putInt("TodayOtherIncome", todayOtherIncome);
        tag.putInt("YesterdayLodgingIncome", yesterdayLodgingIncome);
        tag.putInt("YesterdayDiningIncome", yesterdayDiningIncome);
        tag.putInt("YesterdayOtherIncome", yesterdayOtherIncome);
        tag.putLong("IncomeStatDay", incomeStatDay);
        tag.putBoolean("InitialChartsGiven", initialChartsGiven);
        tag.putInt("LastChartWeek", lastChartDistributionWeek);
        tag.putString("State", state.name());

        ListTag guestsTag = new ListTag();
        for (UUID guestId : guestIds) {
            CompoundTag guestTag = new CompoundTag();
            guestTag.putUUID("UUID", guestId);
            guestsTag.add(guestTag);
        }
        tag.put("Guests", guestsTag);

        ListTag roomsTag = new ListTag();
        for (RoomData room : rooms.values()) {
            roomsTag.add(room.save(new CompoundTag()));
        }
        tag.put("Rooms", roomsTag);

        // 保存待办事项
        ListTag todosTag = new ListTag();
        for (String todo : todoList) {
            todosTag.add(StringTag.valueOf(todo));
        }
        tag.put("TodoList", todosTag);

        tag.putLong("NextGuestSpawnTime", nextGuestSpawnTime);
        CompoundTag facilityLevelsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : facilityLevels.entrySet()) {
            facilityLevelsTag.putInt(entry.getKey(), Math.max(0, entry.getValue()));
        }
        tag.put("FacilityLevels", facilityLevelsTag);

        ListTag activeDecorationsTag = new ListTag();
        for (Map.Entry<String, List<DecorationPlacement>> entry : activeDecorations.entrySet()) {
            for (DecorationPlacement placement : entry.getValue()) {
                CompoundTag placementTag = new CompoundTag();
                placementTag.putString("Id", entry.getKey());
                placementTag.putLong("Origin", placement.origin().asLong());
                placementTag.putString("Rotation", placement.rotation().name());
                activeDecorationsTag.add(placementTag);
            }
        }
        tag.put("ActiveDecorations", activeDecorationsTag);

        return tag;
    }

    /**
     * 从 NBT 加载数据
     *
     * @param tag 源标签
     */
    public void load(CompoundTag tag) {
        if (tag.contains("Name")) {
            name = tag.getString("Name");
        }
        if (tag.contains("Rating")) {
            rating = tag.getInt("Rating");
        }
        if (tag.contains("Reputation")) {
            reputation = tag.getInt("Reputation");
        }
        if (tag.contains("TotalLodgingIncome")) {
            totalLodgingIncome = tag.getInt("TotalLodgingIncome");
        } else {
            totalLodgingIncome = 0;
        }
        if (tag.contains("TotalCheckInCount")) {
            totalCheckInCount = Math.max(0, tag.getInt("TotalCheckInCount"));
        } else {
            totalCheckInCount = 0;
        }
        if (tag.contains("TotalDiningIncome")) {
            totalDiningIncome = tag.getInt("TotalDiningIncome");
        } else {
            totalDiningIncome = 0;
        }
        if (tag.contains("TotalOtherIncome")) {
            totalOtherIncome = tag.getInt("TotalOtherIncome");
        } else {
            totalOtherIncome = 0;
        }
        if (tag.contains("TodayLodgingIncome")) {
            todayLodgingIncome = tag.getInt("TodayLodgingIncome");
        } else {
            todayLodgingIncome = 0;
        }
        if (tag.contains("TodayDiningIncome")) {
            todayDiningIncome = tag.getInt("TodayDiningIncome");
        } else {
            todayDiningIncome = 0;
        }
        if (tag.contains("TodayOtherIncome")) {
            todayOtherIncome = tag.getInt("TodayOtherIncome");
        } else {
            todayOtherIncome = 0;
        }
        if (tag.contains("YesterdayLodgingIncome")) {
            yesterdayLodgingIncome = tag.getInt("YesterdayLodgingIncome");
        } else {
            yesterdayLodgingIncome = 0;
        }
        if (tag.contains("YesterdayDiningIncome")) {
            yesterdayDiningIncome = tag.getInt("YesterdayDiningIncome");
        } else {
            yesterdayDiningIncome = 0;
        }
        if (tag.contains("YesterdayOtherIncome")) {
            yesterdayOtherIncome = tag.getInt("YesterdayOtherIncome");
        } else {
            yesterdayOtherIncome = 0;
        }
        if (tag.contains("IncomeStatDay")) {
            incomeStatDay = tag.getLong("IncomeStatDay");
        } else {
            incomeStatDay = -1L;
        }

        initialChartsGiven = tag.contains("InitialChartsGiven") && tag.getBoolean("InitialChartsGiven");
        lastChartDistributionWeek = tag.contains("LastChartWeek") ? tag.getInt("LastChartWeek") : -1;

        if (tag.contains("State")) {
            try {
                state = InnState.valueOf(tag.getString("State"));
            } catch (IllegalArgumentException e) {
                state = InnState.CLOSED;
            }
        } else {
            boolean isOpen = tag.contains("Open") && tag.getBoolean("Open");
            state = isOpen ? InnState.OPEN : InnState.CLOSED;
        }

        if (tag.contains("NextGuestSpawnTime")) {
            nextGuestSpawnTime = tag.getLong("NextGuestSpawnTime");
        }

        guestIds.clear();
        if (tag.contains("Guests")) {
            ListTag guestsTag = tag.getList("Guests", Tag.TAG_COMPOUND);
            for (Tag t : guestsTag) {
                if (t instanceof CompoundTag guestTag) {
                    guestIds.add(guestTag.getUUID("UUID"));
                }
            }
        }

        rooms.clear();
        if (tag.contains("Rooms")) {
            ListTag roomsTag = tag.getList("Rooms", Tag.TAG_COMPOUND);
            for (Tag t : roomsTag) {
                if (t instanceof CompoundTag roomTag) {
                    RoomData room = RoomData.load(roomTag);
                    rooms.put(room.getId(), room);
                }
            }
        }

        todoList.clear();
        if (tag.contains("TodoList")) {
            ListTag todosTag = tag.getList("TodoList", Tag.TAG_STRING);
            for (Tag t : todosTag) {
                todoList.add(t.getAsString());
            }
        }

        facilityLevels.clear();
        if (tag.contains("FacilityLevels", Tag.TAG_COMPOUND)) {
            CompoundTag facilityLevelsTag = tag.getCompound("FacilityLevels");
            for (String key : facilityLevelsTag.getAllKeys()) {
                facilityLevels.put(key, Math.max(0, facilityLevelsTag.getInt(key)));
            }
        }

        activeDecorations.clear();
        if (tag.contains("ActiveDecorations", Tag.TAG_LIST)) {
            ListTag activeDecorationsTag = tag.getList("ActiveDecorations", Tag.TAG_COMPOUND);
            for (Tag entryTag : activeDecorationsTag) {
                if (!(entryTag instanceof CompoundTag placementTag)) {
                    continue;
                }
                String decorationId = placementTag.getString("Id");
                if (decorationId == null || decorationId.isBlank()) {
                    continue;
                }
                BlockPos origin = BlockPos.of(placementTag.getLong("Origin"));
                net.minecraft.world.level.block.Rotation rotation =
                        net.minecraft.world.level.block.Rotation.NONE;
                if (placementTag.contains("Rotation", Tag.TAG_STRING)) {
                    try {
                        rotation =
                                net.minecraft.world.level.block.Rotation.valueOf(
                                        placementTag.getString("Rotation"));
                    } catch (IllegalArgumentException ignored) {
                        rotation = net.minecraft.world.level.block.Rotation.NONE;
                    }
                }
                activeDecorations
                        .computeIfAbsent(decorationId, ignored -> new ArrayList<>())
                        .add(new DecorationPlacement(origin, rotation));
            }
            for (Map.Entry<String, List<DecorationPlacement>> entry : new ArrayList<>(activeDecorations.entrySet())) {
                activeDecorations.put(entry.getKey(), List.copyOf(entry.getValue()));
            }
        }
    }
}
