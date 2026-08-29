package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModBlockProperties;
import com.otherworldinn.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
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
public class BedSheetOverlay {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册到 HUD 管理器，优先级 12
        ItemHudOverlay.register(12, (unused) -> shouldShow(), BedSheetOverlay::render);
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return false;

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        return mainHand.is(ModItems.BED_SHEET.get())
                || offHand.is(ModItems.BED_SHEET.get())
                || mainHand.is(ModItems.MESSY_BED_SHEET.get())
                || offHand.is(ModItems.MESSY_BED_SHEET.get());
    }

    private static void render(
            GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult)) return;

        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!isRelevantItem(stack)) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
        }

        // 1. 手持干净床单 -> 对准脏乱的床
        if (stack.is(ModItems.BED_SHEET.get())) {
            if (state.getBlock() instanceof BedBlock
                    && state.hasProperty(ModBlockProperties.MESSY)
                    && state.getValue(ModBlockProperties.MESSY)) {

                ItemHudOverlay.renderMouseActions(
                        guiGraphics,
                        new ItemHudOverlay.MouseAction(
                                ItemHudOverlay.MouseButton.RIGHT,
                                Component.translatable(
                                        "message.otherworldinn.bed_sheet.overlay.replace")));
            }
        }
        // 2. 手持脏乱床单 -> 对准水流
        else if (stack.is(ModItems.MESSY_BED_SHEET.get())) {

            // 执行包含流体的射线检测
            HitResult fluidHit = player.pick(player.blockInteractionRange(), 0.0F, true);
            if (fluidHit.getType() == HitResult.Type.BLOCK) {
                BlockPos fluidPos = ((BlockHitResult) fluidHit).getBlockPos();
                BlockState fluidState = mc.level.getBlockState(fluidPos);

                boolean isWater = fluidState.getBlock() == Blocks.WATER;
                boolean isWaterlogged = fluidState.getFluidState().is(FluidTags.WATER);
                boolean isWaterCauldron =
                        fluidState.is(Blocks.WATER_CAULDRON)
                                && fluidState.hasProperty(LayeredCauldronBlock.LEVEL)
                                && fluidState.getValue(LayeredCauldronBlock.LEVEL) > 0;

                if (isWater || isWaterlogged || isWaterCauldron) {
                    ItemHudOverlay.renderMouseActions(
                            guiGraphics,
                            new ItemHudOverlay.MouseAction(
                                    ItemHudOverlay.MouseButton.RIGHT,
                                    Component.translatable(
                                            "message.otherworldinn.messy_bed_sheet.overlay.wash")));
                }
            }
        }
    }

    private static boolean isRelevantItem(ItemStack stack) {
        return stack.is(ModItems.BED_SHEET.get()) || stack.is(ModItems.MESSY_BED_SHEET.get());
    }
}
