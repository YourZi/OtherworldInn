package com.otherworldinn.network;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.network.packet.C2SDialogueClosePacket;
import com.otherworldinn.network.packet.C2SDialogueOptionPacket;
import com.otherworldinn.network.packet.C2SMapModeSyncPacket;
import com.otherworldinn.network.packet.C2SAcceptCommissionPacket;
import com.otherworldinn.network.packet.C2SStorePurchasePacket;
import com.otherworldinn.network.packet.C2STeleportPacket;
import com.otherworldinn.network.packet.C2SWithdrawCoinPacket;
import com.otherworldinn.network.packet.S2CCommissionBoardPacket;
import com.otherworldinn.network.packet.S2CDialogueClosePacket;
import com.otherworldinn.network.packet.S2CDialogueNodePacket;
import com.otherworldinn.network.packet.S2CFatigueSyncPacket;
import com.otherworldinn.network.packet.S2CPriceSyncPacket;
import com.otherworldinn.network.packet.S2CTeamSyncPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络消息注册中心
 *
 * <p>负责注册和发送自定义数据包。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModMessages {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        // 注册 S2C 数据包
        registrar.playToClient(
                S2CTeamSyncPacket.TYPE, S2CTeamSyncPacket.STREAM_CODEC, S2CTeamSyncPacket::handle);
        registrar.playToClient(
                S2CDialogueNodePacket.TYPE,
                S2CDialogueNodePacket.STREAM_CODEC,
                S2CDialogueNodePacket::handle);
        registrar.playToClient(
                S2CDialogueClosePacket.TYPE,
                S2CDialogueClosePacket.STREAM_CODEC,
                S2CDialogueClosePacket::handle);
        registrar.playToClient(
                S2CCommissionBoardPacket.TYPE,
                S2CCommissionBoardPacket.STREAM_CODEC,
                S2CCommissionBoardPacket::handle);
        registrar.playToClient(
                S2CPriceSyncPacket.TYPE,
                S2CPriceSyncPacket.STREAM_CODEC,
                S2CPriceSyncPacket::handle);
        registrar.playToClient(
                S2CFatigueSyncPacket.TYPE,
                S2CFatigueSyncPacket.STREAM_CODEC,
                S2CFatigueSyncPacket::handle);

        // 注册 C2S 数据包
        registrar.playToServer(
                C2STeleportPacket.TYPE, C2STeleportPacket.STREAM_CODEC, C2STeleportPacket::handle);

        registrar.playToServer(
                C2SMapModeSyncPacket.TYPE,
                C2SMapModeSyncPacket.STREAM_CODEC,
                C2SMapModeSyncPacket::handle);

        registrar.playToServer(
                C2SStorePurchasePacket.TYPE,
                C2SStorePurchasePacket.STREAM_CODEC,
                C2SStorePurchasePacket::handle);

        registrar.playToServer(
                C2SWithdrawCoinPacket.TYPE,
                C2SWithdrawCoinPacket.STREAM_CODEC,
                C2SWithdrawCoinPacket::handle);

        registrar.playToServer(
                C2SDialogueOptionPacket.TYPE,
                C2SDialogueOptionPacket.STREAM_CODEC,
                C2SDialogueOptionPacket::handle);
        registrar.playToServer(
                C2SDialogueClosePacket.TYPE,
                C2SDialogueClosePacket.STREAM_CODEC,
                C2SDialogueClosePacket::handle);
        registrar.playToServer(
                C2SAcceptCommissionPacket.TYPE,
                C2SAcceptCommissionPacket.STREAM_CODEC,
                C2SAcceptCommissionPacket::handle);
    }

    /**
     * 发送数据包给指定玩家 (Server -> Client)
     *
     * @param packet 数据包
     * @param player 目标玩家
     */
    public static void sendToPlayer(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    /**
     * 发送数据包给服务端 (Client -> Server)
     *
     * @param packet 数据包
     */
    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }
}
