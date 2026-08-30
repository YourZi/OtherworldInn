package com.otherworldinn.world.team;

import com.otherworldinn.init.ModSounds;
import com.otherworldinn.world.commission.TeamCommissionData;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.map.MapPoint;
import com.otherworldinn.world.map.TownDataProvider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;

/** 队伍数据：成员、解锁状态与旅社区域等。 */
@Data
public class TeamData {

    /** 全局最大旅社范围：整个大地块的边界 */
    public static final int GLOBAL_MAX_MIN_X = 30;
    public static final int GLOBAL_MAX_MIN_Z = -28;
    public static final int GLOBAL_MAX_MAX_X = 80;
    public static final int GLOBAL_MAX_MAX_Z = 27;

    /** 新队伍初始地皮，作为旅社默认区域和地契面积基准的唯一来源。 */
    public static final InnRegion DEFAULT_INN_REGION = new InnRegion(55, -15, 78, 13);

    public static int getDefaultInnArea() {
        return (DEFAULT_INN_REGION.maxX() - DEFAULT_INN_REGION.minX() + 1)
                * (DEFAULT_INN_REGION.maxZ() - DEFAULT_INN_REGION.minZ() + 1);
    }

    public record InnRegion(int minX, int minZ, int maxX, int maxZ) {

        public boolean contains(int x, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }

        public boolean contains(InnRegion other) {
            return this.minX <= other.minX
                    && this.maxX >= other.maxX
                    && this.minZ <= other.minZ
                    && this.maxZ >= other.maxZ;
        }

        public boolean intersects(InnRegion other) {
            return this.minX <= other.maxX
                    && this.maxX >= other.minX
                    && this.minZ <= other.maxZ
                    && this.maxZ >= other.minZ;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("MinX", minX);
            tag.putInt("MinZ", minZ);
            tag.putInt("MaxX", maxX);
            tag.putInt("MaxZ", maxZ);
            return tag;
        }

        public static InnRegion load(CompoundTag tag) {
            return new InnRegion(
                    tag.getInt("MinX"), tag.getInt("MinZ"), tag.getInt("MaxX"), tag.getInt("MaxZ"));
        }

        public boolean contains(BlockPos pos) {
            return contains(pos.getX(), pos.getZ());
        }
    }

    private final UUID teamId;
    private String name;
    private UUID leaderId;
    private final Set<UUID> members = new HashSet<>();

    @Setter(AccessLevel.NONE)
    private final Set<ResourceLocation> unlockedMapPoints = new HashSet<>();

    private boolean teleportUnlocked = false;

    @Setter(AccessLevel.NONE)
    private int coins = 0;

    private final List<InnRegion> innRegions = new ArrayList<>();

    private final InnData innData = new InnData();
    private final TeamCommissionData commissionData = new TeamCommissionData();
    private final com.otherworldinn.world.quest.TeamQuestData questData =
            new com.otherworldinn.world.quest.TeamQuestData();

    public com.otherworldinn.world.quest.TeamQuestData getQuestData() {
        return questData;
    }

    public TeamData(UUID teamId) {
        this.teamId = teamId;
        this.name = "Team-" + teamId.toString().substring(0, 8);
        addRegion(DEFAULT_INN_REGION);
    }

    public void addMember(UUID playerId) {
        members.add(playerId);
        if (leaderId == null) {
            leaderId = playerId;
        }
    }

    public void removeMember(UUID playerId) {
        members.remove(playerId);
        if (playerId.equals(leaderId) && !members.isEmpty()) {
            leaderId = members.iterator().next();
        } else if (members.isEmpty()) {
            leaderId = null;
        }
    }

    public boolean hasMember(UUID playerId) {
        return members.contains(playerId);
    }

    // --- 解锁状态 ---

