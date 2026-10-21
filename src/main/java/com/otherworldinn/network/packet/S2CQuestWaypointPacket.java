package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.compat.xaero.XaeroWaypointBridge;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * S2C：把队伍"当前应存在的委托标点"整表下发给客户端（空表 = 清空全部委托标点）。
 * 整表覆盖而非增量，客户端只需"删掉所有名为「委托」的标点再按表重建"。
 */
public record S2CQuestWaypointPacket(long[] positions) implements CustomPacketPayload {

    public static final Type<S2CQuestWaypointPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "quest_waypoints"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CQuestWaypointPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, packet) -> {
                        buffer.writeVarInt(packet.positions().length);
                        for (long position : packet.positions()) {
                            buffer.writeLong(position);
                        }
                    },
                    buffer -> {
                        int size = buffer.readVarInt();
                        long[] positions = new long[size];
                        for (int i = 0; i < size; i++) {
                            positions[i] = buffer.readLong();
                        }
                        return new S2CQuestWaypointPacket(positions);
                    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CQuestWaypointPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> XaeroWaypointBridge.replaceAll(packet.positions()));
    }
}
