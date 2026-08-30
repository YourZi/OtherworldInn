package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.ClientFestivalData;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 服务端 -> 客户端节日旅社属性加成同步包。 */
public record S2CFestivalInnAttributeBoostPacket(Map<String, Double> innAttributeBoosts)
        implements CustomPacketPayload {

    public static final Type<S2CFestivalInnAttributeBoostPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            OtherworldInn.MODID, "festival_inn_attribute_boost"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CFestivalInnAttributeBoostPacket>
            STREAM_CODEC =
                    StreamCodec.composite(
                            ByteBufCodecs.map(
                                    HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE),
                            S2CFestivalInnAttributeBoostPacket::innAttributeBoosts,
                            S2CFestivalInnAttributeBoostPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CFestivalInnAttributeBoostPacket packet, IPayloadContext context) {
        context.enqueueWork(
                () -> ClientFestivalData.setInnAttributeBoosts(packet.innAttributeBoosts()));
    }
}
