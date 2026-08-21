package com.otherworldinn.compat.jei;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModItems;
import java.util.List;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * JEI 信息页插件
 *
 * <p>集中注册模组物品的 JEI Information 信息页说明，与商店类别插件相互独立。
 * 文案见 {@link com.otherworldinn.datagen.ModLanguageProvider} 中的 jei.otherworldinn.* 翻译键。
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
            ITypedIngredient<ItemStack> ingredient =
                    ingredientManager
                            .createTypedIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item))
                            .orElseThrow();
            registration.addRecipes(
                    RecipeTypes.INFORMATION,
                    List.of(
                            new InfoRecipe(
                                    List.of(ingredient),
                                    List.of(Component.translatable(infoKey(item))))));
        }
    }

    /** 由物品注册名派生信息页翻译键，如 item.otherworldinn.recall_scroll -> jei.otherworldinn.recall_scroll.info */
    private static String infoKey(Item item) {
        String id = item.getDescriptionId();
        return "jei.otherworldinn" + id.substring("item.otherworldinn".length()) + ".info";
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
