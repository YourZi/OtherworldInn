package com.otherworldinn.compat.kaleidoscopetavern;

import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.DrinkBlock;
import com.otherworldinn.OtherworldInn;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class DrinkBlockInteractionHandler {
    private DrinkBlockInteractionHandler() {}

    private static boolean shouldBlockDrinkBlockUse(
            BlockState state, ItemStack stack, PlayerInteractEvent.RightClickBlock event) {
        return state.getBlock() instanceof DrinkBlock
                && !stack.isEmpty()
                && !event.getEntity().isShiftKeyDown();
    }

    private static void denyRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setUseItem(TriState.FALSE);
        event.setUseBlock(TriState.FALSE);
        event.setCancellationResult(InteractionResult.FAIL);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!shouldBlockDrinkBlockUse(
                event.getLevel().getBlockState(event.getPos()),
                event.getItemStack(),
                event)) {
            return;
        }
        denyRightClickBlock(event);
    }

    @EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
    public static final class ClientHandler {
        private ClientHandler() {}

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (!shouldBlockDrinkBlockUse(
                    event.getLevel().getBlockState(event.getPos()),
                    event.getItemStack(),
                    event)) {
                return;
            }
            denyRightClickBlock(event);
        }
    }
}
