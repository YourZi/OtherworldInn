package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.item.RoomKeyItem;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(
        modid = OtherworldInn.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public class RoomKeyOverlay {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 优先级 15，介于房间登记册和地契之间
        ItemHudOverlay.register(15, (unused) -> shouldShow(), RoomKeyOverlay::render);
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return false;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean holding = stack.is(ModItems.ROOM_KEY.get());
        if (!holding) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
            holding = stack.is(ModItems.ROOM_KEY.get());
        }
        return holding;
    }

    private static void render(
            GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!stack.is(ModItems.ROOM_KEY.get())) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
        }

        Optional<Integer> roomId = RoomKeyItem.getBoundRoomId(stack);

        boolean isLookingAtGuest =
                mc.hitResult instanceof EntityHitResult entityHitResult
                        && entityHitResult.getEntity() instanceof GuestEntity;

        if (roomId.isPresent()) {
            if (isLookingAtGuest) {
                // 旅客入住 (消耗钥匙)
                ItemHudOverlay.renderMouseActions(
                        guiGraphics,
                        new ItemHudOverlay.MouseAction(
                                ItemHudOverlay.MouseButton.RIGHT,
                                Component.translatable(
                                        "message.otherworldinn.room_key.overlay.checkin")));
            } else {
                // 已绑定：左键解绑，右键重新绑定
                ItemHudOverlay.renderMouseActions(
                        guiGraphics,
                        new ItemHudOverlay.MouseAction(
                                ItemHudOverlay.MouseButton.LEFT,
                                Component.translatable(
                                        "message.otherworldinn.room_key.overlay.unbind")),
                        new ItemHudOverlay.MouseAction(
                                ItemHudOverlay.MouseButton.RIGHT,
                                Component.translatable(
                                        "message.otherworldinn.room_key.overlay.bind")));
            }
        } else {
            // 未绑定：右键绑定
            ItemHudOverlay.renderMouseActions(
                    guiGraphics,
                    new ItemHudOverlay.MouseAction(
                            ItemHudOverlay.MouseButton.RIGHT,
                            Component.translatable("message.otherworldinn.room_key.overlay.bind")));
        }
    }
}
