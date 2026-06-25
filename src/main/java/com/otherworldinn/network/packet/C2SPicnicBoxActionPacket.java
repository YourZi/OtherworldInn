package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.item.PicnicBoxItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SPicnicBoxActionPacket(int action, int slotIndex) implements CustomPacketPayload {
    public static final int INSERT_FROM_SLOT = 0;
    public static final int REMOVE_TO_SLOT = 1;
    public static final int INSERT_FROM_CARRIED = 2;
    public static final int REMOVE_TO_CARRIED = 3;

    public static final Type<C2SPicnicBoxActionPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "picnic_box_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SPicnicBoxActionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    C2SPicnicBoxActionPacket::action,
                    ByteBufCodecs.INT,
                    C2SPicnicBoxActionPacket::slotIndex,
                    C2SPicnicBoxActionPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SPicnicBoxActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> PicnicBoxItem.handleServerInventoryAction(context.player(), packet.action(), packet.slotIndex()));
    }
}
