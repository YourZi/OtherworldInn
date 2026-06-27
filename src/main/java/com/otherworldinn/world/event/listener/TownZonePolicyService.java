package com.otherworldinn.world.event.listener;

import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.RoomData;
import com.otherworldinn.world.inn.facility.FacilityRegistry;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 城镇维度保护规则的统一语义层。
 *
 * <p>职责分为三类：
 * <ul>
 *   <li>位置语义：自由修改区 / 已入住客房保护壳层 / 其它保护区</li>
 *   <li>行为授权：玩家修改、自然生长、活塞、Create 兼容接入</li>
 *   <li>底层绕过：项目内受控结构变更与创造模式直改的显式上下文</li>
 * </ul>
 */
public final class TownZonePolicyService {

    private static final String GREENHOUSE_FACILITY_ID = "greenhouse";
    private static final ThreadLocal<Deque<ProtectionBypassContext>> BYPASS_STACK =
            ThreadLocal.withInitial(ArrayDeque::new);

    private TownZonePolicyService() {}

    public enum ZoneKind {
        NON_TOWN,
        FREE_EDIT_ZONE,
        OCCUPIED_ROOM_PROTECTED_ZONE,
        TOWN_PROTECTED_ZONE
    }

    public enum ContraptionPlacementPolicy {
        ALLOW_PLACE,
        DENY_AND_DROP
    }

    public enum ProtectionBypassReason {
        FACILITY_UPGRADE,
        TRADER_SHIP_REBUILD,
        CHECKOUT_CLUTTER_SPAWN,
        ROOM_CLEAN_CLUTTER,
        CREATIVE_PLAYER_ACTION
    }

    public record MovePair(@Nullable BlockPos fromPos, @Nullable BlockPos toPos) {}

    public record ProtectionBypassContext(
            ProtectionBypassReason reason,
            Predicate<BlockPos> allows,
            @Nullable Collection<BlockPos> debugPositions) {}

    public static final class ProtectionBypassScope implements AutoCloseable {
        private final ProtectionBypassContext context;
        private boolean closed;

        private ProtectionBypassScope(ProtectionBypassContext context) {
            this.context = context;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            Deque<ProtectionBypassContext> stack = BYPASS_STACK.get();
            if (!stack.isEmpty() && stack.peek() == context) {
                stack.pop();
            } else {
                stack.remove(context);
            }
            if (stack.isEmpty()) {
                BYPASS_STACK.remove();
            }
        }
    }

    public static boolean isTownDimension(@Nullable Level level) {
        return level != null && level.dimension() == TownDimensions.TOWN_LEVEL;
    }

    public static boolean isInsideInnMaxBounds(@Nullable BlockPos pos) {
        return pos != null && TeamData.isInGlobalMaxInnZone(pos);
    }

    public static boolean isInsideGreenhouseEditableRange(ServerLevel level, BlockPos pos) {
        return getGreenhouseLevelAtPos(level, pos) > 0;
    }

    public static boolean isFreeEditZone(ServerLevel level, BlockPos pos) {
        if (!isTownDimension(level)) {
            return true;
        }
        if (isOccupiedRoomShell(level, pos)) {
            return false;
        }
        return isInsideInnMaxBounds(pos) || isInsideGreenhouseEditableRange(level, pos);
    }

    public static boolean isFreeEditZone(Level level, BlockPos pos) {
        if (!isTownDimension(level)) {
            return true;
        }
        if (level instanceof ServerLevel serverLevel) {
            return isFreeEditZone(serverLevel, pos);
        }
        return isFreeEditZoneClient(level, pos);
    }

    public static ZoneKind getZoneKind(ServerLevel level, BlockPos pos) {
        if (!isTownDimension(level)) {
            return ZoneKind.NON_TOWN;
        }
        if (isOccupiedRoomShell(level, pos)) {
            return ZoneKind.OCCUPIED_ROOM_PROTECTED_ZONE;
        }
        if (isInsideInnMaxBounds(pos) || isInsideGreenhouseEditableRange(level, pos)) {
            return ZoneKind.FREE_EDIT_ZONE;
        }
        return ZoneKind.TOWN_PROTECTED_ZONE;
    }

    public static boolean isOccupiedRoomShell(ServerLevel level, BlockPos pos) {
        RoomData room = findAffectedRoom(level, pos);
        return room != null && !room.getCurrentGuests().isEmpty();
    }

