package com.otherworldinn.compat.xaero;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * 在城镇维度中禁用 Xaero 世界地图。
 * 小地图渲染在专用 Mixin 中单点拦截，这里只保留世界地图相关兜底。
 */
@EventBusSubscriber(value = Dist.CLIENT)
public final class XaeroMapBlocker {
    private XaeroMapBlocker() {}

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        boolean inTown = XaeroCompatBridge.isTownBlocked(mc);

        if (inTown) {
            XaeroCompatBridge.suppressWorldMapInputs();
            XaeroCompatBridge.closeBlockedWorldMapScreen(mc);
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Minecraft mc = Minecraft.getInstance();
        if (!XaeroCompatBridge.isTownBlocked(mc) || event.getScreen() == null) {
            return;
        }
        if (XaeroCompatBridge.isBlockedWorldMapScreen(event.getScreen())) {
            event.setCanceled(true);
        }
    }
}
