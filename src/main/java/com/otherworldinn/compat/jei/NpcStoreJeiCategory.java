package com.otherworldinn.compat.jei;

import com.otherworldinn.init.ModItems;
import com.otherworldinn.world.festival.FestivalDefinition;
import com.otherworldinn.world.festival.FestivalRegistry;
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

final class NpcStoreJeiCategory implements IRecipeCategory<NpcStoreJeiRecipe> {
    private static final int WIDTH = 166;
    private static final int HEIGHT = 70;
    private static final String FARMER = "entity.otherworldinn.farmer";
    private static final String BLACKSMITH = "entity.otherworldinn.blacksmith";
    private static final String MAGICIAN = "entity.otherworldinn.magician";
    private static final String GROCER = "entity.otherworldinn.grocer";
    private static final String BUTCHER = "entity.otherworldinn.butcher";
    private static final String BUILDER = "entity.otherworldinn.builder";
    private static final String FISHERMAN = "entity.otherworldinn.fisherman";
    private static final String WANDERING_TRADER = "entity.otherworldinn.wandering_trader";
    private static final int ICON_SIZE = 40;
    private static final int ICON_MARGIN = 18;
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath("otherworldinn", "textures/gui/jei/npc_store.png");
    private static final ResourceLocation ICON_FARMER =
            ResourceLocation.fromNamespaceAndPath("otherworldinn", "textures/gui/jei/farmer-icon.png");
    private static final ResourceLocation ICON_BLACKSMITH =
            ResourceLocation.fromNamespaceAndPath("otherworldinn", "textures/gui/jei/blacksmith-icon.png");
    private static final ResourceLocation ICON_MAGICIAN =
            ResourceLocation.fromNamespaceAndPath("otherworldinn", "textures/gui/jei/magician-icon.png");
    private static final ResourceLocation ICON_GROCER =
            ResourceLocation.fromNamespaceAndPath("otherworldinn", "textures/gui/jei/grocer-icon.png");
    private static final ResourceLocation ICON_BUTCHER =
            ResourceLocation.fromNamespaceAndPath("otherworldinn", "textures/gui/jei/butcher-icon.png");
    private static final ResourceLocation ICON_BUILDER =
            ResourceLocation.fromNamespaceAndPath("otherworldinn", "textures/gui/jei/builder-icon.png");
    private static final ResourceLocation ICON_FISHERMAN =
            ResourceLocation.fromNamespaceAndPath("otherworldinn", "textures/gui/jei/fisherman-icon.png");
    private static final ResourceLocation ICON_WANDERING_TRADER =
            ResourceLocation.fromNamespaceAndPath(
                    "otherworldinn", "textures/gui/jei/wandering_trader-icon.png");

    private final IDrawableStatic background;
    private final IDrawable icon;

    NpcStoreJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(ModItems.COIN.get().getDefaultInstance());
    }

    @Override
    public mezz.jei.api.recipe.RecipeType<NpcStoreJeiRecipe> getRecipeType() {
        return NpcStoreJeiPlugin.NPC_STORE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.otherworldinn.npc_store.title");
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
    public void setRecipe(IRecipeLayoutBuilder builder, NpcStoreJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(8, 24).setStandardSlotBackground().addItemStacks(buildCoinDisplayStacks(recipe));
        builder.addOutputSlot(50, 24).setOutputSlotBackground().addItemStack(recipe.output().copy());
    }

    @Override
    public void draw(
            NpcStoreJeiRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY) {
        drawNpcBackground(guiGraphics);
        Font font = Minecraft.getInstance().font;

        drawStoreIcon(guiGraphics, recipe);

        // 店主名：始终绘制，与店主图标水平中心线居中对齐
        Component name = Component.translatable(recipe.storeNameKey());
        int iconCenterX = WIDTH - ICON_SIZE - ICON_MARGIN + ICON_SIZE / 2;
        guiGraphics.drawString(font, name, iconCenterX - font.width(name) / 2, 4, 0x404040, false);

        int bottomY = 56;
        List<Component> segments = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        if (recipe.requiredFavorLevel() > 1) {
            segments.add(
                    Component.translatable(
                            "jei.otherworldinn.npc_store.favor", recipe.requiredFavorLevel()));
            colors.add(0x8B5A2B);
        }
        if (recipe.requiredAdvancementTitleKey() != null) {
            segments.add(
                    Component.translatable(
                            "jei.otherworldinn.npc_store.advancement",
                            Component.translatable(recipe.requiredAdvancementTitleKey())));
            colors.add(0x8A2BE2);
        }
        if (recipe.randomOffer()) {
            segments.add(Component.translatable("jei.otherworldinn.npc_store.random"));
            colors.add(0x2E8B57);
        }
        if (recipe.festivalId() != null) {
            String festivalName =
                    FestivalRegistry.get(recipe.festivalId())
                            .map(FestivalDefinition::zhName)
                            .orElse(recipe.festivalId());
            segments.add(
                    Component.translatable(
                            "jei.otherworldinn.npc_store.festival_exclusive", festivalName));
            colors.add(0xE91E63);
        }
        if (!segments.isEmpty()) {
            int gap = 8;
            int totalWidth = 0;
            for (int i = 0; i < segments.size(); i++) {
                totalWidth += font.width(segments.get(i));
                if (i < segments.size() - 1) {
                    totalWidth += gap;
                }
            }
            int x = (WIDTH - totalWidth) / 2;
            for (int i = 0; i < segments.size(); i++) {
                guiGraphics.drawString(font, segments.get(i), x, bottomY, colors.get(i), false);
                x += font.width(segments.get(i)) + gap;
            }
        }
    }

    private static List<ItemStack> buildCoinDisplayStacks(NpcStoreJeiRecipe recipe) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(ModItems.COIN.get(), recipe.minPrice()));
        if (recipe.maxPrice() > recipe.minPrice()) {
            stacks.add(new ItemStack(ModItems.COIN.get(), recipe.maxPrice()));
        }
        return stacks;
    }

    private static void drawNpcBackground(GuiGraphics guiGraphics) {
        if (!Minecraft.getInstance().getResourceManager().getResource(BG).isPresent()) {
            // 纹理不存在时回退 JEI 默认背景表现（不绘制自定义背景层）。
            return;
        }
        guiGraphics.blit(BG, 0, 0, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    /** 在面板右侧垂直居中的位置绘制店主图标（源纹理 800x800，缩放到 ICON_SIZE）。 */
    private static void drawStoreIcon(GuiGraphics guiGraphics, NpcStoreJeiRecipe recipe) {
        ResourceLocation texture = switch (recipe.storeNameKey()) {
            case FARMER -> ICON_FARMER;
            case BLACKSMITH -> ICON_BLACKSMITH;
            case MAGICIAN -> ICON_MAGICIAN;
            case GROCER -> ICON_GROCER;
            case BUTCHER -> ICON_BUTCHER;
            case BUILDER -> ICON_BUILDER;
            case FISHERMAN -> ICON_FISHERMAN;
            case WANDERING_TRADER -> ICON_WANDERING_TRADER;
            default -> null;
        };
        if (texture == null) {
            return;
        }
        if (!Minecraft.getInstance().getResourceManager().getResource(texture).isPresent()) {
            return;
        }
        int x = WIDTH - ICON_SIZE - ICON_MARGIN;
        int y = (HEIGHT - ICON_SIZE) / 2;
        guiGraphics.blit(texture, x, y, ICON_SIZE, ICON_SIZE, 0.0F, 0.0F, 800, 800, 800, 800);
    }
}
