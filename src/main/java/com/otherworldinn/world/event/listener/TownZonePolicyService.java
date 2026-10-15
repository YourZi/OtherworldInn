package com.otherworldinn.world.event.listener;

import com.otherworldinn.OtherworldInn;
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
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 城镇维度保护规则的统一语义层：区域判定、行为授权与受控 bypass 都在这里统一处理。
 */
public final class TownZonePolicyService {

    private static final String GREENHOUSE_FACILITY_ID = "greenhouse";
    private static final ThreadLocal<Deque<ActiveBypass>> BYPASS_STACK =
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
        /** 玩家/女仆为住客房间更换床单：只改床方块自身的 {@code messy} 属性，不改结构。 */
        ROOM_CLEAN_BED_SHEET,
        CREATIVE_PLAYER_ACTION,
        FESTIVAL_DECORATION
    }

    public record MovePair(@Nullable BlockPos fromPos, @Nullable BlockPos toPos) {}

    /**
     * 需要抑制方块掉落的旁路原因：结构贴图与整洁类清理。
     *
     * <p>刻意<b>不含</b> {@link ProtectionBypassReason#CREATIVE_PLAYER_ACTION}：创造模式挖掘本来就不产生掉落，
     * 把它排除后，即使该作用域意外泄漏，也不会牵连全局的普通挖掘掉落。
     */
    private static final Set<ProtectionBypassReason> DROP_SUPPRESSING_REASONS =
            Set.of(
                    ProtectionBypassReason.FACILITY_UPGRADE,
                    ProtectionBypassReason.TRADER_SHIP_REBUILD,
                    ProtectionBypassReason.FESTIVAL_DECORATION,
                    ProtectionBypassReason.CHECKOUT_CLUTTER_SPAWN,
                    ProtectionBypassReason.ROOM_CLEAN_CLUTTER);

    /**
     * 旁路栈内元素：上下文 + 打开时刻。
     *
     * <p>记录打开时刻是为了让 {@link #expireStaleBypasses(long)} 能自动清理"忘记关闭"的作用域： 泄漏的旁路会让服务端线程永久处于旁路态，
     * 进而持续取消所有方块掉落（联机时表现为破坏方块没有掉落物，且只能重启恢复）。
     */
    private record ActiveBypass(ProtectionBypassContext context, long openedAtNanos) {}

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
            Deque<ActiveBypass> stack = BYPASS_STACK.get();
            if (!stack.isEmpty() && stack.peek().context() == context) {
                stack.pop();
            } else {
                // 乱序/越界关闭：正常流程不应发生，记录下来便于定位（否则会留下泄漏的旁路态）
                OtherworldInn.LOGGER.warn(
                        "Protection bypass scope closed out of order (reason={}); removing its context",
                        context.reason());
                stack.removeIf(active -> active.context() == context);
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

    public static boolean canPlayerHarvestCropAt(
            Level level, BlockPos pos, BlockState state, @Nullable Player player) {
        return !isProtectedCropState(state) || canPlayerModifyAt(level, pos, player);
    }

    public static boolean isProtectedCropState(BlockState state) {
        if (state == null) {
            return false;
        }
        return state.getBlock() instanceof CropBlock
                || state.getBlock() instanceof SweetBerryBushBlock
                || state.getBlock() instanceof CocoaBlock
                || state.getBlock() instanceof NetherWartBlock
                || state.is(BlockTags.CROPS);
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
        if (isProtectedCropChange(oldState, newState)) {
            return false;
        }
        if (Objects.equals(oldState, newState)) {
            return true;
        }
        // 同方块属性更新（生长、液位、门、红石）一律放行
        if (oldState.getBlock() == newState.getBlock()) {
            return true;
        }
        // 房间整洁类方块（杂物垃圾等）被清除成空气：放行。
        // 否则会出现"事件层白名单放行玩家清理、写入层却静默拒绝"的矛盾：方块清不掉且没有任何提示，
        // 屋子还有住客时就永远无法打扫。语义与 TownProtectionHandler#isPlayerCleanableClutter 一致。
        if (newState.isAir() && oldState.is(OtherworldInn.PROTECTED_ZONE_PLAYER_CLEANABLE)) {
            return true;
        }
        // 季节性雪冰：积雪、融化、铲除、覆盖均视为临时变化
        if (isSeasonalSnowIce(oldState) || isSeasonalSnowIce(newState)) {
            return true;
        }
        // 白名单内的自然表面转换（草蔓延/退化、菌丝、耕地退化），tag 可由数据包扩展
        if (oldState.is(OtherworldInn.PROTECTED_ZONE_NATURAL_CONVERSIONS)
                && newState.is(OtherworldInn.PROTECTED_ZONE_NATURAL_CONVERSIONS)) {
            return true;
        }
        if (isAllowedNaturalFluidFlowWrite(level, pos, oldState, newState)) {
            return true;
        }
        if (isForbiddenFluidReplacement(oldState, newState)) {
            return false;
        }
        boolean oldAir = oldState.isAir();
        boolean newAir = newState.isAir();
        if (oldAir != newAir) {
            return false;
        }
        // 其余为不同固体间替换（苔藓蔓延、骨粉催熟等），保护区内一律拒绝
        return false;
    }

    private static boolean isAllowedNaturalFluidFlowWrite(
            ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        FluidState oldFluid = oldState.getFluidState();
        FluidState newFluid = newState.getFluidState();
        if (oldFluid.isEmpty() && newFluid.isEmpty()) {
            return false;
        }

        if (oldState.isAir() && !newFluid.isEmpty()) {
            return !newFluid.isSource() && hasAdjacentFluid(level, pos, newFluid);
        }

        if (!oldFluid.isEmpty() && newState.isAir()) {
            return !oldFluid.isSource();
        }

        return false;
    }

    private static boolean isForbiddenFluidReplacement(BlockState oldState, BlockState newState) {
        FluidState oldFluid = oldState.getFluidState();
        FluidState newFluid = newState.getFluidState();
        return oldFluid.isEmpty() && !oldState.isAir() && !newFluid.isEmpty();
    }

    private static boolean hasAdjacentFluid(ServerLevel level, BlockPos pos, FluidState fluidState) {
        for (Direction direction : Direction.values()) {
            FluidState neighborFluid = level.getFluidState(pos.relative(direction));
            if (!neighborFluid.isEmpty() && neighborFluid.getType().isSame(fluidState.getType())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isProtectedCropChange(BlockState oldState, BlockState newState) {
        return isProtectedCropState(oldState) || isProtectedCropState(newState);
    }

    /** 季节性临时方块：雪层、雪块、冰。 */
    public static boolean isSeasonalSnowIce(@Nullable BlockState state) {
        return state != null
                && (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.ICE));
    }

    /** 保护旁路是否激活（仅在结构贴图等受控世界写入的 try-with-resources 窗口内为真） */
    public static boolean isProtectionBypassActive() {
        return !BYPASS_STACK.get().isEmpty();
    }

    /**
     * 当前线程是否处于"需要抑制方块掉落"的旁路窗口内。
     *
     * <p>与 {@link #isProtectionBypassActive()} 的区别：只认结构贴图/整洁类清理这些确实会产生掉落的场景，
     * 创造模式玩家操作不算在内。这样即便某个旁路作用域意外泄漏，也不会导致全局挖掘掉落消失。
     */
    public static boolean isDropSuppressingBypassActive() {
        for (ActiveBypass active : BYPASS_STACK.get()) {
            if (DROP_SUPPRESSING_REASONS.contains(active.context().reason())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 清理打开过久（疑似忘记关闭/异常泄漏）的旁路作用域。
     *
     * <p>由 {@link BypassWatchdog} 每服务端 tick 调用一次。正常的结构贴图/清理窗口都只有一瞬间，
     * 因此超过阈值即可判定为泄漏；主动清理可避免"全局方块不掉落"这类需要重启才能恢复的故障。
     *
     * @return 被清理掉的泄漏作用域数量
     */
    public static int expireStaleBypasses(long maxAgeNanos) {
        Deque<ActiveBypass> stack = BYPASS_STACK.get();
        if (stack.isEmpty()) {
            return 0;
        }
        long now = System.nanoTime();
        int expired = 0;
        Iterator<ActiveBypass> iterator = stack.iterator();
        while (iterator.hasNext()) {
            ActiveBypass active = iterator.next();
            if (now - active.openedAtNanos() < maxAgeNanos) {
                continue;
            }
            iterator.remove();
            expired++;
            OtherworldInn.LOGGER.warn(
                    "Expired a leaked protection bypass scope (reason={}, open for {}s);"
                            + " this would otherwise suppress block drops server-wide",
                    active.context().reason(),
                    (now - active.openedAtNanos()) / 1_000_000_000L);
        }
        if (stack.isEmpty()) {
            BYPASS_STACK.remove();
        }
        return expired;
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
        BYPASS_STACK.get().push(new ActiveBypass(context, System.nanoTime()));
        return new ProtectionBypassScope(context);
    }

    private static boolean isBypassAllowed(BlockPos pos) {
        Deque<ActiveBypass> stack = BYPASS_STACK.get();
        if (stack.isEmpty()) {
            return false;
        }
        for (ActiveBypass active : stack) {
            if (active.context().allows().test(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 旁路泄漏看门狗：每服务端 tick 清理一次打开过久的旁路作用域。
     *
     * <p>这是一道兜底保险——即使将来又出现"忘记 close / 跨线程 close"的写法，也不会演变成
     * "所有维度破坏方块不掉落、只能重启"的线上事故；同时会在日志里留下明确告警。
     */
    @EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.GAME)
    public static final class BypassWatchdog {

        /** 正常旁路窗口只有一瞬间；超过 60 秒即判定为泄漏。 */
        private static final long MAX_BYPASS_AGE_NANOS =
                java.util.concurrent.TimeUnit.SECONDS.toNanos(60);

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            expireStaleBypasses(MAX_BYPASS_AGE_NANOS);
        }
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
