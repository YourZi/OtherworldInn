package com.otherworldinn.client.renderer;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.store.BlacksmithEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class BlacksmithRenderer extends MobRenderer<BlacksmithEntity, BlacksmithModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/store/blacksmith.png");

    public BlacksmithRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new BlacksmithModel(context.bakeLayer(BlacksmithModel.LAYER_LOCATION)),
                0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(BlacksmithEntity entity) {
        return TEXTURE;
    }
}
