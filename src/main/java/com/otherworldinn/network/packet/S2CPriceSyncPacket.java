package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.economy.service.ItemSellPriceManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 服务端 -> 客户端：同步服务端计算后的全量浮动价格表。 */
public record S2CPriceSyncPacket(Map<String, Integer> prices) implements CustomPacketPayload {
    public static final Type<S2CPriceSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "price_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CPriceSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
                    S2CPriceSyncPacket::prices,
                    S2CPriceSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CPriceSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemSellPriceManager.setFloatedPrices(packet.prices());
        });
    }
}
