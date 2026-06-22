package com.otherworldinn.compat.starcatcher;

import com.otherworldinn.entity.store.FishermanEntity;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

public final class StarcatcherFishingSaleFavor {
    private static final double SEARCH_RADIUS = 256.0D;

    private StarcatcherFishingSaleFavor() {}

    public static void awardFishermanFavor(ServerPlayer player, BlockPos sourcePos, int saleValue) {
        if (player == null || player.level().isClientSide || saleValue <= 0) {
            return;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int favorProgress = Math.max(1, Math.round(saleValue * 0.5F));
        FishermanEntity fisherman = findNearestFisherman(serverLevel, sourcePos);
        if (fisherman != null) {
            fisherman.addFavorProgress(favorProgress);
        }
    }

    private static FishermanEntity findNearestFisherman(ServerLevel level, BlockPos sourcePos) {
        AABB searchBox = AABB.ofSize(sourcePos.getCenter(), SEARCH_RADIUS * 2, 128.0D, SEARCH_RADIUS * 2);
        List<FishermanEntity> fishermen = level.getEntitiesOfClass(FishermanEntity.class, searchBox);
        return fishermen.stream()
                .filter(FishermanEntity::isAlive)
                .min(Comparator.comparingDouble(fisherman -> fisherman.distanceToSqr(sourcePos.getCenter())))
                .orElse(null);
    }
}
