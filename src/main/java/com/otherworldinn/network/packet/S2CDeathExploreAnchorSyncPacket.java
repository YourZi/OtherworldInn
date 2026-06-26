package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CDeathExploreAnchorSyncPacket(boolean hasPendingDeathPos)
        implements CustomPacketPayload {
    public static final Type<S2CDeathExploreAnchorSyncPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            OtherworldInn.MODID, "death_explore_anchor_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CDeathExploreAnchorSyncPacket>
            STREAM_CODEC =
                    StreamCodec.composite(
                            ByteBufCodecs.BOOL,
                            S2CDeathExploreAnchorSyncPacket::hasPendingDeathPos,
                            S2CDeathExploreAnchorSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CDeathExploreAnchorSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if (context.player() == null) {
                        return;
                    }
                    context.player()
                            .getData(ModAttachments.PLAYER_DEATH_EXPLORE_ANCHOR)
                            .applyClientSyncSnapshot(packet.hasPendingDeathPos());
                });
    }
}
