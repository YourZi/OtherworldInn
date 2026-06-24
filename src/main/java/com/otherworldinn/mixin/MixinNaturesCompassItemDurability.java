package com.otherworldinn.mixin;

import com.otherworldinn.compat.naturescompass.NaturesCompassDurabilityHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.chaosthedude.naturescompass.items.NaturesCompassItem", remap = false)
public abstract class MixinNaturesCompassItemDurability {
    @Inject(method = "succeed", at = @At("TAIL"), remap = false)
    private void otherworldinn$consumeSearchDurability(
            ItemStack stack,
            Player player,
            int foundX,
            int foundZ,
            int samples,
            boolean displayCoordinates,
            CallbackInfo ci) {
        NaturesCompassDurabilityHelper.onSearchSucceeded(stack, player);
    }
}
