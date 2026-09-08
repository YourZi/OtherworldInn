package com.otherworldinn.client.gui.screen;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.C2SDialogueClosePacket;
import com.otherworldinn.network.packet.C2SDialogueOptionPacket;
import com.otherworldinn.world.dialogue.DialogueNodeView;
import com.otherworldinn.world.dialogue.DialogueOptionType;
import com.otherworldinn.world.dialogue.DialogueOptionView;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.components.WidgetSprites;

public class NpcDialogueScreen extends Screen {
    private static final String BRANCH_ICON = "\uE007";
    private static final String FUNCTION_ICON = "\uE008";
    private static final int OPTION_HEIGHT = 20;
    private static final int OPTION_GAP = 3;
    private static final int DIALOG_BOX_HEIGHT = 110;
    private static final int DIALOG_BOX_WIDTH = 720;
    private static final int DIALOG_MARGIN = 18;
    private static final int OPTION_TO_DIALOG_GAP = 10;
    private static final int OPTION_TEXT_LEFT_PADDING = 8;
    private static final int OPTION_TEXT_RIGHT_PADDING = 8;
    private static final int OPTION_MIN_WIDTH = 120;
    private static final int REVEAL_CHARS_PER_SECOND = 20;
    private static final ResourceLocation DIALOGUE_BOX_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/gui/dialogue/dialogue_box.png");

    private static final WidgetSprites OPTION_BUTTON_SPRITES =
            new WidgetSprites(
                    ResourceLocation.withDefaultNamespace("widget/button"),
                    ResourceLocation.withDefaultNamespace("widget/button_disabled"),
                    ResourceLocation.withDefaultNamespace("widget/button_highlighted"));

    private DialogueNodeView view;
    private DialogueNodeView animatedView;
    private long revealStartMillis;
    private boolean revealAll;
    private final List<Button> optionButtons = new ArrayList<>();

    public NpcDialogueScreen(DialogueNodeView view) {
        super(Component.translatable("screen.otherworldinn.dialogue.title"));
        this.view = view;
    }

    public void updateView(DialogueNodeView newView) {
        this.view = newView;
        rebuildOptions();
    }

    @Override
    protected void init() {
        super.init();
        rebuildOptions();
    }

    private void rebuildOptions() {
        this.clearWidgets();
        this.optionButtons.clear();

        int boxY = this.height - DIALOG_BOX_HEIGHT - DIALOG_MARGIN;
        int optionBaseY = boxY - OPTION_TO_DIALOG_GAP - OPTION_HEIGHT;
        List<DialogueOptionView> options = this.view.options();
        int optionWidth = calculateOptionWidth(options);
        int rightPanelX = this.width - optionWidth - DIALOG_MARGIN;
        int index = 0;
        for (int i = options.size() - 1; i >= 0; i--) {
            DialogueOptionView option = options.get(i);
            int y = optionBaseY - index * (OPTION_HEIGHT + OPTION_GAP);
            String label = buildOptionLabelString(option);
            Button button =
                    new LeftAlignedOptionButton(
                            rightPanelX,
                            y,
                            optionWidth,
                            OPTION_HEIGHT,
                            label,
                            btn ->
                                    ModMessages.sendToServer(
                                            new C2SDialogueOptionPacket(this.view.entityId(), option.id())));
            this.addRenderableWidget(button);
            this.optionButtons.add(button);
            index++;
        }
        if (this.optionButtons.isEmpty()) {
            Button closeButton =
                    new LeftAlignedOptionButton(
                            rightPanelX,
                            optionBaseY,
                            optionWidth,
                            OPTION_HEIGHT,
                            Component.translatable("dialogue.otherworldinn.option.close").getString(),
                            btn -> {
                                ModMessages.sendToServer(new C2SDialogueClosePacket());
                                this.onClose();
                            });
            this.addRenderableWidget(closeButton);
            this.optionButtons.add(closeButton);
        }
    }

    private int calculateOptionWidth(List<DialogueOptionView> options) {
        int widestLabel = 0;
        if (options.isEmpty()) {
            widestLabel = this.font.width(Component.translatable("dialogue.otherworldinn.option.close"));
        } else {
            for (DialogueOptionView option : options) {
                widestLabel = Math.max(widestLabel, this.font.width(buildOptionLabelString(option)));
            }
        }
        int targetWidth = Math.max(OPTION_MIN_WIDTH, widestLabel + OPTION_TEXT_LEFT_PADDING + OPTION_TEXT_RIGHT_PADDING);
        return Math.min(targetWidth, this.width - DIALOG_MARGIN * 2);
    }