    public static boolean isOccupiedRoomShell(Level level, BlockPos pos) {
        if (!isTownDimension(level)) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            return isOccupiedRoomShell(serverLevel, pos);
        }
        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        if (team == null) {
            return false;
        }
        RoomData room = team.getInnData().getRoomAffectedBy(pos);
        return room != null && !room.getCurrentGuests().isEmpty();
    }

    @Nullable
    public static RoomData findAffectedRoom(ServerLevel level, BlockPos pos) {
        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());
        if (team == null) {
            return null;
        }
        return team.getInnData().getRoomAffectedBy(pos);
    }

    public static boolean canPlayerModifyAt(Level level, BlockPos pos, @Nullable Player player) {
        if (!isTownDimension(level)) {
            return true;
        }
        if (player != null && player.isCreative()) {
            return true;
        }
        return isFreeEditZone(level, pos);
    }

    public static boolean canCreateModifyBlockAt(Level level, BlockPos pos) {
        return !isTownDimension(level) || isFreeEditZone(level, pos);
    }

    public static boolean canMaidOperateAt(@Nullable Entity maidEntity, BlockPos pos, Level level) {
        if (maidEntity == null || pos == null || level == null) {
            return false;
        }
        if (!isTownDimension(level)) {
            return true;
        }
        if (isFreeEditZone(level, pos)) {
            return true;
        }
        Player actor = resolveActorPlayer(maidEntity);
        return actor != null && actor.isCreative();
    }

    public static boolean canNaturalGrowth(
            ServerLevel level, BlockPos sourcePos, Collection<BlockPos> affectedPositions) {
        if (!isTownDimension(level)) {
            return true;
        }
        if (!isFreeEditZone(level, sourcePos)) {
            return false;
        }
        return affectedPositions.stream().allMatch(pos -> isFreeEditZone(level, pos));
    }

    public static boolean canChangeAll(Level level, Collection<BlockPos> positions, @Nullable Player player) {
        if (!isTownDimension(level)) {
            return true;
        }
        if (player != null && player.isCreative()) {
            return true;
        }
        return positions.stream().allMatch(pos -> isFreeEditZone(level, pos));
    }

    public static boolean canPistonMove(
            ServerLevel level, Collection<MovePair> moves, @Nullable Player actor) {
        if (!isTownDimension(level)) {
            return true;
        }
        if (actor != null && actor.isCreative()) {
            return true;
        }
        for (MovePair move : moves) {
            if (move.fromPos() != null && !isFreeEditZone(level, move.fromPos())) {
                return false;
            }
            if (move.toPos() != null && !isFreeEditZone(level, move.toPos())) {
                return false;
            }
        }
        return true;
    }

    public static ContraptionPlacementPolicy resolveContraptionPlacementPolicy(
            ServerLevel level, BlockPos pos, BlockState state) {
        if (!isTownDimension(level)) {
            return ContraptionPlacementPolicy.ALLOW_PLACE;
        }
        return canStructuralWriteAt(level, pos, level.getBlockState(pos), state)
                ? ContraptionPlacementPolicy.ALLOW_PLACE
                : ContraptionPlacementPolicy.DENY_AND_DROP;
    }

    public static boolean canStructuralWriteAt(
            ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        if (!isTownDimension(level)) {
            return true;
        }
        if (isBypassAllowed(pos)) {
            return true;
        }
        if (isFreeEditZone(level, pos)) {
            return true;
        }
        if (isProtectedWheatChange(oldState, newState)) {
            return false;
        }
        if (Objects.equals(oldState, newState)) {
            return true;
        }
        boolean oldAir = oldState.isAir();
        boolean newAir = newState.isAir();
        if (oldAir != newAir) {
            return false;
        }
        return !wouldCauseProtectedDestruction(level, pos, oldState, newState);
    }

    public static boolean wouldCauseProtectedDestruction(
            ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        if (newState.isAir()) {
            return true;
        }
        if (!newState.canSurvive(level, pos)) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (!neighborState.isAir()) {
                BlockState updatedNeighbor = neighborState.updateShape(
                        direction.getOpposite(), newState, level, neighborPos, pos);
                if (updatedNeighbor.isAir()) {
                    return true;
                }
            }
            BlockState updatedSelf =
                    newState.updateShape(direction, neighborState, level, pos, neighborPos);
            if (updatedSelf.isAir()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isProtectedWheatChange(BlockState oldState, BlockState newState) {
        return oldState.is(Blocks.WHEAT) || newState.is(Blocks.WHEAT);
    }

    public static ProtectionBypassScope beginProtectionBypass(
            ProtectionBypassReason reason, Collection<BlockPos> allowedPositions) {
        Set<BlockPos> immutablePositions = allowedPositions.stream()
                .filter(Objects::nonNull)
                .map(BlockPos::immutable)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return pushBypass(new ProtectionBypassContext(
                reason,
                immutablePositions::contains,
                immutablePositions));
    }

    public static ProtectionBypassScope beginCreativePlayerBypass(Level level, @Nullable Player player) {
        if (!(level instanceof ServerLevel) || player == null || !player.isCreative() || !isTownDimension(level)) {
            return null;
        }
        return pushBypass(new ProtectionBypassContext(
                ProtectionBypassReason.CREATIVE_PLAYER_ACTION,
                pos -> true,
                null));
    }

    @Nullable
    public static Component getDenyMessage(Level level, BlockPos pos) {
        if (!isTownDimension(level)) {
            return null;
        }
        if (isOccupiedRoomShell(level, pos)) {
            return Component.translatable("message.otherworldinn.protection.deny_guest_in_room")
                    .copy()
                    .withStyle(style -> style.withColor(ModColors.RED));
        }
        return Component.translatable("message.otherworldinn.protection.deny")
                .copy()
                .withStyle(style -> style.withColor(ModColors.RED));
    }

    private static ProtectionBypassScope pushBypass(ProtectionBypassContext context) {
        BYPASS_STACK.get().push(context);
        return new ProtectionBypassScope(context);
    }

    private static boolean isBypassAllowed(BlockPos pos) {
        Deque<ProtectionBypassContext> stack = BYPASS_STACK.get();
        if (stack.isEmpty()) {
            return false;
        }
        for (ProtectionBypassContext context : stack) {
            if (context.allows().test(pos)) {
                return true;
            }
        }
        return false;
    }

    private static int getGreenhouseLevelAtPos(ServerLevel level, BlockPos pos) {
        FacilityRegistry.FacilityDefinition greenhouse =
                FacilityRegistry.get(GREENHOUSE_FACILITY_ID);
        if (greenhouse == null) {
            return 0;
        }

        TeamManager manager = TeamManager.getInstance();
        TeamData teamAtPos = manager.getTeamAt(pos, level.getServer());
        int levelAtPos = getGreenhouseLevelForTeam(teamAtPos, greenhouse, pos);
        if (levelAtPos > 0) {
            return levelAtPos;
        }

        TeamData nearestTeam = manager.getNearestInn(pos, level.getServer());
        if (nearestTeam == null || nearestTeam == teamAtPos) {
            return 0;
        }
        return getGreenhouseLevelForTeam(nearestTeam, greenhouse, pos);
    }

    private static int getGreenhouseLevelForTeam(
            @Nullable TeamData team,
            FacilityRegistry.FacilityDefinition greenhouse,
            BlockPos pos) {
        if (team == null) {
            return 0;
        }
        InnData innData = team.getInnData();
        int level = Math.max(0, innData.getFacilityLevel(GREENHOUSE_FACILITY_ID));
        if (level <= 0) {
            return 0;
        }
        if (greenhouse.facilityRange().contains(pos)) {
            return level;
        }
        for (FacilityRegistry.FacilityRange range : greenhouse.getExtraBuildAllowRanges(level)) {
            if (range.contains(pos)) {
                return level;
            }
        }
        return 0;
    }

    private static boolean isFreeEditZoneClient(Level level, BlockPos pos) {
        if (!isTownDimension(level)) {
            return true;
        }
        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        if (team == null) {
            return false;
        }
        if (isOccupiedRoomShell(level, pos)) {
            return false;
        }
        if (isInsideInnMaxBounds(pos)) {
            return true;
        }
        int greenhouseLevel = Math.max(0, team.getInnData().getFacilityLevel(GREENHOUSE_FACILITY_ID));
        if (greenhouseLevel <= 0) {
            return false;
        }
        FacilityRegistry.FacilityDefinition greenhouse = FacilityRegistry.get(GREENHOUSE_FACILITY_ID);
        if (greenhouse == null) {
            return false;
        }
        if (greenhouse.facilityRange().contains(pos)) {
            return true;
        }
        for (FacilityRegistry.FacilityRange range : greenhouse.getExtraBuildAllowRanges(greenhouseLevel)) {
            if (range.contains(pos)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Player resolveActorPlayer(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        if (!(entity instanceof OwnableEntity ownable)
                || !(entity.level() instanceof ServerLevel level)) {
            return null;
        }
        if (ownable.getOwnerUUID() == null) {
            return null;
        }
        return level.getServer().getPlayerList().getPlayer(ownable.getOwnerUUID());
    }
}
