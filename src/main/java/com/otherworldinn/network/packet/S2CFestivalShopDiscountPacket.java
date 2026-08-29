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

/**
 * 服务端 -> 客户端节日商店折扣同步包（shopType → 折扣率 0~1），
 * 在节日开始/结束及玩家登录时发送。
 */
public record S2CFestivalShopDiscountPacket(Map<String, Double> shopDiscounts)
        implements CustomPacketPayload {

    public static final Type<S2CFestivalShopDiscountPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            OtherworldInn.MODID, "festival_shop_discount"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CFestivalShopDiscountPacket>
            STREAM_CODEC =
                    StreamCodec.composite(
                            ByteBufCodecs.map(
                                    HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE),
                            S2CFestivalShopDiscountPacket::shopDiscounts,
                            S2CFestivalShopDiscountPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CFestivalShopDiscountPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientFestivalData.setShopDiscounts(packet.shopDiscounts()));
    }
}
