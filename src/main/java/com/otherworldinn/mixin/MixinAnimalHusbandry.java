package com.otherworldinn.mixin;

import com.otherworldinn.init.ModGameRules;
import com.otherworldinn.world.husbandry.HusbandryService;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 把"非玩家来源的喂食"接进好感系统。
 *
 * <p>典型来源是饲养槽（animal_feeding_trough 的 {@code SelfFeedGoal}）：动物走到槽边吃掉一份食物后
 * 调用 {@code setInLove(null)}。这里把它改造成一次"自动喂食"——加
 * {@link HusbandryService#AUTO_FEED_AFFECTION} 好感（玩家手动的 1/5），但**不进入发情**。
 *
 * <p>同时 {@code canFallInLove()} 在"今天已经自动喂过"时返回 false：饲养槽的 canUse/tick 都以它为前提，
 * 于是当天不会再去吃，避免食物被反复消耗。
 */
@Mixin(Animal.class)
public abstract class MixinAnimalHusbandry {

    @Inject(method = "setInLove", at = @At("HEAD"), cancellable = true)
    private void otherworldinnAutoFeedRedirection(Player player, CallbackInfo callback) {
        if (player != null) {
            return;
        }
        Animal animal = (Animal) (Object) this;
        if (!(animal.level() instanceof ServerLevel level)) {
            return;
        }
        if (level.getGameRules().getBoolean(ModGameRules.RULE_ALLOW_VANILLA_BREEDING)) {
            return;
        }
        HusbandryService.tryAutoFeed(animal, level);
        callback.cancel();
    }

    @Inject(method = "canFallInLove", at = @At("HEAD"), cancellable = true)
    private void otherworldinnSkipTroughWhenAlreadyAutoFed(
            CallbackInfoReturnable<Boolean> callback) {
        Animal animal = (Animal) (Object) this;
        if (!(animal.level() instanceof ServerLevel level)) {
            return;
        }
        if (level.getGameRules().getBoolean(ModGameRules.RULE_ALLOW_VANILLA_BREEDING)) {
            return;
        }
        if (HusbandryService.hasEatenToday(animal, level)) {
            callback.setReturnValue(false);
        }
    }
}
