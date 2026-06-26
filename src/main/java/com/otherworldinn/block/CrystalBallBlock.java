package com.otherworldinn.block;

import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.teleport.TeleportUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CrystalBallBlock extends Block {
    public CrystalBallBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return handleUse(level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {
        handleUse(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private InteractionResult handleUse(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }

        ServerLevel magicLevel = serverPlayer.getServer().getLevel(TownDimensions.MAGIC_SPACE_LEVEL);
        if (magicLevel == null) {
            return InteractionResult.FAIL;
        }

        TeleportUtils.changeDimensionTo(serverPlayer, magicLevel, new BlockPos(0, 70, 0));
        level.playSound(null, pos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);

        return InteractionResult.SUCCESS;
    }
}
