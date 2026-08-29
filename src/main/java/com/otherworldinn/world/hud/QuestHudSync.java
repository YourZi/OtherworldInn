package com.otherworldinn.world.hud;

import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CQuestHudPacket;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * 任务系统 HUD 同步（主 mod 自有管线，S2CQuestHudPacket → QuestHudState → QuestHudOverlay）。
 *
 * <p>条目 = 任务（quest）+ 城镇委托 + 故事委托（后两者复用 {@link TaskHudSnapshotBuilder}
 * 的归一化输出），统一外显。委托/故事委托的刷新沿用既有触发点——
 * {@link TaskHudSnapshotSync} 的各 sync 入口会联动调用本类。
 */
public final class QuestHudSync {
    private QuestHudSync() {}

    public static void syncPlayer(ServerPlayer player) {
        if (player == null) {
            return;
        }
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        send(player, team);
    }

    public static void syncTeam(TeamData team, ServerLevel level) {
        if (team == null || level == null) {
            return;
        }
        for (UUID memberId : team.getMembers().stream()
                .sorted(Comparator.comparing(UUID::toString))
                .toList()) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                send(member, team);
            }
        }
    }

    private static void send(ServerPlayer player, TeamData team) {
        ListTag tasks = new ListTag();
        if (team != null) {
            QuestHudSnapshotBuilder.addQuestTasks(tasks, player, team);
            ListTag commissionAndStory =
                    TaskHudSnapshotBuilder.buildForPlayer(player, team).getList("Tasks", Tag.TAG_COMPOUND);
            for (int i = 0; i < commissionAndStory.size(); i++) {
                tasks.add(commissionAndStory.get(i));
            }
        }
        CompoundTag payload = new CompoundTag();
        payload.put("Tasks", tasks);
        ModMessages.sendToPlayer(new S2CQuestHudPacket(payload), player);
    }
}
