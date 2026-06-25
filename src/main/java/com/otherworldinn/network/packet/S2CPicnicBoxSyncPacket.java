package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CPicnicBoxSyncPacket(CompoundTag data) implements CustomPacketPayload {
    public static final Type<S2CPicnicBoxSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "picnic_box_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CPicnicBoxSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG,
                    S2CPicnicBoxSyncPacket::data,
                    S2CPicnicBoxSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CPicnicBoxSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if (context.player() == null) {
                        return;
                    }
                    context.player()
                            .getData(ModAttachments.PLAYER_PICNIC_BOX)
                            .deserializeNBT(context.player().registryAccess(), packet.data());
                });
    }
}
