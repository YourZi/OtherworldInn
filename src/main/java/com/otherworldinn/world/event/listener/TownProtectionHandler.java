package com.otherworldinn.world.event.listener;

import com.github.ysbbbbbb.kaleidoscopecookery.block.food.FoodBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BottleBlock;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModBlockProperties;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.RoomData;
import com.otherworldinn.world.inn.facility.FacilityRegistry;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.PistonEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

/**
 * 城镇维度方块保护系统。
 *
 * <h3>核心概念</h3>
 * <p>城镇维度分为两类区域：
 * <ul>
 *   <li><b>免保区</b> — 旅社范围（无人入住房间）或温室范围。除爆炸外所有操作放行。</li>
 *   <li><b>限制区</b> — 城镇维度中免保区以外的全部区域。方块破坏/放置被拦截，某些交互受限。</li>
 * </ul>
 *
 * <h3>限制区拦截一览</h3>
 * <table>
 *   <tr><th>操作</th><th>规则</th></tr>
 *   <tr><td>方块破坏</td><td>仅创造模式放行</td></tr>
 *   <tr><td>方块放置（玩家）</td><td>仅创造模式放行</td></tr>
 *   <tr><td>方块放置（掉落方块）</td><td>转为掉落物</td></tr>
 *   <tr><td>方块放置（自然生长）</td><td>按 0.3x 概率放行</td></tr>
 *   <tr><td>右键交互（容器等）</td><td>放行</td></tr>
 *   <tr><td>右键交互（手持方块/锄头）</td><td>拒绝</td></tr>
 *   <tr><td>农作物生长</td><td>0.3x 倍速，树苗/树木禁止</td></tr>
 *   <tr><td>耕地践踏</td><td>阻止</td></tr>
 *   <tr><td>邻方块更新破坏</td><td>阻止</td></tr>
 *   <tr><td>活塞</td><td>跨队阻止，同队放行</td></tr>
 *   <tr><td>爆炸</td><td>零破坏（清空方块列表）</td></tr>
 * </table>
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public class TownProtectionHandler {

    // ── 常量 ──────────────────────────────────────────────

    private static final double TOWN_CROP_GROWTH_MULTIPLIER = 0.3D;
    private static final double GREENHOUSE_CROP_GROWTH_MULTIPLIER = 1.5D;
    private static final String GREENHOUSE_FACILITY_ID = "greenhouse";
    private static final ResourceLocation CREATE_DEPOT_ID =
            ResourceLocation.fromNamespaceAndPath("create", "depot");

    // ── 顶层判定 ──────────────────────────────────────────

    private static boolean isTownDimension(Level level) {
        return level.dimension() == TownDimensions.TOWN_LEVEL;
    }

    /**
     * 免保区判定（服务端）。旅社最大范围内无人入住的房间区域，或温室区域。
     */
    private static boolean isFreeZone(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null || !isTownDimension(level)) return false;

        if (isInsideGreenhouseZone(level, pos)) return true;

        // 使用全局最大旅社范围（而非队伍已购买的地皮范围）
        if (!TeamData.isInGlobalMaxInnZone(pos)) return false;

        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());
        if (team == null) return false;

        RoomData room = team.getInnData().getRoomAffectedBy(pos);
        return room == null || room.getCurrentGuests().isEmpty();
    }

    /**
     * 客户端免保区判定。使用全局最大旅社范围。
     */
    private static boolean isFreeZoneClient(Level level, BlockPos pos) {
        if (level == null || pos == null || !isTownDimension(level)) return false;

        // 使用全局最大旅社范围（而非队伍已购买的地皮范围）
        if (!TeamData.isInGlobalMaxInnZone(pos)) return false;

        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        if (team == null) return false;

        // 温室判定
        int ghLevel = Math.max(0, team.getInnData().getFacilityLevel(GREENHOUSE_FACILITY_ID));
        if (ghLevel > 0) {
            FacilityRegistry.FacilityDefinition greenhouse = FacilityRegistry.get(GREENHOUSE_FACILITY_ID);
            if (greenhouse != null) {
                if (greenhouse.facilityRange().contains(pos)) return true;
                for (FacilityRegistry.FacilityRange range : greenhouse.getExtraBuildAllowRanges(ghLevel)) {
                    if (range.contains(pos)) return true;
                }
            }
        }

        RoomData room = team.getInnData().getRoomAffectedBy(pos);
        return room == null || room.getCurrentGuests().isEmpty();
    }

    /** 免保区判定，自动区分服务端/客户端。 */
    private static boolean isFreeZoneAny(Level level, BlockPos pos) {
        if (level instanceof ServerLevel sl) return isFreeZone(sl, pos);
        return isFreeZoneClient(level, pos);
    }

    /** 根据坐标是否在有人入住的房间内，返回对应的拒绝提示消息。 */
    private static Component getDenyMessage(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            TeamData team = TeamManager.getInstance().getTeamAt(pos, serverLevel.getServer());
            if (team != null && team.isInInnZone(pos)) {
                RoomData room = team.getInnData().getRoomAffectedBy(pos);
                if (room != null && !room.getCurrentGuests().isEmpty()) {
                    return Component.translatable("message.otherworldinn.protection.deny_guest_in_room");
                }
            }
        } else {
            TeamData team = TeamManager.getInstance().getClientPlayerTeam();
            if (team != null && team.isInInnZone(pos)) {
                RoomData room = team.getInnData().getRoomAffectedBy(pos);
                if (room != null && !room.getCurrentGuests().isEmpty()) {
                    return Component.translatable("message.otherworldinn.protection.deny_guest_in_room");
                }
            }
        }
        return Component.translatable("message.otherworldinn.protection.deny");
    }

    public static boolean isInnRestrictionLiftedAt(ServerLevel level, BlockPos pos) {
        return isInnRestrictionLiftedAt((Level) level, pos);
    }

    public static boolean isInnRestrictionLiftedAt(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        if (!isTownDimension(level)) return true;
        return isFreeZoneAny(level, pos);
    }

    /** 女仆操作权限：免保区或创造模式放行，否则拒绝。 */
    public static boolean canMaidOperateAt(Entity maidEntity, BlockPos pos, Level level) {
        if (maidEntity == null || pos == null || level == null) return false;
        if (!isTownDimension(level)) return true;
        if (level instanceof ServerLevel sl && isFreeZone(sl, pos)) return true;

        Player actor = resolveActorPlayer(maidEntity);
        return actor != null && actor.isCreative();
    }

    // ── 温室判定 ──────────────────────────────────────────

    private static boolean isInsideGreenhouseZone(Level level, BlockPos pos) {
        return getGreenhouseLevelAtPos(level, pos) > 0;
    }

    private static int getGreenhouseLevelAtPos(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || pos == null || !isTownDimension(level))
            return 0;
        FacilityRegistry.FacilityDefinition greenhouse =
                FacilityRegistry.get(GREENHOUSE_FACILITY_ID);
        if (greenhouse == null) return 0;

        TeamManager manager = TeamManager.getInstance();
        TeamData teamAtPos = manager.getTeamAt(pos, serverLevel.getServer());
        int levelAtPos = getGreenhouseLevelForTeam(teamAtPos, greenhouse, pos);
        if (levelAtPos > 0) return levelAtPos;

        TeamData nearestTeam = manager.getNearestInn(pos, serverLevel.getServer());
        if (nearestTeam == null || nearestTeam == teamAtPos) return 0;
        return getGreenhouseLevelForTeam(nearestTeam, greenhouse, pos);
    }

    private static int getGreenhouseLevelForTeam(
            TeamData team, FacilityRegistry.FacilityDefinition greenhouse, BlockPos pos) {
        if (team == null || greenhouse == null || pos == null) return 0;
        int level = Math.max(0, team.getInnData().getFacilityLevel(GREENHOUSE_FACILITY_ID));
        if (level <= 0) return 0;
        if (greenhouse.facilityRange().contains(pos)) return level;
        for (FacilityRegistry.FacilityRange range : greenhouse.getExtraBuildAllowRanges(level)) {
            if (range.contains(pos)) return level;
        }
        return 0;
    }

    // ── 农作物 ────────────────────────────────────────────

    private static boolean isFarmingBlock(BlockState state) {
        if (state == null) return false;
        return state.getBlock() instanceof CropBlock
                || state.getBlock() instanceof StemBlock
                || state.getBlock() instanceof AttachedStemBlock
                || state.getBlock() instanceof NetherWartBlock
                || state.getBlock() instanceof CocoaBlock
                || state.getBlock() instanceof SweetBerryBushBlock
                || state.getBlock() instanceof FarmBlock
                || state.is(net.minecraft.tags.BlockTags.CROPS)
                || state.is(net.minecraft.tags.BlockTags.MAINTAINS_FARMLAND);
    }

    private static boolean shouldRestrictNaturalGrowthBlock(BlockState state) {
        if (state == null) return false;
        return isFarmingBlock(state)
                || state.is(net.minecraft.world.level.block.Blocks.PUMPKIN)
                || state.is(net.minecraft.world.level.block.Blocks.MELON)
                || state.is(net.minecraft.world.level.block.Blocks.SUGAR_CANE)
                || state.is(net.minecraft.world.level.block.Blocks.BAMBOO)
                || state.is(net.minecraft.world.level.block.Blocks.CACTUS)
                || state.is(net.minecraft.world.level.block.Blocks.COCOA)
                || state.is(net.minecraft.world.level.block.Blocks.SWEET_BERRY_BUSH);
    }

    private static double getCropGrowthMultiplier(Level level, BlockPos pos, BlockState state) {
        if (!isTownDimension(level)) return 1.0D;
        if (state != null && (state.getBlock() instanceof SaplingBlock
                || state.is(net.minecraft.tags.BlockTags.SAPLINGS)))
            return 0.0D;
        int ghLevel = getGreenhouseLevelAtPos(level, pos);
        if (ghLevel > 0)
            return GREENHOUSE_CROP_GROWTH_MULTIPLIER + (1.0D * (ghLevel - 1));
        return TOWN_CROP_GROWTH_MULTIPLIER;
    }

    // ── 辅助判定 ──────────────────────────────────────────

    private static boolean isDepotDisplayBlock(BlockState state) {
        return state != null && CREATE_DEPOT_ID.equals(
                BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private static boolean isInnFreeInteractBlock(BlockState state) {
        if (state == null || state.isAir()) return false;
        return state.is(OtherworldInn.INN_FREE_INTERACT)
                || state.getBlock() instanceof FoodBlock
                || state.getBlock() instanceof BottleBlock;
    }

    private static boolean isBedSheetCleaningUpdate(Player player, BlockState state) {
        if (!(state.getBlock() instanceof net.minecraft.world.level.block.BedBlock)
                || !state.hasProperty(ModBlockProperties.MESSY))
            return false;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.is(ModItems.BED_SHEET.get())
                || off.is(ModItems.BED_SHEET.get())
                || main.is(ModItems.MESSY_BED_SHEET.get())
                || off.is(ModItems.MESSY_BED_SHEET.get());
    }

    private static boolean isNormalTownDecorArea(ServerLevel level, BlockPos pos) {
        return isTownDimension(level) && !isFreeZone(level, pos);
    }

    private static boolean isTouhouLittleMaid(Entity entity) {
        if (entity == null || entity.getType() == null) return false;
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null
                && "touhou_little_maid".equals(key.getNamespace())
                && "maid".equals(key.getPath());
    }

    private static Player resolveActorPlayer(Entity entity) {
        if (entity instanceof Player player) return player;
        if (!(entity instanceof OwnableEntity ownable) || !(entity.level() instanceof ServerLevel sl))
            return null;
        UUID ownerId = ownable.getOwnerUUID();
        if (ownerId == null) return null;
        return sl.getServer().getPlayerList().getPlayer(ownerId);
    }

    // ── 副作用帮助方法 ────────────────────────────────────

    private static void sendDenyMessage(Player player, Component message) {
        player.displayClientMessage(
                message.copy().withStyle(style -> style.withColor(ModColors.RED)), true);
    }

    private static void syncInventoryIfServerPlayer(Player player) {
        if (player instanceof ServerPlayer sp) sp.inventoryMenu.sendAllDataToRemote();
    }

    private static void denyRightClickBlock(
            PlayerInteractEvent.RightClickBlock event, Player player) {
        event.setCanceled(true);
        event.setUseItem(TriState.FALSE);
        event.setUseBlock(TriState.FALSE);
        event.setCancellationResult(InteractionResult.FAIL);
        syncInventoryIfServerPlayer(player);
    }

    private static void denyRightClickBlock(
            PlayerInteractEvent.RightClickBlock event, Player player, Component message) {
        denyRightClickBlock(event, player);
        sendDenyMessage(player, message);
    }

    private static BlockPos resolveBucketRestrictionPos(Level level, BlockPos clickedPos, BlockPos placePos) {
        if (!isFreeZoneAny(level, clickedPos)) {
            return clickedPos;
        }
        if (!isFreeZoneAny(level, placePos)) {
            return placePos;
        }
        return null;
    }

    // ══════════════════════════════════════════════════════
    //  事件处理
    // ══════════════════════════════════════════════════════

    // ── 方块破坏 ──────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;
        if (isFreeZone(level, event.getPos())) return;

        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer) || player instanceof FakePlayer || !player.isCreative()) {
            event.setCanceled(true);
            if (player instanceof ServerPlayer && !(player instanceof FakePlayer))
                sendDenyMessage(player, getDenyMessage(level, event.getPos()));
        }
    }

    // ── 方块放置 ──────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;
        if (isFreeZone(level, event.getPos())) return;

        // 自然生长方块 (作物、南瓜藤、竹子等)
        if (event.getEntity() == null) {
            handleNaturalGrowthPlacement(level, event);
            return;
        }

        // 掉落方块 → 转为掉落物
        if (event.getEntity() instanceof FallingBlockEntity fallingBlock) {
            event.setCanceled(true);
            ItemStack drop = new ItemStack(event.getState().getBlock().asItem());
            if (!drop.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(
                        level,
                        fallingBlock.getX(), fallingBlock.getY(), fallingBlock.getZ(),
                        drop);
                itemEntity.setDeltaMovement(fallingBlock.getDeltaMovement());
                level.addFreshEntity(itemEntity);
            }
            fallingBlock.discard();
            return;
        }

        // 玩家/女仆放置
        if (event.getEntity() instanceof Player player) {
            if (isBedSheetCleaningUpdate(player, event.getState())) return;
            if (player.isCreative()) return;

            if (player instanceof ServerPlayer && !(player instanceof FakePlayer)) {
                event.setCanceled(true);
                sendDenyMessage(player, getDenyMessage(level, event.getPos()));
                syncInventoryIfServerPlayer(player);
            } else {
                event.setCanceled(true);
            }
            return;
        }

        // 其余一切实体放置 → 拒绝
        event.setCanceled(true);
    }

    /** 限制区内禁止一切自然生长方块 */
    private static void handleNaturalGrowthPlacement(
            ServerLevel level, BlockEvent.EntityPlaceEvent event) {
        if (shouldRestrictNaturalGrowthBlock(event.getState())) {
            event.setCanceled(true);
        }
    }

    // ── 右键方块 ──────────────────────────────────────────

    /**
     * 右键方块拦截流程：
     * <ol>
     *   <li>非城镇维度 + only_in_town 物品 → 拒绝</li>
     *   <li>城镇维度 + banned_in_town 物品 → 拒绝</li>
     *   <li>免保区 → 放行</li>
     *   <li>限制区：容器/方块实体交互放行；手持方块/锄头拒绝</li>
     * </ol>
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        // 1. 维度外限制：仅限城镇物品在其他维度使用
        if (!isTownDimension(level)) {
            if (stack.is(OtherworldInn.ONLY_IN_TOWN)) {
                denyRightClickBlock(event, player);
                if (player instanceof ServerPlayer sp)
                    sp.displayClientMessage(
                            Component.translatable("message.otherworldinn.protection.only_in_town"),
                            true);
            }
            return;
        }

        // 2. 城镇维度 + 禁用物品
        if (stack.is(OtherworldInn.BANNED_IN_TOWN)) {
            denyRightClickBlock(event, player);
            if (player instanceof ServerPlayer sp)
                sp.displayClientMessage(
                        Component.translatable("message.otherworldinn.protection.banned_item"),
                        true);
            return;
        }

        BlockState clickedState = level.getBlockState(event.getPos());
        BlockPos placePos = event.getPos().relative(event.getFace());

        // 3. 始终放行：Depot 展示块
        if (isDepotDisplayBlock(clickedState)) return;

        // 4. 限制区内：
        //    a) 方块实体交互 (容器、工作台等) → 放行
        //       （免保区内直接放行，限制区内允许容器交互）
        if (clickedState.hasBlockEntity()
                && !(clickedState.getBlock() instanceof net.minecraft.world.level.block.DecoratedPotBlock)) {
            if (isFreeZoneAny(level, event.getPos())) return;   // 免保区放行
            return;  // 限制区内允许容器交互
        }

        //    b) 饰纹陶罐 → 免保区放行，限制区内仅创造放行
        if (clickedState.getBlock() instanceof net.minecraft.world.level.block.DecoratedPotBlock) {
            if (isFreeZoneAny(level, event.getPos())) return;
            if (!player.isCreative())
                denyRightClickBlock(event, player,
                        getDenyMessage(level, event.getPos()));
            return;
        }

        //    c) 手持方块 → 以放置目标位置判定
        if (!player.isCreative() && !stack.isEmpty()) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                if (isFreeZoneAny(level, placePos)) return;
                if (!isInnFreeInteractBlock(blockItem.getBlock().defaultBlockState())) {
                    denyRightClickBlock(event, player,
                            getDenyMessage(level, placePos));
                }
            } else if (stack.getItem() instanceof BucketItem) {
                BlockPos restrictedPos = resolveBucketRestrictionPos(level, event.getPos(), placePos);
                if (restrictedPos == null) return;
                denyRightClickBlock(event, player,
                        getDenyMessage(level, restrictedPos));
            } else if (stack.getItem() instanceof HoeItem) {
                if (isFreeZoneAny(level, event.getPos())) return;
                denyRightClickBlock(event, player,
                        getDenyMessage(level, event.getPos()));
            }
        }
    }

    // ── 右键空气 ──────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();
        ItemStack stack = event.getItemStack();
        boolean inTown = level.dimension() == TownDimensions.TOWN_LEVEL;

        if (inTown && stack.is(OtherworldInn.BANNED_IN_TOWN)) {
            event.setCanceled(true);
            if (event.getEntity() instanceof ServerPlayer sp)
                sp.displayClientMessage(
                        Component.translatable("message.otherworldinn.protection.banned_item"),
                        true);
        } else if (!inTown && stack.is(OtherworldInn.ONLY_IN_TOWN)) {
            event.setCanceled(true);
            if (event.getEntity() instanceof ServerPlayer sp)
                sp.displayClientMessage(
                        Component.translatable("message.otherworldinn.protection.only_in_town"),
                        true);
        }
    }

    // ── 方块更新 / 生长 ───────────────────────────────────

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPreventDestructiveNeighborUpdate(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;
        if (!isNormalTownDecorArea(level, event.getPos())) return;

        BlockState currentState = level.getBlockState(event.getPos());
        if (currentState.isAir()) return;
        if (!currentState.canSurvive(level, event.getPos())) {
            event.setCanceled(true);
            return;
        }
        for (Direction side : event.getNotifiedSides()) {
            if (willSelfUpdateDestroyBlock(level, event.getPos(), currentState, side)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean willSelfUpdateDestroyBlock(
            ServerLevel level, BlockPos pos, BlockState currentState, Direction notifiedSide) {
        BlockPos neighborPos = pos.relative(notifiedSide);
        BlockState neighborState = level.getBlockState(neighborPos);
        return currentState.updateShape(
                notifiedSide, neighborState, level, pos, neighborPos).isAir();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockGrowFeature(BlockGrowFeatureEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;
        BlockState state = level.getBlockState(event.getPos());
        if (state.getBlock() instanceof SaplingBlock
                || state.is(net.minecraft.tags.BlockTags.SAPLINGS))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCropGrowPre(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;
        if (!isFreeZone(level, event.getPos())) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            return;
        }
        double multiplier = getCropGrowthMultiplier(level, event.getPos(), event.getState());
        if (multiplier < 1.0D && level.random.nextDouble() >= multiplier) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCropGrowPost(CropGrowEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;
        if (!isFreeZone(level, event.getPos())) return;
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof SaplingBlock
                || state.is(net.minecraft.tags.BlockTags.SAPLINGS))
            return;
        double multiplier = getCropGrowthMultiplier(level, pos, state);
        if (multiplier <= 1.0D) return;

        double extraGrowth = multiplier - 1.0D;
        int guaranteedExtraSteps = (int) Math.floor(extraGrowth);
        if (fractionalExtraGrowth(level, extraGrowth)) guaranteedExtraSteps++;
        for (int i = 0; i < guaranteedExtraSteps; i++) {
            if (!tryApplyExtraGrowth(level, pos, level.getBlockState(pos))) break;
        }
    }

    private static boolean fractionalExtraGrowth(ServerLevel level, double extraGrowth) {
        double frac = extraGrowth - Math.floor(extraGrowth);
        return frac > 0.0D && level.random.nextDouble() < frac;
    }

    private static boolean tryApplyExtraGrowth(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
            level.setBlockAndUpdate(pos, crop.getStateForAge(crop.getAge(state) + 1));
            return true;
        }
        if (state.getBlock() instanceof StemBlock && state.hasProperty(StemBlock.AGE)) {
            int age = state.getValue(StemBlock.AGE);
            if (age < 7) { level.setBlockAndUpdate(pos, state.setValue(StemBlock.AGE, age + 1)); return true; }
        }
        if (state.getBlock() instanceof NetherWartBlock && state.hasProperty(NetherWartBlock.AGE)) {
            int age = state.getValue(NetherWartBlock.AGE);
            if (age < 3) { level.setBlockAndUpdate(pos, state.setValue(NetherWartBlock.AGE, age + 1)); return true; }
        }
        if (state.getBlock() instanceof CocoaBlock && state.hasProperty(CocoaBlock.AGE)) {
            int age = state.getValue(CocoaBlock.AGE);
            if (age < 2) { level.setBlockAndUpdate(pos, state.setValue(CocoaBlock.AGE, age + 1)); return true; }
        }
        if (state.getBlock() instanceof SweetBerryBushBlock && state.hasProperty(SweetBerryBushBlock.AGE)) {
            int age = state.getValue(SweetBerryBushBlock.AGE);
            if (age < 3) { level.setBlockAndUpdate(pos, state.setValue(SweetBerryBushBlock.AGE, age + 1)); return true; }
        }
        return false;
    }

    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel() instanceof ServerLevel sl && isFreeZone(sl, event.getPos())) return;
        event.setCanceled(true);
    }

    // ── 活塞 ──────────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPistonPre(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;

        BlockPos pos = event.getPos();
        Direction moveDir = event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND
                ? event.getDirection() : event.getDirection().getOpposite();

        PistonStructureResolver resolver = new PistonStructureResolver(
                level, pos, event.getDirection(),
                event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND);
        if (!resolver.resolve()) return;

        Set<BlockPos> points = collectPistonAffectedPositions(
                event, pos, resolver, moveDir);

        // 全部在免保区 → 放行
        if (points.stream().allMatch(p -> isFreeZone(level, p))) return;

        // 检查是否跨越多个队伍
        TeamData firstTeam = null;
        for (BlockPos p : points) {
            TeamData teamAt = TeamManager.getInstance().getTeamAt(p, level.getServer());
            if (firstTeam == null) {
                firstTeam = teamAt;
            } else if (teamAt != firstTeam) {
                event.setCanceled(true);
                return;
            }
        }
    }

    private static Set<BlockPos> collectPistonAffectedPositions(
            PistonEvent.Pre event, BlockPos origin,
            PistonStructureResolver resolver, Direction moveDir) {
        Set<BlockPos> points = new HashSet<>();
        points.add(origin);
        if (event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND)
            points.add(origin.relative(event.getDirection()));
        points.addAll(resolver.getToDestroy());
        for (BlockPos p : resolver.getToPush()) {
            points.add(p);
            points.add(p.relative(moveDir));
        }
        return points;
    }

    // ── 爆炸 / 实体 ───────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getLevel().dimension() == TownDimensions.TOWN_LEVEL)
            event.getAffectedBlocks().clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().dimension() != TownDimensions.TOWN_LEVEL) return;
        Entity entity = event.getEntity();
        if (entity instanceof LightningBolt bolt) bolt.setVisualOnly(true);
        if (entity instanceof SkeletonHorse horse && horse.isTrap()) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityMobGriefing(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !isTownDimension(level)) return;
        if (!isTouhouLittleMaid(entity)) return;
        event.setCanGrief(isFreeZone(level, entity.blockPosition()));
    }

    // ── 客户端预处理 ───────────────────────────────────────

    /**
     * 客户端侧预先拦截 banned_in_town 物品，避免客户端预测放行后又被服务端拒绝造成闪烁。
     */
    @EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
    public static class ClientHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (event.getLevel().dimension() != TownDimensions.TOWN_LEVEL) return;
            if (event.getItemStack().is(OtherworldInn.BANNED_IN_TOWN)) {
                event.setCanceled(true);
                event.setUseItem(TriState.FALSE);
                event.setUseBlock(TriState.FALSE);
                event.setCancellationResult(InteractionResult.FAIL);
                return;
            }
            if (!(event.getItemStack().getItem() instanceof BucketItem)) return;

            BlockPos placePos = event.getPos().relative(event.getFace());
            BlockPos restrictedPos = resolveBucketRestrictionPos(event.getLevel(), event.getPos(), placePos);
            if (restrictedPos != null) {
                event.setCanceled(true);
                event.setUseItem(TriState.FALSE);
                event.setUseBlock(TriState.FALSE);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            if (event.getLevel().dimension() != TownDimensions.TOWN_LEVEL) return;
            if (event.getItemStack().is(OtherworldInn.BANNED_IN_TOWN)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        }
    }
}
