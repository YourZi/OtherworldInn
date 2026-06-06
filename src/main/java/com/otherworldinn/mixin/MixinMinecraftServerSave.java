package com.otherworldinn.mixin;

import com.otherworldinn.world.expedition.ExpeditionService;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServerSave {

    @Inject(method = "saveAllChunks", at = @At("HEAD"))
    private void otherworldinn$cleanupExpeditionLevels(
            boolean bl, boolean bl2, boolean bl3,
            CallbackInfoReturnable<Boolean> cir) {
        MinecraftServer self = (MinecraftServer) (Object) this;
        // 仅在真正关服（无在线玩家）时清理远征维度，避免暂停等场景误触发
        if (!self.getPlayerList().getPlayers().isEmpty()) return;
        ExpeditionService.cleanupAllExpeditionLevels(self);
    }
}
