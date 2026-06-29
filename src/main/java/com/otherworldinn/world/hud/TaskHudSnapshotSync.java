package com.otherworldinn.world.hud;

import com.otherworldinn.api.OtherworldInnHudSnapshotApi;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CTaskHudSnapshotPacket;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.TeamSavedData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class TaskHudSnapshotSync {
    private TaskHudSnapshotSync() {}

    public static void syncPlayer(ServerPlayer player) {
        if (player == null) {
            return;
        }
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            sendSnapshot(player, OtherworldInnHudSnapshotApi.createEmptySnapshot());
            return;
        }
        sendSnapshot(player, TaskHudSnapshotBuilder.buildForPlayer(player, team));
    }

    public static void syncTeam(TeamData team, ServerLevel level) {
        if (team == null || level == null) {
            return;
        }
        MinecraftServer server = level.getServer();
        for (UUID memberId : team.getMembers().stream()
                .sorted(Comparator.comparing(UUID::toString))
                .toList()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberId);
            if (member != null) {
                sendSnapshot(member, TaskHudSnapshotBuilder.buildForPlayer(member, team));
            }
        }
    }

    public static void syncAllTeams(ServerLevel level) {
        if (level == null) {
            return;
        }
        TeamSavedData data = TeamManager.getInstance().getData(level.getServer());
        for (TeamData team : data.getTeams().values()) {
            syncTeam(team, level);
        }
    }

    private static void sendSnapshot(ServerPlayer player, CompoundTag snapshot) {
        ModMessages.sendToPlayer(new S2CTaskHudSnapshotPacket(snapshot.copy()), player);
    }
}
