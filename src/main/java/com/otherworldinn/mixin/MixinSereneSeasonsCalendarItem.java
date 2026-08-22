package com.otherworldinn.mixin;

import com.otherworldinn.util.ClientServices;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class MixinSereneSeasonsCalendarItem {
    private static final String SERENE_SEASONS_CALENDAR_CLASS = "sereneseasons.item.CalendarItem";

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$openSeasonCalendarOnBlock(
            UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!SERENE_SEASONS_CALENDAR_CLASS.equals(this.getClass().getName())) {
            return;
        }
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return;
        }
        Level level = context.getLevel();
        if (level.isClientSide) {
            ClientServices.openSeasonCalendar();
        }
        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$openSeasonCalendar(
            Level level,
            Player player,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!SERENE_SEASONS_CALENDAR_CLASS.equals(this.getClass().getName())) {
            return;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            return;
        }
        if (level.isClientSide) {
            ClientServices.openSeasonCalendar();
        }
        cir.setReturnValue(InteractionResultHolder.sidedSuccess(stack, level.isClientSide));
    }
}
