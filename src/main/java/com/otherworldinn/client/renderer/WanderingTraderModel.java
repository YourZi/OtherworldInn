package com.otherworldinn.client.renderer;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.store.WanderingTraderEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

public class WanderingTraderModel extends StoreHumanoidModel<WanderingTraderEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "wandering_trader"), "main");

    public WanderingTraderModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return StoreHumanoidModel.createSlimBodyLayer();
    }

    @Override
    protected void applyIdlePose(WanderingTraderEntity entity, float ageInTicks) {
        float t = ageInTicks * 0.08F;
        float sway = (float) Math.sin(t * 1.0F);
        float breathe = (float) Math.sin(t * 1.2F);

        this.body.yRot = sway * 0.03F;

        this.head.yRot += sway * 0.05F;
        this.head.xRot += breathe * 0.01F;

        this.rightArm.xRot = -0.30F + breathe * 0.04F;
        this.rightArm.yRot = -0.04F + sway * 0.03F;

        this.leftArm.xRot = -0.36F - breathe * 0.04F;
        this.leftArm.yRot = 0.04F - sway * 0.03F;
    }
}
