package com.otherworldinn.compat.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidArriveAtBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidMoveToPredicateBlockTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.compat.MaidEfficiencyHelper;
import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.item.RoomKeyItem;
import com.otherworldinn.world.inn.GuestData;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.RoomData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FrontDeskTask implements IMaidTask {
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "front_desk");
    private static final ItemStack ICON = new ItemStack(ModItems.ROOM_KEY.get());
    private static final int RECEPTION_RANGE = 16;

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
        float moveSpeed = MaidEfficiencyHelper.adjustMoveSpeed(0.85F, efficiency);
        double arrivalDelay = MaidEfficiencyHelper.adjustCooldown(26, efficiency) / 10.0D;
        return Lists.newArrayList(
                Pair.of(
                        5,
                        new MaidMoveToPredicateBlockTask(
                                moveSpeed,
                                IMaidTask.VERTICAL_SEARCH_RANGE,
                                FrontDeskTask::canStartReception,
                                FrontDeskTask::hasWaitingGuestNearby)),
                Pair.of(6, new MaidArriveAtBlockTask(arrivalDelay, FrontDeskTask::handleReceptionAt)));
    }

    private static boolean canStartReception(EntityMaid maid) {
        if (!(maid.level() instanceof ServerLevel level)) {
            clearTarget(maid);
            return false;
        }
        TeamData team = getOpenInnTeam(maid);
        if (team == null) {
            clearTarget(maid);
            return false;
        }
        if (!hasBoundRoomKey(maid.getMaidInv())) {
            clearTarget(maid);
            return false;
        }
        GuestEntity nearestInRange = findNearestWaitingGuest(level, team, maid.blockPosition(), RECEPTION_RANGE);
        if (nearestInRange == null) {
            clearTarget(maid);
            return false;
        }
        return true;
    }

    private static TeamData getOpenInnTeam(EntityMaid maid) {
        if (!(maid.level() instanceof ServerLevel level)) {
            return null;
        }
        TeamData team = TeamManager.getInstance().getTeamAt(maid.blockPosition(), level.getServer());
        if (team == null) {
            return null;
        }
        return team.getInnData().getState() == InnData.InnState.OPEN ? team : null;
    }

    private static boolean hasWaitingGuestNearby(EntityMaid maid, BlockPos pos) {
        if (!pos.equals(maid.blockPosition())) {
            return false;
        }
        if (!(maid.level() instanceof ServerLevel level)) {
            clearTarget(maid);
            return false;
        }
        TeamData team = getOpenInnTeam(maid);
        if (team == null) {
            clearTarget(maid);
            return false;
        }
        GuestEntity guest = findNearestWaitingGuest(level, team, pos, RECEPTION_RANGE);
        if (guest == null) {
            clearTarget(maid);
        }
        return guest != null;
    }

    private static void handleReceptionAt(EntityMaid maid, BlockPos pos) {
        if (!(maid.level() instanceof ServerLevel level)) {
            clearTarget(maid);
            return;
        }
        TeamData team = getOpenInnTeam(maid);
        if (team == null) {
            clearTarget(maid);
            return;
        }
        GuestEntity guest = findNearestWaitingGuest(level, team, maid.blockPosition(), RECEPTION_RANGE);
        if (guest == null) {
            clearTarget(maid);
            return;
        }
        int keySlot = findBestRoomKeySlotForGuest(maid.getMaidInv(), team, guest.getGuestData());
        if (keySlot < 0) {
            clearTarget(maid);
            return;
        }
        ItemStack keyStack = maid.getMaidInv().getStackInSlot(keySlot);
        Optional<Integer> roomIdOpt = RoomKeyItem.getBoundRoomId(keyStack);
        if (roomIdOpt.isEmpty()) {
            clearTarget(maid);
            return;
        }
        if (team.getInnData().checkIn(guest.getUUID(), roomIdOpt.get(), level)) {
            consumeOne(maid.getMaidInv(), keySlot);
            maid.swing(InteractionHand.MAIN_HAND, true);
        }
        clearTarget(maid);
    }

    private static void clearTarget(EntityMaid maid) {
        maid.getBrain().eraseMemory(InitEntities.TARGET_POS.get());
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private static int findBestRoomKeySlotForGuest(
            ItemStackHandler inventory, TeamData team, GuestData guestData) {
        int bestSlot = -1;
        int bestScore = Integer.MIN_VALUE;
        int bestCurrentGuests = Integer.MAX_VALUE;
        InnData innData = team.getInnData();
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty() || !stack.is(ModItems.ROOM_KEY.get())) {
                continue;
            }
            Optional<Integer> roomIdOpt = RoomKeyItem.getBoundRoomId(stack);
            if (roomIdOpt.isEmpty()) {
                continue;
            }
            RoomData room = innData.getRoom(roomIdOpt.get());
            if (!isValidBoundRoom(stack, room)) {
                continue;
            }
            if (room.getCurrentGuests().size() >= room.getMaxGuests()) {
                continue;
            }
            int score = calculatePreferenceScore(guestData, room);
            int currentGuests = room.getCurrentGuests().size();
            if (score > bestScore || (score == bestScore && currentGuests < bestCurrentGuests)) {
                bestScore = score;
                bestCurrentGuests = currentGuests;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private static boolean isValidBoundRoom(ItemStack keyStack, RoomData room) {
        if (room == null) {
            return false;
        }
        Optional<UUID> boundUuid = RoomKeyItem.getBoundRoomUUID(keyStack);
        return boundUuid.isEmpty() || boundUuid.get().equals(room.getUuid());
    }

    private static int calculatePreferenceScore(GuestData guestData, RoomData room) {
        int penalty = 0;
        penalty += calculatePenalty(guestData.getComfortPreference(), room.getComfort());
        penalty += calculatePenalty(guestData.getLightPreference(), room.getLight());
        penalty += calculatePenalty(guestData.getHumidityPreference(), room.getHumidity());
        return Math.max(0, 10 - penalty);
    }

    private static int calculatePenalty(GuestData.IntRange preference, int actualValue) {
        double median = (preference.min() + preference.max()) / 2.0;
        double radius = (preference.max() - preference.min()) / 2.0;
        double diff = Math.abs(actualValue - median);
        if (diff <= radius) {
            return 0;
        }
        double excess = diff - radius;
        return (int) Math.ceil(excess / 5.0);
    }

    private static List<GuestEntity> getWaitingGuests(TeamData team, ServerLevel level) {
        return team.getInnData().getGuestIds().stream()
                .map(level::getEntity)
                .filter(GuestEntity.class::isInstance)
                .map(GuestEntity.class::cast)
                .filter(
                        guest ->
                                guest.getGuestData().getState() == GuestData.GuestState.WAITING
                                        && !guest.getGuestData().isCheckedOut())
                .toList();
    }

    private static GuestEntity findNearestWaitingGuest(ServerLevel level, TeamData team, BlockPos pos, int maxRange) {
        int maxRangeSqr = maxRange * maxRange;
        GuestEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (GuestEntity guest : getWaitingGuests(team, level)) {
            double dist = guest.blockPosition().distSqr(pos);
            if (dist <= maxRangeSqr && dist < nearestDist) {
                nearestDist = dist;
                nearest = guest;
            }
        }
        return nearest;
    }

    private static boolean hasBoundRoomKey(ItemStackHandler inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()
                    && stack.is(ModItems.ROOM_KEY.get())
                    && RoomKeyItem.getBoundRoomId(stack).isPresent()) {
                return true;
            }
        }
        return false;
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
}
