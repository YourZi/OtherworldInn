package com.otherworldinn.client.gui.screen;

import com.otherworldinn.foundation.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * GUI 动效开关的极简配置屏：两个布尔项 + 完成。由 {@code IConfigScreenFactory} 挂在模组列表的"配置"按钮上，
 * 改动立即写回 {@code config/otherworldinn-client.toml} 并即时生效（无需重启）。
 */
public class GuiAnimationConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 24;

    private final Screen parent;

    public GuiAnimationConfigScreen(Screen parent) {
        super(Component.literal("GUI 动效设置 / GUI Animations"));
        this.parent = parent;
    }

    private static Component label(String zh, String en, boolean enabled) {
        return Component.literal((enabled ? "✔ " : "✘ ") + zh + " (" + en + ")");
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - GAP;

        this.addRenderableWidget(Button.builder(
                        label("物品动效", "Item Animations", ClientConfig.itemAnimations()),
                        button -> {
                            ClientConfig.INSTANCE.itemAnimations.set(
                                    !ClientConfig.INSTANCE.itemAnimations.get());
                            ClientConfig.SPEC.save();
                            button.setMessage(label(
                                    "物品动效", "Item Animations", ClientConfig.itemAnimations()));
                        })
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        y += GAP;
        this.addRenderableWidget(Button.builder(
                        label("界面动效", "Interface Animations", ClientConfig.interfaceAnimations()),
                        button -> {
                            ClientConfig.INSTANCE.interfaceAnimations.set(
                                    !ClientConfig.INSTANCE.interfaceAnimations.get());
                            ClientConfig.SPEC.save();
                            button.setMessage(label(
                                    "界面动效", "Interface Animations", ClientConfig.interfaceAnimations()));
                        })
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        y += GAP * 2;
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.done"),
                        button -> this.onClose())
                .bounds(centerX - BUTTON_WIDTH / 2, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 24, 0xFFFFFF);
        guiGraphics.drawCenteredString(
                this.font,
                Component.literal("改动即时生效，无需重启游戏"),
                this.width / 2,
                40,
                0xA0A0A0);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
