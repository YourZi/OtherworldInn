package com.otherworldinn.block;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public final class HoneyCauldronInteractions {
    public static final CauldronInteraction.InteractionMap INTERACTIONS =
            CauldronInteraction.newInteractionMap("otherworldinn_honey");

    private static boolean bootstrapped;

    private HoneyCauldronInteractions() {}

    public static void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;

        CauldronInteraction.EMPTY.map().put(Items.HONEY_BOTTLE, HoneyCauldronInteractions::fillEmptyFromBottle);
        CauldronInteraction.EMPTY.map().put(getHoneyBucketItem(), HoneyCauldronInteractions::fillEmptyFromBucket);
        CauldronInteraction.EMPTY.map().put(Items.HONEY_BLOCK, HoneyCauldronInteractions::fillEmptyFromHoneyBlock);

        INTERACTIONS.map().put(Items.GLASS_BOTTLE, HoneyCauldronInteractions::drainIntoBottle);
        INTERACTIONS.map().put(Items.HONEY_BOTTLE, HoneyCauldronInteractions::fillFromBottle);
        INTERACTIONS.map().put(Items.BUCKET, HoneyCauldronInteractions::drainIntoBucket);
        INTERACTIONS.map().put(Items.APPLE, HoneyCauldronInteractions::craftHoneyedApple);
    }

    private static Item getHoneyBucketItem() {
        return AllFluids.HONEY
                .getBucket()
                .orElseThrow(() -> new IllegalStateException("Create honey bucket is missing"));
    }

    private static ItemInteractionResult fillEmptyFromBottle(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack) {
        return setLevelFromBottle(level, pos, player, hand, stack, HoneyCauldronService.withLevel(1));
    }

    private static ItemInteractionResult fillEmptyFromBucket(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack) {
        return CauldronInteraction.emptyBucket(
                level,
                pos,
                player,
                hand,
                stack,
                HoneyCauldronService.withLevel(HoneyCauldronService.MAX_LEVEL),
                SoundEvents.BUCKET_EMPTY);
    }

    private static ItemInteractionResult fillEmptyFromHoneyBlock(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack) {
        if (!level.isClientSide) {
            Item item = stack.getItem();
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, HoneyCauldronService.withLevel(HoneyCauldronService.MAX_LEVEL));
            level.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static ItemInteractionResult fillFromBottle(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack) {
        int levelValue = HoneyCauldronService.getLevel(state);
        if (levelValue >= HoneyCauldronService.MAX_LEVEL) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return setLevelFromBottle(
                level,
                pos,
                player,
                hand,
                stack,
                state.setValue(HoneyCauldronBlock.LEVEL, Integer.valueOf(levelValue + 1)));
    }

    private static ItemInteractionResult setLevelFromBottle(
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack,
            BlockState newState) {
        if (!level.isClientSide) {
            Item item = stack.getItem();
            player.setItemInHand(
                    hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static ItemInteractionResult drainIntoBottle(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack) {
        if (!level.isClientSide) {
            Item item = stack.getItem();
            player.setItemInHand(
                    hand,
                    ItemUtils.createFilledResult(
                            stack, player, new ItemStack(Items.HONEY_BOTTLE)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));

            int levelValue = HoneyCauldronService.getLevel(state) - 1;
            BlockState newState =
                    levelValue <= 0
                            ? Blocks.CAULDRON.defaultBlockState()
                            : state.setValue(HoneyCauldronBlock.LEVEL, Integer.valueOf(levelValue));
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private static ItemInteractionResult drainIntoBucket(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack) {
        return CauldronInteraction.fillBucket(
                state,
                level,
                pos,
                player,
                hand,
                stack,
                new ItemStack(getHoneyBucketItem()),
                blockState ->
                        blockState.is(ModBlocks.HONEY_CAULDRON.get())
                                && HoneyCauldronService.getLevel(blockState)
                                        == HoneyCauldronService.MAX_LEVEL,
                SoundEvents.BUCKET_FILL);
    }

    private static ItemInteractionResult craftHoneyedApple(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack) {
        if (!level.isClientSide) {
            Item item = stack.getItem();
            player.setItemInHand(
                    hand,
                    ItemUtils.createFilledResult(
                            stack, player, new ItemStack(AllItems.HONEYED_APPLE.get())));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));

            int levelValue = HoneyCauldronService.getLevel(state) - 1;
            BlockState newState =
                    levelValue <= 0
                            ? Blocks.CAULDRON.defaultBlockState()
                            : state.setValue(HoneyCauldronBlock.LEVEL, Integer.valueOf(levelValue));
            level.setBlockAndUpdate(pos, newState);
            AllSoundEvents.SPOUTING.playOnServer(level, pos, 0.75f, 1.0f);
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    public static ItemInteractionResult solidifyIntoHoneyBlock(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack stack) {
        if (stack.is(Items.BUCKET) || !HoneyCauldronService.isFull(state)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            Item item = stack.getItem();
            ItemStack honeyBlock = new ItemStack(Items.HONEY_BLOCK);
            if (!player.getInventory().add(honeyBlock)) {
                player.drop(honeyBlock, false);
            }
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            level.playSound(null, pos, SoundEvents.HONEY_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
