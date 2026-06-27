package com.otherworldinn.mixin;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.brew.DrinkBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DrinkBlock.class, remap = false)
public class MixinKaleidoscopeDrinkBlock {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$pickupDrinkFromBlockEntity(
            ItemStack heldStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult,
            CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (player == null
                || heldStack.isEmpty()
                || player.isSecondaryUseActive()
                || !(state.getBlock() instanceof DrinkBlock drinkBlock)) {
            return;
        }

        if (level.isClientSide) {
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DrinkBlockEntity drinkBlockEntity)) {
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
            return;
        }

        ItemStack pickedUp = drinkBlockEntity.removeItem();
        if (pickedUp.isEmpty()) {
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
            return;
        }

        drinkBlockEntity.refresh();
        ItemHandlerHelper.giveItemToPlayer(player, pickedUp);
        int count = state.getValue(drinkBlock.getCountProperty());
        if (count > 1) {
            level.setBlockAndUpdate(pos, state.setValue(drinkBlock.getCountProperty(), count - 1));
        } else {
            level.removeBlock(pos, false);
        }
        level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS);
        cir.setReturnValue(ItemInteractionResult.SUCCESS);
    }
}
