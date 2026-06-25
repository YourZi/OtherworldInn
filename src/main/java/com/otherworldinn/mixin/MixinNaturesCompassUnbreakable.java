package com.otherworldinn.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.chaosthedude.naturescompass.items.NaturesCompassItem", remap = false)
public abstract class MixinNaturesCompassUnbreakable {
    @Inject(method = "damageCompass", at = @At("HEAD"), cancellable = true, remap = false)
    private void otherworldinn$skipDamageForUnbreakable(ItemStack stack, CallbackInfo ci) {
        if (stack.has(DataComponents.UNBREAKABLE)) {
            ci.cancel();
        }
    }
}
