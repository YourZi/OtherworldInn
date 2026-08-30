package com.otherworldinn.item;

import com.otherworldinn.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** 脏乱的床单：对水源或含水方块长按右键清洗回干净床单，消耗耐久。 */
public class MessyBedSheetItem extends Item {

    public MessyBedSheetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        BlockHitResult hitResult =
                getPlayerPOVHitResult(
                        level, player, net.minecraft.world.level.ClipContext.Fluid.ANY);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (isWashTarget(state)) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(stack);
            }
        }

        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(
            Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (livingEntity instanceof Player player && level.isClientSide) {
            if (level.getGameTime() % 5 == 0) {
                BlockHitResult hitResult =
                        getPlayerPOVHitResult(
                                level, player, net.minecraft.world.level.ClipContext.Fluid.ANY);
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = hitResult.getBlockPos();
                    for (int i = 0; i < 25; ++i) {
                        level.addParticle(
                                ParticleTypes.SPLASH,
                                pos.getX() + 0.5 + (level.random.nextDouble() - 0.5),
                                pos.getY() + 1.0,
                                pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5),
                                (level.random.nextDouble() - 0.5) * 0.5,
                                level.random.nextDouble() * 0.5,
                                (level.random.nextDouble() - 0.5) * 0.5);
                    }
                }
            }
            if (level.getGameTime() % 10 == 0) {
                level.playSound(
                        player,
                        player.blockPosition(),
                        SoundEvents.BRUSH_GENERIC,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof Player player) {
            BlockHitResult hitResult =
                    getPlayerPOVHitResult(level, player, net.minecraft.world.level.ClipContext.Fluid.ANY);
            BlockState washState =
                    hitResult.getType() == HitResult.Type.BLOCK
                            ? level.getBlockState(hitResult.getBlockPos())
                            : Blocks.AIR.defaultBlockState();
            if (isWaterCauldron(washState)) {
                consumeCauldronWater(level, hitResult.getBlockPos(), washState);
            }
            boolean skipDurability = isWaterCauldron(washState) && level.random.nextFloat() < 0.5F;
            if (!skipDurability) {
                stack.hurtAndBreak(1, player, Player.getSlotForHand(player.getUsedItemHand()));
            }

            if (!stack.isEmpty()) {
                ItemStack cleanSheet = createCleanSheet(stack, false);
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.BUCKET_EMPTY,
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F);

                return cleanSheet;
            }
        }
        return stack;
    }

    public static boolean isWashTarget(BlockState state) {
        boolean isWater = state.getBlock() == Blocks.WATER;
        boolean isWaterlogged = state.getFluidState().is(net.minecraft.tags.FluidTags.WATER);
        boolean isWaterCauldron = isWaterCauldron(state);
        return isWater || isWaterlogged || isWaterCauldron;
    }

    private static BlockState getCurrentWashState(Level level, Player player) {
        BlockHitResult hitResult =
                getPlayerPOVHitResult(level, player, net.minecraft.world.level.ClipContext.Fluid.ANY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return Blocks.AIR.defaultBlockState();
        }
        return level.getBlockState(hitResult.getBlockPos());
    }

    public static ItemStack createCleanSheet(ItemStack source, boolean consumeDurability) {
        ItemStack cleanSheet = new ItemStack(ModItems.BED_SHEET.get());
        int damage = source.getDamageValue() + (consumeDurability ? 1 : 0);
        cleanSheet.setDamageValue(Math.min(cleanSheet.getMaxDamage(), Math.max(0, damage)));
        return cleanSheet;
    }

    public static boolean consumeCauldronWaterIfNeeded(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!isWaterCauldron(state)) {
            return false;
        }
        consumeCauldronWater(level, pos, state);
        return true;
    }

    public static boolean isWaterCauldron(BlockState state) {
        return state.is(Blocks.WATER_CAULDRON)
                && state.hasProperty(LayeredCauldronBlock.LEVEL)
                && state.getValue(LayeredCauldronBlock.LEVEL) > 0;
    }

    private static void consumeCauldronWater(Level level, BlockPos pos, BlockState state) {
        int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);
        if (waterLevel <= 1) {
            level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            level.setBlock(
                    pos, state.setValue(LayeredCauldronBlock.LEVEL, waterLevel - 1), Block.UPDATE_ALL);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            BlockState state = getCurrentWashState(entity.level(), player);
            if (isWaterCauldron(state)) {
                return 120;
            }
        }
        return 200;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }
}
