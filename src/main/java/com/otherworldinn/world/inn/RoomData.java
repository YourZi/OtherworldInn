package com.otherworldinn.world.inn;

import com.otherworldinn.world.team.TeamData;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * 房间数据
 *
 * <p>存储旅社中单个房间的信息，包括空间范围、编号和属性。
 */
@Data
public class RoomData {
    private final int id;
    private final UUID uuid; // 房间唯一标识符
    private final BlockPos minPos;
    private final BlockPos maxPos;

    // 房间属性 (0-100)
    @Setter(AccessLevel.NONE)
    private int comfort;

    @Setter(AccessLevel.NONE)
    private int light;

    @Setter(AccessLevel.NONE)
    private int humidity;

    // 房间整洁度 (0-100)
    // 当前按床位整洁度计算：(干净床位 / 总床位) * 100
    @Setter(AccessLevel.NONE)
    private int cleanliness = 100;

    // 旅客信息
    @Setter(AccessLevel.NONE)
    private int maxGuests = 1;

    @Setter(AccessLevel.NONE)
    private int totalBeds = 0;

    @Setter(AccessLevel.NONE)
    private String theme = "";

    @Setter(AccessLevel.NONE)
    private String name = "";

    private final Set<UUID> currentGuests = new HashSet<>();

    /**
     * 创建一个新的房间数据
     *
     * @param id 房间编号
     * @param minPos 最小坐标
     * @param maxPos 最大坐标
     */
    public RoomData(int id, BlockPos minPos, BlockPos maxPos) {
        this.id = id;
        this.uuid = UUID.randomUUID();
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.comfort = 0;
        this.light = 0;
        this.humidity = 0;
        this.cleanliness = 100;
    }

    /** 内部构造函数 (用于加载) */
    private RoomData(int id, UUID uuid, BlockPos minPos, BlockPos maxPos) {
        this.id = id;
        this.uuid = uuid;
        this.minPos = minPos;
        this.maxPos = maxPos;
        this.comfort = 0;
        this.light = 0;
        this.humidity = 0;
        this.cleanliness = 100;
    }

    public void setComfort(int comfort) {
        this.comfort = Math.max(0, Math.min(100, comfort));
    }

    public void setLight(int light) {
        this.light = Math.max(0, Math.min(100, light));
    }

    public void setHumidity(int humidity) {
        this.humidity = Math.max(0, Math.min(100, humidity));
    }

    public void setCleanliness(int cleanliness) {
        this.cleanliness = Math.max(0, Math.min(100, cleanliness));
    }

    public void setMaxGuests(int maxGuests) {
        this.maxGuests = Math.max(0, maxGuests);
    }

    public void setTotalBeds(int totalBeds) {
        this.totalBeds = Math.max(0, totalBeds);
    }

    public void setTheme(String theme) {
        this.theme = theme == null ? "" : theme;
    }

    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    /**
     * 获取房间的显示名称。如果有自定义名称则返回该名称，否则返回"X号房间"。
     *
     * @param room 房间数据（可为 null，返回 "?"）
     * @return 房间显示名称
     */
    public static String getDisplayName(RoomData room) {
        if (room == null) return "?";
        if (room.name != null && !room.name.isEmpty()) return room.name;
        return room.id + "号房间";
    }

    public boolean addGuest(UUID guestId) {
        if (currentGuests.size() < maxGuests) {
            return currentGuests.add(guestId);
        }
        return false;
    }

    public void removeGuest(UUID guestId) {
        currentGuests.remove(guestId);
    }

    public boolean hasGuest(UUID guestId) {
        return currentGuests.contains(guestId);
    }

