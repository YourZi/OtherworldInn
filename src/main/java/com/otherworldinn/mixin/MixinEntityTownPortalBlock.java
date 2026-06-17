package com.otherworldinn.mixin;

import com.otherworldinn.world.dimension.TownDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 拦截实体从城镇维度通过传送门进入下界。
 *
 * <p>只拦截传送门触发的跨维度传送（{@code Entity#handlePortal()}），
 * 不影响模组自身的传送（通过 {@code TeleportUtils#changeDimensionTo} 直接调用
 * {@code Entity#changeDimension}，不经过此方法）。
 */
@Mixin(Entity.class)
public class MixinEntityTownPortalBlock {

    @Inject(method = "handlePortal", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$blockTownPortalTravel(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        Level level = self.level();
        if (level instanceof ServerLevel serverLevel
                && serverLevel.dimension() == TownDimensions.TOWN_LEVEL) {
            ci.cancel();
        }
    }
}
