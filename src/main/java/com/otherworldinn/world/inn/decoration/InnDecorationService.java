package com.otherworldinn.world.inn.decoration;

import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.team.TeamData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 旅社装饰识别服务。
 *
 * <p>不依赖核心方块，而是把玩家点击的方块视作模板中的潜在成员，反推出原点后做整体匹配。
 */
public final class InnDecorationService {
    private InnDecorationService() {}

    public enum ActivationStatus {
        ACTIVATED,
        ALREADY_ACTIVE,
        LIMIT_REACHED,
        NOT_FOUND
    }

    public record ActivationResult(ActivationStatus status, String decorationId) {}

    public record InvalidationResult(boolean changed, Set<String> invalidatedDecorationIds) {}

    public static InvalidationResult invalidateAffectedDecorations(
            ServerLevel level, TeamData team, Set<BlockPos> changedPositions) {
        if (level == null || team == null || changedPositions == null || changedPositions.isEmpty()) {
            return new InvalidationResult(false, Set.of());
        }

        InnData innData = team.getInnData();
        Map<String, List<InnData.DecorationPlacement>> oldPlacements =
                innData.copyActiveDecorations();
        Map<String, List<InnData.DecorationPlacement>> nextPlacements = new HashMap<>();
        Set<String> invalidatedDecorationIds = new LinkedHashSet<>();

        for (InnDecorationDefinition definition : InnDecorationRegistry.getAll()) {
            Optional<InnDecorationTemplate> templateOptional =
                    InnDecorationTemplateLoader.getTemplate(definition);
            if (templateOptional.isEmpty()) {
                continue;
            }
            List<InnData.DecorationPlacement> kept = new ArrayList<>();
            InnDecorationTemplate template = templateOptional.get();
            List<InnData.DecorationPlacement> existing =
                    oldPlacements.getOrDefault(definition.id(), List.of());
            if (existing.isEmpty()) {
                continue;
            }
            for (InnData.DecorationPlacement placement : existing) {
                if (placement == null) {
                    continue;
                }
                Rotation rotation = placement.rotation();
                if (!definition.allowedRotations().contains(rotation)) {
                    continue;
                }
                if (isAffectedByChanges(changedPositions, template, placement)) {
                    if (matchesAt(level, template, placement.origin(), rotation)) {
                        kept.add(placement);
                    }
                } else {
                    kept.add(placement);
                }
            }
            kept.sort(Comparator.comparing(p -> p.origin().asLong()));
            if (!kept.isEmpty()) {
                nextPlacements.put(definition.id(), kept);
            }
            if (kept.size() < existing.size()) {
                invalidatedDecorationIds.add(definition.id());
            }
        }

        if (oldPlacements.equals(nextPlacements)) {
            return new InvalidationResult(false, Set.of());
        }

        innData.replaceActiveDecorations(nextPlacements);
        return new InvalidationResult(true, Set.copyOf(invalidatedDecorationIds));
    }

    public static ActivationResult tryActivateDecorationAt(
            ServerLevel level, TeamData team, BlockPos clickedPos) {
        if (level == null || team == null || clickedPos == null) {
            return new ActivationResult(ActivationStatus.NOT_FOUND, null);
        }

        InnData innData = team.getInnData();
        Map<String, List<InnData.DecorationPlacement>> activeDecorations = innData.copyActiveDecorations();
        Set<PlacementKey> activeKeys = new HashSet<>();
        for (Map.Entry<String, List<InnData.DecorationPlacement>> entry : activeDecorations.entrySet()) {
            for (InnData.DecorationPlacement placement : entry.getValue()) {
                activeKeys.add(new PlacementKey(entry.getKey(), placement.origin(), placement.rotation()));
            }
        }

        for (InnDecorationDefinition definition : InnDecorationRegistry.getAll()) {
            Optional<InnDecorationTemplate> templateOptional =
                    InnDecorationTemplateLoader.getTemplate(definition);
            if (templateOptional.isEmpty()) {
                continue;
            }
            InnDecorationTemplate template = templateOptional.get();
            for (Rotation rotation : definition.allowedRotations()) {
                for (InnDecorationTemplate.TemplateBlock templateBlock : template.blocks()) {
                    BlockPos rotatedOffset = rotate(templateBlock.relativePos(), rotation);
                    BlockPos origin = clickedPos.subtract(rotatedOffset);
                    if (!matchesAt(level, template, origin, rotation)) {
                        continue;
                    }
                    PlacementKey key = new PlacementKey(definition.id(), origin.immutable(), rotation);
                    if (activeKeys.contains(key)) {
                        return new ActivationResult(ActivationStatus.ALREADY_ACTIVE, definition.id());
                    }
                    List<InnData.DecorationPlacement> placements =
                            new ArrayList<>(activeDecorations.getOrDefault(definition.id(), List.of()));
                    if (placements.size() >= definition.maxInstances()) {
                        return new ActivationResult(ActivationStatus.LIMIT_REACHED, definition.id());
                    }
                    placements.add(new InnData.DecorationPlacement(origin.immutable(), rotation));
                    placements.sort(Comparator.comparing(p -> p.origin().asLong()));
                    activeDecorations.put(definition.id(), List.copyOf(placements));
                    innData.replaceActiveDecorations(activeDecorations);
                    return new ActivationResult(ActivationStatus.ACTIVATED, definition.id());
                }
            }
        }
        return new ActivationResult(ActivationStatus.NOT_FOUND, null);
    }

    private static boolean isAffectedByChanges(
            Set<BlockPos> changedPositions,
            InnDecorationTemplate template,
            InnData.DecorationPlacement placement) {
        for (InnDecorationTemplate.TemplateBlock templateBlock : template.blocks()) {
            BlockPos worldPos =
                    placement.origin().offset(rotate(templateBlock.relativePos(), placement.rotation()));
            if (changedPositions.contains(worldPos)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesAt(
            ServerLevel level, InnDecorationTemplate template, BlockPos origin, Rotation rotation) {
        for (InnDecorationTemplate.TemplateBlock templateBlock : template.blocks()) {
            BlockPos worldPos = origin.offset(rotate(templateBlock.relativePos(), rotation));
            if (!TeamData.isInGlobalMaxInnZone(worldPos)) {
                return false;
            }
            BlockState actual = level.getBlockState(worldPos);
            if (!actual.equals(templateBlock.state())) {
                return false;
            }
        }
        return true;
    }

    private static BlockPos rotate(BlockPos pos, Rotation rotation) {
        return switch (rotation) {
            case NONE -> pos;
            case CLOCKWISE_90 -> new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
            case CLOCKWISE_180 -> new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
            case COUNTERCLOCKWISE_90 -> new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
        };
    }

    private record PlacementKey(String decorationId, BlockPos origin, Rotation rotation) {}
}
