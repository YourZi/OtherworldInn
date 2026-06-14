package com.otherworldinn.block;

import com.otherworldinn.world.teleport.TeleportUtils;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.data.CrystalBallSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrystalBallBlock extends Block {

    /** 记录玩家进入魔法空间前的水晶球位置，用于虚空坠落时返回 */
    public static final Map<UUID, BlockPos> RETURN_POSITIONS = new ConcurrentHashMap<>();

    /** 当前在魔法空间内的玩家 */
    public static final Set<UUID> PLAYERS_IN_MAGIC_SPACE =
            ConcurrentHashMap.newKeySet();

    public CrystalBallBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide) return;

        // 城镇维度才能放置水晶球（由 only_in_town tag + TownProtectionHandler 保证）
        // 此处仅做存档唯一性检查
        ServerLevel townLevel = level.getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) return;

        CrystalBallSavedData data = CrystalBallSavedData.get(townLevel);
        BlockPos existing = data.getBallPos();

        // 如果已存在不同位置的水晶球，拒绝新放置
        if (existing != null && !existing.equals(pos)) {
            level.removeBlock(pos, false);
            if (placer instanceof ServerPlayer sp) {
                sp.displayClientMessage(
                        Component.translatable("message.otherworldinn.crystal_ball.already_exists")
                                .withStyle(net.minecraft.ChatFormatting.RED), true);
            }
            return;
        }

        // 记录此位置为唯一水晶球
        data.setBallPos(pos.immutable());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
            BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide && !state.is(newState.getBlock())) {
            ServerLevel townLevel = level.getServer().getLevel(TownDimensions.TOWN_LEVEL);
            if (townLevel != null) {
                CrystalBallSavedData data = CrystalBallSavedData.get(townLevel);
                if (pos.equals(data.getBallPos())) {
                    data.setBallPos(null);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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

        RETURN_POSITIONS.put(player.getUUID(), pos.immutable());
        PLAYERS_IN_MAGIC_SPACE.add(player.getUUID());
        TeleportUtils.changeDimensionTo(serverPlayer, magicLevel, new BlockPos(0, 70, 0));
        level.playSound(null, pos, SoundEvents.PORTAL_TRAVEL, SoundSource.BLOCKS, 1.0F, 1.0F);

        return InteractionResult.SUCCESS;
    }
}
