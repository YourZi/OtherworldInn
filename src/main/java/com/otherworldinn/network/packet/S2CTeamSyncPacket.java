package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端 -> 客户端 数据包
 *
 * <p>用于同步队伍数据到客户端。 包含队伍ID、名称、队长ID、成员列表、解锁点列表、传送功能状态、金币数、旅社数据。
 */
public record S2CTeamSyncPacket(
        UUID teamId,
        String teamName,
        UUID leaderId,
        List<UUID> members,
        List<ResourceLocation> unlockedPoints,
        boolean teleportUnlocked,
        int coins,
        CompoundTag innData,
        List<CompoundTag> innRegions)
        implements CustomPacketPayload {

    public static final Type<S2CTeamSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "team_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CTeamSyncPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        // 编码顺序必须与解码顺序严格一致
                        UUIDUtil.STREAM_CODEC.encode(buf, packet.teamId());
                        ByteBufCodecs.STRING_UTF8.encode(buf, packet.teamName());
                        UUIDUtil.STREAM_CODEC.encode(buf, packet.leaderId());
                        ByteBufCodecs.collection(ArrayList::new, UUIDUtil.STREAM_CODEC)
                                .encode(buf, new ArrayList<>(packet.members()));
                        ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC)
                                .encode(buf, new ArrayList<>(packet.unlockedPoints()));
                        ByteBufCodecs.BOOL.encode(buf, packet.teleportUnlocked());
                        ByteBufCodecs.INT.encode(buf, packet.coins());
                        ByteBufCodecs.COMPOUND_TAG.encode(buf, packet.innData());
                        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.COMPOUND_TAG)
                                .encode(buf, new ArrayList<>(packet.innRegions()));
                    },
                    buf ->
                            // 按相同字段顺序恢复队伍快照
                            new S2CTeamSyncPacket(
                                    UUIDUtil.STREAM_CODEC.decode(buf),
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    UUIDUtil.STREAM_CODEC.decode(buf),
                                    ByteBufCodecs.collection(ArrayList::new, UUIDUtil.STREAM_CODEC)
                                            .decode(buf),
                                    ByteBufCodecs.collection(
                                                    ArrayList::new, ResourceLocation.STREAM_CODEC)
                                            .decode(buf),
                                    ByteBufCodecs.BOOL.decode(buf),
                                    ByteBufCodecs.INT.decode(buf),
                                    ByteBufCodecs.COMPOUND_TAG.decode(buf),
                                    ByteBufCodecs.collection(
                                                    ArrayList::new, ByteBufCodecs.COMPOUND_TAG)
                                            .decode(buf)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 处理数据包
     *
     * @param context 数据包上下文
     */
    public void handle(IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    // 在客户端主线程覆写本地缓存，保证 UI/逻辑读取一致
                    TeamManager.getInstance()
                            .updateClientTeamData(
                                    teamId(),
                                    teamName(),
                                    leaderId(),
                                    new HashSet<>(members()),
                                    new HashSet<>(unlockedPoints()),
                                    teleportUnlocked(),
                                    coins(),
                                    innData(),
                                    innRegions());
                });
    }
}
