package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.PlayerEasterEggFlags;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.C2SWithdrawCoinPacket;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.decoration.InnDecorationBuffType;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public class InventoryTeamOverlay {
    private static final int INVENTORY_WIDTH = 176;
    private static final int INVENTORY_HEIGHT = 166;
    private static final int CONTENT_INSET = 6;
    private static final int BAR_WIDTH = INVENTORY_WIDTH;
    private static final int BAR_HEIGHT = 16;
    private static final int RATING_ROWS = 6;
    private static final ResourceLocation RATING_BACKGROUND_ATLAS =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/gui/overlay/inventory_team_bg_atlas.png");
    private static final ResourceLocation RATING_BACKGROUND_ATLAS_MAIMAI =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/gui/overlay/inventory_team_bg_atlas_maimai.png");

    @SubscribeEvent
    public static void onRenderScreen(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen)) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        if (team == null) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int guiLeft = (guiGraphics.guiWidth() - INVENTORY_WIDTH) / 2;
        int guiTop = (guiGraphics.guiHeight() - INVENTORY_HEIGHT) / 2;
        int barLeft = guiLeft;
        int contentLeft = barLeft + CONTENT_INSET;
        int contentRight = barLeft + BAR_WIDTH - CONTENT_INSET;
        int barY = guiTop - BAR_HEIGHT;
        int y = guiTop - 10;

        int displayRating = Math.max(0, team.getInnData().getRating());
        boolean useMaimaiAtlas = PlayerEasterEggFlags.isMaimaiAtlasEnabled();
        ResourceLocation ratingBackgroundAtlas =
                useMaimaiAtlas ? RATING_BACKGROUND_ATLAS_MAIMAI : RATING_BACKGROUND_ATLAS;
        int clampedRating = Math.min(5, displayRating);
        int atlasV = clampedRating * BAR_HEIGHT;
        guiGraphics.blit(
                ratingBackgroundAtlas,
                barLeft,
                barY,
                0,
                atlasV,
                BAR_WIDTH,
                BAR_HEIGHT,
                BAR_WIDTH,
                BAR_HEIGHT * RATING_ROWS);
        Component coinsText =
                Component.translatable("message.otherworldinn.inventory.overlay.coins", team.getCoins());
        int coinsX = contentRight - mc.font.width(coinsText);

        guiGraphics.drawString(
                mc.font,
                coinsText,
                coinsX,
                y,
                ModColors.WHITE,
                true);

        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();
        int ratingWidth = mc.font.width(Component.literal(String.valueOf('\uE005').repeat(5)));
        int lineHeight = mc.font.lineHeight;
        boolean overRating =
                mouseX >= contentLeft
                && mouseX <= contentLeft + ratingWidth
                && mouseY >= y
                && mouseY <= y + lineHeight;
        if (overRating) {
            InnData innData = team.getInnData();
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(
                    Component.translatable("message.otherworldinn.inventory.overlay.level_up.title")
                            .withStyle(ChatFormatting.GOLD));

            int currentRating = Math.max(0, Math.min(5, innData.getRating()));
            if (currentRating >= 5) {
                tooltip.add(
                        Component.translatable("message.otherworldinn.inventory.overlay.level_up.max")
                                .withStyle(ChatFormatting.YELLOW));
            } else {
                int targetRating = currentRating + 1;
                int currentRooms = innData.getRoomCount();
                int requiredRooms = innData.getRequiredRoomCount(currentRating);
                int currentIncome = innData.getTotalIncome();
                int requiredIncome = innData.getRequiredTotalIncome(currentRating);
                int currentReputation = innData.getReputation();
                int requiredReputation = innData.getMaxReputation(currentRating);

                tooltip.add(
                        Component.translatable(
                                        "message.otherworldinn.inventory.overlay.level_up.target_rating",
                                        targetRating)
                                .withStyle(ChatFormatting.YELLOW));
                boolean roomRequirementMet = currentRooms >= requiredRooms;
                Component roomCurrentText =
                        Component.literal(String.valueOf(currentRooms))
                                .withStyle(
                                        roomRequirementMet ? ChatFormatting.WHITE : ChatFormatting.RED);
                Component roomRequiredText =
                        Component.literal(String.valueOf(requiredRooms))
                                .withStyle(ChatFormatting.WHITE);
                Component roomStatusText =
                        Component.translatable(
                                        roomRequirementMet
                                                ? "message.otherworldinn.inventory.overlay.level_up.status.pass"
                                                : "message.otherworldinn.inventory.overlay.level_up.status.fail")
                                .withStyle(
                                        roomRequirementMet ? ChatFormatting.GREEN : ChatFormatting.RED);
                tooltip.add(
                        Component.literal("  ")
                                .append(
                                        Component.translatable(
                                                        "message.otherworldinn.inventory.overlay.level_up.requirement.rooms",
                                                        roomCurrentText,
                                                        roomRequiredText,
                                                        roomStatusText)
                                                .withStyle(ChatFormatting.GRAY)));
                boolean incomeRequirementMet = currentIncome >= requiredIncome;
                Component incomeCurrentText =
                        Component.literal(String.valueOf(currentIncome))
                                .withStyle(
                                        incomeRequirementMet
                                                ? ChatFormatting.WHITE
                                                : ChatFormatting.RED);
                Component incomeRequiredText =
                        Component.literal(String.valueOf(requiredIncome))
                                .withStyle(ChatFormatting.WHITE);
                Component incomeStatusText =
                        Component.translatable(
                                        incomeRequirementMet
                                                ? "message.otherworldinn.inventory.overlay.level_up.status.pass"
                                                : "message.otherworldinn.inventory.overlay.level_up.status.fail")
                                .withStyle(
                                        incomeRequirementMet
                                                ? ChatFormatting.GREEN
                                                : ChatFormatting.RED);
                tooltip.add(
                        Component.literal("  ")
                                .append(
                                        Component.translatable(
                                                        "message.otherworldinn.inventory.overlay.level_up.requirement.income",
                                                        incomeCurrentText,
                                                        incomeRequiredText,
                                                        incomeStatusText)
                                                .withStyle(ChatFormatting.GRAY)));
                boolean reputationRequirementMet = currentReputation >= requiredReputation;
                Component reputationCurrentText =
                        Component.literal(String.valueOf(currentReputation))
                                .withStyle(
                                        reputationRequirementMet
                                                ? ChatFormatting.WHITE
                                                : ChatFormatting.RED);
                Component reputationRequiredText =
                        Component.literal(String.valueOf(requiredReputation))
                                .withStyle(ChatFormatting.WHITE);
                Component reputationStatusText =
                        Component.translatable(
                                        reputationRequirementMet
                                                ? "message.otherworldinn.inventory.overlay.level_up.status.pass"
                                                : "message.otherworldinn.inventory.overlay.level_up.status.fail")
                                .withStyle(
                                        reputationRequirementMet
                                                ? ChatFormatting.GREEN
                                                : ChatFormatting.RED);
                tooltip.add(
                        Component.literal("  ")
                                .append(
                                        Component.translatable(
                                                        "message.otherworldinn.inventory.overlay.level_up.requirement.reputation",
                                                        reputationCurrentText,
                                                        reputationRequiredText,
                                                        reputationStatusText)
                                                .withStyle(ChatFormatting.GRAY)));
            }
            renderTooltip(guiGraphics, mc, tooltip, mouseX, mouseY);
            return;
        }

        int coinsWidth = mc.font.width(coinsText);
        boolean overCoins =
                mouseX >= coinsX
                && mouseX <= coinsX + coinsWidth
                && mouseY >= y
                && mouseY <= y + lineHeight;
        if (overCoins) {
            InnData innData = team.getInnData();
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(
                    Component.translatable("message.otherworldinn.inventory.overlay.income.title")
                            .withStyle(ChatFormatting.GOLD));
            tooltip.add(
                    Component.translatable(
                                    "message.otherworldinn.inventory.overlay.income.total",
                                    innData.getTotalIncome())
                            .withStyle(ChatFormatting.YELLOW));
            tooltip.add(
                    Component.literal("  ")
                            .append(
                                    Component.translatable(
                                                    "message.otherworldinn.inventory.overlay.income.lodging",
                                                    innData.getTotalLodgingIncome())
                                            .withStyle(ChatFormatting.GRAY)));
            tooltip.add(
                    Component.literal("  ")
                            .append(
                                    Component.translatable(
                                                    "message.otherworldinn.inventory.overlay.income.dining",
                                                    innData.getTotalDiningIncome())
                                            .withStyle(ChatFormatting.GRAY)));
            tooltip.add(
                    Component.literal("  ")
                            .append(
                                    Component.translatable(
                                                    "message.otherworldinn.inventory.overlay.income.other",
                                                    innData.getTotalOtherIncome())
                                            .withStyle(ChatFormatting.GRAY)));
            tooltip.add(
                    Component.translatable("message.otherworldinn.inventory.overlay.income.yesterday")
                            .withStyle(ChatFormatting.AQUA));
            tooltip.add(
                    Component.literal("  ")
                            .append(
                                    Component.translatable(
                                                    "message.otherworldinn.inventory.overlay.income.lodging",
                                                    innData.getYesterdayLodgingIncome())
                                            .withStyle(ChatFormatting.DARK_AQUA)));
            tooltip.add(
                    Component.literal("  ")
                            .append(
                                    Component.translatable(
                                                    "message.otherworldinn.inventory.overlay.income.dining",
                                                    innData.getYesterdayDiningIncome())
                                            .withStyle(ChatFormatting.DARK_AQUA)));
            tooltip.add(
                    Component.literal("  ")
                            .append(
                                    Component.translatable(
                                                    "message.otherworldinn.inventory.overlay.income.other",
                                                    innData.getYesterdayOtherIncome())
                                            .withStyle(ChatFormatting.DARK_AQUA)));
            tooltip.add(Component.empty());
            tooltip.add(
                    Component.translatable("message.otherworldinn.inventory.overlay.coins.withdraw_one")
                            .withStyle(ChatFormatting.GRAY));
            renderTooltip(guiGraphics, mc, tooltip, mouseX, mouseY);
            return;
        }

        boolean overHudBar =
                mouseX >= barLeft
                        && mouseX <= barLeft + BAR_WIDTH
                        && mouseY >= barY
                        && mouseY <= barY + BAR_HEIGHT;
        if (overHudBar) {
            InnData innData = team.getInnData();
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(
                    Component.translatable("message.otherworldinn.inventory.overlay.buffs.title")
                            .withStyle(ChatFormatting.GOLD));
            tooltip.add(
                    Component.translatable(
                                    "tooltip.otherworldinn.decoration.lodging_income",
                                    formatPercentage(
                                            innData.getDecorationBuffValue(
                                                    InnDecorationBuffType.LODGING_INCOME_MULTIPLIER)))
                            .withStyle(style -> style.withColor(ModColors.INN_LODGING_BUFF)));
            tooltip.add(
                    Component.translatable(
                                    "tooltip.otherworldinn.decoration.dining_income",
                                    formatPercentage(
                                            innData.getDecorationBuffValue(
                                                            InnDecorationBuffType.DINING_INCOME_MULTIPLIER)
                                                    + innData.getCurrentDiningVarietyBonusValue()))
                            .withStyle(style -> style.withColor(ModColors.INN_DINING_BUFF)));
            tooltip.add(
                    Component.translatable(
                                    "tooltip.otherworldinn.decoration.guest_arrival_speed",
                                    formatPercentage(
                                            innData.getDecorationBuffValue(
                                                    InnDecorationBuffType
                                                            .GUEST_ARRIVAL_SPEED_MULTIPLIER)))
                            .withStyle(style -> style.withColor(ModColors.INN_GUEST_ARRIVAL_BUFF)));
            tooltip.add(
                    Component.translatable(
                                    "tooltip.otherworldinn.decoration.reputation_gain",
                                    formatPercentage(
                                            innData.getDecorationBuffValue(
                                                    InnDecorationBuffType.REPUTATION_GAIN_MULTIPLIER)))
                            .withStyle(style -> style.withColor(ModColors.INN_REPUTATION_BUFF)));
            renderTooltip(guiGraphics, mc, tooltip, mouseX, mouseY);
        }
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen)) {
            return;
        }
        if (event.getButton() != 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        if (team == null) {
            return;
        }
        int screenWidth = event.getScreen().width;
        int screenHeight = event.getScreen().height;
        int guiLeft = (screenWidth - INVENTORY_WIDTH) / 2;
        int guiTop = (screenHeight - INVENTORY_HEIGHT) / 2;
        int contentRight = guiLeft + BAR_WIDTH - CONTENT_INSET;
        int y = guiTop - 10;
        Component coinsText =
                Component.translatable("message.otherworldinn.inventory.overlay.coins", team.getCoins());
        int coinsWidth = mc.font.width(coinsText);
        int coinsX = contentRight - coinsWidth;
        int lineHeight = mc.font.lineHeight;
        int mouseX = (int) event.getMouseX();
        int mouseY = (int) event.getMouseY();
        if (mouseX >= coinsX
                && mouseX <= coinsX + coinsWidth
                && mouseY >= y
                && mouseY <= y + lineHeight) {
            ModMessages.sendToServer(new C2SWithdrawCoinPacket());
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            event.setCanceled(true);
        }
    }

    private static void renderTooltip(
            GuiGraphics guiGraphics, Minecraft mc, List<Component> tooltip, int mouseX, int mouseY) {
        List<FormattedCharSequence> tooltipLines = new ArrayList<>();
        for (Component line : tooltip) {
            tooltipLines.add(Language.getInstance().getVisualOrder(line));
        }
        guiGraphics.renderTooltip(mc.font, tooltipLines, mouseX, mouseY);
    }

    private static String formatPercentage(double value) {
        double percent = value * 100.0D;
        if (Math.abs(percent - Math.rint(percent)) < 0.0001D) {
            return String.format("%+.0f%%", percent);
        }
        return String.format("%+.1f%%", percent);
    }
}
