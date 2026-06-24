package com.otherworldinn.mixin;

import com.otherworldinn.compat.naturescompass.NaturesCompassDurabilityHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class MixinItemNaturesCompassSupport {
    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$allowNatureCompassEnchanting(
            ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (NaturesCompassDurabilityHelper.isNatureCompass(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getEnchantmentValue", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$setNatureCompassEnchantability(CallbackInfoReturnable<Integer> cir) {
        if (NaturesCompassDurabilityHelper.isNatureCompassItem((Item) (Object) this)) {
            cir.setReturnValue(NaturesCompassDurabilityHelper.getEnchantmentValue());
        }
    }

    @Inject(method = "isValidRepairItem", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$allowNatureCompassLogRepair(
            ItemStack stack, ItemStack repairCandidate, CallbackInfoReturnable<Boolean> cir) {
        if (NaturesCompassDurabilityHelper.isNatureCompass(stack) && repairCandidate.is(ItemTags.LOGS)) {
            cir.setReturnValue(true);
        }
    }
}