    public boolean isMapPointUnlocked(ResourceLocation pointId) {
        if (unlockedMapPoints.contains(pointId)) {
            return true;
        }

        Optional<MapPoint> pointOpt = TownDataProvider.getPoint(pointId);
        if (pointOpt.isPresent()) {
            MapPoint point = pointOpt.get();
            if (point.unlockCondition() == null) {
                return true;
            }
        }

        // 3. 硬编码的初始点 (作为后备)
        return pointId.getPath().equals("inn");
    }

    public void unlockMapPoint(ResourceLocation pointId) {
        unlockedMapPoints.add(pointId);
    }

    public void lockMapPoint(ResourceLocation pointId) {
        unlockedMapPoints.remove(pointId);
    }

    public void setCoins(int coins) {
        if (this.coins != coins) {
            this.coins = Math.max(0, coins);
        }
    }

    public void setCoins(int coins, net.minecraft.server.MinecraftServer server) {
        if (this.coins != coins) {
            this.coins = Math.max(0, coins);
            playPaymentSound(server);
        }
    }

    public void addCoins(int amount, net.minecraft.server.MinecraftServer server) {
        if (amount > 0) {
            this.coins += amount;
            playPaymentSound(server);
        }
    }

    // 保留旧方法以兼容，但不播放声音
    public void addCoins(int amount) {
        if (amount > 0) {
            this.coins += amount;
        }
    }

    public boolean removeCoins(int amount, net.minecraft.server.MinecraftServer server) {
        if (amount >= 0 && this.coins >= amount) {
            this.coins -= amount;
            playPaymentSound(server);
            return true;
        }
        return false;
    }

    // 保留旧方法以兼容
    public boolean removeCoins(int amount) {
        if (amount >= 0 && this.coins >= amount) {
            this.coins -= amount;
            return true;
        }
        return false;
    }

    private void playPaymentSound(net.minecraft.server.MinecraftServer server) {
        if (server == null) return;

        for (UUID memberId : members) {
            ServerPlayer player = server.getPlayerList().getPlayer(memberId);
            if (player != null) {
                player.playNotifySound(ModSounds.PAYMENT.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
    }

    public void setUnlockedMapPoints(Set<ResourceLocation> points) {
        this.unlockedMapPoints.clear();
        this.unlockedMapPoints.addAll(points);
    }

    public void setMembers(Set<UUID> newMembers) {
        this.members.clear();
        this.members.addAll(newMembers);
    }

    public void addRegion(InnRegion region) {
        int minX = Math.min(region.minX, region.maxX);
        int maxX = Math.max(region.minX, region.maxX);
        int minZ = Math.min(region.minZ, region.maxZ);
        int maxZ = Math.max(region.minZ, region.maxZ);

        InnRegion normalized = new InnRegion(minX, minZ, maxX, maxZ);

        List<InnRegion> toAdd = new ArrayList<>();
        toAdd.add(normalized);

        // 用现有区域切割新区域，确保互不重叠（避免渲染时颜色叠加加深）
        for (InnRegion existing : innRegions) {
            List<InnRegion> nextPass = new ArrayList<>();
            for (InnRegion candidate : toAdd) {
                nextPass.addAll(subtract(candidate, existing));
            }
            toAdd = nextPass;
            if (toAdd.isEmpty()) break;
        }

        if (!toAdd.isEmpty()) {
            innRegions.addAll(toAdd);
            optimizeRegions();
        }
    }

    /** 计算区域差集 (A - B) 返回一组互不重叠的矩形，其并集等于 (A - B) */
    private List<InnRegion> subtract(InnRegion a, InnRegion b) {
        List<InnRegion> result = new ArrayList<>();

        if (!a.intersects(b)) {
            result.add(a);
            return result;
        }

        if (b.contains(a)) {
            return result;
        }

        // 切割策略：上下左右四个方向

        int ax1 = a.minX, ax2 = a.maxX, az1 = a.minZ, az2 = a.maxZ;
        int bx1 = b.minX, bx2 = b.maxX, bz1 = b.minZ, bz2 = b.maxZ;

        // 1. Top (Z < bz1)
        if (az1 < bz1) {
            result.add(new InnRegion(ax1, az1, ax2, bz1 - 1));
            az1 = bz1;
        }

        // 2. Bottom (Z > bz2)
        if (az2 > bz2) {
            result.add(new InnRegion(ax1, bz2 + 1, ax2, az2));
            az2 = bz2;
        }

        // 现在 Z 范围已经被限制在 B 的 Z 范围内 (或 A 原本的 Z 范围内)

        // 3. Left (X < bx1)
        if (ax1 < bx1) {
            result.add(new InnRegion(ax1, az1, bx1 - 1, az2));
        }

        // 4. Right (X > bx2)
        if (ax2 > bx2) {
            result.add(new InnRegion(bx2 + 1, az1, ax2, az2));
        }

        return result;
    }

    public void removeRegion(InnRegion region) {
        int minX = Math.min(region.minX, region.maxX);
        int maxX = Math.max(region.minX, region.maxX);
        int minZ = Math.min(region.minZ, region.maxZ);
        int maxZ = Math.max(region.minZ, region.maxZ);

        InnRegion normalized = new InnRegion(minX, minZ, maxX, maxZ);

        List<InnRegion> nextRegions = new ArrayList<>();

        for (InnRegion existing : innRegions) {
            nextRegions.addAll(subtract(existing, normalized));
        }

        innRegions.clear();
        innRegions.addAll(nextRegions);
        optimizeRegions();
    }

    /** 优化区域列表，合并可合并的矩形 */
    public void optimizeRegions() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < innRegions.size(); i++) {
                for (int j = i + 1; j < innRegions.size(); j++) {
                    InnRegion r1 = innRegions.get(i);
                    InnRegion r2 = innRegions.get(j);
                    InnRegion merged = tryMerge(r1, r2);
                    if (merged != null) {
                        innRegions.remove(j); // 先移除后面的索引
                        innRegions.remove(i);
                        innRegions.add(merged);
                        changed = true;
                        break;
                    }
                }
                if (changed) break;
            }
        }
    }

