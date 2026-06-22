package com.wdiscute.starcatcher.blocks.tacklebox;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.wdiscute.starcatcher.Starcatcher;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public class TackleBoxRenderer implements BlockEntityRenderer<TackleBoxBlockEntity>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Starcatcher.rl("tackle_box.png"), "main");

    private final ModelPart box;
    private final ModelPart lid;
    private final ModelPart tray;

    public TackleBoxRenderer(BlockEntityRendererProvider.Context context)
    {
        ModelPart modelpart = context.bakeLayer(LAYER_LOCATION);
        this.box = modelpart.getChild("box");
        this.lid = modelpart.getChild("lid");
        this.tray = modelpart.getChild("tray");
    }

    @Override
    public void render(TackleBoxBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(Starcatcher.rl("textures/block/tackle_box.png/tackle_box_white.png")));
        box.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        lid.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        tray.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    @Override
    public AABB getRenderBoundingBox(TackleBoxBlockEntity tackleBoxBlockEntity)
    {
        BlockPos pos = tackleBoxBlockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0F, pos.getY() + 1.5F, pos.getZ() + 1.0F);
    }






    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition box = partdefinition.addOrReplaceChild("box", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 1.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 10).addBox(-8.0F, -2.0F, 1.2F, 15.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-8.0F, -2.0F, 11.2F, 15.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-7.0F, 3.0F, 2.2F, 13.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(35, 1).addBox(6.0F, -2.0F, 2.2F, 1.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(35, 1).addBox(-8.0F, -2.0F, 2.2F, 1.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 20.0F, -6.7F));

        PartDefinition lid = partdefinition.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 3).addBox(-2.0F, -2.0F, -11.3F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(-8.0F, -4.0F, -11.0F, 15.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(35, 16).addBox(-8.0F, -4.0F, -10.0F, 1.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(32, 34).addBox(-8.0F, -4.0F, -1.0F, 15.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(41, 29).addBox(-5.0F, -4.3F, -6.0F, 9.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-7.0F, -4.0F, -10.0F, 13.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(35, 16).addBox(6.0F, -4.0F, -10.0F, 1.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 18.0F, 5.5F));

        PartDefinition tray = partdefinition.addOrReplaceChild("tray", CubeListBuilder.create().texOffs(0, 39).addBox(-6.0F, 1.0F, -11.0F, 12.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, 6.5F));

        PartDefinition armSet2 = partdefinition.addOrReplaceChild("armSet2", CubeListBuilder.create(), PartPose.offsetAndRotation(7.3F, 20.4F, 0.5F, 1.6144F, 0.0F, 0.0F));

        PartDefinition cube_r1 = armSet2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(7, -1).addBox(-1.0F, -5.0F, -1.0F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, -1).addBox(11.6F, -5.0F, -1.0F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.6F, 0.0F, 0.0F, -0.3752F, 0.0F, 0.0F));

        PartDefinition armSet3 = partdefinition.addOrReplaceChild("armSet3", CubeListBuilder.create(), PartPose.offsetAndRotation(7.3F, 20.4F, 3.0F, 1.6144F, 0.0F, 0.0F));

        PartDefinition cube_r2 = armSet3.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(7, -1).addBox(-1.0F, -5.0F, -1.0F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, -1).addBox(11.6F, -5.0F, -1.0F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.6F, 0.0F, 0.0F, -0.3752F, 0.0F, 0.0F));

        PartDefinition armSet1 = partdefinition.addOrReplaceChild("armSet1", CubeListBuilder.create(), PartPose.offsetAndRotation(7.3F, 20.1F, -1.3F, -0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r3 = armSet1.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(7, -1).addBox(-1.0F, -5.0F, -1.0F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, -1).addBox(11.4F, -5.0F, -1.0F, 0.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-12.5F, 0.0F, 0.0F, -0.8116F, 0.0F, 0.0F));

        PartDefinition vfx = partdefinition.addOrReplaceChild("vfx", CubeListBuilder.create(), PartPose.offsetAndRotation(7.0F, 19.5F, 7.5F, 2.0508F, 0.0F, 0.0F));

        PartDefinition cube_r4 = vfx.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(30, 27).addBox(0.0F, -1.5F, -12.0F, 0.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(30, 27).addBox(14.0F, -1.5F, -12.0F, 0.0F, 3.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, 0.0F, 0.0F, -2.0508F, 0.0F, 0.0F));

        PartDefinition vfx2 = partdefinition.addOrReplaceChild("vfx2", CubeListBuilder.create(), PartPose.offsetAndRotation(7.0F, 19.5F, 7.5F, 2.0508F, 0.0F, 0.0F));

        PartDefinition cube_r5 = vfx2.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(30, 27).addBox(0.0F, -1.5F, -12.0F, 0.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(30, 27).addBox(14.0F, -1.5F, -12.0F, 0.0F, 3.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, 0.0F, 0.0F, -2.0508F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
