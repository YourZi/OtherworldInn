package com.otherworldinn.mixin;

import com.otherworldinn.foundation.SchematicSurvivalPrintHelper;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.client.tools.ToolType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ToolType.class)
public class MixinToolTypeSurvivalPrint {

    /** 手持的蓝图开启了生存打印标记时，也显示 Print 工具。 */
    @Inject(method = "getTools", at = @At("RETURN"), cancellable = true)
    private static void otherworldinn$addPrintTool(
            boolean creative, CallbackInfoReturnable<List<ToolType>> cir) {
        if (creative) return;

        ItemStack stack = CreateClient.SCHEMATIC_HANDLER.getActiveSchematicItem();
        if (stack == null) return;
        if (!SchematicSurvivalPrintHelper.isEnabled(stack)) return;

        List<ToolType> tools = new ArrayList<>(cir.getReturnValue());
        if (!tools.contains(ToolType.PRINT)) {
            tools.add(ToolType.PRINT);
            cir.setReturnValue(tools);
        }
    }
}
