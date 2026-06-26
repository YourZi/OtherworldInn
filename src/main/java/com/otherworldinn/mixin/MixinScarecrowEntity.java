package com.otherworldinn.mixin;

import com.otherworldinn.compat.kaleidoscopecookery.ScarecrowBonemealAccess;
import com.otherworldinn.compat.kaleidoscopecookery.ScarecrowBonemealService;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.github.ysbbbbbb.kaleidoscopecookery.entity.ScarecrowEntity", remap = false)
public abstract class MixinScarecrowEntity implements ScarecrowBonemealAccess {
    @Unique
    private long otherworldinn$nextBonemealGameTime;

    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    private void otherworldinn$tickBonemeal(CallbackInfo ci) {
        ScarecrowBonemealService.tick((Entity) (Object) this);
    }

    @Override
    public long otherworldinn$getNextBonemealGameTime() {
        return otherworldinn$nextBonemealGameTime;
    }

    @Override
    public void otherworldinn$setNextBonemealGameTime(long gameTime) {
        otherworldinn$nextBonemealGameTime = gameTime;
    }
}
