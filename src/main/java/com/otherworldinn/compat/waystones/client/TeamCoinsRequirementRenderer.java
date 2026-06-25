package com.otherworldinn.compat.waystones.client;

import com.otherworldinn.compat.waystones.TeamCoinsRequirement;
import com.otherworldinn.init.ModItems;
import net.blay09.mods.waystones.client.requirement.RequirementRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TeamCoinsRequirementRenderer implements RequirementRenderer<TeamCoinsRequirement> {
    @Override
    public void renderWidget(
            Player player,
            TeamCoinsRequirement requirement,
            GuiGraphics guiGraphics,
            int mouseX,
            int mouseY,
            float partialTick,
            int x,
            int y) {
        Font font = Minecraft.getInstance().font;
        ItemStack coinStack = new ItemStack(ModItems.COIN.get());

        if (!requirement.canAfford(player)) {
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
        }

        guiGraphics.renderItem(coinStack, x, y, 16, 16);
        guiGraphics.renderItemDecorations(
                font,
                coinStack,
                x,
                y,
                requirement.getCoins() > 1 ? String.valueOf(requirement.getCoins()) : null);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
