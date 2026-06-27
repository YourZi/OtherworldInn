package com.otherworldinn.world.event.listener;

import com.github.ysbbbbbb.kaleidoscopecookery.api.event.SickleHarvestEvent;
import com.github.ysbbbbbb.kaleidoscopecookery.block.food.FoodBlock;
import com.github.ysbbbbbb.kaleidoscopetavern.block.brew.BottleBlock;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.world.dimension.TownDimensions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
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
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.StemBlock;
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
 * 城镇维度方块保护事件适配层。
 *
 * <p>区域语义、Create 接入、底层结构性写入总闸门与受控 bypass
 * 均由 {@link TownZonePolicyService} 统一提供，这里只负责把不同事件映射到新规则系统。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public class TownProtectionHandler {

    // ── 常量 ──────────────────────────────────────────────

    private static final ResourceLocation CREATE_DEPOT_ID =
            ResourceLocation.fromNamespaceAndPath("create", "depot");

    // ── 顶层判定 ──────────────────────────────────────────

    private static boolean isTownDimension(Level level) {
        return TownZonePolicyService.isTownDimension(level);
    }

    /** 根据坐标是否在有人入住的房间内，返回对应的拒绝提示消息。 */
    private static Component getDenyMessage(Level level, BlockPos pos) {
        Component message = TownZonePolicyService.getDenyMessage(level, pos);
        return message != null
                ? message
                : Component.translatable("message.otherworldinn.protection.deny");
    }

    public static boolean isInnRestrictionLiftedAt(ServerLevel level, BlockPos pos) {
        return isInnRestrictionLiftedAt((Level) level, pos);
    }

    public static boolean isInnRestrictionLiftedAt(Level level, BlockPos pos) {
        if (level == null || pos == null) return false;
        if (!isTownDimension(level)) return true;
        return TownZonePolicyService.isFreeEditZone(level, pos);
    }

    /** Create 自动机关修改方块时复用的统一判定。 */
    public static boolean canCreateModifyBlockAt(Level level, BlockPos pos) {
        return TownZonePolicyService.canCreateModifyBlockAt(level, pos);
    }

    /** 女仆操作权限：自由修改区或创造模式放行，否则拒绝。 */
    public static boolean canMaidOperateAt(Entity maidEntity, BlockPos pos, Level level) {
        return TownZonePolicyService.canMaidOperateAt(maidEntity, pos, level);
    }

    // ── 农作物 ────────────────────────────────────────────

    private static boolean isFarmingBlock(BlockState state) {
        if (state == null) return false;
        return state.getBlock() instanceof CropBlock
                || state.getBlock() instanceof StemBlock
                || state.getBlock() instanceof AttachedStemBlock
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

    private static boolean isPlayerCleanableClutter(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(ModBlocks.CLUTTER.get());
    }

    private static boolean isTouhouLittleMaid(Entity entity) {
        if (entity == null || entity.getType() == null) return false;
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null
                && "touhou_little_maid".equals(key.getNamespace())
                && "maid".equals(key.getPath());
    }

    // ── 副作用帮助方法 ────────────────────────────────────

    private static void sendDenyMessage(Player player, Component message) {
        player.displayClientMessage(message, true);
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

    private static BlockPos resolveBucketRestrictionPos(
            Level level, BlockPos clickedPos, BlockPos placePos, @org.jetbrains.annotations.Nullable Player player) {
        if (!TownZonePolicyService.canPlayerModifyAt(level, clickedPos, player)) {
            return clickedPos;
        }
        if (!TownZonePolicyService.canPlayerModifyAt(level, placePos, player)) {
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
        Player player = event.getPlayer();
        if (TownZonePolicyService.canPlayerModifyAt(level, event.getPos(), player)) {
            return;
        }
        if (isPlayerCleanableClutter(level, event.getPos())) {
            return;
        }
        if (!(player instanceof ServerPlayer) || player instanceof FakePlayer || !player.isCreative()) {
            event.setCanceled(true);
            if (player instanceof ServerPlayer && !(player instanceof FakePlayer)) {
                sendDenyMessage(player, getDenyMessage(level, event.getPos()));
            }
        }
    }

    // ── 方块放置 ──────────────────────────────────────────

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;

        // 自然生长方块 (作物、南瓜藤、竹子等)
        if (event.getEntity() == null) {
            handleNaturalGrowthPlacement(level, event);
            return;
        }

        // 掉落方块 → 转为掉落物
        if (event.getEntity() instanceof FallingBlockEntity fallingBlock) {
            if (TownZonePolicyService.canStructuralWriteAt(
                    level, event.getPos(), event.getPlacedBlock(), event.getState())) {
                return;
            }
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
            if (TownZonePolicyService.canPlayerModifyAt(level, event.getPos(), player)) return;

            if (player instanceof ServerPlayer && !(player instanceof FakePlayer)) {
                event.setCanceled(true);
                sendDenyMessage(player, getDenyMessage(level, event.getPos()));
                syncInventoryIfServerPlayer(player);
            } else {
                event.setCanceled(true);
            }
            return;
        }

        if (!TownZonePolicyService.canStructuralWriteAt(
                level, event.getPos(), event.getPlacedBlock(), event.getState())) {
            event.setCanceled(true);
        }
    }

    /** 保护区中的自然生长由结构性写入规则统一阻止。 */
    private static void handleNaturalGrowthPlacement(
            ServerLevel level, BlockEvent.EntityPlaceEvent event) {
        if (shouldRestrictNaturalGrowthBlock(event.getState())
                && !TownZonePolicyService.canStructuralWriteAt(
                        level, event.getPos(), event.getPlacedBlock(), event.getState())) {
            event.setCanceled(true);
        }
    }

    private static List<BlockPos> collectGrowthTargets(BlockPos pos, BlockState state) {
        List<BlockPos> targets = new ArrayList<>();
        targets.add(pos);
        if (state.is(net.minecraft.world.level.block.Blocks.SUGAR_CANE)
                || state.is(net.minecraft.world.level.block.Blocks.BAMBOO)
                || state.is(net.minecraft.world.level.block.Blocks.CACTUS)) {
            targets.add(pos.above());
        }
        return targets;
    }

    // ── 右键方块 ──────────────────────────────────────────

    /**
     * 右键方块拦截流程：
     * <ol>
     *   <li>非城镇维度 + only_in_town 物品 → 拒绝</li>
     *   <li>城镇维度 + banned_in_town 物品 → 拒绝</li>
     *   <li>自由修改区 → 正常放行</li>
     *   <li>保护区：容器/方块实体交互放行；手持方块/锄头拒绝</li>
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

        if (!TownZonePolicyService.canPlayerHarvestCropAt(level, event.getPos(), clickedState, player)) {
            denyRightClickBlock(event, player,
                    getDenyMessage(level, event.getPos()));
            return;
        }

        // 4. 保护区内：
        //    a) 方块实体交互 (容器、工作台等) → 放行
        //       （自由修改区内直接放行，保护区内允许容器交互）
        if (clickedState.hasBlockEntity()
                && !(clickedState.getBlock() instanceof net.minecraft.world.level.block.DecoratedPotBlock)) {
            if (TownZonePolicyService.isFreeEditZone(level, event.getPos())) return;
            return;
        }

        //    b) 饰纹陶罐 → 自由修改区放行，保护区内仅创造放行
        if (clickedState.getBlock() instanceof net.minecraft.world.level.block.DecoratedPotBlock) {
            if (TownZonePolicyService.isFreeEditZone(level, event.getPos())) return;
            if (!player.isCreative())
                denyRightClickBlock(event, player,
                        getDenyMessage(level, event.getPos()));
            return;
        }

        //    c) 手持方块 → 以放置目标位置判定
        if (!player.isCreative() && !stack.isEmpty()) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                if (TownZonePolicyService.canPlayerModifyAt(level, placePos, player)) return;
                if (!isInnFreeInteractBlock(blockItem.getBlock().defaultBlockState())) {
                    denyRightClickBlock(event, player,
                            getDenyMessage(level, placePos));
                }
            } else if (stack.getItem() instanceof BucketItem) {
                BlockPos restrictedPos = resolveBucketRestrictionPos(level, event.getPos(), placePos, player);
                if (restrictedPos == null) return;
                denyRightClickBlock(event, player,
                        getDenyMessage(level, restrictedPos));
            } else if (stack.getItem() instanceof HoeItem) {
                if (TownZonePolicyService.canPlayerModifyAt(level, event.getPos(), player)) return;
                denyRightClickBlock(event, player,
                        getDenyMessage(level, event.getPos()));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onSickleHarvest(SickleHarvestEvent event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level) || !isTownDimension(level)) return;
        if (TownZonePolicyService.canPlayerHarvestCropAt(
                level, event.getHarvestPos(), event.getHarvestState(), player)) {
            return;
        }
        event.setCanceled(true);
        event.setCostDurability(false);
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
        if (TownZonePolicyService.isFreeEditZone(level, event.getPos())) return;

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
        if (!TownZonePolicyService.canNaturalGrowth(level, event.getPos(), List.of(event.getPos())))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCropGrowPre(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;
        if (!TownZonePolicyService.canNaturalGrowth(level, event.getPos(), collectGrowthTargets(event.getPos(), event.getState()))) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCropGrowPost(CropGrowEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel) || !isTownDimension((Level) event.getLevel())) return;
    }

    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel() instanceof ServerLevel sl
                && TownZonePolicyService.canChangeAll(sl, List.of(event.getPos()), null)) return;
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

        if (!TownZonePolicyService.canPistonMove(
                level, collectPistonMoves(event, pos, resolver, moveDir), null)) {
            event.setCanceled(true);
        }
    }

    private static List<TownZonePolicyService.MovePair> collectPistonMoves(
            PistonEvent.Pre event, BlockPos origin,
            PistonStructureResolver resolver, Direction moveDir) {
        List<TownZonePolicyService.MovePair> moves = new ArrayList<>();
        moves.add(new TownZonePolicyService.MovePair(origin, origin));
        if (event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND) {
            BlockPos headPos = origin.relative(event.getDirection());
            moves.add(new TownZonePolicyService.MovePair(headPos, headPos));
        }
        for (BlockPos destroyPos : resolver.getToDestroy()) {
            moves.add(new TownZonePolicyService.MovePair(destroyPos, null));
        }
        for (BlockPos p : resolver.getToPush()) {
            moves.add(new TownZonePolicyService.MovePair(p, p.relative(moveDir)));
        }
        return moves;
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
        if (entity.getType() == EntityType.WITHER) {
            event.setCanceled(true);
            return;
        }
        if (entity instanceof LightningBolt bolt) bolt.setVisualOnly(true);
        if (entity instanceof SkeletonHorse horse && horse.isTrap()) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityMobGriefing(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) || !isTownDimension(level)) return;
        if (!isTouhouLittleMaid(entity)) return;
        event.setCanGrief(TownZonePolicyService.canMaidOperateAt(entity, entity.blockPosition(), level));
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
            BlockPos restrictedPos =
                    resolveBucketRestrictionPos(
                            event.getLevel(),
                            event.getPos(),
                            placePos,
                            event.getEntity());
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
