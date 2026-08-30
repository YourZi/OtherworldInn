package com.otherworldinn.network;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.network.packet.C2SDialogueClosePacket;
import com.otherworldinn.network.packet.C2SDialogueOptionPacket;
import com.otherworldinn.network.packet.C2SMapModeSyncPacket;
import com.otherworldinn.network.packet.C2SPicnicBoxActionPacket;
import com.otherworldinn.network.packet.C2SAcceptCommissionPacket;
import com.otherworldinn.network.packet.C2SStorePurchasePacket;
import com.otherworldinn.network.packet.C2STeleportPacket;
import com.otherworldinn.network.packet.C2SWithdrawCoinPacket;
import com.otherworldinn.network.packet.S2CCommissionBoardPacket;
import com.otherworldinn.network.packet.S2CDeathExploreAnchorSyncPacket;
import com.otherworldinn.network.packet.S2CDialogueClosePacket;
import com.otherworldinn.network.packet.S2CDialogueNodePacket;
import com.otherworldinn.network.packet.S2CFatigueSyncPacket;
import com.otherworldinn.network.packet.S2CFestivalInnAttributeBoostPacket;
import com.otherworldinn.network.packet.S2CFestivalShopDiscountPacket;
import com.otherworldinn.network.packet.S2CPicnicBoxSyncPacket;
import com.otherworldinn.network.packet.S2CPriceSyncPacket;
import com.otherworldinn.network.packet.S2CQuestHudPacket;
import com.otherworldinn.network.packet.S2CTaskHudSnapshotPacket;
import com.otherworldinn.network.packet.S2CTeamSyncPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** 网络消息注册中心，负责注册和发送自定义数据包。 */
@EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModMessages {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

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
                S2CDeathExploreAnchorSyncPacket.TYPE,
                S2CDeathExploreAnchorSyncPacket.STREAM_CODEC,
                S2CDeathExploreAnchorSyncPacket::handle);
        registrar.playToClient(
                S2CPriceSyncPacket.TYPE,
                S2CPriceSyncPacket.STREAM_CODEC,
                S2CPriceSyncPacket::handle);
        registrar.playToClient(
                S2CFatigueSyncPacket.TYPE,
                S2CFatigueSyncPacket.STREAM_CODEC,
                S2CFatigueSyncPacket::handle);
        registrar.playToClient(
                S2CFestivalInnAttributeBoostPacket.TYPE,
                S2CFestivalInnAttributeBoostPacket.STREAM_CODEC,
                S2CFestivalInnAttributeBoostPacket::handle);
        registrar.playToClient(
                S2CFestivalShopDiscountPacket.TYPE,
                S2CFestivalShopDiscountPacket.STREAM_CODEC,
                S2CFestivalShopDiscountPacket::handle);
        registrar.playToClient(
                S2CPicnicBoxSyncPacket.TYPE,
                S2CPicnicBoxSyncPacket.STREAM_CODEC,
                S2CPicnicBoxSyncPacket::handle);
        registrar.playToClient(
                S2CTaskHudSnapshotPacket.TYPE,
                S2CTaskHudSnapshotPacket.STREAM_CODEC,
                S2CTaskHudSnapshotPacket::handle);
        registrar.playToClient(
                S2CQuestHudPacket.TYPE,
                S2CQuestHudPacket.STREAM_CODEC,
                S2CQuestHudPacket::handle);

        registrar.playToServer(
                C2STeleportPacket.TYPE, C2STeleportPacket.STREAM_CODEC, C2STeleportPacket::handle);

        registrar.playToServer(
                C2SMapModeSyncPacket.TYPE,
                C2SMapModeSyncPacket.STREAM_CODEC,
                C2SMapModeSyncPacket::handle);

        registrar.playToServer(
                C2SPicnicBoxActionPacket.TYPE,
                C2SPicnicBoxActionPacket.STREAM_CODEC,
                C2SPicnicBoxActionPacket::handle);

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

    public static void sendToPlayer(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }
}
