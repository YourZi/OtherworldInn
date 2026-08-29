package com.otherworldinn.client.gui.screen;

import com.otherworldinn.OtherworldInn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

public class SeasonCalendarScreen extends Screen {
    private static final int DAYS_PER_SEASON = 24;
    private static final int GRID_COLUMNS = 8;
    private static final int GRID_ROWS = 3;
    private static final int CELL_SIZE = 32;
    private static final int BACKGROUND_TEXTURE_WIDTH = 352;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 208;
    private static final int GRID_START_X = 48;
    private static final int GRID_START_Y = 56;
    private static final int ARROW_TEXTURE_SIZE = 32;
    private static final int ARROW_GAP = 8;

    private static final ResourceLocation SPRING_TEXTURE =
            texture("textures/gui/calendar/spring.png");
    private static final ResourceLocation SUMMER_TEXTURE =
            texture("textures/gui/calendar/summer.png");
    private static final ResourceLocation AUTUMN_TEXTURE =
            texture("textures/gui/calendar/autumn.png");
    private static final ResourceLocation WINTER_TEXTURE =
            texture("textures/gui/calendar/winter.png");
    private static final ResourceLocation HIGHLIGHT_TEXTURE =
            texture("textures/gui/calendar/day_highlight.png");
    private static final ResourceLocation ARROW_ATLAS_TEXTURE =
            texture("textures/gui/calendar/arrows.png");

    private SeasonPage currentSeasonPage;
    private SeasonPage selectedSeasonPage;
    private int currentDayIndex;

    public SeasonCalendarScreen() {
        super(Component.empty());
        SeasonView currentView = readCurrentSeasonView();
        this.currentSeasonPage = currentView.seasonPage();
        this.selectedSeasonPage = currentView.seasonPage();
        this.currentDayIndex = currentView.dayIndex();
    }

    @Override
    protected void init() {
        super.init();
        refreshCurrentSeasonView();
        int backgroundWidth = BACKGROUND_TEXTURE_WIDTH;
        int backgroundHeight = BACKGROUND_TEXTURE_HEIGHT;
        int arrowSize = ARROW_TEXTURE_SIZE;
        int arrowGap = ARROW_GAP;
        int backgroundLeft = (this.width - backgroundWidth) / 2;
        int backgroundTop = (this.height - backgroundHeight) / 2;
        int arrowY = backgroundTop + backgroundHeight / 2 - arrowSize / 2;
        addRenderableWidget(new ArrowButton(
                backgroundLeft - arrowSize - arrowGap,
                arrowY,
                true,
                button -> this.turnPage(true)));
        addRenderableWidget(new ArrowButton(
                backgroundLeft + backgroundWidth + arrowGap,
                arrowY,
                false,
                button -> this.turnPage(false)));
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
        renderCalendar(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void renderCalendar(GuiGraphics guiGraphics) {
        int backgroundLeft = (this.width - BACKGROUND_TEXTURE_WIDTH) / 2;
        int backgroundTop = (this.height - BACKGROUND_TEXTURE_HEIGHT) / 2;
        guiGraphics.blit(
                this.selectedSeasonPage.background(),
                backgroundLeft,
                backgroundTop,
                0,
                0,
                BACKGROUND_TEXTURE_WIDTH,
                BACKGROUND_TEXTURE_HEIGHT,
                BACKGROUND_TEXTURE_WIDTH,
                BACKGROUND_TEXTURE_HEIGHT);

        if (this.selectedSeasonPage == this.currentSeasonPage) {
            int clampedDayIndex = Mth.clamp(this.currentDayIndex, 0, DAYS_PER_SEASON - 1);
            int column = clampedDayIndex % GRID_COLUMNS;
            int row = clampedDayIndex / GRID_COLUMNS;
            int x = backgroundLeft + GRID_START_X + column * CELL_SIZE;
            int y = backgroundTop + GRID_START_Y + row * CELL_SIZE;
            guiGraphics.blit(
                    HIGHLIGHT_TEXTURE,
                    x,
                    y,
                    0,
                    0,
                    CELL_SIZE,
                    CELL_SIZE,
                    CELL_SIZE,
                    CELL_SIZE);
        }
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, path);
    }

    private void turnPage(boolean previous) {
        this.selectedSeasonPage = previous
                ? this.selectedSeasonPage.previous()
                : this.selectedSeasonPage.next();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }

    private void refreshCurrentSeasonView() {
        SeasonPage previousCurrentPage = this.currentSeasonPage;
        SeasonView currentView = readCurrentSeasonView();
        this.currentSeasonPage = currentView.seasonPage();
        this.currentDayIndex = currentView.dayIndex();
        if (this.selectedSeasonPage == previousCurrentPage) {
            this.selectedSeasonPage = this.currentSeasonPage;
        }
    }

    private static SeasonView readCurrentSeasonView() {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft == null ? null : minecraft.level;
        if (level == null) {
            return new SeasonView(SeasonPage.SPRING, 0);
        }
        try {
            ISeasonState state = SeasonHelper.getSeasonState(level);
            if (state == null) {
                return new SeasonView(SeasonPage.SPRING, 0);
            }
            return new SeasonView(
                    SeasonPage.fromSeason(state.getSeason()),
                    Math.floorMod(state.getDay(), DAYS_PER_SEASON));
        } catch (RuntimeException ignored) {
            return new SeasonView(SeasonPage.SPRING, 0);
        }
    }

    private record SeasonView(SeasonPage seasonPage, int dayIndex) {}

    private enum SeasonPage {
        SPRING(SPRING_TEXTURE, Season.SPRING),
        SUMMER(SUMMER_TEXTURE, Season.SUMMER),
        AUTUMN(AUTUMN_TEXTURE, Season.AUTUMN),
        WINTER(WINTER_TEXTURE, Season.WINTER);

        private final ResourceLocation background;
        private final Season season;

        SeasonPage(ResourceLocation background, Season season) {
            this.background = background;
            this.season = season;
        }

        public ResourceLocation background() {
            return background;
        }

        public SeasonPage previous() {
            return values()[(ordinal() + values().length - 1) % values().length];
        }

        public SeasonPage next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static SeasonPage fromSeason(Season season) {
            for (SeasonPage value : values()) {
                if (value.season == season) {
                    return value;
                }
            }
            return SPRING;
        }
    }

    private static final class ArrowButton extends Button {
        private final boolean leftArrow;

        private ArrowButton(int x, int y, boolean leftArrow, OnPress onPress) {
            super(
                    x,
                    y,
                    ARROW_TEXTURE_SIZE,
                    ARROW_TEXTURE_SIZE,
                    Component.empty(),
                    onPress,
                    DEFAULT_NARRATION);
            this.leftArrow = leftArrow;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int texX = this.leftArrow ? 0 : ARROW_TEXTURE_SIZE;
            int texY = this.isMouseOver(mouseX, mouseY) ? ARROW_TEXTURE_SIZE : 0;
            guiGraphics.blit(
                    ARROW_ATLAS_TEXTURE,
                    getX(),
                    getY(),
                    texX,
                    texY,
                    ARROW_TEXTURE_SIZE,
                    ARROW_TEXTURE_SIZE,
                    ARROW_TEXTURE_SIZE * 2,
                    ARROW_TEXTURE_SIZE * 2);
        }
    }
}
