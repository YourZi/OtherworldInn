package com.otherworldinn.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class HoneyCauldronBlock extends AbstractCauldronBlock {
    public static final MapCodec<HoneyCauldronBlock> CODEC = simpleCodec(HoneyCauldronBlock::new);
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 1, 4);

    @Override
    public MapCodec<HoneyCauldronBlock> codec() {
        return CODEC;
    }

    public HoneyCauldronBlock(BlockBehaviour.Properties properties) {
        super(properties, HoneyCauldronInteractions.INTERACTIONS);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(1)));
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
        if (HoneyCauldronService.isFull(state) && !(stack.getItem() instanceof BucketItem)) {
            if (stack.is(Items.GLASS_BOTTLE) || stack.is(Items.HONEY_BOTTLE) || stack.is(Items.APPLE)) {
                return HoneyCauldronInteractions.INTERACTIONS
                        .map()
                        .get(stack.getItem())
                        .interact(state, level, pos, player, hand, stack);
            }
            return HoneyCauldronInteractions.solidifyIntoHoneyBlock(
                    state, level, pos, player, hand, stack);
        }
        if (stack.is(Items.HONEY_BLOCK) && !HoneyCauldronService.isFull(state)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(Items.CAULDRON);
    }

    @Override
    protected double getContentHeight(BlockState state) {
        return (5.0 + state.getValue(LEVEL) * 2.75) / 16.0;
    }

    @Override
    public boolean isFull(BlockState state) {
        return state.getValue(LEVEL) == 4;
    }

    @Override
    public void handlePrecipitation(
            BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {}

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return state.getValue(LEVEL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }
}
