package com.otherworldinn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

/** 有机肥料：对随机刻方块强力催熟，带 age 属性的直接推到最高生长阶段，其余触发一次随机刻。 */
public class OrganicFertilizerItem extends Item {
    public OrganicFertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!state.isRandomlyTicking()) {
            return super.useOn(context);
        }
        if (level instanceof ServerLevel serverLevel && tryApplyToBlock(serverLevel, pos, state)) {
            context.getItemInHand().consume(1, context.getPlayer());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean tryApplyToBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (!state.isRandomlyTicking()) {
            return false;
        }
        triggerRandomTick(level, pos, state);
        // 骨粉式粒子与音效，data 即粒子数量，必须传 15 而非 0
        level.levelEvent(1505, pos, 15);
        // 非可骨粉催熟的随机刻方块，客户端不会出粒子，额外补一堆粒子
        if (!(state.getBlock() instanceof BonemealableBlock)) {
            level.sendParticles(
                    ParticleTypes.COMPOSTER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    45, 0.25, 0.25, 0.25, 0.0
            );
        }
        return true;
    }

    public static void triggerRandomTick(ServerLevel level, BlockPos pos, BlockState state) {
        Property<?> property = state.getBlock().getStateDefinition().getProperty("age");
        if (property instanceof IntegerProperty age) {
            int maxAge = getMaxAge(age);
            int targetAge = isTerminalAge(maxAge) ? Math.min(state.getValue(age) + 1, maxAge - 1) : maxAge;
            level.setBlockAndUpdate(pos, state.setValue(age, targetAge));
            level.getBlockState(pos).randomTick(level, pos, level.getRandom());
        } else {
            state.randomTick(level, pos, level.getRandom());
        }
    }

    private static boolean isTerminalAge(int maxAge) {
        return maxAge == ChorusFlowerBlock.DEAD_AGE || maxAge == GrowingPlantHeadBlock.MAX_AGE;
    }

    public static int getMaxAge(IntegerProperty age) {
        return age.getPossibleValues().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }
}
