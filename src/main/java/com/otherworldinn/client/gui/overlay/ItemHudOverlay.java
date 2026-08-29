package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.simibubi.create.foundation.gui.AllIcons;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** 物品 HUD 管理器，统一管理手持物品时的 HUD 提示，按优先级只显示最高的一个。 */
@EventBusSubscriber(
        modid = OtherworldInn.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public class ItemHudOverlay {

    private static final List<OverlayEntry> overlays = new ArrayList<>();

    public record OverlayEntry(
            int priority,
            Predicate<Void> condition,
            BiConsumer<GuiGraphics, DeltaTracker> renderer) {}

    public enum MouseButton {
        LEFT,
        RIGHT
    }

    public record MouseAction(MouseButton button, Component text, int color) {
        public MouseAction(MouseButton button, Component text) {
            this(button, text, ModColors.WHITE);
        }
    }

    /** 注册一个新的 HUD 层，priority 越高越优先显示。 */
    public static void register(
            int priority,
            Predicate<Void> condition,
            BiConsumer<GuiGraphics, DeltaTracker> renderer) {
        overlays.add(new OverlayEntry(priority, condition, renderer));
        overlays.sort(Comparator.comparingInt(OverlayEntry::priority).reversed());
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "item_hud_overlay"),
                (guiGraphics, deltaTracker) -> {
                    for (OverlayEntry entry : overlays) {
                        if (entry.condition.test(null)) {
                            entry.renderer.accept(guiGraphics, deltaTracker);
                            return; // 只显示优先级最高的一个
                        }
                    }
                });
    }

    /** 渲染通用的鼠标操作提示，支持多个操作并排显示。 */
    public static void renderMouseActions(GuiGraphics guiGraphics, MouseAction... actions) {
        if (actions == null || actions.length == 0) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int centerX = screenWidth / 2;
        int startY = 60;
        int iconSize = 16;
        int padding = 4;

        int totalWidth = 0;
        for (int i = 0; i < actions.length; i++) {
            int textWidth = font.width(actions[i].text);
            totalWidth += iconSize + padding + textWidth;
            if (i < actions.length - 1) {
                totalWidth += padding * 3; // 间隔
            }
        }

        int currentX = centerX - totalWidth / 2;

        for (MouseAction action : actions) {
            if (action.button == MouseButton.RIGHT) {
                AllIcons.I_RMB.render(guiGraphics, currentX, startY);
            } else {
                AllIcons.I_LMB.render(guiGraphics, currentX, startY);
            }

            guiGraphics.drawString(
                    font,
                    action.text,
                    currentX + iconSize + padding,
                    startY + (iconSize - font.lineHeight) / 2 + 1,
                    action.color,
                    true);

            int textWidth = font.width(action.text);
            currentX += iconSize + padding + textWidth + padding * 3;
        }
    }
}
