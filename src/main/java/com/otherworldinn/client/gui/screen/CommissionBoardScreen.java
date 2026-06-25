package com.otherworldinn.client.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.C2SAcceptCommissionPacket;
import com.otherworldinn.world.photo.PhotoObjectiveRegistry;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.otherworldinn.world.inventory.CommissionBoardMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class CommissionBoardScreen extends AbstractContainerScreen<CommissionBoardMenu> {
    private static final int CARD_WIDTH = 155;
    private static final int CARD_GAP = 40;
    private static final int BOARD_WIDTH_TWO = CARD_WIDTH * 2 + CARD_GAP + 20;
    private static final int BOARD_WIDTH_ONE = 175;
    private static final int BOARD_HEIGHT = 252;
    private static final int BASE_BUTTON_HEIGHT = 18;
    private static final ResourceLocation BOARD_TEXTURE_ONE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/gui/commission/commission_board.png");
    private static final ResourceLocation ACCEPT_BUTTON_ATLAS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/gui/commission/commission_accept_button_atlas.png");
    private static final int BUTTON_ATLAS_STATE_COUNT = 3;
    private static final int COLOR_PRIMARY = 0xFFB38A3D;
    private static final int COLOR_SECONDARY = 0xFF5C73A0;
    private static final int COLOR_ACCENT = 0xFFA85A5A;
    private static final int COLOR_BODY = 0xFF000000;
    private static final int COLOR_MUTED = 0xFF6A6A6A;
    private static final int COLOR_REQ = 0xFF6C6FA8;
    private static final int COLOR_REWARD = 0xFF4B8A58;

    private CompoundTag boardData;

    private int leftPos;
    private int topPos;

    public CommissionBoardScreen(CompoundTag boardData) {
        super(
                new CommissionBoardMenu(0, Minecraft.getInstance().player.getInventory()),
                Minecraft.getInstance().player.getInventory(),
                Component.translatable("screen.otherworldinn.commission_board.title"));
        this.boardData = boardData == null ? new CompoundTag() : boardData.copy();
        this.imageWidth = BOARD_WIDTH_ONE;
        this.imageHeight = BOARD_HEIGHT;
    }

    public void updateBoardData(CompoundTag data) {
        this.boardData = data == null ? new CompoundTag() : data.copy();
        rebuildLayout();
    }

    @Override
    protected void init() {
        super.init();
        rebuildLayout();
    }

    private boolean isTwoCardMode() {
        return boardData.getInt("AcceptedIndex") < 0
                && !boardData.getBoolean("RewardClaimed")
                && getEntries().size() >= 2;
    }

    private void rebuildLayout() {
        clearWidgets();
        if (isTwoCardMode()) {
            this.imageWidth = BOARD_WIDTH_TWO;
        } else {
            this.imageWidth = BOARD_WIDTH_ONE;
        }
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - BOARD_HEIGHT) / 2;

        int acceptedIndex = boardData.getInt("AcceptedIndex");
        ListTag entries = getEntries();
        boolean rewardClaimed = boardData.getBoolean("RewardClaimed");
        boolean hasEntry = !entries.isEmpty();

        if (rewardClaimed) {
            return;
        }
        if (acceptedIndex < 0 && entries.size() >= 2) {
            for (int slot = 0; slot < 2; slot++) {
                int cardX = this.leftPos + 10 + slot * (CARD_WIDTH + CARD_GAP);
                int cardY = this.topPos + 25;
                int buttonWidth = Math.max(48, (CARD_WIDTH - 16) / 2);
                int buttonX = cardX + (CARD_WIDTH - buttonWidth) / 2;
                int btnY = cardY + 187;
                final int acceptIndex = slot;

                Button btn =
                        new CommissionAcceptButton(
                                buttonX,
                                btnY,
                                buttonWidth,
                                BASE_BUTTON_HEIGHT,
                                Component.translatable("message.otherworldinn.commission.accept"),
                                b -> ModMessages.sendToServer(new C2SAcceptCommissionPacket(acceptIndex)));
                btn.active = hasEntry;
                if (!hasEntry) {
                    btn.setMessage(Component.translatable("message.otherworldinn.commission.empty"));
                    btn.active = false;
                }
                addRenderableWidget(btn);
            }
        } else {
            int cardX = this.leftPos + 10;
            int cardY = this.topPos + 25;
            int buttonWidth = Math.max(48, (CARD_WIDTH - 16) / 2);
            int buttonX = cardX + (CARD_WIDTH - buttonWidth) / 2;
            int btnY = cardY + 187;

            Button btn =
                    new CommissionAcceptButton(
                            buttonX,
                            btnY,
                            buttonWidth,
                            BASE_BUTTON_HEIGHT,
                            Component.translatable("message.otherworldinn.commission.accepted")
                                    .setStyle(Style.EMPTY.withBold(false).withItalic(false)),
                            b -> {});
            btn.active = false;
            if (!hasEntry) {
                btn.setMessage(Component.translatable("message.otherworldinn.commission.empty"));
            }
            addRenderableWidget(btn);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        if (isTwoCardMode()) {
            for (int slot = 0; slot < 2; slot++) {
                int cardLeft = this.leftPos + slot * (CARD_WIDTH + CARD_GAP);
                if (mc.getResourceManager().getResource(BOARD_TEXTURE_ONE).isPresent()) {
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    guiGraphics.blit(
                            BOARD_TEXTURE_ONE,
                            cardLeft,
                            this.topPos,
                            0,
                            0,
                            BOARD_WIDTH_ONE,
                            BOARD_HEIGHT,
                            BOARD_WIDTH_ONE,
                            BOARD_HEIGHT);
                    RenderSystem.disableBlend();
                } else {
                    guiGraphics.fill(cardLeft, this.topPos, cardLeft + BOARD_WIDTH_ONE, this.topPos + BOARD_HEIGHT, 0xAA101018);
                    guiGraphics.fill(cardLeft + 1, this.topPos + 1, cardLeft + BOARD_WIDTH_ONE - 1, this.topPos + BOARD_HEIGHT - 1, 0xCC1B1B24);
                }
            }
            return;
        }
        if (mc.getResourceManager().getResource(BOARD_TEXTURE_ONE).isPresent()) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            guiGraphics.blit(
                    BOARD_TEXTURE_ONE,
                    this.leftPos,
                    this.topPos,
                    0,
                    0,
                    BOARD_WIDTH_ONE,
                    BOARD_HEIGHT,
                    BOARD_WIDTH_ONE,
                    BOARD_HEIGHT);
            RenderSystem.disableBlend();
            return;
        }
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + BOARD_WIDTH_ONE, this.topPos + BOARD_HEIGHT, 0xAA101018);
        guiGraphics.fill(this.leftPos + 1, this.topPos + 1, this.leftPos + BOARD_WIDTH_ONE - 1, this.topPos + BOARD_HEIGHT - 1, 0xCC1B1B24);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        ListTag entries = getEntries();
        int acceptedIndex = boardData.getInt("AcceptedIndex");
        long expireDay = boardData.getLong("ExpireDay");
        long currentDay = boardData.getLong("CurrentDay");
        boolean rewardClaimed = boardData.getBoolean("RewardClaimed");

        ItemStack hoveredStack = ItemStack.EMPTY;
        ItemStack[] hoverHolder = new ItemStack[] {ItemStack.EMPTY};

        if (rewardClaimed) {
            int cardX = this.leftPos + 10;
            int cardY = this.topPos + 25;
            List<FormattedCharSequence> lines =
                    this.font.split(
                            Component.translatable("message.otherworldinn.commission.no_new"),
                            CARD_WIDTH - 16);
            int textY = cardY + 187 / 2 - (Math.min(2, lines.size()) * 10) / 2;
            for (int li = 0; li < Math.min(2, lines.size()); li++) {
                FormattedCharSequence line = lines.get(li);
                int lineX = cardX + (CARD_WIDTH - this.font.width(line)) / 2;
                guiGraphics.drawString(this.font, line, lineX, textY, COLOR_MUTED, false);
                textY += 10;
            }
        } else if (acceptedIndex < 0 && entries.size() >= 2) {
            for (int i = 0; i < 2; i++) {
                if (entries.get(i) instanceof CompoundTag entry) {
                    int cardX = this.leftPos + 10 + i * (CARD_WIDTH + CARD_GAP);
                    hoverHolder = renderCard(guiGraphics, entry, cardX, false, expireDay, currentDay, mouseX, mouseY, hoverHolder);
                }
            }
            hoveredStack = hoverHolder[0];
        } else if (!entries.isEmpty() && entries.get(Math.max(0, acceptedIndex)) instanceof CompoundTag entry) {
            int cardX = this.leftPos + 10;
            boolean showKillProgress = acceptedIndex >= 0 && !rewardClaimed;
            hoverHolder = renderCard(guiGraphics, entry, cardX, showKillProgress, expireDay, currentDay, mouseX, mouseY, hoverHolder);
            hoveredStack = hoverHolder[0];
        } else {
            int cardX = this.leftPos + 10;
            int cardY = this.topPos + 25;
            guiGraphics.drawString(
                    this.font,
                    Component.translatable("message.otherworldinn.commission.empty"),
                    cardX + 8,
                    cardY + 12,
                    COLOR_MUTED,
                    false);
        }
        if (!hoveredStack.isEmpty()) {
            guiGraphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
        }
    }

    private ItemStack[] renderCard(
            GuiGraphics guiGraphics,
            CompoundTag entry,
            int cardX,
            boolean showKillProgress,
            long expireDay,
            long currentDay,
            int mouseX,
            int mouseY,
            ItemStack[] currentHover) {
        int cardY = this.topPos + 25;
        int cardHeight = 192;
        int contentBottomY = cardY + cardHeight - 30;

        int stars = entry.getInt("Stars");
        long duration = entry.getLong("DurationDays");
        Component difficultyLabel = Component.translatable("message.otherworldinn.commission.difficulty");
        guiGraphics.drawString(this.font, difficultyLabel, cardX + 8, cardY + 10, COLOR_PRIMARY, false);
        int starsX = cardX + 8 + this.font.width(difficultyLabel) + 4;
        int maxStars = Math.max(1, Math.min(5, stars));
        String starsText = "\uE005".repeat(maxStars);
        guiGraphics.drawString(this.font, Component.literal(starsText), starsX, cardY + 10, 0xFFFFFFFF, false);
        guiGraphics.drawString(
                this.font,
                Component.translatable("message.otherworldinn.commission.limit_day", duration),
                cardX + 8,
                cardY + 24,
                COLOR_SECONDARY,
                false);

        int lineY = drawDescription(guiGraphics, entry, cardX, cardY + 38, CARD_WIDTH, contentBottomY);
        lineY =
                drawRequirements(
                        guiGraphics,
                        entry,
                        cardX + 8,
                        lineY,
                        CARD_WIDTH - 16,
                        contentBottomY,
                        mouseX,
                        mouseY,
                        currentHover,
                        showKillProgress);
        drawRewards(
                guiGraphics,
                entry,
                cardX + 8,
                Math.min(lineY + 4, contentBottomY),
                CARD_WIDTH - 16,
                contentBottomY,
                mouseX, mouseY, currentHover);

        if (showKillProgress) {
            long remainingDays = Math.max(0L, expireDay - currentDay);
            Component remainText =
                    remainingDays <= 0L
                            ? Component.translatable("message.otherworldinn.commission.today")
                            : Component.translatable(
                                    "message.otherworldinn.commission.remaining_day", remainingDays);
            guiGraphics.drawString(
                    this.font,
                    remainText,
                    cardX + 8,
                    cardY + cardHeight - 36,
                    COLOR_ACCENT,
                    false);
        }
        return currentHover;
    }

    private int drawRequirements(
            GuiGraphics guiGraphics,
            CompoundTag entry,
            int x,
            int y,
            int maxWidth,
            int bottomY,
            int mouseX,
            int mouseY,
            ItemStack[] currentHover,
            boolean showKillProgress) {
        int lineY =
                drawSingleLineClamped(
                        guiGraphics,
                        Component.translatable("message.otherworldinn.commission.requirement"),
                        x,
                        y,
                        maxWidth,
                        bottomY,
                        COLOR_REQ);
        lineY += 2;
        if (entry.contains("SubmitRequirements", Tag.TAG_LIST)) {
            ListTag submit = entry.getList("SubmitRequirements", Tag.TAG_COMPOUND);
            for (Tag t : submit) {
                if (t instanceof CompoundTag req) {
                    lineY =
                            drawItemEntryLine(
                                    guiGraphics,
                                    req.getString("ItemId"),
                                    req.getInt("Count"),
                                    x,
                                    lineY,
                                    maxWidth,
                                    bottomY,
                                    "message.otherworldinn.commission.line.submit_icon",
                                    mouseX, mouseY, currentHover);
                }
            }
        }
        if (entry.contains("KillRequirements", Tag.TAG_LIST)) {
            ListTag kills = entry.getList("KillRequirements", Tag.TAG_COMPOUND);
            for (Tag t : kills) {
                if (t instanceof CompoundTag req) {
                    Component entityName = entityNameComponent(req.getString("EntityTypeId"));
                    int required = req.getInt("Count");
                    Component line;
                    if (showKillProgress) {
                        int current = getKillProgress(req.getString("EntityTypeId"));
                        line =
                                Component.translatable(
                                        "message.otherworldinn.commission.line.kill_progress",
                                        entityName,
                                        current,
                                        required);
                    } else {
                        line =
                                Component.translatable(
                                        "message.otherworldinn.commission.line.kill",
                                        entityName,
                                        required);
                    }
                    lineY = drawSingleLineClamped(guiGraphics, line, x, lineY, maxWidth, bottomY, COLOR_BODY);
                }
            }
        }
        if (entry.contains("PhotoRequirements", Tag.TAG_LIST)) {
            ListTag photos = entry.getList("PhotoRequirements", Tag.TAG_COMPOUND);
            for (Tag t : photos) {
                if (t instanceof CompoundTag req) {
                    ResourceLocation objectiveId =
                            ResourceLocation.tryParse(req.getString("ObjectiveId"));
                    Component objectiveName =
                            objectiveId == null
                                    ? Component.literal(req.getString("ObjectiveId"))
                                    : PhotoObjectiveRegistry.getDisplayName(objectiveId);
                    Component line;
                    if (showKillProgress) {
                        int current = objectiveId != null && hasPhotoProgress(objectiveId.toString()) ? 1 : 0;
                        line =
                                Component.translatable(
                                        "message.otherworldinn.commission.line.photo_progress",
                                        objectiveName,
                                        current,
                                        1);
                    } else {
                        line =
                                Component.translatable(
                                        "message.otherworldinn.commission.line.photo",
                                        objectiveName);
                    }
                    lineY = drawSingleLineClamped(guiGraphics, line, x, lineY, maxWidth, bottomY, COLOR_BODY);
                }
            }
        }
        return lineY;
    }

    private int getKillProgress(String entityId) {
        CompoundTag commissionData = boardData.getCompound("CommissionData");
        if (!commissionData.contains("KillProgress", Tag.TAG_LIST)) {
            return 0;
        }
        ListTag killProgress = commissionData.getList("KillProgress", Tag.TAG_COMPOUND);
        for (Tag tag : killProgress) {
            if (tag instanceof CompoundTag progressTag
                    && entityId.equals(progressTag.getString("EntityId"))) {
                return Math.max(0, progressTag.getInt("Count"));
            }
        }
        return 0;
    }

    private boolean hasPhotoProgress(String objectiveId) {
        CompoundTag commissionData = boardData.getCompound("CommissionData");
        if (!commissionData.contains("PhotoProgress", Tag.TAG_LIST)) {
            return false;
        }
        ListTag photoProgress = commissionData.getList("PhotoProgress", Tag.TAG_STRING);
        for (Tag tag : photoProgress) {
            if (objectiveId.equals(tag.getAsString())) {
                return true;
            }
        }
        return false;
    }

    private int drawDescription(
            GuiGraphics guiGraphics,
            CompoundTag entry,
            int cardX,
            int startY,
            int cardWidth,
            int bottomY) {
        String descriptionKey = entry.getString("DescriptionKey");
        if (descriptionKey == null || descriptionKey.isBlank()) {
            return startY + 8;
        }
        List<FormattedCharSequence> lines =
                this.font.split(Component.translatable(descriptionKey), cardWidth - 20);
        int y = startY;
        int maxLines = Math.min(3, lines.size());
        for (int i = 0; i < maxLines; i++) {
            if (y > bottomY - 9) {
                break;
            }
            FormattedCharSequence line = lines.get(i);
            int lineX = cardX + (cardWidth - this.font.width(line)) / 2;
            guiGraphics.drawString(this.font, line, lineX, y, COLOR_BODY, false);
            y += 10;
        }
        return y + 4;
    }

    private static class CommissionAcceptButton extends Button {
        protected CommissionAcceptButton(
                int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getResourceManager().getResource(ACCEPT_BUTTON_ATLAS_TEXTURE).isPresent()) {
                int vOffset = 0;
                if (this.isHoveredOrFocused()) {
                    vOffset =
                            this.isActive() && mc.mouseHandler.isLeftPressed()
                                    ? this.height * 2
                                    : this.height;
                }
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                guiGraphics.blit(
                        ACCEPT_BUTTON_ATLAS_TEXTURE,
                        this.getX(),
                        this.getY(),
                        0,
                        vOffset,
                        this.width,
                        this.height,
                        this.width,
                        this.height * BUTTON_ATLAS_STATE_COUNT);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();
            } else {
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            }
            int textColor = this.active ? COLOR_BODY : COLOR_MUTED;
            int textX = this.getX() + (this.width - mc.font.width(this.getMessage())) / 2;
            guiGraphics.drawString(
                    mc.font, this.getMessage(), textX, this.getY() + (this.height - 8) / 2, textColor, false);
        }
    }

    private int drawRewards(
            GuiGraphics guiGraphics, CompoundTag entry, int x, int y, int maxWidth, int bottomY, int mouseX, int mouseY, ItemStack[] currentHover) {
        int lineY =
                drawSingleLineClamped(
                        guiGraphics,
                        Component.translatable("message.otherworldinn.commission.reward"),
                        x,
                        y,
                        maxWidth,
                        bottomY,
                        COLOR_REWARD);
        lineY += 2;
        int coin = entry.getInt("CoinReward");
        if (coin > 0) {
            lineY = drawCoinLine(guiGraphics, coin, x, lineY, maxWidth, bottomY);
        }
        if (entry.contains("ItemRewards", Tag.TAG_LIST)) {
            ListTag rewards = entry.getList("ItemRewards", Tag.TAG_COMPOUND);
            for (Tag t : rewards) {
                if (t instanceof CompoundTag reward) {
                    lineY =
                            drawItemEntryLine(
                                    guiGraphics,
                                    reward.getString("ItemId"),
                                    reward.getInt("Count"),
                                    x,
                                    lineY,
                                    maxWidth,
                                    bottomY,
                                    "message.otherworldinn.commission.line.reward_item_icon",
                                    mouseX, mouseY, currentHover);
                }
            }
        }
        if (entry.contains("NpcFavorRewards", Tag.TAG_LIST)) {
            ListTag favors = entry.getList("NpcFavorRewards", Tag.TAG_COMPOUND);
            for (Tag t : favors) {
                if (t instanceof CompoundTag favor) {
                    Component npcName = entityNameComponent(favor.getString("NpcEntityTypeId"));
                    Component line =
                            Component.translatable(
                                    "message.otherworldinn.commission.line.favor",
                                    npcName,
                                    favor.getInt("FavorProgress"));
                    lineY = drawSingleLineClamped(guiGraphics, line, x, lineY, maxWidth, bottomY, COLOR_BODY);
                }
            }
        }
        return lineY;
    }

    private int drawSingleLineClamped(
            GuiGraphics guiGraphics,
            Component text,
            int x,
            int y,
            int maxWidth,
            int bottomY,
            int color) {
        if (y > bottomY - 9) {
            return y;
        }
        String raw = text.getString();
        String fitted = this.font.plainSubstrByWidth(raw, Math.max(1, maxWidth));
        if (fitted.length() < raw.length()) {
            String ellipsis = "...";
            fitted =
                    this.font.plainSubstrByWidth(
                                    raw, Math.max(1, maxWidth - this.font.width(ellipsis)))
                            + ellipsis;
        }
        guiGraphics.drawString(this.font, Component.literal(fitted), x, y, color, false);
        return y + 10;
    }

    private int drawItemEntryLine(
            GuiGraphics guiGraphics,
            String itemId,
            int count,
            int x,
            int y,
            int maxWidth,
            int bottomY,
            String textKey,
            int mouseX, int mouseY, ItemStack[] currentHover) {
        if (y > bottomY - 16) {
            return y;
        }
        ItemStack stack = stackFromItemId(itemId, count);
        Component label = Component.translatable(textKey);
        int nextY =
                drawSingleLineClamped(
                        guiGraphics,
                        label,
                        x,
                        y + 4,
                        Math.max(1, maxWidth - 18),
                        bottomY,
                        COLOR_BODY);
        if (!stack.isEmpty()) {
            int iconX = x + this.font.width(label) + 2;
            guiGraphics.renderItem(stack, iconX, y);
            guiGraphics.renderItemDecorations(this.font, stack, iconX, y);
            if (mouseX >= iconX && mouseX < iconX + 16 && mouseY >= y && mouseY < y + 16) {
                currentHover[0] = stack;
            }
        }
        return Math.max(nextY, y + 18);
    }

    private int drawCoinLine(
            GuiGraphics guiGraphics, int coin, int x, int y, int maxWidth, int bottomY) {
        if (y > bottomY - 9) {
            return y;
        }
        Component dash = Component.literal("- ");
        guiGraphics.drawString(this.font, dash, x, y, COLOR_BODY, false);
        int iconX = x + this.font.width(dash);
        Component icon = Component.literal("\uE001");
        guiGraphics.drawString(this.font, icon, iconX, y, 0xFFFFFFFF, false);
        int valueX = iconX + this.font.width(icon) + 1;
        int remainWidth = Math.max(1, maxWidth - (valueX - x));
        String raw = Integer.toString(coin);
        String fitted = this.font.plainSubstrByWidth(raw, remainWidth);
        if (fitted.length() < raw.length()) {
            String ellipsis = "...";
            fitted =
                    this.font.plainSubstrByWidth(raw, Math.max(1, remainWidth - this.font.width(ellipsis)))
                            + ellipsis;
        }
        guiGraphics.drawString(this.font, Component.literal(fitted), valueX, y, COLOR_BODY, false);
        return y + 10;
    }

    private Component itemNameComponent(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return Component.literal(itemId);
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null) {
            return Component.literal(itemId);
        }
        return Component.translatable(item.getDescriptionId());
    }

    private ItemStack stackFromItemId(String itemId, int count) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, Math.max(1, Math.min(99, count)));
    }

    private Component entityNameComponent(String entityId) {
        ResourceLocation id = ResourceLocation.tryParse(entityId);
        if (id == null) {
            return Component.literal(entityId);
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        if (type == null) {
            return Component.literal(entityId);
        }
        return type.getDescription();
    }

    private ListTag getEntries() {
        CompoundTag commissionData = boardData.getCompound("CommissionData");
        if (commissionData.contains("BoardEntries", Tag.TAG_LIST)) {
            return commissionData.getList("BoardEntries", Tag.TAG_COMPOUND);
        }
        return new ListTag();
    }
}
