package com.otherworldinn.mixin;

import com.otherworldinn.compat.starcatcher.StarcatcherFishingSaleFavor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.wdiscute.sellingbin.bin.SellingBinMenu", remap = false)
public abstract class MixinSellingBinMenu {
    @Unique private int otherworldinn$progressBeforeSale;
    @Unique private BlockPos otherworldinn$salePos;

    @Inject(method = "clickMenuButton", at = @At("HEAD"), remap = false)
    private void otherworldinn$captureProgressBeforeSale(
            Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!otherworldinn$isSellButton(id)) {
            return;
        }
        this.otherworldinn$progressBeforeSale = this.otherworldinn$getProgressAvailable();
        this.otherworldinn$salePos = this.otherworldinn$getBlockPos();
    }

    @Inject(method = "clickMenuButton", at = @At("RETURN"), remap = false)
    private void otherworldinn$awardFavorAfterSale(
            Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!otherworldinn$isSellButton(id) || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        int soldValue = Math.max(0, this.otherworldinn$getProgressAvailable() - this.otherworldinn$progressBeforeSale);
        if (soldValue > 0 && this.otherworldinn$salePos != null) {
            StarcatcherFishingSaleFavor.awardFishermanFavor(
                    serverPlayer, this.otherworldinn$salePos, soldValue);
        }
    }

    @Unique
    private static boolean otherworldinn$isSellButton(int id) {
        return id == 67 || id == 68;
    }

    @Unique
    private int otherworldinn$getProgressAvailable() {
        Object blockEntity = this.otherworldinn$getSellingBinBlockEntity();
        if (blockEntity == null) {
            return 0;
        }
        try {
            Method method = blockEntity.getClass().getMethod("getProgressAvailable");
            Object result = method.invoke(blockEntity);
            return result instanceof Integer value ? value : 0;
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }

    @Unique
    private BlockPos otherworldinn$getBlockPos() {
        Object blockEntity = this.otherworldinn$getSellingBinBlockEntity();
        if (blockEntity == null) {
            return null;
        }
        try {
            Method method = blockEntity.getClass().getMethod("getBlockPos");
            Object result = method.invoke(blockEntity);
            return result instanceof BlockPos blockPos ? blockPos : null;
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    @Unique
    private Object otherworldinn$getSellingBinBlockEntity() {
        try {
            Field field = this.getClass().getDeclaredField("be");
            field.setAccessible(true);
            return field.get(this);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
