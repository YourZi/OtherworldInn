package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.item.CoinItem;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record C2SWithdrawCoinPacket() implements CustomPacketPayload {
    public static final Type<C2SWithdrawCoinPacket> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "withdraw_coin"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SWithdrawCoinPacket> STREAM_CODEC =
            StreamCodec.unit(new C2SWithdrawCoinPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }
                    TeamManager manager = TeamManager.getInstance();
                    TeamData team = manager.getPlayerTeam(player);
                    if (team == null) {
                        return;
                    }
                    if (!manager.removeCoins(team, 1, player.getServer())) {
                        return;
                    }
                    ItemStack coin = new ItemStack(ModItems.COIN.get());
                    CoinItem.markWithdrawnSourceTeam(coin, team.getTeamId());
                    if (!player.getInventory().add(coin)) {
                        player.drop(coin, false);
                    }
                });
    }
}
