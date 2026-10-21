package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.inn.facility.FacilityRegistry;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(
        modid = OtherworldInn.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public class FacilityUpgradeOverlay {
    private static final int PANEL_PADDING = 6;
    private static final int PANEL_RIGHT_SAFE_EXTRA = 24;
    private static final int CROSSHAIR_RIGHT_OFFSET = 14;
    private static final int CROSSHAIR_DOWN_OFFSET = 12;
    private static final int LINE_HEIGHT = 10;
    private static final int LINE_GAP = 2;
    private static final int LINE_ADVANCE = LINE_HEIGHT + LINE_GAP;
    private static final int INDENT = 8;
    private static final int ICON_SIZE = 16;
    private static final int ICON_GAP = 2;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemHudOverlay.register(24, (unused) -> shouldShow(), FacilityUpgradeOverlay::render);
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return false;
        }
        if (player.level().dimension() != TownDimensions.TOWN_LEVEL) {
            return false;
        }
        if (!FacilityRegistry.isHoldingFacilityTool(player)) {
            return false;
        }
        return FacilityRegistry.findFacilityInRange(player.blockPosition()).isPresent();
    }

    private static void render(
            GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        FacilityRegistry.FacilityDefinition facility =
                FacilityRegistry.findFacilityInRange(player.blockPosition()).orElse(null);
        if (facility == null) {
            return;
        }

        int currentLevel = team.getInnData().getFacilityLevel(facility.id());
        FacilityRegistry.FacilityLevelDefinition next =
                currentLevel < facility.maxLevel() ? facility.getLevel(currentLevel + 1) : null;
        boolean repairing = currentLevel == 0;
        List<ItemStack> requiredItems = next == null ? List.of() : next.requiredItems();
        boolean insufficientCoins = next != null && team.getCoins() < next.requiredCoins();

        Component titleText =
                Component.translatable("facility.otherworldinn.overlay.title")
                        .append(" - ")
                        .append(Component.translatable(facility.translationKey()));
        Component levelText =
                Component.translatable(
                        "facility.otherworldinn.overlay.level", currentLevel, facility.maxLevel());
        Component costText =
                next == null
                        ? Component.translatable("facility.otherworldinn.upgrade_max_level")
                        : Component.translatable(
                                repairing
                                        ? "facility.otherworldinn.overlay.repair_cost"
                                        : "facility.otherworldinn.overlay.upgrade_cost",
                                Component.literal(com.otherworldinn.foundation.CoinFormat.format(next.requiredCoins()))
                                        .withStyle(
                                                style ->
                                                        style.withColor(
                                                                insufficientCoins
                                                                        ? ModColors.ERROR
                                                                        : ModColors.SUCCESS)));
        Component materialLabel =
                Component.translatable(
                        repairing
                                ? "facility.otherworldinn.overlay.repair_items"
                                : "facility.otherworldinn.overlay.upgrade_items",
                        "");

        int textWidth =
                Math.max(
                        mc.font.width(titleText),
                        Math.max(
                                mc.font.width(levelText),
                                Math.max(mc.font.width(costText), mc.font.width(materialLabel))));
        int iconsWidth =
                requiredItems.isEmpty()
                        ? mc.font.width(Component.translatable("facility.otherworldinn.overlay.no_items"))
                        : requiredItems.size() * ICON_SIZE + Math.max(0, requiredItems.size() - 1) * ICON_GAP;
        int contentWidth = Math.max(textWidth + PANEL_RIGHT_SAFE_EXTRA, INDENT + iconsWidth + 2);
        int baseLineCount = 3;
        int contentHeight = baseLineCount * LINE_HEIGHT + (baseLineCount - 1) * LINE_GAP;
        if (next != null) {
            int materialHeaderHeight = LINE_HEIGHT + LINE_GAP;
            int materialValueHeight = ICON_SIZE;
            contentHeight += materialHeaderHeight + materialValueHeight;
        }
        int panelWidth = contentWidth + PANEL_PADDING * 2;
        int panelHeight = contentHeight + PANEL_PADDING * 2;

        int preferredX = guiGraphics.guiWidth() / 2 + CROSSHAIR_RIGHT_OFFSET;
        int preferredY = guiGraphics.guiHeight() / 2 - panelHeight / 2 + CROSSHAIR_DOWN_OFFSET;
        int panelX = Math.min(preferredX, guiGraphics.guiWidth() - panelWidth - 4);
        panelX = Math.max(4, panelX);
        int panelY = Math.max(4, Math.min(preferredY, guiGraphics.guiHeight() - panelHeight - 4));
        guiGraphics.fill(
                panelX,
                panelY,
                panelX + panelWidth,
                panelY + panelHeight,
                ModColors.BLACK_ALPHA_62);

        int textX = panelX + PANEL_PADDING;
        int y = panelY + PANEL_PADDING;
        guiGraphics.drawString(mc.font, titleText, textX, y, ModColors.INFO, true);
        y += LINE_ADVANCE;
        guiGraphics.drawString(mc.font, levelText, textX + INDENT, y, ModColors.YELLOW, true);
        y += LINE_ADVANCE;
        guiGraphics.drawString(
                mc.font,
                costText,
                textX + INDENT,
                y,
                next == null ? ModColors.INFO : ModColors.WHITE,
                true);

        if (next != null) {
            y += LINE_ADVANCE;
            guiGraphics.drawString(mc.font, materialLabel, textX + INDENT, y, ModColors.GRAY_LIGHT, true);
            y += LINE_ADVANCE;
            renderRequiredItems(guiGraphics, mc, player, requiredItems, textX + INDENT, y);
        }

        Component actionText =
                repairing
                        ? Component.translatable("facility.otherworldinn.overlay.repair")
                        : Component.translatable("facility.otherworldinn.overlay.upgrade");
        ItemHudOverlay.renderMouseActions(
                guiGraphics,
                new ItemHudOverlay.MouseAction(ItemHudOverlay.MouseButton.RIGHT, actionText));
    }

    private static void renderRequiredItems(
            GuiGraphics guiGraphics, Minecraft mc, Player player, List<ItemStack> items, int x, int y) {
        if (items == null || items.isEmpty()) {
            guiGraphics.drawString(
                    mc.font,
                    Component.translatable("facility.otherworldinn.overlay.no_items"),
                    x,
                    y,
                    ModColors.GRAY_LIGHT,
                    true);
            return;
        }
        int currentX = x;
        for (ItemStack stack : items) {
            int owned = countOwnedItems(player, stack);
            boolean insufficient = owned < stack.getCount();
            guiGraphics.renderItem(stack, currentX, y);
            String countText = String.valueOf(stack.getCount());
            int textX = currentX + ICON_SIZE - mc.font.width(countText);
            int textY = y + ICON_SIZE - 8;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 200);
            guiGraphics.drawString(
                    mc.font,
                    countText,
                    textX,
                    textY,
                    insufficient ? ModColors.ERROR : ModColors.SUCCESS,
                    true);
            guiGraphics.pose().popPose();
            currentX += ICON_SIZE + ICON_GAP;
        }
    }

    private static int countOwnedItems(Player player, ItemStack required) {
        if (player == null || required == null || required.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (ItemStack.isSameItemSameComponents(slot, required)) {
                total += slot.getCount();
            }
        }
        return total;
    }
}
