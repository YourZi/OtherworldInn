package com.otherworldinn.compat.jei;

import com.otherworldinn.OtherworldInn;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

/** 生存模式隐藏刷怪蛋等创造专属物品的 JEI 插件入口。逻辑见 {@link CreativeOnlyJeiFilter}。 */
@JeiPlugin
public class CreativeOnlyJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "creative_only_filter");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        CreativeOnlyJeiFilter.attach(registration.getIngredientManager());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        CreativeOnlyJeiFilter.attach(jeiRuntime.getIngredientManager());
    }
}
