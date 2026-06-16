package com.otherworldinn.client.renderer;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.entity.store.FishermanEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;

public class FishermanModel extends StoreHumanoidModel<FishermanEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "fisherman"), "main");

    public FishermanModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return StoreHumanoidModel.createSlimBodyLayer();
    }

    @Override
    protected void applyIdlePose(FishermanEntity entity, float ageInTicks) {
        float t = ageInTicks * 0.08F;
        float sway = (float) Math.sin(t * 0.7F);
        float breathe = (float) Math.sin(t * 1.0F);

        this.body.yRot = sway * 0.02F;

        this.head.yRot += sway * 0.03F;
        this.head.xRot += breathe * 0.01F;

        this.rightArm.xRot = -0.55F + breathe * 0.06F;
        this.rightArm.yRot = -0.05F + sway * 0.04F;
        this.rightArm.zRot = 0.05F;

        this.leftArm.xRot = -0.50F - breathe * 0.05F;
        this.leftArm.yRot = 0.08F - sway * 0.04F;
        this.leftArm.zRot = -0.05F;

        this.rightLeg.xRot = -0.01F;
        this.leftLeg.xRot = 0.01F;
    }
}
