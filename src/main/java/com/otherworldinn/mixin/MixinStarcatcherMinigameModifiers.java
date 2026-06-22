package com.otherworldinn.mixin;

import com.otherworldinn.compat.starcatcher.ReskillableFishingMinigameModifier;
import com.otherworldinn.compat.starcatcher.StarcatcherFishingSkillScaling;
import com.wdiscute.starcatcher.registry.minigamemodifiers.AbstractMinigameModifier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.wdiscute.starcatcher.registry.minigamemodifiers.SCMinigameModifiers", remap = false)
public interface MixinStarcatcherMinigameModifiers {
    @Inject(method = "getMinigameModifiers", at = @At("RETURN"), cancellable = true, remap = false)
    private static void appendReskillableFishingSkillModifier(
            Player player, CallbackInfoReturnable<List<AbstractMinigameModifier>> cir) {
        int skillLevel = StarcatcherFishingSkillScaling.getFishingSkillLevel(player);
        if (skillLevel <= 0) {
            return;
        }
        List<AbstractMinigameModifier> modifiers = new ArrayList<>(cir.getReturnValue());
        modifiers.add(new ReskillableFishingMinigameModifier(skillLevel));
        cir.setReturnValue(modifiers);
    }
}
