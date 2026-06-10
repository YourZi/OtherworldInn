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
public class EndSpaceSphereOverlay {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemHudOverlay.register(14, (unused) -> shouldShow(), EndSpaceSphereOverlay::render);
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return false;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (isEndSpaceSphere(stack)) return true;
        stack = player.getItemInHand(InteractionHand.OFF_HAND);
        return isEndSpaceSphere(stack);
    }

    private static boolean isEndSpaceSphere(ItemStack stack) {
        return stack.getItem() == ModItems.END_SPACE_SPHERE.get();
    }

    private static void render(
            GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        ItemHudOverlay.renderMouseActions(
                guiGraphics,
                new ItemHudOverlay.MouseAction(
                        ItemHudOverlay.MouseButton.RIGHT,
                        Component.translatable("message.otherworldinn.end_space_sphere.overlay.use")));
    }
}
