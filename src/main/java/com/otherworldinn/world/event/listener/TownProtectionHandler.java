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
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

@EventBusSubscriber(modid = OtherworldInn.MODID)
public class TownProtectionHandler {
    private static final double TOWN_CROP_GROWTH_MULTIPLIER = 0.3D;
    private static final double GREENHOUSE_CROP_GROWTH_MULTIPLIER = 1.5D;
    private static final String GREENHOUSE_FACILITY_ID = "greenhouse";
    private static final ResourceLocation CREATE_DEPOT_ID =
            ResourceLocation.fromNamespaceAndPath("create", "depot");


    //  核心判定 — 免保区 (Free Zone)


    private static boolean isTownDimension(Level level) {
        return level.dimension() == TownDimensions.TOWN_LEVEL;
    }

    /**
     * 免保区判定。
     *
     * <p>旅社范围（排除有旅客入住的房间）或温室范围（等级 &gt; 0）。
     * 在免保区内，除爆炸破坏外一切操作放行。
     */
    private static boolean isFreeZone(ServerLevel level, BlockPos pos) {
        if (level == null || pos == null || !isTownDimension(level)) {
            return false;
        }

        if (isInsideGreenhouseZone(level, pos)) {
            return true;
        }

        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());
        if (team == null) {
            return false;
        }
        if (!team.isInInnZone(pos)) {
            return false;
        }

        InnData innData = team.getInnData();
        RoomData room = innData.getRoomAffectedBy(pos);
        if (room != null && !room.getCurrentGuests().isEmpty()) {
            return false;
        }

