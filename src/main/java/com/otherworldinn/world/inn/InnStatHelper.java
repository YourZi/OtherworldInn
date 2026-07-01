package com.otherworldinn.world.inn;

import com.otherworldinn.init.ModStats;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.stats.Stat;
import com.otherworldinn.world.team.TeamData;

public final class InnStatHelper {
    private InnStatHelper() {}

    public static void awardTeamMemberStat(
            TeamData team,
            MinecraftServer server,
            DeferredHolder<ResourceLocation, ResourceLocation> statHolder) {
        if (team == null || server == null || statHolder == null) {
            return;
        }
        ResourceLocation statKey = statHolder.get();
        Stat<?> stat = Stats.CUSTOM.get(statKey);
        if (stat == null) {
            return;
        }
        for (UUID memberId : team.getMembers()) {
            ServerPlayer player = server.getPlayerList().getPlayer(memberId);
            if (player != null) {
                player.awardStat(stat, 1);
            }
        }
    }
}
