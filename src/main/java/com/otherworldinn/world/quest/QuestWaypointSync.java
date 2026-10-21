package com.otherworldinn.world.quest;

import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CQuestWaypointPacket;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 委托标点同步：把队伍当前活跃任务节点的指示点整表推给在线队友，客户端转成 Xaero 标点。
 * 任务状态变化走 {@link #syncTeam}，玩家登录走 {@link #syncPlayer}（补偿离线期间的变化并清掉残留标点）。
 */
public final class QuestWaypointSync {

    /** 每支队伍上次下发的标点表，用于去重（队伍对象被回收时自动清理）。 */
    private static final Map<TeamData, long[]> LAST_SENT = new WeakHashMap<>();

    private QuestWaypointSync() {}

    /** 队伍任务状态变化后调用：整表下发（无变化则跳过）。 */
    public static void syncTeam(TeamData team, MinecraftServer server) {
        long[] positions = collect(team, server);
        if (Arrays.equals(LAST_SENT.get(team), positions)) {
            return;
        }
        LAST_SENT.put(team, positions);
        send(positions, QuestService.onlineMembers(team, server));
    }

    /** 玩家登录时调用：按当前进度补一份，顺带清掉他上次会话残留的委托标点。 */
    public static void syncPlayer(ServerPlayer player) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        List<ServerPlayer> target = new ArrayList<>(1);
        target.add(player);
        if (team == null) {
            send(new long[0], target);
            return;
        }
        send(collect(team, player.server), target);
    }

    private static void send(long[] positions, List<ServerPlayer> players) {
        if (players.isEmpty()) {
            return;
        }
        S2CQuestWaypointPacket packet = new S2CQuestWaypointPacket(positions);
        for (ServerPlayer player : players) {
            ModMessages.sendToPlayer(packet, player);
        }
    }

    /** 汇总队伍所有进行中任务里"活跃且有 marker"的节点坐标（relative 按城镇出生点偏移）。 */
    private static long[] collect(TeamData team, MinecraftServer server) {
        Set<Long> packed = new LinkedHashSet<>();
        ServerLevel townLevel = server.getLevel(TownDimensions.TOWN_LEVEL);
        for (QuestDefinition quest : QuestRegistry.all()) {
            TeamQuestData.QuestProgress progress = team.getQuestData().getProgress(quest.id());
            if (progress == null || progress.isCompleted()) {
                continue;
            }
            for (QuestNodeDef node : quest.nodes()) {
                if (!progress.getActiveNodeIds().contains(node.id()) || node.marker() == null) {
                    continue;
                }
                BlockPos pos = node.marker();
                if (node.markerRelative() && townLevel != null) {
                    pos = pos.offset(townLevel.getSharedSpawnPos());
                }
                packed.add(pos.asLong());
            }
        }
        long[] positions = new long[packed.size()];
        int index = 0;
        for (long value : packed) {
            positions[index++] = value;
        }
        Arrays.sort(positions);
        return positions;
    }
}
