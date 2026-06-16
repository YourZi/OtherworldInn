package com.otherworldinn.client.renderer;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.store.ButcherEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

public class ButcherModel extends StoreHumanoidModel<ButcherEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "butcher"), "main");

    public ButcherModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return StoreHumanoidModel.createBodyLayer();
    }

    @Override
    protected void applyIdlePose(ButcherEntity entity, float ageInTicks) {
        float t = ageInTicks * 0.08F;
        float sway = (float) Math.sin(t * 1.1F);
        float breathe = (float) Math.sin(t * 1.2F);

        this.body.yRot = sway * 0.04F;

        this.head.yRot += sway * 0.06F;
        this.head.xRot += breathe * 0.015F;

        this.rightArm.xRot = -0.45F + breathe * 0.05F;
        this.rightArm.yRot = -0.10F + sway * 0.05F;
        this.rightArm.zRot = 0.04F;

        this.leftArm.xRot = -0.50F - breathe * 0.04F;
        this.leftArm.yRot = 0.10F - sway * 0.04F;
        this.leftArm.zRot = -0.04F;

        this.rightLeg.xRot = -0.02F;
        this.leftLeg.xRot = 0.02F;
    }
}
