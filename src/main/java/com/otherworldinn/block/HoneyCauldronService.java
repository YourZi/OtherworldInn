package com.otherworldinn.block;

import com.otherworldinn.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public final class HoneyCauldronService {
    public static final int HONEY_PER_LEVEL = 250;
    public static final int MAX_LEVEL = 4;
    public static final int MAX_HONEY_LEVEL = HONEY_PER_LEVEL * MAX_LEVEL;
    public static final int DRIP_INTERVAL_TICKS = 200;

    private HoneyCauldronService() {}

    public static boolean canAccumulateFromHive(BlockState hiveState) {
        return hiveState.hasProperty(BeehiveBlock.HONEY_LEVEL)
                && hiveState.getValue(BeehiveBlock.HONEY_LEVEL) >= BeehiveBlock.MAX_HONEY_LEVELS;
    }

    public static BlockState withLevel(int level) {
        return ModBlocks.HONEY_CAULDRON
                .get()
                .defaultBlockState()
                .setValue(HoneyCauldronBlock.LEVEL, Integer.valueOf(level));
    }

    public static int getLevel(BlockState state) {
        return state.getValue(HoneyCauldronBlock.LEVEL);
    }

    public static boolean isFull(BlockState state) {
        return isHoneyCauldron(state) && getLevel(state) >= MAX_LEVEL;
    }

    public static boolean isHoneyCauldron(BlockState state) {
        return state.is(ModBlocks.HONEY_CAULDRON.get());
    }

    public static boolean tryAccumulateBelowHive(net.minecraft.server.level.ServerLevel level, BlockPos hivePos) {
        BlockPos cauldronPos = hivePos.below();
        BlockState cauldronState = level.getBlockState(cauldronPos);
        BlockState newState = null;

        if (cauldronState.is(Blocks.CAULDRON)) {
            newState = withLevel(1);
        } else if (isHoneyCauldron(cauldronState) && getLevel(cauldronState) < MAX_LEVEL) {
            newState = cauldronState.setValue(HoneyCauldronBlock.LEVEL, Integer.valueOf(getLevel(cauldronState) + 1));
        }

        if (newState == null) {
            return false;
        }

        level.setBlockAndUpdate(cauldronPos, newState);
        level.gameEvent(GameEvent.BLOCK_CHANGE, cauldronPos, GameEvent.Context.of(newState));
        return true;
    }
}
