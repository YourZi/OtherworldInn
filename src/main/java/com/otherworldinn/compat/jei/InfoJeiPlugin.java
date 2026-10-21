package com.otherworldinn.compat.jei;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModItems;
import java.util.List;
import java.util.ArrayList;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

/**
 * JEI 信息页插件：集中注册模组物品的 JEI Information 说明，
 * 文案见 {@link com.otherworldinn.datagen.ModLanguageProvider} 中的 {@code jei.otherworldinn.*} 翻译键。
 */
@JeiPlugin
public class InfoJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "info_jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        IIngredientManager ingredientManager = registration.getJeiHelpers().getIngredientManager();

        List<Item> items =
                List.of(
                        ModItems.RECALL_SCROLL.get(),
                        ModItems.ROOM_REGISTER.get(),
                        ModItems.ROOM_KEY.get(),
                        ModItems.BED_SHEET.get(),
                        ModItems.MESSY_BED_SHEET.get(),
                        ModItems.LAND_DEED.get(),
                        ModItems.INN_KEY.get(),
                        ModItems.INN_UPGRADE_VOUCHER.get(),
                        ModItems.FACILITY_UPGRADE_TEMPLATE.get(),
                        ModItems.COIN.get(),
                        ModItems.SPACE_SPHERE.get(),
                        ModItems.NETHER_SPACE_SPHERE.get(),
                        ModItems.END_SPACE_SPHERE.get(),
                        ModItems.PICNIC_BOX.get(),
                        ModItems.ORGANIC_FERTILIZER.get());

        for (Item item : items) {
            registerInfo(registration, ingredientManager, List.of(new ItemStack(item)), infoKey(item));
        }

        List<ItemStack> honeySystemItems = new ArrayList<>();
        honeySystemItems.add(new ItemStack(Blocks.CAULDRON));
        honeySystemItems.add(new ItemStack(Items.HONEY_BOTTLE));
        honeySystemItems.add(new ItemStack(Items.HONEY_BLOCK));
        BuiltInRegistries.ITEM
                .getOptional(ResourceLocation.fromNamespaceAndPath("create", "honey_bucket"))
                .ifPresent(item -> honeySystemItems.add(new ItemStack(item)));
        registerInfo(
                registration,
                ingredientManager,
                honeySystemItems,
                "jei.otherworldinn.honey_cauldron_system.info");

        registerInfo(
                registration,
                ingredientManager,
                List.of(
                        new ItemStack(Items.PAINTING),
                        new ItemStack(Items.ITEM_FRAME),
                        new ItemStack(Items.GLOW_ITEM_FRAME)),
                "jei.otherworldinn.hanging_entity_surface.info");
    }

    /** 由物品注册名派生信息页翻译键，如 item.otherworldinn.recall_scroll -> jei.otherworldinn.recall_scroll.info */
    private static String infoKey(Item item) {
        String id = item.getDescriptionId();
        return "jei.otherworldinn" + id.substring("item.otherworldinn".length()) + ".info";
    }

    private static void registerInfo(
            IRecipeRegistration registration,
            IIngredientManager ingredientManager,
            List<ItemStack> stacks,
            String translationKey) {
        List<ITypedIngredient<?>> ingredients = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ingredients.add(
                    ingredientManager
                            .createTypedIngredient(VanillaTypes.ITEM_STACK, stack)
                            .orElseThrow());
        }
        registration.addRecipes(
                RecipeTypes.INFORMATION,
                List.of(new InfoRecipe(ingredients, List.of(Component.translatable(translationKey)))));
    }

    /** JEI 信息页实现 */
    private static final class InfoRecipe implements IJeiIngredientInfoRecipe {
        private final List<ITypedIngredient<?>> ingredients;
        private final List<FormattedText> description;

        private InfoRecipe(
                List<ITypedIngredient<?>> ingredients, List<FormattedText> description) {
            this.ingredients = ingredients;
            this.description = description;
        }

        @Override
        public List<ITypedIngredient<?>> getIngredients() {
            return ingredients;
        }

        @Override
        public List<FormattedText> getDescription() {
            return description;
        }
    }
}
