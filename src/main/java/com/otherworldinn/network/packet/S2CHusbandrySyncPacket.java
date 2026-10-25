package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.husbandry.HusbandrySync;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端 -> 客户端：单只养殖动物的好感度同步。
 *
 * <p>客户端不另建缓存：直接把值写进客户端实体上的 {@code HusbandryData} 附件实例，
 * 读取一律走 {@code HusbandryService.getAffection / getHearts}，服务端与客户端同一套 API。
 */
public record S2CHusbandrySyncPacket(int entityId, int affection) implements CustomPacketPayload {

    public static final Type<S2CHusbandrySyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "husbandry_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CHusbandrySyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    S2CHusbandrySyncPacket::entityId,
                    ByteBufCodecs.VAR_INT,
                    S2CHusbandrySyncPacket::affection,
                    S2CHusbandrySyncPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        // 刻意只写数据、不触发心形 HUD：HUD 只由玩家自己右键动物时显示
        // （饲养槽等自动喂食不该在玩家没操作时往屏幕上弹东西）。
        context.enqueueWork(
                () -> HusbandrySync.applyOnClient(
                        entityId, affection, context.player() == null ? null : context.player().level()));
    }
}
