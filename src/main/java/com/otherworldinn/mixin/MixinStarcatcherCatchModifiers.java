package com.otherworldinn.mixin;

import com.otherworldinn.compat.starcatcher.ReskillableFishingCatchModifier;
import com.otherworldinn.compat.starcatcher.StarcatcherFishingSkillScaling;
import com.wdiscute.starcatcher.registry.catchmodifiers.AbstractCatchModifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.wdiscute.starcatcher.registry.catchmodifiers.SCCatchModifiers", remap = false)
public interface MixinStarcatcherCatchModifiers {
    @Inject(method = "getCatchModifiers", at = @At("RETURN"), cancellable = true, remap = false)
    private static void appendReskillableFishingSkillModifier(
            Player player, CallbackInfoReturnable<List<AbstractCatchModifier>> cir) {
        int skillLevel = StarcatcherFishingSkillScaling.getFishingSkillLevel(player);
        if (skillLevel <= 0) {
            return;
        }
        List<AbstractCatchModifier> modifiers = new ArrayList<>(cir.getReturnValue());
        modifiers.add(new ReskillableFishingCatchModifier(skillLevel));
        cir.setReturnValue(modifiers);
    }
}
