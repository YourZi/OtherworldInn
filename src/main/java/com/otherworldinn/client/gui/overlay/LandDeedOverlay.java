package com.otherworldinn.client.gui.overlay;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.item.LandDeedItem;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
public class LandDeedOverlay {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 优先级 20，需高于房间登记册
        ItemHudOverlay.register(20, (unused) -> shouldShow(), LandDeedOverlay::render);
    }

    private static boolean shouldShow() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return false;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean holding = stack.is(ModItems.LAND_DEED.get());
        if (!holding) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
            holding = stack.is(ModItems.LAND_DEED.get());
        }
        return holding;
    }

    private static void render(
            GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!stack.is(ModItems.LAND_DEED.get())) {
            stack = player.getItemInHand(InteractionHand.OFF_HAND);
        }

        // 状态机：Pos1 未定 -> Pos2 未定 -> 确认
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        boolean hasPos1 = tag.contains("Pos1");

        if (!hasPos1) {
            // [RMB] 设置第一点
            ItemHudOverlay.renderMouseActions(
                    guiGraphics,
                    new ItemHudOverlay.MouseAction(
                            ItemHudOverlay.MouseButton.RIGHT,
                            Component.translatable(
                                    "message.otherworldinn.land_deed.overlay.set_pos1")));
        } else if (!tag.contains("Pos2")) {
            // [RMB] 设置第二点 (显示价格)
            BlockPos pos1 = BlockPos.of(tag.getLong("Pos1"));

            Component text;
            int color = ModColors.WHITE;

            // 实时获取光标位置
            BlockPos pos2 = null;
            HitResult hitResult = mc.hitResult;
            if (hitResult instanceof BlockHitResult blockHitResult) {
                pos2 = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
            }

            if (pos2 != null) {
                TeamData team = TeamManager.getInstance().getClientPlayerTeam();

                if (!LandDeedItem.isWithinBounds(pos1, pos2)) {
                    text =
                            Component.translatable(
                                    "message.otherworldinn.land_deed.fail_out_of_bounds");
                    color = ModColors.RED;
                } else if (!LandDeedItem.isWithinRatingAreaLimit(team, pos1, pos2)) {
                    text =
                            Component.translatable(
                                    "message.otherworldinn.land_deed.fail_rating_limit");
                    color = ModColors.RED;
                } else {
                    int price = LandDeedItem.calculatePrice(team, pos1, pos2);
                    int coins = team != null ? team.getCoins() : 0;

                    if (price > coins) {
                        color = ModColors.RED;
                    }

                    text =
                            Component.translatable(
                                    "message.otherworldinn.land_deed.overlay.set_pos2_with_cost",
                                    price);
                }
            } else {
                text = Component.translatable("message.otherworldinn.land_deed.overlay.set_pos2");
            }

            ItemHudOverlay.renderMouseActions(
                    guiGraphics,
                    new ItemHudOverlay.MouseAction(
                            ItemHudOverlay.MouseButton.LEFT,
                            Component.translatable(
                                    "message.otherworldinn.land_deed.overlay.cancel")),
                    new ItemHudOverlay.MouseAction(ItemHudOverlay.MouseButton.RIGHT, text, color));

        } else {
            // [RMB] 确认扩展 (显示价格)
            BlockPos pos1 = BlockPos.of(tag.getLong("Pos1"));
            BlockPos pos2 = BlockPos.of(tag.getLong("Pos2"));

            Component text;
            int color = ModColors.WHITE;

            TeamData team = TeamManager.getInstance().getClientPlayerTeam();

            if (!LandDeedItem.isWithinBounds(pos1, pos2)) {
                text = Component.translatable("message.otherworldinn.land_deed.fail_out_of_bounds");
                color = ModColors.RED;
            } else if (!LandDeedItem.isWithinRatingAreaLimit(team, pos1, pos2)) {
                text = Component.translatable("message.otherworldinn.land_deed.fail_rating_limit");
                color = ModColors.RED;
            } else {
                int price = LandDeedItem.calculatePrice(team, pos1, pos2);
                int coins = team != null ? team.getCoins() : 0;

                if (price > coins) {
                    color = ModColors.RED;
                }

                text =
                        Component.translatable(
                                "message.otherworldinn.land_deed.overlay.confirm_with_cost", price);
            }

            ItemHudOverlay.renderMouseActions(
                    guiGraphics,
                    new ItemHudOverlay.MouseAction(
                            ItemHudOverlay.MouseButton.LEFT,
                            Component.translatable(
                                    "message.otherworldinn.land_deed.overlay.cancel")),
                    new ItemHudOverlay.MouseAction(ItemHudOverlay.MouseButton.RIGHT, text, color));
        }
    }
}
