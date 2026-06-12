package com.otherworldinn.mixin;

import com.otherworldinn.foundation.SchematicSurvivalPrintHelper;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.schematics.SchematicItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SchematicItem.class)
public class MixinSchematicItemToggle {

    /**
     * 创造模式玩家手持蓝图 Shift+右键空气 → 切换生存打印标志。
     */
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$toggleSurvivalPrint(
            Level worldIn, Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || hand != InteractionHand.MAIN_HAND) return;
        if (!stack.has(AllDataComponents.SCHEMATIC_FILE)) return;
        if (worldIn.isClientSide) return;
        if (!player.isCreative()) return;

        boolean wasEnabled = SchematicSurvivalPrintHelper.isEnabled(stack);
        SchematicSurvivalPrintHelper.toggle(stack);

        player.displayClientMessage(
                Component.translatable(wasEnabled
                        ? "message.otherworldinn.schematic.survival_print.disabled"
                        : "message.otherworldinn.schematic.survival_print.enabled")
                        .withStyle(wasEnabled ? ChatFormatting.RED : ChatFormatting.GREEN),
                true);

        cir.setReturnValue(InteractionResultHolder.success(stack));
    }
}
