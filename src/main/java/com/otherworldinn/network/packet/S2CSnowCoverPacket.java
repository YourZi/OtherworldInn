package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.snow.SnowCoverController;
import com.otherworldinn.world.snow.SnowCoverState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** 服务端 -> 客户端：某个区块里哪些列已经积上了真实覆雪（256 位位图）。 */
public record S2CSnowCoverPacket(ChunkPos pos, long[] mask) implements CustomPacketPayload {

    public static final Type<S2CSnowCoverPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "snow_cover"));

    /** 手写编解码：区块坐标两段 int + 四个 long 的位图。 */
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSnowCoverPacket> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public S2CSnowCoverPacket decode(RegistryFriendlyByteBuf buffer) {
                    ChunkPos pos = new ChunkPos(buffer.readInt(), buffer.readInt());
                    long[] mask = new long[4];
                    for (int i = 0; i < 4; i++) {
                        mask[i] = buffer.readLong();
                    }
                    return new S2CSnowCoverPacket(pos, mask);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, S2CSnowCoverPacket packet) {
                    buffer.writeInt(packet.pos().x);
                    buffer.writeInt(packet.pos().z);
                    for (long part : packet.mask()) {
                        buffer.writeLong(part);
                    }
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            SnowCoverState.put(pos, mask);
            // 收包后重建该区块的渲染段——这是客户端行为，留在包处理器里，
            // 免得 world.snow.SnowCoverState 反过来依赖 client.* 的类。
            SnowCoverController.markChunkDirty(pos.x, pos.z);
        });
    }
}
