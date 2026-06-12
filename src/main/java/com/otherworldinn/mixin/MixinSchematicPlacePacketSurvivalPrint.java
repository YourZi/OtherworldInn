package com.otherworldinn.mixin;

import com.otherworldinn.foundation.SchematicSurvivalPrintHelper;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SchematicPlacePacket.class)
public class MixinSchematicPlacePacketSurvivalPrint {

    /**
     * 蓝图开启了生存打印标记时，绕过 isCreative() 检查。
     */
    @Redirect(
            method = "handle",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;isCreative()Z"))
    private boolean otherworldinn$bypassCreativeCheck(ServerPlayer player) {
        SchematicPlacePacket self = (SchematicPlacePacket) (Object) this;
        if (SchematicSurvivalPrintHelper.isEnabled(self.stack())) return true;
        return player.isCreative();
    }

    /**
     * 放置完成后，生存模式下消耗 1 个蓝图。
     */
    @Inject(method = "handle", at = @At("RETURN"))
    private void otherworldinn$consumeAfterPlace(ServerPlayer player, CallbackInfo ci) {
        if (player.isCreative()) return;

        SchematicPlacePacket self = (SchematicPlacePacket) (Object) this;
        if (!SchematicSurvivalPrintHelper.isEnabled(self.stack())) return;

        ItemStack schematic = self.stack();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (ItemStack.matches(schematic, invStack)) {
                invStack.shrink(1);
                break;
            }
        }
    }
}
