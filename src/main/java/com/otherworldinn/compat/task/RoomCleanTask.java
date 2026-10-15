package com.otherworldinn.compat.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidArriveAtBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToPredicateBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.navigation.MaidNodeEvaluator;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.compat.MaidEfficiencyHelper;
import com.otherworldinn.foundation.ModBlockProperties;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.item.BedSheetItem;
import com.otherworldinn.item.MessyBedSheetItem;
import com.otherworldinn.world.event.listener.TownZonePolicyService;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.RoomData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.neoforge.items.ItemStackHandler;

public class RoomCleanTask implements IMaidTask{

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "clean_room");

    private static final ItemStack ICON = new ItemStack(InitItems.BROOM.get());

    private static final Map<UUID, Long> WASH_COOLDOWN = new HashMap<>();
    private static final int CLEAN_VERTICAL_SEARCH_RANGE = 16;

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public ItemStack getIcon() {
        return ICON;
    }

    @Override
    @Nullable
    public SoundEvent getAmbientSound(EntityMaid maid) {
        return null;
    }

    @Override
    public List<Pair<Integer, BehaviorControl<? super EntityMaid>>> createBrainTasks(EntityMaid maid) {
        float efficiency = MaidEfficiencyHelper.getEfficiency(maid);
        float moveSpeed = MaidEfficiencyHelper.adjustMoveSpeed(0.8F, efficiency);
        return Lists.newArrayList(
                Pair.of(
                        5,
                        new RoomCleanMoveTask(
                                moveSpeed,
                                RoomCleanTask::shouldWashDirtySheets,
                                RoomCleanTask::isWashTarget)),
                Pair.of(6, new MaidArriveAtBlockTask(2.2, RoomCleanTask::washMessySheetAt)),
                Pair.of(
                        7,
                        new RoomCleanMoveTask(
                                moveSpeed,
                                RoomCleanTask::shouldCleanBeds,
                                RoomCleanTask::isMessyBed)),
                Pair.of(8, new MaidArriveAtBlockTask(2.2, RoomCleanTask::cleanMessyBedAt)),
                Pair.of(
                        9,
                        new RoomCleanMoveTask(
                                moveSpeed,
                                RoomCleanTask::shouldCleanClutter,
                                RoomCleanTask::isClutter)),
                Pair.of(10, new MaidArriveAtBlockTask(2.2, RoomCleanTask::cleanClutterAt)));
    }

    private static class RoomCleanMoveTask extends MaidMoveToPredicateBlockTask {
        private RoomCleanMoveTask(
                float movementSpeed,
                Predicate<EntityMaid> searchCondition,
                BiPredicate<EntityMaid, BlockPos> blockPredicate) {
            super(movementSpeed, CLEAN_VERTICAL_SEARCH_RANGE, searchCondition, blockPredicate);
        }

        @Override
        protected MaidPathFindingBFS getOrCreateArrivalMap(ServerLevel worldIn, EntityMaid maid) {
            // 使用独立的 NodeEvaluator 做 BFS 初筛，避免和之后 createPath 使用的
            // 导航 NodeEvaluator 共享状态、互相污染。
            MaidNodeEvaluator evaluator = new MaidNodeEvaluator();
            evaluator.setCanOpenDoors(true);
            evaluator.setCanPassDoors(true);
            evaluator.setCanFloat(true);
            return new MaidPathFindingBFS(
                    evaluator,
                    worldIn,
                    maid,
                    maid.searchRadius(),
                    CLEAN_VERTICAL_SEARCH_RANGE);
        }

        @Override
        protected boolean checkPathReach(
                EntityMaid maid, MaidPathFindingBFS pathFinding, BlockPos pos) {
            // TLM 的 BFS 对“目标上方一格可达”也会返回 true，跨楼层时会出现假可达。
            // 这里先用 BFS 初筛，再对实际可站立位置做一次原版路径校验，只有真能走到才选择目标。
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos candidate = pos.offset(x, y, z);
                        if (!pathFinding.canPathReach(candidate)) {
                            continue;
                        }
                        Path path = maid.getNavigation().createPath(candidate, 1);
                        if (path != null && path.canReach()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    private static boolean hasCleanSheet(EntityMaid maid) {
        return findFirstSlot(maid.getMaidInv(), ModItems.BED_SHEET.get()) >= 0;
    }

    private static boolean hasDirtySheet(EntityMaid maid) {
        return findFirstSlot(maid.getMaidInv(), ModItems.MESSY_BED_SHEET.get()) >= 0;
    }

    private static boolean shouldCleanBeds(EntityMaid maid) {
        return hasCleanSheet(maid) && !shouldWashDirtySheets(maid);
    }

    private static boolean shouldCleanClutter(EntityMaid maid) {
        return true;
    }

    private static boolean isWashOnCooldown(EntityMaid maid) {
        Long expiry = WASH_COOLDOWN.get(maid.getUUID());
        return expiry != null && maid.level().getGameTime() < expiry;
    }

    private static void setWashCooldown(EntityMaid maid) {
        long baseCd = 20 + maid.getRandom().nextInt(21);
        float efficiency = MaidEfficiencyHelper.getEfficiency(maid);
        long cd = MaidEfficiencyHelper.adjustCooldown(baseCd, efficiency);
        WASH_COOLDOWN.put(maid.getUUID(), maid.level().getGameTime() + cd);
    }

    private static boolean shouldWashDirtySheets(EntityMaid maid) {
        if (isWashOnCooldown(maid)) {
            return false;
        }
        return hasDirtySheet(maid);
    }

    private static boolean isMessyBed(EntityMaid maid, BlockPos pos) {
        BlockState state = maid.level().getBlockState(pos);
        return state.getBlock() instanceof BedBlock
                && state.hasProperty(ModBlockProperties.MESSY)
                && state.getValue(ModBlockProperties.MESSY);
    }

    private static boolean isWashTarget(EntityMaid maid, BlockPos pos) {
        return MessyBedSheetItem.isWashTarget(maid.level().getBlockState(pos));
    }

    private static boolean isClutter(EntityMaid maid, BlockPos pos) {
        return maid.level().getBlockState(pos).is(ModBlocks.CLUTTER.get());
    }

    private static void cleanMessyBedAt(EntityMaid maid, BlockPos pos) {
        if (!(maid.level() instanceof ServerLevel level)) {
            return;
        }
        int cleanSlot = findFirstSlot(maid.getMaidInv(), ModItems.BED_SHEET.get());
        if (cleanSlot < 0) {
            clearTarget(maid);
            return;
        }
        ItemStack cleanSheet = maid.getMaidInv().getStackInSlot(cleanSlot);
        if (!BedSheetItem.cleanMessyBed(level, pos)) {
            clearTarget(maid);
            return;
        }
        ItemStack dirtySheet = BedSheetItem.createDirtySheet(cleanSheet, 1);
        consumeOne(maid.getMaidInv(), cleanSlot);
        insertOrDrop(maid, dirtySheet);
        maid.swing(InteractionHand.MAIN_HAND, true);
        clearTarget(maid);
    }

    private static void washMessySheetAt(EntityMaid maid, BlockPos pos) {
        if (!(maid.level() instanceof ServerLevel level)) {
            return;
        }
        BlockState targetState = level.getBlockState(pos);
        if (!MessyBedSheetItem.isWashTarget(targetState)) {
            clearTarget(maid);
            return;
        }
        int dirtySlot = findFirstSlot(maid.getMaidInv(), ModItems.MESSY_BED_SHEET.get());
        if (dirtySlot < 0) {
            clearTarget(maid);
            return;
        }
        ItemStack dirtySheet = maid.getMaidInv().getStackInSlot(dirtySlot);
        ItemStack cleanSheet = MessyBedSheetItem.createCleanSheet(dirtySheet, false);
        consumeOne(maid.getMaidInv(), dirtySlot);
        insertOrDrop(maid, cleanSheet);
        MessyBedSheetItem.consumeCauldronWaterIfNeeded(level, pos);
        maid.swing(InteractionHand.MAIN_HAND, true);
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.8F, 1.0F);
        setWashCooldown(maid);
        clearTarget(maid);
    }

    private static void cleanClutterAt(EntityMaid maid, BlockPos pos) {
        if (!(maid.level() instanceof ServerLevel level)) {
            return;
        }
        if (!level.getBlockState(pos).is(ModBlocks.CLUTTER.get())) {
            clearTarget(maid);
            return;
        }
        try (TownZonePolicyService.ProtectionBypassScope ignored =
                TownZonePolicyService.beginProtectionBypass(
                        TownZonePolicyService.ProtectionBypassReason.ROOM_CLEAN_CLUTTER,
                        java.util.List.of(pos.immutable()))) {
            if (!level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3)) {
                clearTarget(maid);
                return;
            }
        }
        maid.swing(InteractionHand.MAIN_HAND, true);
        level.playSound(null, pos, SoundEvents.WOOL_BREAK, SoundSource.PLAYERS, 0.7F, 1.1F);

        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());
        if (team != null) {
            InnData innData = team.getInnData();
            RoomData room = innData.getRoomAt(pos);
            if (room != null) {
                innData.checkAndUpdateRoom(room.getId(), level, team);
                TeamManager.getInstance().syncTeam(team, level.getServer());
            }
        }
        clearTarget(maid);
    }

    private static void clearTarget(EntityMaid maid) {
        maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private static int findFirstSlot(ItemStackHandler inv, net.minecraft.world.item.Item item) {
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty() && stack.is(item)) {
                return i;
            }
        }
        return -1;
    }

    private static void consumeOne(ItemStackHandler inv, int slot) {
        ItemStack stack = inv.getStackInSlot(slot);
        if (stack.isEmpty()) {
            return;
        }
        if (stack.getCount() <= 1) {
            inv.setStackInSlot(slot, ItemStack.EMPTY);
        } else {
            ItemStack copy = stack.copy();
            copy.shrink(1);
            inv.setStackInSlot(slot, copy);
        }
    }

    private static void insertOrDrop(EntityMaid maid, ItemStack stack) {
        ItemStack remain = stack.copy();
        ItemStackHandler inv = maid.getMaidInv();
        for (int i = 0; i < inv.getSlots() && !remain.isEmpty(); i++) {
            remain = inv.insertItem(i, remain, false);
        }
        if (!remain.isEmpty()) {
            maid.spawnAtLocation(remain);
        }
    }
}
