package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModAttachments;
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
public class PicnicBoxOverlay {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemHudOverlay.register(26, (unused) -> shouldShow(), PicnicBoxOverlay::render);
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) {
            return false;
        }
        boolean holdingPicnicBox =
                mc.player.getMainHandItem().is(ModItems.PICNIC_BOX.get())
                        || mc.player.getOffhandItem().is(ModItems.PICNIC_BOX.get());
        return holdingPicnicBox && !mc.player.getData(ModAttachments.PLAYER_PICNIC_BOX).isEmpty();
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        ItemHudOverlay.renderMouseActions(
                guiGraphics,
                new ItemHudOverlay.MouseAction(
                        ItemHudOverlay.MouseButton.RIGHT,
                        Component.translatable("item.otherworldinn.picnic_box.hud.hint")));
    }
}
