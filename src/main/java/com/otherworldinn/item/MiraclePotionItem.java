package com.otherworldinn.item;

import com.otherworldinn.world.husbandry.HusbandryService;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 奇迹药水：唯一能让养殖动物繁殖的手段。
 *
 * <p>平时所有繁殖都被 {@code HusbandryInteractionHandler} 拦掉；喝过药水的那一次发情会被放行。
 * 右键成年动物使用，每只动物每天一次，需要两只同类都处于发情状态才会生下幼崽。
 */
public class MiraclePotionItem extends Item {
    public MiraclePotionItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!(target instanceof Animal animal)) {
            return InteractionResult.PASS;
        }
        if (player.level() instanceof ServerLevel level && player instanceof ServerPlayer serverPlayer) {
            HusbandryService.tryMiraclePotion(animal, level, serverPlayer, stack);
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide());
    }

    @Override
    public void appendHoverText(
            ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(
                Component.translatable("item.otherworldinn.miracle_potion.tooltip")
                        .withStyle(ChatFormatting.GRAY));
    }
}
