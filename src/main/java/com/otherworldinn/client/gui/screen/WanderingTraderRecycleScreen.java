package com.otherworldinn.client.gui.screen;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.inventory.WanderingTraderRecycleMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WanderingTraderRecycleScreen extends AbstractContainerScreen<WanderingTraderRecycleMenu> {

    private static final ResourceLocation RECYCLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "textures/gui/recycle.png");
    private static final ResourceLocation INVENTORY_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    private static final int PLAYER_INV_Y = 84;

    public WanderingTraderRecycleScreen(
            WanderingTraderRecycleMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 上半：自定义纹理（回收物品槽位区域）
        graphics.blit(RECYCLE_TEXTURE, x, y, 0, 0, this.imageWidth, PLAYER_INV_Y, this.imageWidth, PLAYER_INV_Y);

        // 下半：原版物品栏纹理
        int bottomHeight = this.imageHeight - PLAYER_INV_Y;
        graphics.blit(INVENTORY_TEXTURE,
                x, y + PLAYER_INV_Y,
                0, PLAYER_INV_Y,
                this.imageWidth, bottomHeight,
                256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // 居中靠上显示总价值
        int total = this.menu.getTotalPrice();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        String icon = "\uE001";
        String value = Integer.toString(total);
        int iconWidth = this.font.width(icon);
        int valueWidth = this.font.width(value);
        int gap = 0;
        int totalTextWidth = iconWidth + gap + valueWidth;
        int textX = x + (this.imageWidth - totalTextWidth) / 2;
        int textY = y + 6;

        graphics.drawString(this.font, icon, textX, textY, 0xFFFFFFFF, false);
        graphics.drawString(this.font, value, textX + iconWidth + gap, textY, 0xFFFFFFFF, false);

        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
