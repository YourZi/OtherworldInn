package com.otherworldinn.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.otherworldinn.client.schematic.SchematicPreviewProtectionHelper;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.content.schematics.client.tools.DeployTool;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeployTool.class)
public abstract class MixinDeployToolPreviewColor {
    @Inject(
            method = "renderTool",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/createmod/catnip/outliner/AABBOutline;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/createmod/catnip/render/SuperRenderTypeBuffer;Lnet/minecraft/world/phys/Vec3;F)V",
                    shift = At.Shift.BEFORE))
    private void otherworldinn$updatePreviewOutlineColor(
            PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, CallbackInfo ci) {
        MixinSchematicToolBaseAccessor accessor = (MixinSchematicToolBaseAccessor) this;
        SchematicHandler schematicHandler = accessor.otherworldinn$getSchematicHandler();
        BlockPos selectedPos = accessor.otherworldinn$getSelectedPos();
        if (selectedPos == null || schematicHandler == null || schematicHandler.getBounds() == null) {
            return;
        }
        AABB bounds = schematicHandler.getBounds();
        Vec3 center = bounds.getCenter();
        BlockPos prospectiveAnchor =
                selectedPos.offset(-((int) center.x), 0, -((int) center.z));
        schematicHandler.getOutline()
                .getParams()
                .colored(SchematicPreviewProtectionHelper.getOutlineColor(
                        schematicHandler, prospectiveAnchor));
    }
}
