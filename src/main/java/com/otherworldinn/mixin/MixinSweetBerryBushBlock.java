package com.otherworldinn.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 浆果丛只减速、不再扎人。
 *
 * <p>用 {@code @WrapOperation} 只拦住方法里那一句 {@code entity.hurt(甜浆果伤害, 1)}，
 * 保留原版的 {@code makeStuckInBlock} 减速，也保留其他模组对同一方法的注入
 * （实测东方小女仆也有 SweetBerryBushBlockMixin，HEAD 直接 cancel 会把它一起短路）。
 */
@Mixin(SweetBerryBushBlock.class)
public abstract class MixinSweetBerryBushBlock {

    @WrapOperation(
            method = "entityInside",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean otherworldinn$noBerryDamage(
            Entity entity,
            DamageSource source,
            float amount,
            Operation<Boolean> original) {
        return false;
    }
}