        return true;
    }

    public static boolean isInnRestrictionLiftedAt(ServerLevel level, BlockPos pos) {
        return isFreeZone(level, pos);
    }

    public static boolean canMaidOperateAt(Entity maidEntity, BlockPos pos, Level level) {
        if (maidEntity == null || pos == null || level == null) return false;
        if (!isTownDimension(level)) return true;
        if (level instanceof ServerLevel serverLevel && isFreeZone(serverLevel, pos)) return true;

        Player actor = resolveActorPlayer(maidEntity);
        if (actor != null) {
            if (actor.isCreative()) return true;
            if (level instanceof ServerLevel serverLevel
                    && isFreeZone(serverLevel, pos)) {
                return true;
            }
        }
        return false;
    }


    //  温室判定


    private static boolean isInsideGreenhouseZone(Level level, BlockPos pos) {
        return getGreenhouseLevelAtPos(level, pos) > 0;
    }

    private static int getGreenhouseLevelAtPos(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || pos == null || !isTownDimension(level)) {
            return 0;
        }
        FacilityRegistry.FacilityDefinition greenhouse = FacilityRegistry.get(GREENHOUSE_FACILITY_ID);
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


    //  农作物


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
        if (isFarmingBlock(state)) return true;
        return state.is(net.minecraft.world.level.block.Blocks.PUMPKIN)
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
                || state.is(net.minecraft.tags.BlockTags.SAPLINGS))) {
            return 0.0D;
        }
        int ghLevel = getGreenhouseLevelAtPos(level, pos);
        if (ghLevel > 0) {
            return GREENHOUSE_CROP_GROWTH_MULTIPLIER + (1.0D * (ghLevel - 1));
        }
        return TOWN_CROP_GROWTH_MULTIPLIER;
    }

    //  辅助判定

    private static boolean isDepotDisplayBlock(BlockState state) {
        return state != null && CREATE_DEPOT_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    private static boolean isInnFreeInteractBlock(BlockState state) {
        if (state == null || state.isAir()) return false;
        if (state.is(OtherworldInn.INN_FREE_INTERACT)) return true;
        return state.getBlock() instanceof FoodBlock || state.getBlock() instanceof BottleBlock;
    }

    private static boolean isInnFreeInteractBlockItem(BlockItem blockItem) {
        return isInnFreeInteractBlock(blockItem.getBlock().defaultBlockState());
    }

    private static boolean isBedSheetCleaningUpdate(Player player, BlockState state) {
        if (!(state.getBlock() instanceof net.minecraft.world.level.block.BedBlock)
                || !state.hasProperty(ModBlockProperties.MESSY)) {
            return false;
        }
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

    private static boolean willSelfUpdateDestroyBlock(
            ServerLevel level, BlockPos pos, BlockState currentState, Direction notifiedSide) {
        BlockPos neighborPos = pos.relative(notifiedSide);
        BlockState neighborState = level.getBlockState(neighborPos);
        BlockState updatedState =
                currentState.updateShape(notifiedSide, neighborState, level, pos, neighborPos);
        return updatedState.isAir();
    }

    private static boolean isTouhouLittleMaid(Entity entity) {
        if (entity == null || entity.getType() == null) return false;
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null) return false;
        return "touhou_little_maid".equals(key.getNamespace()) && "maid".equals(key.getPath());
    }

    private static Player resolveActorPlayer(Entity entity) {
        if (entity instanceof Player player) return player;
        if (!(entity instanceof OwnableEntity ownable) || !(entity.level() instanceof ServerLevel serverLevel))
            return null;
        UUID ownerId = ownable.getOwnerUUID();
        if (ownerId == null) return null;
        return serverLevel.getServer().getPlayerList().getPlayer(ownerId);
    }

    private static void sendDenyMessage(Player player, Component message) {
        player.displayClientMessage(
                message.copy().withStyle(style -> style.withColor(ModColors.RED)), true);
    }

    private static void syncInventoryIfServerPlayer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.sendAllDataToRemote();
        }
    }

    private static void denyBuildForPlayer(Player player, Component message) {
        sendDenyMessage(player, message);
        syncInventoryIfServerPlayer(player);
    }

    private static void denyRightClickBlock(PlayerInteractEvent.RightClickBlock event, Player player) {
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

    private static void spawnFallingBlockDrop(
            ServerLevel level, FallingBlockEntity fallingBlock, BlockState state, BlockPos pos) {
        Item item = state.getBlock().asItem();
        if (item == net.minecraft.world.item.Items.AIR) return;
        ItemStack drop = new ItemStack(item);
        ItemEntity itemEntity =
                new ItemEntity(level, fallingBlock.getX(), fallingBlock.getY(), fallingBlock.getZ(), drop);
        itemEntity.setDeltaMovement(fallingBlock.getDeltaMovement());
        level.addFreshEntity(itemEntity);
    }

    //  方块更新 / 生长


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPreventDestructiveNeighborUpdate(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;
        BlockPos pos = event.getPos();
        if (!isNormalTownDecorArea(level, pos)) return;
        BlockState currentState = level.getBlockState(pos);
        if (currentState.isAir()) return;

        if (!currentState.canSurvive(level, pos)) {
            event.setCanceled(true);
            return;
        }
        for (Direction side : event.getNotifiedSides()) {
            if (willSelfUpdateDestroyBlock(level, pos, currentState, side)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockGrowFeature(BlockGrowFeatureEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;
        BlockState stateAtPos = level.getBlockState(event.getPos());
        if (stateAtPos.getBlock() instanceof SaplingBlock
                || stateAtPos.is(net.minecraft.tags.BlockTags.SAPLINGS)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCropGrowPre(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || !isTownDimension(serverLevel))
            return;
        double multiplier = getCropGrowthMultiplier(serverLevel, event.getPos(), event.getState());
        if (multiplier <= 0.0D) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
            return;
        }
        if (multiplier < 1.0D && serverLevel.random.nextDouble() >= multiplier) {
            event.setResult(CropGrowEvent.Pre.Result.DO_NOT_GROW);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCropGrowPost(CropGrowEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || !isTownDimension(serverLevel))
            return;
        BlockPos pos = event.getPos();
        BlockState state = serverLevel.getBlockState(pos);
        if (state.getBlock() instanceof SaplingBlock
                || state.is(net.minecraft.tags.BlockTags.SAPLINGS)) return;
        double multiplier = getCropGrowthMultiplier(serverLevel, pos, state);
        if (multiplier <= 1.0D) return;
        double extraGrowth = multiplier - 1.0D;
        int guaranteedExtraSteps = (int) Math.floor(extraGrowth);
        double fractionalChance = extraGrowth - guaranteedExtraSteps;
        int extraSteps = guaranteedExtraSteps;
        if (fractionalChance > 0.0D && serverLevel.random.nextDouble() < fractionalChance) {
            extraSteps++;
        }
        for (int i = 0; i < extraSteps; i++) {
            BlockState current = serverLevel.getBlockState(pos);
            if (!tryApplyExtraGrowth(serverLevel, pos, current)) break;
        }
    }

    private static boolean tryApplyExtraGrowth(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            if (!crop.isMaxAge(state)) {
                level.setBlockAndUpdate(pos, crop.getStateForAge(crop.getAge(state) + 1));
                return true;
            }
            return false;
        }
        if (state.getBlock() instanceof StemBlock && state.hasProperty(StemBlock.AGE)) {
            int age = state.getValue(StemBlock.AGE);
            if (age < 7) {
                level.setBlockAndUpdate(pos, state.setValue(StemBlock.AGE, age + 1));
                return true;
            }
            return false;
        }
        if (state.getBlock() instanceof NetherWartBlock && state.hasProperty(NetherWartBlock.AGE)) {
            int age = state.getValue(NetherWartBlock.AGE);
            if (age < 3) {
                level.setBlockAndUpdate(pos, state.setValue(NetherWartBlock.AGE, age + 1));
                return true;
            }
            return false;
        }
        if (state.getBlock() instanceof CocoaBlock && state.hasProperty(CocoaBlock.AGE)) {
            int age = state.getValue(CocoaBlock.AGE);
            if (age < 2) {
                level.setBlockAndUpdate(pos, state.setValue(CocoaBlock.AGE, age + 1));
                return true;
            }
            return false;
        }
        if (state.getBlock() instanceof SweetBerryBushBlock
                && state.hasProperty(SweetBerryBushBlock.AGE)) {
            int age = state.getValue(SweetBerryBushBlock.AGE);
            if (age < 3) {
                level.setBlockAndUpdate(pos, state.setValue(SweetBerryBushBlock.AGE, age + 1));
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel
                && isFreeZone(serverLevel, event.getPos())) {
            return;
        }
        event.setCanceled(true);
    }

    //  客户端预处理

    @EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
    public static class ClientHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (event.getLevel().dimension() != TownDimensions.TOWN_LEVEL) return;
            ItemStack stack = event.getItemStack();
            if (stack.is(OtherworldInn.BANNED_IN_TOWN)) {
                event.setCanceled(true);
                event.setUseItem(TriState.FALSE);
                event.setUseBlock(TriState.FALSE);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            if (event.getLevel().dimension() != TownDimensions.TOWN_LEVEL) return;
            ItemStack stack = event.getItemStack();
            if (stack.is(OtherworldInn.BANNED_IN_TOWN)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
            }
        }
    }

    //  实体事件

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityMobGriefing(EntityMobGriefingEvent event) {
        Entity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel) || !isTownDimension(serverLevel))
            return;
        if (!isTouhouLittleMaid(entity)) return;

        BlockPos pos = entity.blockPosition();
        if (isFreeZone(serverLevel, pos)) {
            event.setCanGrief(true);
            return;
        }
        event.setCanGrief(false);
    }


    //  破坏 — 限制区拦截

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = (Level) event.getLevel();
        if (!isTownDimension(level)) return;

        if (level instanceof ServerLevel serverLevel && isFreeZone(serverLevel, event.getPos())) {
            return;
        }

        if (player instanceof ServerPlayer && !(player instanceof FakePlayer)) {
            if (!player.isCreative()) {
                event.setCanceled(true);
                sendDenyMessage(player,
                        Component.translatable("message.otherworldinn.protection.deny"));
            }
        } else {
            event.setCanceled(true);
        }
    }

    //  放置 — 限制区拦截

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || !isTownDimension(serverLevel))
            return;

        BlockPos pos = event.getPos();

        if (isFreeZone(serverLevel, pos)) return;

        if (event.getEntity() == null && shouldRestrictNaturalGrowthBlock(event.getState())) {
            double multiplier = getCropGrowthMultiplier(serverLevel, pos, event.getState());
            if (multiplier <= 0.0D) {
                event.setCanceled(true);
                return;
            }
            if (multiplier < 1.0D && serverLevel.random.nextDouble() >= multiplier) {
                event.setCanceled(true);
            }
            return;
        }

        if (event.getEntity() instanceof FallingBlockEntity fallingBlock) {
            event.setCanceled(true);
            spawnFallingBlockDrop(serverLevel, fallingBlock, event.getState(), pos);
            fallingBlock.discard();
            return;
        }

        if (event.getEntity() instanceof Player player) {
            if (isBedSheetCleaningUpdate(player, event.getState())) return;
            if (player.isCreative()) return;

            if (player instanceof ServerPlayer && !(player instanceof FakePlayer)) {
                event.setCanceled(true);
                denyBuildForPlayer(player,
                        Component.translatable("message.otherworldinn.protection.deny"));
            } else {
                event.setCanceled(true);
            }
            return;
        }

        event.setCanceled(true);
    }

    //  右键物品

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Level level = event.getLevel();
        ItemStack stack = event.getItemStack();

        if (level.dimension() == TownDimensions.TOWN_LEVEL) {
            if (stack.is(OtherworldInn.BANNED_IN_TOWN)) {
                event.setCanceled(true);
                if (event.getEntity() instanceof ServerPlayer player) {
                    player.displayClientMessage(
                            Component.translatable("message.otherworldinn.protection.banned_item"), true);
                }
            }
        } else {
            if (stack.is(OtherworldInn.ONLY_IN_TOWN)) {
                event.setCanceled(true);
                if (event.getEntity() instanceof ServerPlayer player) {
                    player.displayClientMessage(
                            Component.translatable("message.otherworldinn.protection.only_in_town"), true);
                }
            }
        }
    }


    //  右键方块

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        ItemStack stack = event.getItemStack();

        if (level.dimension() != TownDimensions.TOWN_LEVEL) {
            if (stack.is(OtherworldInn.ONLY_IN_TOWN)) {
                denyRightClickBlock(event, player);
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.otherworldinn.protection.only_in_town"), true);
                }
            }
            return;
        }

        BlockState clickedState = level.getBlockState(pos);

        if (isDepotDisplayBlock(clickedState)) return;

        if (stack.is(OtherworldInn.BANNED_IN_TOWN)) {
            denyRightClickBlock(event, player);
            if (player instanceof ServerPlayer serverPlayer) {
                player.displayClientMessage(
                        Component.translatable("message.otherworldinn.protection.banned_item"), true);
            }
            return;
        }

        if (clickedState.hasBlockEntity()
                && !(clickedState.getBlock() instanceof net.minecraft.world.level.block.DecoratedPotBlock)) {
            return;
        }

        if (level instanceof ServerLevel serverLevel && isFreeZone(serverLevel, pos)) {
            return;
        }

        if (clickedState.getBlock() instanceof net.minecraft.world.level.block.DecoratedPotBlock) {
            if (!player.isCreative()) {
                denyRightClickBlock(event, player,
                        Component.translatable("message.otherworldinn.protection.deny"));
            }
            return;
        }

        if (!player.isCreative() && !stack.isEmpty()) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                BlockPos placePos = pos.relative(event.getFace());

                if (isInnFreeInteractBlockItem(blockItem)) {
                    if (level instanceof ServerLevel sl
                            && (isFreeZone(sl, pos) || isFreeZone(sl, placePos))) {
                        return;
                    }
                }

                if (level instanceof ServerLevel sl && isFreeZone(sl, placePos)) return;

                denyRightClickBlock(event, player,
                        Component.translatable("message.otherworldinn.protection.deny"));
            } else if (stack.getItem() instanceof HoeItem) {
                denyRightClickBlock(event, player,
                        Component.translatable("message.otherworldinn.protection.deny"));
            }
        }
    }


    //  活塞
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPistonPre(PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isTownDimension(level)) return;

        BlockPos pos = event.getPos();
        PistonStructureResolver resolver =
                new PistonStructureResolver(
                        level, pos, event.getDirection(),
                        event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND);
        if (!resolver.resolve()) return;

        Set<BlockPos> pointsToCheck = new HashSet<>();
        pointsToCheck.add(pos);
        if (event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND) {
            pointsToCheck.add(pos.relative(event.getDirection()));
        }
        pointsToCheck.addAll(resolver.getToDestroy());

        Direction moveDir = event.getPistonMoveType() == PistonEvent.PistonMoveType.EXTEND
                ? event.getDirection()
                : event.getDirection().getOpposite();
        for (BlockPos p : resolver.getToPush()) {
            pointsToCheck.add(p);
            pointsToCheck.add(p.relative(moveDir));
        }

        boolean allFree = true;
        for (BlockPos p : pointsToCheck) {
            if (!isFreeZone(level, p)) {
                allFree = false;
                break;
            }
        }
        if (allFree) return;

        TeamData firstTeam = null;
        for (BlockPos p : pointsToCheck) {
            TeamData teamAt = TeamManager.getInstance().getTeamAt(p, level.getServer());
            if (firstTeam == null) {
                firstTeam = teamAt;
            } else if (teamAt != firstTeam) {
                event.setCanceled(true);
                return;
            }
        }
    }

    //  爆炸 / 实体加入 — 全局拦截
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getLevel().dimension() == TownDimensions.TOWN_LEVEL) {
            event.getAffectedBlocks().clear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().dimension() == TownDimensions.TOWN_LEVEL) {
            Entity entity = event.getEntity();
            if (entity instanceof LightningBolt lightningBolt) {
                lightningBolt.setVisualOnly(true);
            }
            // 阻止骷髅陷阱（Skeleton Trap）生成
            if (entity instanceof SkeletonHorse skeletonHorse && skeletonHorse.isTrap()) {
                event.setCanceled(true);
            }
        }
    }
}
