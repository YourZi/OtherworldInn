package com.otherworldinn.mixin;

import com.otherworldinn.world.dimension.TownDimensions;
import java.lang.reflect.Field;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel {

    @Shadow
    private ServerLevelData serverLevelData;

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

    private static ServerLevelData unwrapDerived(ServerLevelData sld) {
        if (sld instanceof DerivedLevelData) {
            try {
                Field wrappedField = DerivedLevelData.class.getDeclaredField("wrapped");
                wrappedField.setAccessible(true);
                return (ServerLevelData) wrappedField.get(sld);
            } catch (Exception ignored) {
            }
        }
        return sld;
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
        long target = dayTime + 24000L;
        target = target - target % 24000L;

        ServerLevelData actual = unwrapDerived(serverLevelData);
        actual.setDayTime(target);
        actual.setRainTime(0);
        actual.setThunderTime(0);
        actual.setRaining(false);
        actual.setThundering(false);
    }
}