    /**
     * 计算床位价格
     *
     * <p>先计算“单床基准价”（根据旅社星级和房间水平面积，最低 8 金币，最高 96 金币，权重：星级 60%，面积 40%）。
     * 然后按床位数做分段衰减：
     *
     * <p>1) 1~4 床：单床价线性下降，4 床时降到单床基准价的 1/3。 2) 5~6 床：单床价快速线性下降，6 床时降到 1。 3) 大于 6 床：单床价固定为 1。
     *
     * @param innRating 旅社评级 (0-5)
     * @return 单个床位的价格
     */
    public int getBedPrice(int innRating) {
        int area = (maxPos.getX() - minPos.getX() + 1) * (maxPos.getZ() - minPos.getZ() + 1);
        int maxEffectiveArea = 48; // 约 7x7 大小作为满分面积基准

        double ratingFactor = Math.max(0, Math.min(5, innRating)) / 5.0;
        double areaFactor = Math.min((double) area, maxEffectiveArea) / maxEffectiveArea;

        // 权重分配：星级 60%，面积 40%
        double score = ratingFactor * 0.6 + areaFactor * 0.4;

        int minPrice = 8;
        int maxPrice = 96;
        int singleBedBasePrice = minPrice + (int) Math.round(score * (maxPrice - minPrice));

        int beds = Math.max(1, this.totalBeds);

        if (beds <= 1) {
            return singleBedBasePrice;
        }

        if (beds <= 4) {
            // 1 床 -> 1.0, 4 床 -> 1/3（线性）
            double t = (beds - 1) / 3.0D;
            double multiplier = 1.0D - (2.0D / 3.0D) * t;
            return Math.max(1, (int) Math.round(singleBedBasePrice * multiplier));
        }

        if (beds <= 6) {
            // 4 床价格作为起点，6 床线性降到 1
            int priceAtFourBeds = Math.max(1, (int) Math.round(singleBedBasePrice / 3.0D));
            double t = (beds - 4) / 2.0D; // 5 床:0.5, 6 床:1.0
            double price = priceAtFourBeds + (1.0D - priceAtFourBeds) * t;
            return Math.max(1, (int) Math.round(price));
        }

        return 1;
    }

    public enum ValidationResult {
        SUCCESS("success"),
        TOO_SMALL("too_small"),
        OUT_OF_BOUNDS("out_of_bounds"),
        HOLE_IN_FLOOR("hole_in_floor"),
        HOLE_IN_CEILING("hole_in_ceiling"),
        HOLE_IN_WALL("hole_in_wall"),
        MISSING_DOOR("missing_door"),
        MISSING_BED("missing_bed"),
        OVERLAP("overlap"),
        TOO_CROWDED("too_crowded");

        private final String translationKeySuffix;

        ValidationResult(String translationKeySuffix) {
            this.translationKeySuffix = translationKeySuffix;
        }

        public String getTranslationKey() {
            return "message.otherworldinn.room_register.validation." + translationKeySuffix;
        }

        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }

    // --- 静态验证方法 ---
    /** 判定区域是否能作为房间。 详情见 {@link #validate(BlockPos, BlockPos, Level, TeamData, Integer)} */
    public static ValidationResult validate(
            BlockPos minPos, BlockPos maxPos, Level level, TeamData team) {
        return validate(minPos, maxPos, level, team, null);
    }

