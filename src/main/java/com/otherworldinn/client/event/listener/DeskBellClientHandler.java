package com.otherworldinn.client.event.listener;

import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.simibubi.create.content.redstone.deskBell.DeskBellBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class DeskBellClientHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (!level.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof DeskBellBlockEntity) {
            Player player = event.getEntity();

            if (player.isShiftKeyDown()) {
                ItemStack held = player.getItemInHand(event.getHand());
                if (held.is(Items.BOOK) || held.is(Items.WRITABLE_BOOK) || held.is(Items.WRITTEN_BOOK)) {
                    event.setCanceled(true);
                }
                return;
            }
            TeamData team = TeamManager.getInstance().getClientPlayerTeam();
            if (team == null) return;

            boolean inside = false;
            for (TeamData.InnRegion region : team.getInnRegions()) {
                if (region.contains(pos)) {
                    inside = true;
                    break;
                }
            }
            if (!inside) return;

            InnData.InnState state = team.getInnData().getState();
            MutableComponent message;
            int color;

            switch (state) {
                case OPEN:
                    message = Component.translatable("message.otherworldinn.desk_bell.status.open");
                    color = ModColors.GREEN;
                    break;
                case CLOSED:
                    message =
                            Component.translatable("message.otherworldinn.desk_bell.status.closed");
                    color = ModColors.RED;
                    break;
                default:
                    return;
            }

            Minecraft.getInstance()
                    .gui
                    .setOverlayMessage(message.withStyle(Style.EMPTY.withColor(color)), false);
        }
    }
}
