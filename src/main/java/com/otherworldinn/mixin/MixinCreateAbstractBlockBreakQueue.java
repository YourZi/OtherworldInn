package com.otherworldinn.mixin;

import com.otherworldinn.world.event.listener.TownProtectionHandler;
import com.simibubi.create.foundation.utility.AbstractBlockBreakQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlockBreakQueue.class)
public abstract class MixinCreateAbstractBlockBreakQueue {

    @Inject(method = "makeCallbackFor", at = @At("RETURN"), cancellable = true)
    private void otherworldinn$wrapProtectedTownBreakCallback(
            Level world,
            float effectChance,
            ItemStack toDamage,
            Player playerEntity,
            BiConsumer<BlockPos, ItemStack> drop,
            CallbackInfoReturnable<Consumer<BlockPos>> cir) {
        Consumer<BlockPos> original = cir.getReturnValue();
        cir.setReturnValue(
                pos -> {
                    if (!TownProtectionHandler.canCreateModifyBlockAt(world, pos)) {
                        return;
                    }
                    original.accept(pos);
                });
    }
}
