package com.otherworldinn.mixin;

import com.otherworldinn.foundation.SchematicSurvivalPrintHelper;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.event.listener.TownProtectionHandler;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.schematics.SchematicInstances;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.packet.SchematicPlacePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SchematicPlacePacket.class)
public class MixinSchematicPlacePacketSurvivalPrint {

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$denyProtectedTownPrint(ServerPlayer player, CallbackInfo ci) {
        if (player == null) {
            return;
        }
        if (!(player.level() instanceof ServerLevel level)
                || level.dimension() != TownDimensions.TOWN_LEVEL) {
            return;
        }

        SchematicPlacePacket self = (SchematicPlacePacket) (Object) this;
        SchematicPrinter printer = new SchematicPrinter();
        printer.loadSchematic(self.stack(), level, !player.canUseGameMasterBlocks());
        if (!printer.isLoaded() || printer.isErrored()) {
            return;
        }

        while (printer.advanceCurrentPos()) {
            if (printer.getPrintStage() == SchematicPrinter.PrintStage.ENTITIES) {
                if (!TownProtectionHandler.isInnRestrictionLiftedAt(level, printer.getCurrentTarget())) {
                    denyPrint(player);
                    ci.cancel();
                    return;
                }
                continue;
            }

            BlockState[] stateHolder = new BlockState[1];
            printer.handleCurrentTarget(
                    (pos, state, blockEntity) -> stateHolder[0] = state,
                    (pos, entity) -> {
                    });

            BlockState state = stateHolder[0];
            if (state == null || state.isAir()) {
                continue;
            }
            if (!TownProtectionHandler.isInnRestrictionLiftedAt(level, printer.getCurrentTarget())) {
                denyPrint(player);
                ci.cancel();
                return;
            }
        }
    }

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

    private static void denyPrint(ServerPlayer player) {
        resetDeniedSchematicState(player);
        player.displayClientMessage(
                Component.translatable("message.otherworldinn.protection.schematic_deny")
                        .withStyle(ChatFormatting.RED),
                true);
    }

    private static void resetDeniedSchematicState(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) {
            return;
        }
        if (mainHand.has(AllDataComponents.SCHEMATIC_DEPLOYED)) {
            mainHand.set(AllDataComponents.SCHEMATIC_DEPLOYED, false);
            SchematicInstances.clearHash(mainHand);
        }
        player.inventoryMenu.sendAllDataToRemote();
    }
}
