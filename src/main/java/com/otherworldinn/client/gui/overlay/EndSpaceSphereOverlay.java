package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
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
        ItemHudOverlay.register(27, (unused) -> shouldShow(), EndSpaceSphereOverlay::render);
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return false;
        }
        return mc.player.getMainHandItem().is(ModItems.END_SPACE_SPHERE.get());
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        ItemHudOverlay.renderMouseActions(
                guiGraphics,
                new ItemHudOverlay.MouseAction(
                        ItemHudOverlay.MouseButton.RIGHT,
                        Component.translatable("item.otherworldinn.end_space_sphere.hud.hint")));
    }
}
