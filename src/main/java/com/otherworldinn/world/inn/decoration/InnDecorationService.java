package com.otherworldinn.world.inn.decoration;

import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.team.TeamData;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * 旅社单方块全局装饰维护服务。
 *
 * <p>只对发生变化的方块坐标做增量校验，不遍历整片旅社地皮。
 */
public final class InnDecorationService {
    private InnDecorationService() {}

    public record ReconcileResult(
            boolean changed, Set<String> activatedDecorationIds, Set<String> removedDecorationIds) {}

    public static ReconcileResult reconcileChangedPositions(
            ServerLevel level, TeamData team, Set<BlockPos> changedPositions) {
        if (level == null || team == null || changedPositions == null || changedPositions.isEmpty()) {
            return new ReconcileResult(false, Set.of(), Set.of());
        }

        InnData innData = team.getInnData();
        Set<String> activatedDecorationIds = new LinkedHashSet<>();
        Set<String> removedDecorationIds = new LinkedHashSet<>();
        boolean changed = false;

        for (BlockPos pos : changedPositions) {
            if (pos == null) {
                continue;
            }

            String previousDecorationId = innData.getActiveDecorationIdAt(pos);
            Optional<InnDecorationStats> currentStats = resolveCurrentDecoration(level, team, pos);
            String currentDecorationId = currentStats.map(InnDecorationStats::id).orElse(null);

            if (Objects.equals(previousDecorationId, currentDecorationId)) {
                continue;
            }

            if (previousDecorationId != null && innData.removeActiveDecorationAt(pos)) {
                removedDecorationIds.add(previousDecorationId);
                changed = true;
            }

            if (currentDecorationId != null && innData.setActiveDecorationAt(pos, currentDecorationId)) {
                activatedDecorationIds.add(currentDecorationId);
                changed = true;
            }
        }

        if (!changed) {
            return new ReconcileResult(false, Set.of(), Set.of());
        }
        return new ReconcileResult(
                true, Set.copyOf(activatedDecorationIds), Set.copyOf(removedDecorationIds));
    }

    private static Optional<InnDecorationStats> resolveCurrentDecoration(
            ServerLevel level, TeamData team, BlockPos pos) {
        if (!team.isInInnZone(pos)) {
            return Optional.empty();
        }
        return InnDecorationRegistry.getStats(level.getBlockState(pos).getBlock());
    }
}
