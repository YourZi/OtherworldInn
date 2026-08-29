package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(
        modid = OtherworldInn.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public class RoomRegisterOverlay {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 优先级 10，低于其他手持物品 HUD
        ItemHudOverlay.register(10, (unused) -> shouldShow(), RoomRegisterOverlay::render);
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return false;

        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offhandItem = player.getItemInHand(InteractionHand.OFF_HAND);
        return mainHandItem.is(ModItems.ROOM_REGISTER.get())
                || offhandItem.is(ModItems.ROOM_REGISTER.get());
    }

    private static void render(
            GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack offhandItem = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offhandItem.is(ModItems.ROOM_REGISTER.get())) {
            Component deleteText =
                    Component.translatable(
                            "message.otherworldinn.room_register.overlay.delete_room");
            Component addText =
                    Component.translatable("message.otherworldinn.room_register.overlay.add_room");
            ItemHudOverlay.renderMouseActions(
                    guiGraphics,
                    new ItemHudOverlay.MouseAction(ItemHudOverlay.MouseButton.LEFT, deleteText),
                    new ItemHudOverlay.MouseAction(ItemHudOverlay.MouseButton.RIGHT, addText));
            return;
        }

        ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (mainHandItem.is(ModItems.ROOM_REGISTER.get())) {
            Component showText =
                    Component.translatable("message.otherworldinn.room_register.overlay.show_room");
            ItemHudOverlay.renderMouseActions(
                    guiGraphics,
                    new ItemHudOverlay.MouseAction(ItemHudOverlay.MouseButton.RIGHT, showText));
        }
    }
}
