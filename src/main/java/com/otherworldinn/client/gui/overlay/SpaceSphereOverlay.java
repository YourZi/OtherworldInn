package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.item.SpaceSphereItem;
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
public class SpaceSphereOverlay {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemHudOverlay.register(11, (unused) -> shouldShow(), SpaceSphereOverlay::render);
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return false;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (stack.is(ModItems.SPACE_SPHERE.get())) return true;
        stack = player.getItemInHand(InteractionHand.OFF_HAND);
        return stack.is(ModItems.SPACE_SPHERE.get());
    }

    private static void render(
            GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!stack.is(ModItems.SPACE_SPHERE.get())) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
            if (!stack.is(ModItems.SPACE_SPHERE.get())) return;
        }

        if (SpaceSphereItem.hasCapturedEntity(stack)) {
            ItemHudOverlay.renderMouseActions(
                    guiGraphics,
                    new ItemHudOverlay.MouseAction(
                            ItemHudOverlay.MouseButton.RIGHT,
                            Component.translatable(
                                    "message.otherworldinn.space_sphere.overlay.release")));
        } else {
            ItemHudOverlay.renderMouseActions(
                    guiGraphics,
                    new ItemHudOverlay.MouseAction(
                            ItemHudOverlay.MouseButton.RIGHT,
                            Component.translatable(
                                    "message.otherworldinn.space_sphere.overlay.capture")));
        }
    }
}
