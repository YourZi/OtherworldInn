package com.otherworldinn.compat.jei;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.inn.service.FurnitureManager;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI"家具属性"类目
 */
final class FurnitureAttributeJeiCategory implements IRecipeCategory<FurnitureAttributeJeiRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 40;
    /** 类目背景纹理：166x40，1:1 绘制；缺失时回退为空白背景。 */
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "textures/gui/jei/furniture_attribute.png");
    private static final int SLOT_X = 8;
    private static final int SLOT_Y = 12;
    private static final int TEXT_X = 84;
    private static final int LINE_GAP = 1;

    private final IDrawableStatic background;
    private final IDrawable icon;

    FurnitureAttributeJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(net.minecraft.world.level.block.Blocks.BOOKSHELF));
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<FurnitureAttributeJeiRecipe> getRecipeType() {
        return FurnitureAttributeJeiPlugin.FURNITURE_ATTRIBUTE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.otherworldinn.furniture_attribute.title");
    }

    @Override
    public IDrawableStatic getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FurnitureAttributeJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(SLOT_X, SLOT_Y).setStandardSlotBackground().addItemStacks(recipe.blockStacks());
    }

    @Override
    public void draw(
            FurnitureAttributeJeiRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY) {
        Font font = Minecraft.getInstance().font;

        if (Minecraft.getInstance().getResourceManager().getResource(BACKGROUND_TEXTURE).isPresent()) {
            guiGraphics.blit(BACKGROUND_TEXTURE, 0, 0, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
        }

        // 三项属性并列各占一行，紧凑排布并垂直居中
        List<Component> segments = buildSegments(recipe.stats());
        if (segments.isEmpty()) {
            return;
        }
        int lineStep = font.lineHeight + LINE_GAP;
        int block = segments.size() * lineStep - LINE_GAP;
        int y = (HEIGHT - block) / 2;
        for (Component segment : segments) {
            guiGraphics.drawString(font, segment, TEXT_X, y, 0x404040, false);
            y += lineStep;
        }
    }

    private static List<Component> buildSegments(FurnitureManager.FurnitureStats stats) {
        List<Component> segments = new ArrayList<>();
        addSegment(
                segments,
                "tooltip.otherworldinn.furniture.comfort",
                ModColors.COMFORT,
                stats.comfort());
        addSegment(
                segments,
                "tooltip.otherworldinn.furniture.light",
                ModColors.LIGHT,
                stats.light());
        addSegment(
                segments,
                "tooltip.otherworldinn.furniture.humidity",
                ModColors.HUMIDITY,
                stats.humidity());
        return segments;
    }

    private static void addSegment(List<Component> segments, String key, int color, int value) {
        if (value == 0) {
            return;
        }
        // 与 tooltip 一致：颜色应用于整段，图标字形内嵌在词条文本中
        segments.add(
                Component.translatable(key, String.format("%+d", value))
                        .withStyle(style -> style.withColor(color)));
    }
}
