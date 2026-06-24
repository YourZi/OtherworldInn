package com.otherworldinn.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.otherworldinn.client.schematic.SchematicPreviewProtectionHelper;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.content.schematics.client.tools.SchematicToolBase;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SchematicToolBase.class)
public abstract class MixinSchematicToolBasePreviewColor {
    @Shadow
    protected SchematicHandler schematicHandler;

    @Inject(
            method = "renderOnSchematic",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/createmod/catnip/outliner/AABBOutline;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/createmod/catnip/render/SuperRenderTypeBuffer;Lnet/minecraft/world/phys/Vec3;F)V",
                    shift = At.Shift.BEFORE))
    private void otherworldinn$updatePlacedOutlineColor(
            PoseStack ms, SuperRenderTypeBuffer buffer, CallbackInfo ci) {
        if (schematicHandler == null || !schematicHandler.isDeployed()) {
            return;
        }
        schematicHandler.getOutline()
                .getParams()
                .colored(SchematicPreviewProtectionHelper.getOutlineColor(schematicHandler, null));
    }
}
