package com.otherworldinn.mixin;

import com.otherworldinn.util.WorldDayUtils;
import com.otherworldinn.world.dimension.TownDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {

    private boolean otherworldinn$fireTickDisabled = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void disableFireTickInTown(CallbackInfo ci) {
        if (otherworldinn$fireTickDisabled) {
            return;
        }
        otherworldinn$fireTickDisabled = true;
        ServerLevel self = (ServerLevel) (Object) this;
        if (self.dimension() == TownDimensions.TOWN_LEVEL) {
            self.getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(false, self.getServer());
        }
    }

    @Inject(method = "wakeUpAllPlayers", at = @At("RETURN"))
    private void onWakeUpAllPlayers(CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        if (self.dimension() != TownDimensions.TOWN_LEVEL) {
            return;
        }
        if (!self.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            return;
        }
        if (!self.isNight() && !self.isThundering()) {
            return;
        }

        long dayTime = self.getDayTime();
        long target = WorldDayUtils.nextSunrise(dayTime);

        // 城镇维度使用 DerivedLevelData，其时间/天气 setter 为 no-op，
        // 所有维度共享主世界时间，因此将跳时写入转发给主世界
        ServerLevel overworld = self.getServer().overworld();
        overworld.setDayTime(target);
        overworld.setWeatherParameters(0, 0, false, false);
    }
}
