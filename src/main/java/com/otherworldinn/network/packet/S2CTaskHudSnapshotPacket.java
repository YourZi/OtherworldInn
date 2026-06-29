package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.api.OtherworldInnHudSnapshotApi;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CTaskHudSnapshotPacket(CompoundTag snapshot) implements CustomPacketPayload {
    public static final Type<S2CTaskHudSnapshotPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "task_hud_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CTaskHudSnapshotPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG,
                    S2CTaskHudSnapshotPacket::snapshot,
                    S2CTaskHudSnapshotPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CTaskHudSnapshotPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> OtherworldInnHudSnapshotApi.updateTaskHudSnapshot(packet.snapshot()));
    }
}