    private InnRegion tryMerge(InnRegion r1, InnRegion r2) {
        if (r1.contains(r2)) return r1;
        if (r2.contains(r1)) return r2;

        if (r1.minZ == r2.minZ && r1.maxZ == r2.maxZ) {
            if (r1.minX <= r2.maxX + 1 && r2.minX <= r1.maxX + 1) {
                return new InnRegion(
                        Math.min(r1.minX, r2.minX), r1.minZ, Math.max(r1.maxX, r2.maxX), r1.maxZ);
            }
        }

        if (r1.minX == r2.minX && r1.maxX == r2.maxX) {
            if (r1.minZ <= r2.maxZ + 1 && r2.minZ <= r1.maxZ + 1) {
                return new InnRegion(
                        r1.minX, Math.min(r1.minZ, r2.minZ), r1.maxX, Math.max(r1.maxZ, r2.maxZ));
            }
        }

        return null;
    }

    /** 检查坐标是否在旅社区域内 */
    public boolean isInInnZone(BlockPos pos) {
        for (InnRegion region : innRegions) {
            if (region.contains(pos.getX(), pos.getZ())) {
                return true;
            }
        }
        return false;
    }

    /** 检查一个水平矩形区域是否被当前已购买地皮完整覆盖。 */
    public boolean isAreaInInnZone(BlockPos minPos, BlockPos maxPos) {
        if (minPos == null || maxPos == null) {
            return false;
        }
        return isAreaInInnZone(minPos.getX(), minPos.getZ(), maxPos.getX(), maxPos.getZ());
    }

    /** 检查一个水平矩形区域是否被当前已购买地皮完整覆盖。 */
    public boolean isAreaInInnZone(int minX, int minZ, int maxX, int maxZ) {
        if (innRegions.isEmpty()) {
            return false;
        }

        InnRegion target =
                new InnRegion(
                        Math.min(minX, maxX),
                        Math.min(minZ, maxZ),
                        Math.max(minX, maxX),
                        Math.max(minZ, maxZ));
        List<InnRegion> uncovered = new ArrayList<>();
        uncovered.add(target);

        for (InnRegion region : innRegions) {
            List<InnRegion> nextPass = new ArrayList<>();
            for (InnRegion candidate : uncovered) {
                nextPass.addAll(subtract(candidate, region));
            }
            if (nextPass.isEmpty()) {
                return true;
            }
            uncovered = nextPass;
        }

        return uncovered.isEmpty();
    }

