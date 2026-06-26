package com.otherworldinn.world.teleport;

import net.minecraft.core.Direction;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class TeleportUtils {
    public static void changeDimensionTo(ServerPlayer player, ServerLevel targetLevel, BlockPos safePos) {
        player.teleportTo(
                targetLevel,
                safePos.getX() + 0.5D,
                safePos.getY(),
                safePos.getZ() + 0.5D,
                Set.of(),
                player.getYRot(),
                player.getXRot());
    }

    public static void changeDimensionTo(ServerPlayer player, ServerLevel targetLevel, Vec3 target) {
        player.teleportTo(
                targetLevel,
                target.x,
                target.y,
                target.z,
                Set.of(),
                player.getYRot(),
                player.getXRot());
    }

    public static BlockPos findSafeSpawnPos(ServerLevel level, BlockPos basePos) {
        Set<Long> ensuredChunks = new HashSet<>();
        BlockPos safePos = findSafeSpawnPosNear(level, basePos, 8, ensuredChunks);
        if (safePos != null) {
            return safePos;
        }

        int baseX = basePos.getX();
        int baseZ = basePos.getZ();
        ensureChunk(level, baseX, baseZ, ensuredChunks);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, baseX, baseZ);
        int fallbackY = Math.max(y, level.getSeaLevel() + 1);
        BlockPos fallback = findSafeYAround(level, baseX, fallbackY, baseZ, 64);
        if (fallback != null) {
            return fallback;
        }

        BlockPos widerSafePos = findSafeSpawnPosNear(level, basePos, 16, ensuredChunks);
        if (widerSafePos != null) {
            return widerSafePos;
        }

        BlockPos spawnSafePos = findSafeSpawnPosNear(level, level.getSharedSpawnPos(), 12, ensuredChunks);
        if (spawnSafePos != null) {
            return spawnSafePos;
        }

        BlockPos emergencySafePos = findEmergencySafeSpawn(level, basePos, ensuredChunks);
        if (emergencySafePos != null) {
            return emergencySafePos;
        }

        BlockPos sharedSpawn = level.getSharedSpawnPos();
        ensureChunk(level, sharedSpawn.getX(), sharedSpawn.getZ(), ensuredChunks);
        int sharedSpawnY =
                level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        sharedSpawn.getX(),
                        sharedSpawn.getZ());
        BlockPos sharedSpawnFallback =
                findSafeYAround(
                        level,
                        sharedSpawn.getX(),
                        Math.max(sharedSpawnY, level.getSeaLevel() + 1),
                        sharedSpawn.getZ(),
                        128);
        if (sharedSpawnFallback != null) {
            return sharedSpawnFallback;
        }

        return level.getSharedSpawnPos().above();
    }

    public static BlockPos findRandomSafeSpawnPos(
            ServerLevel level, BlockPos centerPos, int radius, int attempts) {
        if (radius <= 0 || attempts <= 0) {
            return findSafeSpawnPos(level, centerPos);
        }

        Set<Long> ensuredChunks = new HashSet<>();
        for (int i = 0; i < attempts; i++) {
            BlockPos randomBase = randomizeHorizontalBase(level, centerPos, radius);
            BlockPos safePos = findSafeSpawnPosNear(level, randomBase, 8, ensuredChunks);
            if (safePos != null) {
                return safePos;
            }

            ensureChunk(level, randomBase.getX(), randomBase.getZ(), ensuredChunks);
            int y =
                    level.getHeight(
                            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            randomBase.getX(),
                            randomBase.getZ());
            BlockPos fallback =
                    findSafeYAround(level, randomBase.getX(), Math.max(y, level.getSeaLevel() + 1), randomBase.getZ(), 64);
            if (fallback != null) {
                return fallback;
            }
        }

        return findSafeSpawnPos(level, centerPos);
    }

    private static boolean isSafeSpawn(ServerLevel level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (below.isAir() || !below.getFluidState().isEmpty()) {
            return false;
        }
        if (below.getCollisionShape(level, belowPos).isEmpty()) {
            return false;
        }
        if (!below.isFaceSturdy(level, belowPos, Direction.UP)) {
            return false;
        }

        BlockState body = level.getBlockState(pos);
        BlockState head = level.getBlockState(pos.above());
        if (!body.getFluidState().isEmpty() || !head.getFluidState().isEmpty()) {
            return false;
        }
        if (!body.getCollisionShape(level, pos).isEmpty()) {
            return false;
        }
        return head.getCollisionShape(level, pos.above()).isEmpty();
    }

    private static BlockPos findSafeYAround(ServerLevel level, int x, int baseY, int z, int searchRange) {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 2;
        for (int dy = 0; dy <= searchRange; dy++) {
            for (int sign = -1; sign <= 1; sign += 2) {
                int y = baseY + dy * sign;
                if (y < minY || y > maxY) {
                    continue;
                }
                BlockPos candidate = new BlockPos(x, y, z);
                if (isSafeSpawn(level, candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static BlockPos findSafeSpawnPosNear(
            ServerLevel level, BlockPos centerPos, int maxRadius, Set<Long> ensuredChunks) {
        int baseX = centerPos.getX();
        int baseZ = centerPos.getZ();
        for (int r = 0; r <= maxRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int x = baseX + dx * 4;
                    int z = baseZ + dz * 4;
                    ensureChunk(level, x, z, ensuredChunks);
                    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    BlockPos base = new BlockPos(x, y, z);
                    for (int dy = 0; dy <= 2; dy++) {
                        BlockPos candidate = base.above(dy);
                        if (isSafeSpawn(level, candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static BlockPos findEmergencySafeSpawn(
            ServerLevel level, BlockPos centerPos, Set<Long> ensuredChunks) {
        int baseX = centerPos.getX();
        int baseZ = centerPos.getZ();
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 2;
        for (int r = 0; r <= 24; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int x = baseX + dx;
                    int z = baseZ + dz;
                    ensureChunk(level, x, z, ensuredChunks);
                    int startY =
                            Math.max(
                                    minY,
                                    Math.min(
                                            maxY,
                                            level.getHeight(
                                                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)));
                    BlockPos candidate = findSafeYAround(level, x, startY, z, maxY - minY);
                    if (candidate != null) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private static void ensureChunk(ServerLevel level, int x, int z, Set<Long> ensuredChunks) {
        int chunkX = SectionPos.blockToSectionCoord(x);
        int chunkZ = SectionPos.blockToSectionCoord(z);
        long key = (((long) chunkX) << 32) ^ (chunkZ & 0xffffffffL);
        if (ensuredChunks != null && !ensuredChunks.add(key)) {
            return;
        }
        level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

    private static BlockPos randomizeHorizontalBase(ServerLevel level, BlockPos basePos, int radius) {
        if (radius <= 0) {
            return basePos;
        }
        RandomSource random = level.getRandom();
        int offsetX;
        int offsetZ;
        do {
            offsetX = random.nextInt(radius * 2 + 1) - radius;
            offsetZ = random.nextInt(radius * 2 + 1) - radius;
        } while ((long) offsetX * offsetX + (long) offsetZ * offsetZ > (long) radius * radius);
        return basePos.offset(offsetX, 0, offsetZ);
    }
}