    /**
     * 统计床位数量，并计算整洁度。
     *
     * <p>只有干净的床位 (MESSY=false) 计入有效床位。 整洁度 = (干净床位 / 总床位) * 100 如果没有床，整洁度默认为 100。
     *
     * @return [有效床位数, 整洁度, 物理床位总数]
     */
    public static int[] calculateBedStats(BlockPos minPos, BlockPos maxPos, Level level) {
        int cleanBedCount = 0;
        int totalBedCount = 0;

        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.BEDS)) {
                // 只统计床头，避免重复
                if (state.hasProperty(BedBlock.PART)
                        && state.getValue(BedBlock.PART) == BedPart.HEAD) {

                    totalBedCount++;

                    // 检查是否脏乱
                    boolean isMessy = false;
                    Property<?> messyProp =
                            state.getProperties().stream()
                                    .filter(p -> p.getName().equals("messy"))
                                    .findFirst()
                                    .orElse(null);

                    if (messyProp != null && messyProp instanceof BooleanProperty boolProp) {
                        if (state.getValue(boolProp)) {
                            isMessy = true;
                        }
                    }

                    if (!isMessy) {
                        cleanBedCount++;
                    }
                }
            }
        }

        int cleanliness =
                totalBedCount > 0 ? (int) ((float) cleanBedCount / totalBedCount * 100) : 100;
        return new int[] {cleanBedCount, cleanliness, totalBedCount};
    }

    /** 统计区域内床头数量（包含干净床和脏乱床） */
    public static int countAllBeds(BlockPos minPos, BlockPos maxPos, Level level) {
        int bedCount = 0;
        for (BlockPos pos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.BEDS)
                    && state.hasProperty(BedBlock.PART)
                    && state.getValue(BedBlock.PART) == BedPart.HEAD) {
                bedCount++;
            }
        }
        return bedCount;
    }

    /** 仅统计有效床位数量（向后兼容） */
    public static int countBeds(BlockPos minPos, BlockPos maxPos, Level level) {
        return calculateBedStats(minPos, maxPos, level)[0];
    }

    /**
     * 判定区域是否能作为房间
     *
     * <p>检查逻辑： 1. 房间的整个水平投影必须在旅社区域内。 2. 底面（最低点再低一格）所有方块必须有完整的、可站立的上表面。 3.
     * 顶面（最高点再高一格）所有方块不能为无碰撞体积的方块。 4. 侧面（四个侧面）外层方块中，至少 3/4 的方块必须有碰撞体积。 5. 所有侧面的最外层方块中，至少包含一扇门。 6.
     * 房间内部必须至少包含一张床。 7. 房间不能与已有房间重叠。 8. 房间内必须至少包含一个 2x2x2 的无碰撞箱空间。
     *
     * @param minPos 房间最小坐标
     * @param maxPos 房间最大坐标
     * @param level 世界实例
     * @param team 所属队伍（用于检查区域范围）
     * @param ignoreRoomId 需要忽略的房间ID（用于自身检查时避免重叠误判），可为 null
     */
    public static ValidationResult validate(
            BlockPos minPos, BlockPos maxPos, Level level, TeamData team, Integer ignoreRoomId) {
        // 0. 检查是否在旅社区域内
        if (team != null) {
            // 检查房间整个水平投影是否都在旅社区域内
            if (!team.isAreaInInnZone(minPos, maxPos)) {
                return ValidationResult.OUT_OF_BOUNDS;
            }

            // 0.1 检查是否与现有房间重叠
            InnData innData = team.getInnData();
            for (RoomData existingRoom : innData.getRooms().values()) {
                // 如果是自身检查，跳过
                if (ignoreRoomId != null && existingRoom.getId() == ignoreRoomId) {
                    continue;
                }

                // 简单的 AABB 重叠检查
                if (Math.max(minPos.getX(), existingRoom.getMinPos().getX())
                                <= Math.min(maxPos.getX(), existingRoom.getMaxPos().getX())
                        && Math.max(minPos.getY(), existingRoom.getMinPos().getY())
                                <= Math.min(maxPos.getY(), existingRoom.getMaxPos().getY())
                        && Math.max(minPos.getZ(), existingRoom.getMinPos().getZ())
                                <= Math.min(maxPos.getZ(), existingRoom.getMaxPos().getZ())) {
                    return ValidationResult.OVERLAP;
                }
            }
        }

        int minX = minPos.getX();
        int minY = minPos.getY();
        int minZ = minPos.getZ();
        int maxX = maxPos.getX();
        int maxY = maxPos.getY();
        int maxZ = maxPos.getZ();

        // 检查房间大小 (至少 3x3x3)
        if ((maxX - minX + 1) < 3 || (maxY - minY + 1) < 3 || (maxZ - minZ + 1) < 3) {
            return ValidationResult.TOO_SMALL;
        }

        // 1. 检查底面 (minY - 1)
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos floorPos = new BlockPos(x, minY - 1, z);
                BlockState state = level.getBlockState(floorPos);
                // 检查是否有完整上表面支持站立 (isFaceSturdy with UP)
                if (!state.isFaceSturdy(level, floorPos, Direction.UP)) {
                    return ValidationResult.HOLE_IN_FLOOR;
                }
            }
        }

        // 2. 检查顶面 (maxY + 1)
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                BlockPos ceilingPos = new BlockPos(x, maxY + 1, z);
                BlockState state = level.getBlockState(ceilingPos);
                // 检查是否有碰撞体积 (getCollisionShape not empty)
                if (state.getCollisionShape(level, ceilingPos).isEmpty()) {
                    return ValidationResult.HOLE_IN_CEILING;
                }
            }
        }

        // 3. 检查侧面
        boolean hasDoor = false;

        int northTotal = 0, northSolid = 0;
        int southTotal = 0, southSolid = 0;
        int westTotal = 0, westSolid = 0;
        int eastTotal = 0, eastSolid = 0;

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        // 遍历墙壁
        for (int y = minY; y <= maxY; y++) {
            // 北墙 (minZ - 1) 和 南墙 (maxZ + 1)
            for (int x = minX; x <= maxX; x++) {
                mPos.set(x, y, minZ - 1);
                if (checkWallBlock(level, mPos)) northSolid++;
                if (isDoor(level, mPos)) hasDoor = true;
                northTotal++;

                mPos.set(x, y, maxZ + 1);
                if (checkWallBlock(level, mPos)) southSolid++;
                if (isDoor(level, mPos)) hasDoor = true;
                southTotal++;
            }

            // 西墙 (minX - 1) 和 东墙 (maxX + 1)
            for (int z = minZ; z <= maxZ; z++) {
                mPos.set(minX - 1, y, z);
                if (checkWallBlock(level, mPos)) westSolid++;
                if (isDoor(level, mPos)) hasDoor = true;
                westTotal++;

                mPos.set(maxX + 1, y, z);
                if (checkWallBlock(level, mPos)) eastSolid++;
                if (isDoor(level, mPos)) hasDoor = true;
                eastTotal++;
            }
        }

        if ((double) northSolid / northTotal < 0.75) return ValidationResult.HOLE_IN_WALL;
        if ((double) southSolid / southTotal < 0.75) return ValidationResult.HOLE_IN_WALL;
        if ((double) westSolid / westTotal < 0.75) return ValidationResult.HOLE_IN_WALL;
        if ((double) eastSolid / eastTotal < 0.75) return ValidationResult.HOLE_IN_WALL;

        // 必须包含至少一扇门
        if (!hasDoor) {
            return ValidationResult.MISSING_DOOR;
        }

        // 4. 内部检查：统计床位数量 + 检查是否存在 2x2x2 的空闲空间
        // 合并遍历以优化性能
        int bedCount = 0;
        boolean hasSpace = false;

        // 如果当前点是空的，尝试以此为起点的 2x2x2。
        // 为了减少重复检查，只有当 hasSpace 为 false 时才进行检查。

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mPos.set(x, y, z);
                    BlockState state = level.getBlockState(mPos);

                    // 统计床位
                    if (state.is(BlockTags.BEDS)) {
                        if (state.hasProperty(BedBlock.PART)
                                && state.getValue(BedBlock.PART) == BedPart.HEAD) {

                            // 检查是否脏乱
                            boolean isMessy = false;
                            net.minecraft.world.level.block.state.properties.Property<?> messyProp =
                                    state.getProperties().stream()
                                            .filter(p -> p.getName().equals("messy"))
                                            .findFirst()
                                            .orElse(null);

                            if (messyProp != null
                                    && messyProp
                                            instanceof
                                            net.minecraft.world.level.block.state.properties
                                                                    .BooleanProperty
                                                            boolProp) {
                                if (state.getValue(boolProp)) {
                                    isMessy = true;
                                }
                            }

                            if (!isMessy) {
                                bedCount++;
                            }
                        }
                    }

                    // 检查 2x2x2 空间 (如果尚未找到)
                    // 只有当 x, y, z 都在允许作为 2x2x2 起点的范围内时才检查
                    // 优化：2x2x2 空间必须位于最低处 (y == minY)
                    if (!hasSpace && y == minY && x < maxX && z < maxZ) {
                        // 快速预检：如果当前方块有碰撞箱，则以其为起点的 2x2x2 肯定无效
                        if (state.getCollisionShape(level, mPos).isEmpty()) {
                            if (check2x2x2Space(level, x, y, z)) {
                                hasSpace = true;
                            }
                        }
                    }
                }
            }
        }

        if (bedCount == 0) {
            return ValidationResult.MISSING_BED;
        }

        if (!hasSpace) {
            return ValidationResult.TOO_CROWDED;
        }

        return ValidationResult.SUCCESS;
    }

    /** 检查指定坐标为起点的 2x2x2 区域是否无碰撞箱 */
    private static boolean check2x2x2Space(Level level, int startX, int startY, int startZ) {
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        for (int dx = 0; dx <= 1; dx++) {
            for (int dy = 0; dy <= 1; dy++) {
                for (int dz = 0; dz <= 1; dz++) {
                    // 起点已经检查过了，可以跳过 (dx=0, dy=0, dz=0)
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    mPos.set(startX + dx, startY + dy, startZ + dz);
                    if (!level.getBlockState(mPos).getCollisionShape(level, mPos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // --- NBT 序列化 ---

    private static boolean checkWallBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // 有碰撞体积 或 是门 (门通常有碰撞体积，但打开时可能变化，这里视为有效墙体的一部分)
        return !state.getCollisionShape(level, pos).isEmpty() || isDoor(level, pos);
    }

    private static boolean isDoor(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof DoorBlock || state.is(BlockTags.DOORS);
    }

    /**
     * 将房间数据保存到 NBT
     *
     * @param tag 目标 NBT 标签
     * @return 包含数据的 NBT 标签
     */
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("Id", id);
        tag.putUUID("UUID", uuid);
        tag.putLong("MinPos", minPos.asLong());
        tag.putLong("MaxPos", maxPos.asLong());
        tag.putInt("Comfort", comfort);
        tag.putInt("Light", light);
        tag.putInt("Humidity", humidity);
        tag.putInt("Cleanliness", cleanliness);

        tag.putInt("MaxGuests", maxGuests);
        tag.putInt("TotalBeds", totalBeds);
        tag.putString("Theme", theme);
        if (name != null && !name.isEmpty()) {
            tag.putString("Name", name);
        }
        ListTag guestsTag = new ListTag();
        for (UUID uuid : currentGuests) {
            CompoundTag guestTag = new CompoundTag();
            guestTag.putUUID("UUID", uuid);
            guestsTag.add(guestTag);
        }
        tag.put("CurrentGuests", guestsTag);

        return tag;
    }

    /**
     * 从 NBT 加载房间数据
     *
     * @param tag 源 NBT 标签
     * @return 加载的房间数据
     */
    public static RoomData load(CompoundTag tag) {
        int id = tag.getInt("Id");
        UUID uuid = tag.contains("UUID") ? tag.getUUID("UUID") : UUID.randomUUID(); // 兼容旧数据
        BlockPos minPos = BlockPos.of(tag.getLong("MinPos"));
        BlockPos maxPos = BlockPos.of(tag.getLong("MaxPos"));

        RoomData room = new RoomData(id, uuid, minPos, maxPos);

        if (tag.contains("Comfort")) {
            room.setComfort(tag.getInt("Comfort"));
        }
        if (tag.contains("Light")) {
            room.setLight(tag.getInt("Light"));
        }
        if (tag.contains("Humidity")) {
            room.setHumidity(tag.getInt("Humidity"));
        }
        if (tag.contains("Cleanliness")) {
            room.setCleanliness(tag.getInt("Cleanliness"));
        }

        if (tag.contains("MaxGuests")) {
            room.setMaxGuests(tag.getInt("MaxGuests"));
        }

        if (tag.contains("TotalBeds")) {
            room.setTotalBeds(tag.getInt("TotalBeds"));
        }

        if (tag.contains("Theme")) {
            room.setTheme(tag.getString("Theme"));
        }

        if (tag.contains("Name")) {
            room.setName(tag.getString("Name"));
        }

        if (tag.contains("CurrentGuests")) {
            ListTag guestsTag = tag.getList("CurrentGuests", Tag.TAG_COMPOUND);
            for (Tag t : guestsTag) {
                if (t instanceof CompoundTag guestTag) {
                    room.currentGuests.add(guestTag.getUUID("UUID"));
                }
            }
        }

        return room;
    }
}
