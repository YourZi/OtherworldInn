package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.util.ClientServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CDialogueClosePacket() implements CustomPacketPayload {
    public static final Type<S2CDialogueClosePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "dialogue_close"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CDialogueClosePacket> STREAM_CODEC =
            StreamCodec.unit(new S2CDialogueClosePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CDialogueClosePacket packet, IPayloadContext context) {
        context.enqueueWork(ClientServices::handleDialogueClose);
    }
}
