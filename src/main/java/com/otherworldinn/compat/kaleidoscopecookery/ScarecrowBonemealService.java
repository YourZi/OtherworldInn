package com.otherworldinn.compat.kaleidoscopecookery;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ScarecrowBonemealService {
    private static final int MIN_INTERVAL_TICKS = 8 * 20;
    private static final int MAX_INTERVAL_TICKS = 60 * 20;
    private static final int HORIZONTAL_RADIUS = 16;
    private static final int HORIZONTAL_RADIUS_SQR = HORIZONTAL_RADIUS * HORIZONTAL_RADIUS;
    private static final int VERTICAL_RADIUS = 3;
    private static final int MAX_ACCELERATORS_PER_CROP = 3;
    private static final ResourceLocation SCARECROW_ENTITY_ID =
            ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "scarecrow");

    private ScarecrowBonemealService() {}

    public static void tick(Entity scarecrow) {
        if (!(scarecrow.level() instanceof ServerLevel level) || !scarecrow.isAlive()) {
            return;
        }
        if (!(scarecrow instanceof ScarecrowBonemealAccess access)) {
            return;
        }

        long gameTime = level.getGameTime();
        long nextBonemealGameTime = access.otherworldinn$getNextBonemealGameTime();
        if (nextBonemealGameTime <= 0L) {
            access.otherworldinn$setNextBonemealGameTime(gameTime + randomIntervalTicks(scarecrow.getRandom()));
            return;
        }
        if (gameTime < nextBonemealGameTime) {
            return;
        }

        tryBonemealNearbyCrop(level, scarecrow);
        access.otherworldinn$setNextBonemealGameTime(gameTime + randomIntervalTicks(scarecrow.getRandom()));
    }

    private static void tryBonemealNearbyCrop(ServerLevel level, Entity scarecrow) {
        List<Entity> nearbyScarecrows = collectNearbyScarecrows(level, scarecrow);
        List<BlockPos> candidates = collectCandidateCrops(level, scarecrow, nearbyScarecrows);
        if (candidates.isEmpty()) {
            return;
        }

        RandomSource random = scarecrow.getRandom();
        BlockPos targetPos = candidates.get(random.nextInt(candidates.size()));
        BlockState state = level.getBlockState(targetPos);
        if (!(state.getBlock() instanceof BonemealableBlock bonemealable)) {
            return;
        }
        if (!bonemealable.isValidBonemealTarget(level, targetPos, state)) {
            return;
        }
        if (!bonemealable.isBonemealSuccess(level, random, targetPos, state)) {
            return;
        }

        bonemealable.performBonemeal(level, random, targetPos, state);
    }

    private static List<BlockPos> collectCandidateCrops(
            ServerLevel level, Entity scarecrow, List<Entity> nearbyScarecrows) {
        List<BlockPos> candidates = new ArrayList<>();
        BlockPos origin = scarecrow.blockPosition();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dy = -VERTICAL_RADIUS; dy <= VERTICAL_RADIUS; dy++) {
            for (int dx = -HORIZONTAL_RADIUS; dx <= HORIZONTAL_RADIUS; dx++) {
                for (int dz = -HORIZONTAL_RADIUS; dz <= HORIZONTAL_RADIUS; dz++) {
                    if ((dx * dx) + (dz * dz) > HORIZONTAL_RADIUS_SQR) {
                        continue;
                    }
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (!(state.getBlock() instanceof BonemealableBlock bonemealable)) {
                        continue;
                    }
                    if (!bonemealable.isValidBonemealTarget(level, cursor, state)) {
                        continue;
                    }
                    if (!isCropEligibleForScarecrow(cursor, scarecrow, nearbyScarecrows)) {
                        continue;
                    }
                    candidates.add(cursor.immutable());
                }
            }
        }
        return candidates;
    }

    private static boolean isCropEligibleForScarecrow(
            BlockPos cropPos, Entity currentScarecrow, List<Entity> nearbyScarecrows) {
        int currentId = currentScarecrow.getId();
        double currentDistance = squaredHorizontalDistanceToCenter(currentScarecrow, cropPos);
        int closerCount = 0;

        for (Entity scarecrow : nearbyScarecrows) {
            if (scarecrow == currentScarecrow || !canAffectCrop(scarecrow.blockPosition(), cropPos)) {
                continue;
            }
            double otherDistance = squaredHorizontalDistanceToCenter(scarecrow, cropPos);
            if (otherDistance < currentDistance
                    || (otherDistance == currentDistance && scarecrow.getId() < currentId)) {
                closerCount++;
                if (closerCount >= MAX_ACCELERATORS_PER_CROP) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<Entity> collectNearbyScarecrows(ServerLevel level, Entity currentScarecrow) {
        AABB searchBox =
                currentScarecrow
                        .getBoundingBox()
                        .inflate(HORIZONTAL_RADIUS * 2.0D, VERTICAL_RADIUS * 2.0D, HORIZONTAL_RADIUS * 2.0D);
        return level.getEntities(
                currentScarecrow,
                searchBox,
                entity ->
                        entity.isAlive()
                                && SCARECROW_ENTITY_ID.equals(entity.getType().builtInRegistryHolder().key().location()));
    }

    private static boolean canAffectCrop(BlockPos scarecrowPos, BlockPos cropPos) {
        int dx = scarecrowPos.getX() - cropPos.getX();
        int dy = Math.abs(scarecrowPos.getY() - cropPos.getY());
        int dz = scarecrowPos.getZ() - cropPos.getZ();
        return dy <= VERTICAL_RADIUS && (dx * dx) + (dz * dz) <= HORIZONTAL_RADIUS_SQR;
    }

    private static double squaredHorizontalDistanceToCenter(Entity scarecrow, BlockPos cropPos) {
        double dx = scarecrow.getX() - cropPos.getCenter().x;
        double dz = scarecrow.getZ() - cropPos.getCenter().z;
        return (dx * dx) + (dz * dz);
    }

    private static int randomIntervalTicks(RandomSource random) {
        return MIN_INTERVAL_TICKS + random.nextInt(MAX_INTERVAL_TICKS - MIN_INTERVAL_TICKS + 1);
    }
}
