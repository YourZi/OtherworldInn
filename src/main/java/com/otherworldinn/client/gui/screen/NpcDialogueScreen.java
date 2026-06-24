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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import com.mojang.blaze3d.systems.RenderSystem;

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
    private static final int OPTION_ATLAS_WIDTH = 180;
    private static final int OPTION_ATLAS_STATE_COUNT = 3;
    private static final int OPTION_ATLAS_SLICE_WIDTH = OPTION_ATLAS_WIDTH / 3;
    private static final int OPTION_MIN_WIDTH = OPTION_ATLAS_SLICE_WIDTH * 2;
    private static final ResourceLocation DIALOGUE_BOX_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/gui/dialogue/dialogue_box.png");
    private static final ResourceLocation OPTION_BUTTON_ATLAS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/gui/dialogue/dialogue_option_button_atlas.png");

    private DialogueNodeView view;
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
            Component label = buildOptionLabel(option);
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
                            Component.translatable("dialogue.otherworldinn.option.close"),
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
                widestLabel = Math.max(widestLabel, this.font.width(buildOptionLabel(option)));
            }
        }
        int targetWidth = Math.max(OPTION_MIN_WIDTH, widestLabel + OPTION_TEXT_LEFT_PADDING + OPTION_TEXT_RIGHT_PADDING);
        return Math.min(targetWidth, this.width - DIALOG_MARGIN * 2);
    }

    private static Component buildOptionLabel(DialogueOptionView option) {
        String icon = option.type() == DialogueOptionType.FUNCTION ? FUNCTION_ICON : BRANCH_ICON;
        return Component.literal(icon + " ").append(Component.translatable(option.labelKey()));
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
        // 不调用 renderBackground，避免对话时触发背景模糊。
        super.render(guiGraphics, mouseX, mouseY, partialTick);

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
            RenderSystem.disableBlend();
        }

        Component npcName = getNpcName();
        int npcNameX = boxX + (boxWidth - this.font.width(npcName)) / 2;
        guiGraphics.drawString(this.font, npcName, npcNameX, boxY + 10, 0xFFE9DDAA, false);

        List<FormattedCharSequence> lines =
                this.font.split(
                        Component.translatable(this.view.textKey()),
                        Math.max(60, boxWidth - 20));
        int y = boxY + 30;
        for (FormattedCharSequence line : lines) {
            int lineX = boxX + (boxWidth - this.font.width(line)) / 2;
            guiGraphics.drawString(this.font, line, lineX, y, 0xFFF0F0F0, false);
            y += this.font.lineHeight + 2;
            if (y > boxY + DIALOG_BOX_HEIGHT - 14) {
                break;
            }
        }
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
        private final Component label;

        protected LeftAlignedOptionButton(
                int x, int y, int width, int height, Component label, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.label = label;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getResourceManager().getResource(OPTION_BUTTON_ATLAS_TEXTURE).isPresent()) {
                int vOffset = 0;
                if (this.isHoveredOrFocused()) {
                    vOffset = this.isActive() && mc.mouseHandler.isLeftPressed() ? this.height * 2 : this.height;
                }
                int middleWidth = Math.max(0, this.width - OPTION_ATLAS_SLICE_WIDTH * 2);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                drawOptionSlice(
                        guiGraphics,
                        this.getX(),
                        this.getY(),
                        OPTION_ATLAS_SLICE_WIDTH,
                        0,
                        vOffset,
                        OPTION_ATLAS_SLICE_WIDTH);
                if (middleWidth > 0) {
                    drawOptionSlice(
                            guiGraphics,
                            this.getX() + OPTION_ATLAS_SLICE_WIDTH,
                            this.getY(),
                            middleWidth,
                            OPTION_ATLAS_SLICE_WIDTH,
                            vOffset,
                            OPTION_ATLAS_SLICE_WIDTH);
                }
                drawOptionSlice(
                        guiGraphics,
                        this.getX() + OPTION_ATLAS_SLICE_WIDTH + middleWidth,
                        this.getY(),
                        OPTION_ATLAS_SLICE_WIDTH,
                        OPTION_ATLAS_SLICE_WIDTH * 2,
                        vOffset,
                        OPTION_ATLAS_SLICE_WIDTH);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();
            }
            int textColor = this.active ? 0xFFFFFF : 0xA0A0A0;
            int textX = this.getX() + OPTION_TEXT_LEFT_PADDING;
            int textY = this.getY() + (this.height - mc.font.lineHeight) / 2;
            guiGraphics.drawString(mc.font, this.label, textX, textY, textColor, false);
        }

        private void drawOptionSlice(
                GuiGraphics guiGraphics, int x, int y, int width, int uOffset, int vOffset, int sourceWidth) {
            guiGraphics.blit(
                    OPTION_BUTTON_ATLAS_TEXTURE,
                    x,
                    y,
                    width,
                    this.height,
                    uOffset,
                    vOffset,
                    sourceWidth,
                    this.height,
                    OPTION_ATLAS_WIDTH,
                    this.height * OPTION_ATLAS_STATE_COUNT);
        }
    }
}