    private static String buildOptionLabelString(DialogueOptionView option) {
        String icon = option.type() == DialogueOptionType.FUNCTION ? FUNCTION_ICON : BRANCH_ICON;
        return icon + " " + Component.translatable(option.labelKey()).getString();
    }

    @Override
    public void onClose() {
        ModMessages.sendToServer(new C2SDialogueClosePacket());
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 留空，阻止父类默认背景/模糊渲染。
    }

    @Override
    public void renderTransparentBackground(GuiGraphics guiGraphics) {
        // 留空，阻止透明背景路径触发的模糊。
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int boxWidth = Math.min(this.width - DIALOG_MARGIN * 2, DIALOG_BOX_WIDTH);
        int boxX = (this.width - boxWidth) / 2;
        int boxY = this.height - DIALOG_BOX_HEIGHT - DIALOG_MARGIN;
        if (this.minecraft != null
                && this.minecraft.getResourceManager().getResource(DIALOGUE_BOX_TEXTURE).isPresent()) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(
                    DIALOGUE_BOX_TEXTURE,
                    boxX,
                    boxY,
                    0,
                    0,
                    boxWidth,
                    DIALOG_BOX_HEIGHT,
                    boxWidth,
                    DIALOG_BOX_HEIGHT);
            guiGraphics.flush();
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
        String npcName = getNpcName().getString();
        int npcNameX = boxX + (boxWidth - this.font.width(npcName)) / 2;
        guiGraphics.drawString(this.font, npcName, npcNameX, boxY + 10, 0xFFE9DDAA, false);

        String dialogueText = Component.translatable(this.view.textKey()).getString();
        List<String> lines =
                this.font.getSplitter().splitLines(dialogueText, Math.max(60, boxWidth - 20), Style.EMPTY).stream()
                        .map(FormattedText::getString)
                        .toList();
        if (this.view != this.animatedView) {
            this.animatedView = this.view;
            this.revealStartMillis = Util.getMillis();
            this.revealAll = false;
        }
        int totalChars = 0;
        for (String line : lines) {
            totalChars += line.codePointCount(0, line.length());
        }
        int visibleChars = totalChars;
        if (!this.revealAll) {
            long elapsed = Util.getMillis() - this.revealStartMillis;
            visibleChars = (int) (elapsed * REVEAL_CHARS_PER_SECOND / 1000L);
            if (visibleChars >= totalChars) {
                visibleChars = totalChars;
                this.revealAll = true;
            }
        }
        int[] budget = {visibleChars};
        int y = boxY + 30;
        for (String line : lines) {
            if (budget[0] <= 0) {
                break;
            }
            String visible = limitPlainText(line, budget);
            int lineX = boxX + (boxWidth - this.font.width(visible)) / 2;
            guiGraphics.drawString(this.font, visible, lineX, y, 0xFFF0F0F0, false);
            y += this.font.lineHeight + 2;
            if (y > boxY + DIALOG_BOX_HEIGHT - 14) {
                break;
            }
        }
        guiGraphics.pose().popPose();
        guiGraphics.flush();

        // 最后再绘制 widgets，避免按钮被对话框底板盖住。
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.revealAll) {
            this.revealAll = true;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static String limitPlainText(String text, int[] budget) {
        int codePoints = text.codePointCount(0, text.length());
        int take = Math.min(budget[0], codePoints);
        budget[0] -= take;
        return text.substring(0, text.offsetByCodePoints(0, take));
    }

    private Component getNpcName() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return Component.translatable("dialogue.otherworldinn.npc.unknown");
        }
        net.minecraft.world.entity.Entity entity = this.minecraft.level.getEntity(this.view.entityId());
        if (entity != null) {
            return entity.getDisplayName();
        }
        return Component.translatable("dialogue.otherworldinn.npc.unknown");
    }

    private static class LeftAlignedOptionButton extends Button {
        private final String label;

        protected LeftAlignedOptionButton(
                int x, int y, int width, int height, String label, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.label = label;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            ResourceLocation sprite = this.active
                    ? (this.isHoveredOrFocused()
                            ? OPTION_BUTTON_SPRITES.enabledFocused()
                            : OPTION_BUTTON_SPRITES.enabled())
                    : OPTION_BUTTON_SPRITES.disabled();
            guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);

            Minecraft mc = Minecraft.getInstance();
            int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
            int textX = this.getX() + OPTION_TEXT_LEFT_PADDING;
            int textY = this.getY() + (this.height - mc.font.lineHeight) / 2;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
            guiGraphics.drawString(mc.font, this.label, textX, textY, textColor, false);
            guiGraphics.pose().popPose();
        }
    }
}
