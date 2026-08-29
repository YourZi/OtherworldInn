package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.world.fatigue.FatigueData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 服务端 -> 客户端疲劳值同步包，每秒推送，供 GUI/HUD 读取。 */
public record S2CFatigueSyncPacket(
        double fatigue,
        int stage,
        double lastSecondGain,
        double dimensionMultiplier,
        double journeyMinutes)
        implements CustomPacketPayload {

    public static final Type<S2CFatigueSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "fatigue_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CFatigueSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.DOUBLE,
                    S2CFatigueSyncPacket::fatigue,
                    ByteBufCodecs.INT,
                    S2CFatigueSyncPacket::stage,
                    ByteBufCodecs.DOUBLE,
                    S2CFatigueSyncPacket::lastSecondGain,
                    ByteBufCodecs.DOUBLE,
                    S2CFatigueSyncPacket::dimensionMultiplier,
                    ByteBufCodecs.DOUBLE,
                    S2CFatigueSyncPacket::journeyMinutes,
                    S2CFatigueSyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) {
                return;
            }
            FatigueData data = context.player().getData(ModAttachments.PLAYER_FATIGUE);
            data.applySyncSnapshot(fatigue, stage, lastSecondGain, dimensionMultiplier, journeyMinutes);
        });
    }
}
