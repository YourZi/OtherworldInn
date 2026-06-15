package com.otherworldinn.world.team.service;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CTeamSyncPacket;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.TeamSavedData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 队伍管理器
 *
 * <p>管理所有活跃的队伍数据。 在服务端，这是全局单例。 在客户端，这应该同步当前玩家的队伍数据。
 */
public class TeamManager {

    private static final TeamManager INSTANCE = new TeamManager();
    private final Map<Long, List<TeamData>> teamChunkIndex = new HashMap<>();
    private final Map<MinecraftServer, Map<UUID, TeamData>> pendingTeamSyncs = new HashMap<>();
    private final Map<UUID, CompoundTag> lastSyncedTeamState = new HashMap<>();
    private MinecraftServer indexedServer;
    private MinecraftServer syncStateServer;
    private boolean teamChunkIndexDirty = true;

    public static TeamManager getInstance() {
        return INSTANCE;
    }

    public TeamSavedData getData(MinecraftServer server) {
        return TeamSavedData.get(server.overworld());
    }

    private static long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xffffffffL);
    }

    private void invalidateTeamChunkIndex() {
        teamChunkIndexDirty = true;
    }

    private void ensureTeamChunkIndex(MinecraftServer server) {
        if (indexedServer != server) {
            indexedServer = server;
            teamChunkIndexDirty = true;
        }
        if (!teamChunkIndexDirty) {
            return;
        }
        teamChunkIndex.clear();
        TeamSavedData data = getData(server);
        for (TeamData team : data.getTeams().values()) {
            for (TeamData.InnRegion region : team.getInnRegions()) {
                int minChunkX = region.minX() >> 4;
                int maxChunkX = region.maxX() >> 4;
                int minChunkZ = region.minZ() >> 4;
                int maxChunkZ = region.maxZ() >> 4;
                for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                    for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                        long key = chunkKey(chunkX, chunkZ);
                        List<TeamData> teamsInChunk =
                                teamChunkIndex.computeIfAbsent(key, ignored -> new ArrayList<>());
                        if (!teamsInChunk.contains(team)) {
                            teamsInChunk.add(team);
                        }
                    }
                }
            }
        }
        teamChunkIndexDirty = false;
    }

    private void ensureSyncStateServer(MinecraftServer server) {
        if (syncStateServer != server) {
            syncStateServer = server;
            lastSyncedTeamState.clear();
        }
    }

    private void enqueueTeamSync(TeamData team, MinecraftServer server) {
        if (server == null || team == null) return;
        pendingTeamSyncs.computeIfAbsent(server, ignored -> new HashMap<>()).put(team.getTeamId(), team);
    }

    private void flushPendingTeamSyncs(MinecraftServer server) {
        if (server == null) return;
        Map<UUID, TeamData> pending = pendingTeamSyncs.remove(server);
        if (pending == null || pending.isEmpty()) return;

        ensureSyncStateServer(server);
        for (TeamData team : pending.values()) {
            sendTeamIfChanged(team, server);
        }
    }

    private void sendTeamIfChanged(TeamData team, MinecraftServer server) {
        CompoundTag currentState = team.save(new CompoundTag());
        CompoundTag lastState = lastSyncedTeamState.get(team.getTeamId());
        if (lastState != null && lastState.equals(currentState)) {
            return;
        }
        lastSyncedTeamState.put(team.getTeamId(), currentState.copy());

        S2CTeamSyncPacket packet = createSyncPacket(team);
        for (UUID memberId : team.getMembers()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                ModMessages.sendToPlayer(packet, member);
            }
        }
    }

    /**
     * 获取玩家所在的队伍
     *
     * <p>如果玩家没有队伍，返回 null。 客户端逻辑会返回本地缓存的队伍数据。
     *
     * @param player 目标玩家
     * @return 队伍数据或 null
     */
    public TeamData getPlayerTeam(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            TeamSavedData data = getData(serverPlayer.getServer());
            UUID teamId = data.getPlayerToTeam().get(player.getUUID());
            if (teamId != null) {
                return data.getTeams().get(teamId);
            }
            return null;
        } else {
            // 客户端逻辑
            return getClientPlayerTeam();
        }
    }

    /**
     * 处理玩家加入世界
     *
     * <p>无论单人多人，如果这是该服务器的第一个玩家，则自动建队。 后续加入的玩家默认加入已存在的第一个队伍。
     *
     * @param player 加入的玩家
     * @param server 服务器实例
     */
    public void onPlayerJoin(Player player, MinecraftServer server) {
        UUID playerId = player.getUUID();

        // 尝试获取现有队伍
        TeamData existingTeam = getPlayerTeam(player);
        if (existingTeam != null) {
            // 如果已经在队伍中（可能是重连），同步数据
            if (player instanceof ServerPlayer serverPlayer) {
                syncTeamTeleport(existingTeam, serverPlayer);
            }
            return;
        }

        TeamSavedData data = getData(server);
        if (data.getPlayerToTeam().containsKey(playerId)) return;

        TeamData joinedTeam = null;

        // 只要是第一个玩家，就自动创建队伍
        if (server.getPlayerList().getPlayerCount() <= 1 && data.getTeams().isEmpty()) {
            joinedTeam = createTeam(player, "Team-" + player.getName().getString());
        } else if (!data.getTeams().isEmpty()) {
            // 当前默认策略：后续玩家加入第一个队伍
            UUID firstTeamId = data.getTeams().keySet().iterator().next();
            if (joinTeam(player, firstTeamId)) {
                joinedTeam = data.getTeams().get(firstTeamId);
            }
        }

        // 如果成功加入或创建队伍，同步数据
        if (joinedTeam != null && player instanceof ServerPlayer serverPlayer) {
            syncTeamTeleport(joinedTeam, serverPlayer);
        }
    }

    /**
     * 创建队伍
     *
     * @param player 创建者（队长）
     * @param name 队伍名称
     * @return 创建的队伍数据
     */
    public TeamData createTeam(Player player, String name) {
        UUID playerId = player.getUUID();
        // 离开旧队伍
        leaveTeam(player);

        UUID teamId = UUID.randomUUID();
        TeamData team = new TeamData(teamId);
        team.setName(name);
        team.addMember(playerId);
        team.setLeaderId(playerId);

        // 初始解锁
        team.unlockMapPoint(ResourceLocation.parse("otherworldinn:inn"));

        if (player instanceof ServerPlayer serverPlayer) {
            TeamSavedData data = getData(serverPlayer.getServer());
            data.addTeam(team);
            invalidateTeamChunkIndex();
        }

        return team;
    }

    /**
     * 加入队伍
     *
     * @param player 玩家
     * @param teamId 队伍ID
     * @return 是否成功加入
     */
    public boolean joinTeam(Player player, UUID teamId) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;

        TeamSavedData data = getData(serverPlayer.getServer());
        TeamData team = data.getTeams().get(teamId);
        if (team == null) return false;

        leaveTeam(player);

        data.addMember(teamId, player.getUUID());
        invalidateTeamChunkIndex();
        return true;
    }

    /**
     * 离开当前队伍
     *
     * @param player 玩家
     */
    public void leaveTeam(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        TeamSavedData data = getData(serverPlayer.getServer());
        UUID playerId = player.getUUID();

        UUID oldTeamId = data.getPlayerToTeam().get(playerId);
        if (oldTeamId != null) {
            data.removeMember(oldTeamId, playerId);

            TeamData oldTeam = data.getTeams().get(oldTeamId);
            if (oldTeam != null && oldTeam.getMembers().isEmpty()) {
                data.removeTeam(oldTeamId);
                lastSyncedTeamState.remove(oldTeamId);
                for (Map<UUID, TeamData> pending : pendingTeamSyncs.values()) {
                    pending.remove(oldTeamId);
                }
            }
            invalidateTeamChunkIndex();
        }
    }

    /**
     * 更新队伍传送状态并同步给所有成员
     *
     * @param team 队伍
     * @param unlocked 是否解锁
     * @param server 服务器实例
     */
    public void setTeamTeleportUnlocked(TeamData team, boolean unlocked, MinecraftServer server) {
        team.setTeleportUnlocked(unlocked);
        syncTeam(team, server);
    }

    /**
     * 同步队伍数据给所有成员
     *
     * @param team 队伍
     * @param server 服务器实例
     */
    public void syncTeam(TeamData team, MinecraftServer server) {
        getData(server).markDirty();
        invalidateTeamChunkIndex();
        enqueueTeamSync(team, server);
    }

    /**
     * 同步队伍传送状态给特定玩家
     *
     * @param team 队伍
     * @param player 目标玩家
     */
    public void syncTeamTeleport(TeamData team, ServerPlayer player) {
        S2CTeamSyncPacket packet = createSyncPacket(team);
        ModMessages.sendToPlayer(packet, player);
    }

    /**
     * 创建同步数据包
     *
     * @param team 队伍数据
     * @return 同步数据包
     */
    private S2CTeamSyncPacket createSyncPacket(TeamData team) {
        List<CompoundTag> regionTags = new ArrayList<>();
        for (TeamData.InnRegion region : team.getInnRegions()) {
            regionTags.add(region.save());
        }

        return new S2CTeamSyncPacket(
                team.getTeamId(),
                team.getName(),
                team.getLeaderId(),
                new ArrayList<>(team.getMembers()),
                new ArrayList<>(team.getUnlockedMapPoints()),
                team.isTeleportUnlocked(),
                team.getCoins(),
                team.getInnData().save(new CompoundTag()),
                regionTags,
                new ArrayList<>(team.getUnlockedCookRecipes()));
    }

    /**
     * 根据 ID 获取队伍
     *
     * @param teamId 队伍 ID
     * @param server 服务器实例
     * @return 队伍数据或 null
     */
    public TeamData getTeam(UUID teamId, MinecraftServer server) {
        if (server == null) return null;
        TeamSavedData data = getData(server);
        return data.getTeams().get(teamId);
    }

    /**
     * 获取包含指定坐标的队伍数据
     *
     * <p>检查该坐标是否位于某个队伍的旅社区域内。
     *
     * @param pos 检查的坐标
     * @param server 服务器实例
     * @return 包含该坐标的队伍，如果没有则返回 null
     */
    public TeamData getTeamAt(BlockPos pos, MinecraftServer server) {
        ensureTeamChunkIndex(server);
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        List<TeamData> teamsInChunk = teamChunkIndex.get(chunkKey(chunkX, chunkZ));
        if (teamsInChunk == null) {
            // 如果在块索引中找不到，但坐标在全局最大旅社范围内，尝试遍历所有队伍
            if (!TeamData.isInGlobalMaxInnZone(pos)) return null;
            var allTeams = getData(server).getTeams().values();
            for (TeamData team : allTeams) {
                if (TeamData.isInGlobalMaxInnZone(pos)) return team;
            }
            return null;
        }
        // 优先匹配已购买的地皮范围
        for (TeamData team : teamsInChunk) {
            if (team.isInInnZone(pos)) {
                return team;
            }
        }
        // 回退到全局最大范围匹配
        for (TeamData team : teamsInChunk) {
            if (TeamData.isInGlobalMaxInnZone(pos)) {
                return team;
            }
        }
        return null;
    }

    /**
     * 获取最近的队伍
     *
     * <p>查找距离指定坐标最近的队伍旅社（基于其第一个旅社区域的中心点）。
     *
     * @param pos 参考坐标
     * @param server 服务器实例
     * @return 最近的队伍数据或 null
     */
    public TeamData getNearestInn(BlockPos pos, MinecraftServer server) {
        TeamSavedData data = getData(server);
        TeamData nearestTeam = null;
        double minDistanceSq = Double.MAX_VALUE;

        for (TeamData team : data.getTeams().values()) {
            if (team.getInnRegions().isEmpty()) continue;

            // 使用第一个区域的中心作为参考点
            TeamData.InnRegion region = team.getInnRegions().get(0);
            double centerX = (region.minX() + region.maxX()) / 2.0;
            double centerZ = (region.minZ() + region.maxZ()) / 2.0;

            double distSq =
                    (pos.getX() - centerX) * (pos.getX() - centerX)
                            + (pos.getZ() - centerZ) * (pos.getZ() - centerZ);

            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
                nearestTeam = team;
            }
        }

        return nearestTeam;
    }

    // --- 辅助修改方法 ---

    public void renameTeam(TeamData team, String newName, MinecraftServer server) {
        team.setName(newName);
        syncTeam(team, server);
    }

    public void transferLeader(TeamData team, UUID newLeader, MinecraftServer server) {
        team.setLeaderId(newLeader);
        syncTeam(team, server);
    }

    public void unlockMapPoint(TeamData team, ResourceLocation pointId, MinecraftServer server) {
        team.unlockMapPoint(pointId);
        syncTeam(team, server);
    }

    public void lockMapPoint(TeamData team, ResourceLocation pointId, MinecraftServer server) {
        team.lockMapPoint(pointId);
        syncTeam(team, server);
    }

    // --- 客户端同步逻辑 ---

    private TeamData clientTeamCache;

    /**
     * 获取客户端缓存的队伍数据
     *
     * @return 客户端缓存的 TeamData，可能为 null
     */
    public TeamData getClientTeamCache() {
        return clientTeamCache;
    }

    /**
     * 客户端获取当前玩家的队伍数据
     *
     * @return 客户端缓存的队伍数据
     */
    public TeamData getClientPlayerTeam() {
        if (clientTeamCache == null) {
            // 初始化默认空数据，等待服务端同步
            clientTeamCache = new TeamData(UUID.randomUUID());
            clientTeamCache.setTeleportUnlocked(false);
            // 默认解锁旅社
            clientTeamCache.unlockMapPoint(ResourceLocation.parse("otherworldinn:inn"));
        }
        return clientTeamCache;
    }

    /** 更新客户端缓存 (由网络包调用) */
    public void updateClientTeamData(
            UUID teamId,
            String name,
            UUID leaderId,
            Set<UUID> members,
            Set<ResourceLocation> unlockedPoints,
            boolean teleportUnlocked,
            int coins,
            CompoundTag innDataTag,
            List<CompoundTag> innRegions,
            Set<String> unlockedCookRecipes) {
        if (clientTeamCache == null || !clientTeamCache.getTeamId().equals(teamId)) {
            clientTeamCache = new TeamData(teamId);
        }

        clientTeamCache.setName(name);
        clientTeamCache.setMembers(members);
        clientTeamCache.setLeaderId(leaderId);
        clientTeamCache.setUnlockedMapPoints(unlockedPoints);
        clientTeamCache.setTeleportUnlocked(teleportUnlocked);
        clientTeamCache.setCoins(coins);

        // 更新旅社数据
        if (innDataTag != null) {
            clientTeamCache.getInnData().load(innDataTag);
        }

        // 更新旅社区域
        clientTeamCache.getInnRegions().clear();
        if (innRegions != null) {
            for (CompoundTag regionTag : innRegions) {
                clientTeamCache.getInnRegions().add(TeamData.InnRegion.load(regionTag));
            }
            clientTeamCache.optimizeRegions();
        }
    }

    @EventBusSubscriber(modid = OtherworldInn.MODID)
    public static class ServerSyncTickHandler {
        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            TeamManager.getInstance().flushPendingTeamSyncs(event.getServer());
        }
    }
}
