package com.otherworldinn.world.storyguest;

import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.entity.guest.StoryGuestEntity;
import com.otherworldinn.init.ModEntities;
import com.otherworldinn.util.EntityUtils;
import com.otherworldinn.world.inn.GuestData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class StoryGuestService {
    private StoryGuestService() {}

    @Nullable
    public static StoryGuestEntity createSpawnCandidate(
            ServerLevel level, int innRating, RandomSource random) {
        StoryGuestDefinition definition = pickSpawnDefinition(level, innRating, random);
        if (definition == null) {
            return null;
        }
        StoryGuestEntity guest = ModEntities.STORY_GUEST.get().create(level);
        if (guest == null) {
            return null;
        }
        guest.setStoryGuestId(definition.id());
        return guest;
    }

    public static void markGuestSpawned(StoryGuestEntity guest, ServerLevel level) {
        StoryGuestDefinition definition = guest.getStoryGuestDefinition();
        if (definition == null) {
            return;
        }
        StoryGuestSavedData data = StoryGuestSavedData.get(level);
        StoryGuestProgress progress = data.getOrCreateProgress(definition.id());
        progress.setActiveEntityUuid(guest.getUUID());
        data.setDirty();
    }

    public static void handleGuestVisitEnded(GuestEntity guest, ServerLevel level) {
        if (!(guest instanceof StoryGuestEntity storyGuest)) {
            return;
        }
        StoryGuestDefinition definition = storyGuest.getStoryGuestDefinition();
        if (definition == null) {
            return;
        }
        StoryGuestSavedData data = StoryGuestSavedData.get(level);
        StoryGuestProgress progress = data.getOrCreateProgress(definition.id());
        long currentDay = level.getDayTime() / 24000L;
        progress.setActiveEntityUuid(null);
        progress.setLastCheckoutDay(currentDay);
        progress.setNextEligibleVisitDay(
                currentDay + rollReturnInterval(level.random, progress, definition));
        progress.incrementVisitCount();
        progress.clearPendingReturnRange();
        data.setDirty();
    }

    public static boolean hasStoryFlag(
            StoryGuestEntity guest, ServerLevel level, @Nullable String storyFlag) {
        if (storyFlag == null || storyFlag.isBlank()) {
            return false;
        }
        return getProgress(guest, level).hasFlag(storyFlag);
    }

    public static int getStoryStage(StoryGuestEntity guest, ServerLevel level) {
        return getProgress(guest, level).getStoryStage();
    }

    public static void setStoryStage(StoryGuestEntity guest, ServerLevel level, int stage) {
        StoryGuestProgress progress = getProgress(guest, level);
        progress.setStoryStage(stage);
        StoryGuestSavedData.get(level).setDirty();
        guest.refreshAssignedDialogue(level);
    }

    public static void addStoryFlag(StoryGuestEntity guest, ServerLevel level, @Nullable String storyFlag) {
        if (storyFlag == null || storyFlag.isBlank()) {
            return;
        }
        StoryGuestProgress progress = getProgress(guest, level);
        progress.addFlag(storyFlag);
        StoryGuestSavedData.get(level).setDirty();
        guest.refreshAssignedDialogue(level);
    }

    public static void setPendingReturnRange(
            StoryGuestEntity guest, ServerLevel level, int minDays, int maxDays) {
        StoryGuestProgress progress = getProgress(guest, level);
        progress.setPendingReturnRange(minDays, maxDays);
        StoryGuestSavedData.get(level).setDirty();
    }

    @Nullable
    public static StoryGuestDefinition getDefinition(String storyGuestId) {
        return StoryGuestRegistry.get(storyGuestId);
    }

    public static List<String> getAllStoryGuestIds() {
        return StoryGuestRegistry.allDefinitions().stream().map(StoryGuestDefinition::id).toList();
    }

    public static boolean canVisitAgain(ServerLevel level, String storyGuestId) {
        StoryGuestDefinition definition = StoryGuestRegistry.get(storyGuestId);
        if (definition == null) {
            return false;
        }
        StoryGuestProgress progress = StoryGuestSavedData.get(level).getOrCreateProgress(definition.id());
        if (progress.getStoryStage() < definition.finalStoryStage()) {
            return true;
        }
        for (StoryGuestVisitOutcomeRule rule : definition.visitOutcomeRules()) {
            if (!rule.isValid()) {
                continue;
            }
            if (progress.hasFlag(rule.requiredFlag())) {
                return rule.continueVisiting();
            }
        }
        return true;
    }

    public static StoryGuestProgress getOrCreateProgress(ServerLevel level, String storyGuestId) {
        return StoryGuestSavedData.get(level).getOrCreateProgress(storyGuestId);
    }

    @Nullable
    public static StoryGuestEntity spawnDebugGuest(ServerLevel level, String storyGuestId, Vec3 position) {
        StoryGuestDefinition definition = StoryGuestRegistry.get(storyGuestId);
        if (definition == null) {
            return null;
        }
        if (getActiveStoryGuest(level, storyGuestId) != null) {
            return null;
        }
        StoryGuestEntity guest = ModEntities.STORY_GUEST.get().create(level);
        if (guest == null) {
            return null;
        }
        double x = position.x;
        double y = position.y;
        double z = position.z;
        guest.setStoryGuestId(definition.id());
        guest.moveTo(x, y, z, level.random.nextFloat() * 360F, 0.0F);
        guest.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(BlockPos.containing(position)),
                net.minecraft.world.entity.MobSpawnType.COMMAND,
                null);
        guest.setNoAi(false);
        guest.setPersistenceRequired();
        if (!level.addFreshEntity(guest)) {
            return null;
        }
        markGuestSpawned(guest, level);
        return guest;
    }

    @Nullable
    public static StoryGuestEntity getActiveStoryGuest(ServerLevel level, String storyGuestId) {
        StoryGuestDefinition definition = StoryGuestRegistry.get(storyGuestId);
        if (definition == null) {
            return null;
        }
        StoryGuestProgress progress = StoryGuestSavedData.get(level).getOrCreateProgress(definition.id());
        UUID activeEntityUuid = progress.getActiveEntityUuid();
        if (activeEntityUuid == null) {
            return null;
        }
        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            Entity entity = findEntity(serverLevel, activeEntityUuid);
            if (entity instanceof StoryGuestEntity storyGuest
                    && entity.isAlive()
                    && !storyGuest.isRemoved()
                    && definition.id().equals(storyGuest.getStoryGuestId())) {
                return storyGuest;
            }
        }
        progress.setActiveEntityUuid(null);
        StoryGuestSavedData.get(level).setDirty();
        return null;
    }

    public static boolean clearActiveStoryGuest(ServerLevel level, String storyGuestId) {
        StoryGuestDefinition definition = StoryGuestRegistry.get(storyGuestId);
        if (definition == null) {
            return false;
        }
        StoryGuestProgress progress = StoryGuestSavedData.get(level).getOrCreateProgress(definition.id());
        if (progress.getActiveEntityUuid() == null) {
            return false;
        }
        progress.setActiveEntityUuid(null);
        StoryGuestSavedData.get(level).setDirty();
        return true;
    }

    public static boolean setStoryStage(ServerLevel level, String storyGuestId, int stage) {
        StoryGuestDefinition definition = StoryGuestRegistry.get(storyGuestId);
        if (definition == null) {
            return false;
        }
        StoryGuestProgress progress = StoryGuestSavedData.get(level).getOrCreateProgress(definition.id());
        progress.setStoryStage(stage);
        StoryGuestSavedData.get(level).setDirty();
        StoryGuestEntity activeGuest = getActiveStoryGuest(level, storyGuestId);
        if (activeGuest != null) {
            activeGuest.refreshAssignedDialogue((ServerLevel) activeGuest.level());
        }
        return true;
    }

    public static int getStoryStage(ServerLevel level, String storyGuestId) {
        StoryGuestDefinition definition = StoryGuestRegistry.get(storyGuestId);
        if (definition == null) {
            return -1;
        }
        return StoryGuestSavedData.get(level).getOrCreateProgress(definition.id()).getStoryStage();
    }

    public static boolean hasStoryFlag(ServerLevel level, String storyGuestId, @Nullable String storyFlag) {
        if (storyFlag == null || storyFlag.isBlank()) {
            return false;
        }
        StoryGuestDefinition definition = StoryGuestRegistry.get(storyGuestId);
        if (definition == null) {
            return false;
        }
        return StoryGuestSavedData.get(level).getOrCreateProgress(definition.id()).hasFlag(storyFlag);
    }

    public static boolean addStoryFlag(ServerLevel level, String storyGuestId, @Nullable String storyFlag) {
        if (storyFlag == null || storyFlag.isBlank()) {
            return false;
        }
        StoryGuestDefinition definition = StoryGuestRegistry.get(storyGuestId);
        if (definition == null) {
            return false;
        }
        StoryGuestProgress progress = StoryGuestSavedData.get(level).getOrCreateProgress(definition.id());
        if (progress.hasFlag(storyFlag)) {
            return false;
        }
        progress.addFlag(storyFlag);
        StoryGuestSavedData.get(level).setDirty();
        StoryGuestEntity activeGuest = getActiveStoryGuest(level, storyGuestId);
        if (activeGuest != null) {
            activeGuest.refreshAssignedDialogue((ServerLevel) activeGuest.level());
        }
        return true;
    }

    public static boolean forceLeaveActiveStoryGuest(ServerLevel level, String storyGuestId) {
        StoryGuestEntity activeGuest = getActiveStoryGuest(level, storyGuestId);
        if (activeGuest == null) {
            return false;
        }
        ServerLevel guestLevel = (ServerLevel) activeGuest.level();
        TeamData team = TeamManager.getInstance().getTeamAt(activeGuest.blockPosition(), guestLevel.getServer());
        if (team != null) {
            GuestData.GuestState state = activeGuest.getGuestData().getState();
            if (state == GuestData.GuestState.CHECKED_IN) {
                team.getInnData().checkOut(activeGuest.getUUID(), guestLevel, false);
            } else {
                team.getInnData().handleGuestDeparture(activeGuest.getUUID(), false, guestLevel, team);
                TeamManager.getInstance().syncTeam(team, guestLevel.getServer());
            }
            return true;
        }
        handleGuestVisitEnded(activeGuest, guestLevel);
        EntityUtils.scheduleDisappear(activeGuest);
        return true;
    }

    public static int resetAllStoryGuestProgress(MinecraftServer server) {
        int removedEntities = 0;
        for (ServerLevel level : server.getAllLevels()) {
            List<StoryGuestEntity> guests = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof StoryGuestEntity storyGuest) {
                    guests.add(storyGuest);
                }
            }
            for (StoryGuestEntity storyGuest : guests) {
                storyGuest.kill();
                removedEntities++;
            }
        }
        StoryGuestSavedData.get(server.overworld()).clearAllProgress();
        return removedEntities;
    }

    @Nullable
    private static StoryGuestDefinition pickSpawnDefinition(
            ServerLevel level, int innRating, RandomSource random) {
        StoryGuestSavedData data = StoryGuestSavedData.get(level);
        long currentDay = level.getDayTime() / 24000L;
        List<WeightedDefinition> candidates = new ArrayList<>();
        int totalWeight = 0;
        for (StoryGuestDefinition definition : StoryGuestRegistry.allDefinitions()) {
            if (definition.minInnRating() > innRating) {
                continue;
            }
            if (!canVisitAgain(level, definition.id())) {
                continue;
            }
            StoryGuestProgress progress = data.getOrCreateProgress(definition.id());
            if (hasActiveEntity(level, progress)) {
                continue;
            }
            long nextEligibleVisitDay = progress.getNextEligibleVisitDay();
            if (nextEligibleVisitDay != Long.MIN_VALUE && currentDay < nextEligibleVisitDay) {
                continue;
            }
            int weight = Math.max(1, definition.spawnWeight());
            totalWeight += weight;
            candidates.add(new WeightedDefinition(definition, weight));
        }
        if (candidates.isEmpty()) {
            return null;
        }
        int roll = random.nextInt(totalWeight);
        int current = 0;
        for (WeightedDefinition candidate : candidates) {
            current += candidate.weight();
            if (roll < current) {
                return candidate.definition();
            }
        }
        return candidates.get(candidates.size() - 1).definition();
    }

    private static boolean hasActiveEntity(ServerLevel level, StoryGuestProgress progress) {
        UUID activeEntityUuid = progress.getActiveEntityUuid();
        if (activeEntityUuid == null) {
            return false;
        }
        Entity entity = findEntity(level, activeEntityUuid);
        if (entity instanceof StoryGuestEntity storyGuest && entity.isAlive() && !storyGuest.isRemoved()) {
            return true;
        }
        progress.setActiveEntityUuid(null);
        StoryGuestSavedData.get(level).setDirty();
        return false;
    }

    @Nullable
    private static Entity findEntity(ServerLevel level, UUID entityUuid) {
        for (ServerLevel serverLevel : level.getServer().getAllLevels()) {
            Entity entity = serverLevel.getEntity(entityUuid);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private static StoryGuestProgress getProgress(StoryGuestEntity guest, ServerLevel level) {
        StoryGuestDefinition definition = guest.getStoryGuestDefinition();
        if (definition == null) {
            throw new IllegalStateException("Story guest definition is missing for entity " + guest.getUUID());
        }
        return StoryGuestSavedData.get(level).getOrCreateProgress(definition.id());
    }

    private static int rollReturnInterval(
            RandomSource random, StoryGuestProgress progress, StoryGuestDefinition definition) {
        int minDays = progress.getPendingMinReturnDays();
        int maxDays = progress.getPendingMaxReturnDays();
        if (minDays < 0 || maxDays < 0) {
            minDays = definition.minReturnIntervalDays();
            maxDays = definition.maxReturnIntervalDays();
        }
        maxDays = Math.max(minDays, maxDays);
        if (minDays >= maxDays) {
            return minDays;
        }
        return minDays + random.nextInt(maxDays - minDays + 1);
    }

    private record WeightedDefinition(StoryGuestDefinition definition, int weight) {}
}
