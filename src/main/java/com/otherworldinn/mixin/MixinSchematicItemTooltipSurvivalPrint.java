package com.otherworldinn.mixin;

import com.otherworldinn.foundation.SchematicSurvivalPrintHelper;
import com.simibubi.create.content.schematics.SchematicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SchematicItem.class)
public class MixinSchematicItemTooltipSurvivalPrint {

    @Inject(method = "appendHoverText", at = @At("TAIL"))
    private void otherworldinn$addSurvivalPrintTooltip(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag,
            CallbackInfo ci) {
        if (SchematicSurvivalPrintHelper.isEnabled(stack)) {
            tooltip.add(Component.translatable(
                    "tooltip.otherworldinn.schematic.survival_print")
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}
