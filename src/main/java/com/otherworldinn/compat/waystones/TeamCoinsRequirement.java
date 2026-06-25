package com.otherworldinn.compat.waystones;

import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.List;
import net.blay09.mods.waystones.api.requirement.WarpRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class TeamCoinsRequirement implements WarpRequirement {
    private final int coins;

    public TeamCoinsRequirement(int coins) {
        this.coins = Math.max(0, coins);
    }

    @Override
    public boolean canAfford(Player player) {
        TeamData team = getPlayerTeam(player);
        return team != null && team.getCoins() >= coins;
    }

    @Override
    public void consume(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || coins <= 0) {
            return;
        }

        TeamData team = getPlayerTeam(serverPlayer);
        if (team == null) {
            return;
        }

        TeamManager.getInstance().removeCoins(team, coins, serverPlayer.getServer());
    }

    @Override
    public void rollback(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || coins <= 0) {
            return;
        }

        TeamData team = getPlayerTeam(serverPlayer);
        if (team == null) {
            return;
        }

        TeamManager.getInstance().addCoins(team, coins, serverPlayer.getServer());
    }

    @Override
    public void appendHoverText(Player player, List<Component> tooltip) {
        TeamData team = getPlayerTeam(player);
        boolean affordable = team != null && team.getCoins() >= coins;
        tooltip.add(
                Component.translatable("gui.waystones.waystone_selection.xp_requirement", coins)
                        .withStyle(affordable ? ChatFormatting.YELLOW : ChatFormatting.RED));

        if (team == null) {
            tooltip.add(
                    Component.translatable("message.otherworldinn.coin.no_team")
                            .withStyle(style -> style.withColor(ModColors.ERROR)));
            return;
        }

        if (!affordable) {
            tooltip.add(
                    Component.translatable(
                                    "facility.otherworldinn.upgrade_fail_coins", coins, team.getCoins())
                            .withStyle(style -> style.withColor(ModColors.ERROR)));
        }
    }

    @Override
    public boolean isEmpty() {
        return coins <= 0;
    }

    public int getCoins() {
        return coins;
    }

    private static TeamData getPlayerTeam(Player player) {
        return player == null ? null : TeamManager.getInstance().getPlayerTeam(player);
    }
}
