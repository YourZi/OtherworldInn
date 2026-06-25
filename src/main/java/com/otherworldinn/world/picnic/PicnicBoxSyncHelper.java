package com.otherworldinn.world.picnic;

import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CPicnicBoxSyncPacket;
import net.minecraft.server.level.ServerPlayer;

public final class PicnicBoxSyncHelper {
    private PicnicBoxSyncHelper() {}

    public static void sync(ServerPlayer player) {
        ModMessages.sendToPlayer(
                new S2CPicnicBoxSyncPacket(
                        player.getData(ModAttachments.PLAYER_PICNIC_BOX)
                                .serializeNBT(player.registryAccess())),
                player);
    }
}
