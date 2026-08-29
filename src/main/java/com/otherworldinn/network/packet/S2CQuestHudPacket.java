package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.util.ClientServices;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 任务系统 HUD 快照（主 mod 自渲染管线，与 OtherworldInnHud 的 S2CTaskHudSnapshotPacket 无关）。
 * 载荷：{@code {"Tasks": [统一条目...]}}。
 */
public record S2CQuestHudPacket(CompoundTag payload) implements CustomPacketPayload {
    public static final Type<S2CQuestHudPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "quest_hud_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CQuestHudPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.COMPOUND_TAG,
                    S2CQuestHudPacket::payload,
                    S2CQuestHudPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CQuestHudPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientServices.updateQuestHudSnapshot(packet.payload()));
    }
}
