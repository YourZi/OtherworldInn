package com.otherworldinn.datagen;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ItemDataGenInfo;
import com.otherworldinn.init.ModItems;
import java.util.Map;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * 物品模型生成器：主要处理独立物品，方块物品模型由 BlockStateProvider 生成
 */
public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OtherworldInn.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        for (Map.Entry<DeferredItem<?>, ItemDataGenInfo> entry : ModItems.ITEM_INFOS.entrySet()) {
            DeferredItem<?> item = entry.getKey();
            ItemDataGenInfo info = entry.getValue();

            if (info.generateModel()) {
                if ("handheld".equals(info.modelType())) {
                    handheldItem(item);
                } else {
                    simpleItem(item);
                }
            }
        }
    }

    private void simpleItem(DeferredItem<?> item) {
        withExistingParent(item.getId().getPath(), ResourceLocation.parse("item/generated"))
                .texture(
                        "layer0",
                        ResourceLocation.fromNamespaceAndPath(
                                OtherworldInn.MODID, "item/" + item.getId().getPath()));
    }

    private void handheldItem(DeferredItem<?> item) {
        withExistingParent(item.getId().getPath(), ResourceLocation.parse("item/handheld"))
                .texture(
                        "layer0",
                        ResourceLocation.fromNamespaceAndPath(
                                OtherworldInn.MODID, "item/" + item.getId().getPath()));
    }
}
