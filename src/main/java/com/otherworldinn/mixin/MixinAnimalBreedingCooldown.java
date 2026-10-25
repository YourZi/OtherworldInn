package com.otherworldinn.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把动物的原版繁殖冷却翻倍（6000 → 12000 tick，约 5 分钟 → 10 分钟）。
 *
 * <p>只改成功繁殖的收尾阶段 {@code finalizeSpawnChildFromBreeding}：
 * 原版在这里给父母双方 {@code setAge(6000)}，我们检查到 6000 后改成 12000，
 * 不影响成长、命令或其它途径设置的 age。
 *
 * <p>被 {@code BabyEntitySpawnEvent} 拦下的那次繁殖走的是 {@code spawnChildFromBreeding}
 * 里的取消分支（也是 6000），不属于"成功繁殖"，这里不处理。
 */
@Mixin(Animal.class)
public abstract class MixinAnimalBreedingCooldown {

    private static final int VANILLA_COOLDOWN = 6000;
    private static final int DOUBLED_COOLDOWN = 12000;

    @Inject(method = "finalizeSpawnChildFromBreeding", at = @At("TAIL"))
    private void otherworldinn$doubleBreedingCooldown(
            ServerLevel level, Animal partner, AgeableMob baby, CallbackInfo callback) {
        Animal self = (Animal) (Object) this;
        if (self.getAge() == VANILLA_COOLDOWN) {
            self.setAge(DOUBLED_COOLDOWN);
        }
        if (partner != null && partner.getAge() == VANILLA_COOLDOWN) {
            partner.setAge(DOUBLED_COOLDOWN);
        }
    }
}