    /** 使用硬编码的大地块最大范围判断（不依赖已购地皮），用于建造保护等场景。 */
    public static boolean isInGlobalMaxInnZone(BlockPos pos) {
        int x = pos.getX();
        int z = pos.getZ();
        return x >= GLOBAL_MAX_MIN_X && x <= GLOBAL_MAX_MAX_X
                && z >= GLOBAL_MAX_MIN_Z && z <= GLOBAL_MAX_MAX_Z;
    }

    // --- NBT 序列化 ---

    public CompoundTag save(CompoundTag tag) {
        tag.putUUID("TeamId", teamId);
        tag.putString("Name", name != null ? name : "");
        if (leaderId != null) {
            tag.putUUID("LeaderId", leaderId);
        }

        ListTag membersTag = new ListTag();
        for (UUID member : members) {
            CompoundTag memberTag = new CompoundTag();
            memberTag.putUUID("UUID", member);
            membersTag.add(memberTag);
        }
        tag.put("Members", membersTag);

        ListTag pointsTag = new ListTag();
        for (ResourceLocation point : unlockedMapPoints) {
            pointsTag.add(StringTag.valueOf(point.toString()));
        }
        tag.put("UnlockedPoints", pointsTag);

        tag.putBoolean("TeleportUnlocked", teleportUnlocked);
        tag.putInt("Coins", coins);

        tag.put("InnData", innData.save(new CompoundTag()));
        tag.put("CommissionData", commissionData.save());
        tag.put("QuestData", questData.save());

        ListTag regionsTag = new ListTag();
        for (InnRegion region : innRegions) {
            CompoundTag regionTag = region.save();
            regionsTag.add(regionTag);
        }
        tag.put("InnRegions", regionsTag);

        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("TeamId")) {}

        if (tag.contains("Name")) {
            name = tag.getString("Name");
        }
        if (tag.contains("LeaderId")) {
            leaderId = tag.getUUID("LeaderId");
        }

        members.clear();
        if (tag.contains("Members")) {
            ListTag membersTag = tag.getList("Members", Tag.TAG_COMPOUND);
            for (Tag t : membersTag) {
                if (t instanceof CompoundTag memberTag) {
                    members.add(memberTag.getUUID("UUID"));
                }
            }
        }

        unlockedMapPoints.clear();
        if (tag.contains("UnlockedPoints")) {
            ListTag pointsTag = tag.getList("UnlockedPoints", Tag.TAG_STRING);
            for (Tag t : pointsTag) {
                unlockedMapPoints.add(ResourceLocation.parse(t.getAsString()));
            }
        }

        teleportUnlocked = tag.getBoolean("TeleportUnlocked");
        if (tag.contains("Coins")) {
            coins = tag.getInt("Coins");
        } else {
            coins = 0;
        }

        // 优先加载 InnData，因为后续可能需要用到它
        if (tag.contains("InnData")) {
            innData.load(tag.getCompound("InnData"));
        }
        if (tag.contains("CommissionData")) {
            commissionData.load(tag.getCompound("CommissionData"));
        }
        if (tag.contains("QuestData")) {
            questData.load(tag.getCompound("QuestData"));
        }

        innRegions.clear();
        if (tag.contains("InnRegions")) {
            ListTag regionsTag = tag.getList("InnRegions", Tag.TAG_COMPOUND);
            for (Tag t : regionsTag) {
                if (t instanceof CompoundTag regionTag) {
                    innRegions.add(InnRegion.load(regionTag));
                }
            }
            optimizeRegions();
        }
    }
}
