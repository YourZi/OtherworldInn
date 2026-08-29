package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModItems;
import com.simibubi.create.content.redstone.deskBell.DeskBellBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(
        modid = OtherworldInn.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public class InnKeyOverlay {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册到 HUD 管理器，优先级 15
        ItemHudOverlay.register(15, (unused) -> shouldShow(), InnKeyOverlay::render);
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return false;

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        if (mainHand.is(ModItems.INN_KEY.get()) || offHand.is(ModItems.INN_KEY.get())) {
            return true;
        }

        // 仅主手可触发工具判定
        return isTool(mainHand);
    }

    private static boolean isTool(ItemStack stack) {
        return stack.is(net.minecraft.tags.ItemTags.AXES)
                || stack.is(net.minecraft.tags.ItemTags.PICKAXES)
                || stack.is(net.minecraft.tags.ItemTags.SHOVELS)
                || stack.is(net.minecraft.tags.ItemTags.HOES);
    }

    private static void render(
            GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hitResult = mc.hitResult;

        if (hitResult instanceof BlockHitResult blockHitResult) {
            BlockEntity be = mc.level.getBlockEntity(blockHitResult.getBlockPos());
            if (be instanceof DeskBellBlockEntity) {
                Component toggleText =
                        Component.translatable(
                                "message.otherworldinn.inn_key.overlay.toggle_state");

                ItemHudOverlay.renderMouseActions(
                        guiGraphics,
                        new ItemHudOverlay.MouseAction(
                                ItemHudOverlay.MouseButton.RIGHT, toggleText));
            }
        }
    }
}
