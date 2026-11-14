package com.otherworldinn.mixin;

import com.otherworldinn.world.snow.SnowCoverServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把"原版抽中某一列准备落雪"这件事暴露出来。
 *
 * <p>注入在 {@code tickPrecipitation} 的 HEAD：原版这一次随机刻选中的列就是 {@code pos}，
 * 我们据此判断"原版会不会铺"，不会铺但又只差"托不住一整个雪层"时，记进真实覆雪数据。
 */
@Mixin(ServerLevel.class)
public abstract class MixinServerLevelSnowCover {

    @Inject(method = "tickPrecipitation", at = @At("HEAD"))
    private void otherworldinn$snowCoverOnPrecipitation(BlockPos pos, CallbackInfo ci) {
        SnowCoverServer.tryCover((ServerLevel) (Object) this, pos);
    }
}
