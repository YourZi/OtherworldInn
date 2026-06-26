package com.otherworldinn.world.teleport;

import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CDeathExploreAnchorSyncPacket;
import net.minecraft.server.level.ServerPlayer;

public final class DeathExploreAnchorSyncHelper {
    private DeathExploreAnchorSyncHelper() {}

    public static void sync(ServerPlayer player) {
        ModMessages.sendToPlayer(
                new S2CDeathExploreAnchorSyncPacket(
                        player.getData(ModAttachments.PLAYER_DEATH_EXPLORE_ANCHOR).hasPendingDeathPos()),
                player);
    }
}
