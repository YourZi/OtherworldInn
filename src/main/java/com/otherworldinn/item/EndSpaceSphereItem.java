package com.otherworldinn.item;

import com.otherworldinn.world.teleport.TeleportUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EndSpaceSphereItem extends Item {
    public EndSpaceSphereItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel endLevel = serverPlayer.getServer().getLevel(Level.END);
        if (endLevel == null) {
            return InteractionResultHolder.fail(stack);
        }

        TeleportUtils.changeDimensionTo(serverPlayer, endLevel,
                new BlockPos(0, 71, 0));

        serverPlayer.level().playSound(
                null,
                serverPlayer.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                1.0F,
                1.0F);

        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.success(stack);
    }
}
