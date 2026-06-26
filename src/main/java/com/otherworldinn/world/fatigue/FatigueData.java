package com.otherworldinn.world.fatigue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class FatigueData implements INBTSerializable<CompoundTag> {
    public static final double MAX_FATIGUE = 100.0D;

    private double fatigue;
    private long currentJourneyTicks;

    // 客户端同步快照（从 S2CFatigueSyncPacket 写入，不参与 NBT 持久化）
    private double clientFatigue;
    private int clientStage;
    private double clientLastSecondGain;
    private double clientDimensionMultiplier;
    private double clientJourneyMinutes;

    private int pendingBlocksBroken;
    private int pendingBlocksPlaced;
    private double pendingDamageTaken;

    private int lastWalkCm = -1;
    private int lastSprintCm = -1;
    private int lastSwimCm = -1;
    private int lastClimbCm = -1;
    private int lastJumpCount = -1;
    private int lastFoodLevel = -1;
    private float lastSaturationLevel = -1.0F;

    private double lastSecondGain;
    private double lastBaseGain;
    private double lastActivityGain;
    private double lastTimePressureGain;
    private double lastDimensionMultiplier = 1.0D;
    private double lastWalkMeters;
    private double lastSprintMeters;
    private double lastSwimMeters;
    private double lastClimbMeters;
    private int lastJumpDelta;
    private double lastFoodLoss;
    private double lastSaturationLoss;
    private int lastBlocksBroken;
    private int lastBlocksPlaced;
    private double lastDamageTaken;
    private int lastNotifiedStage;
    private final Map<String, Set<Long>> visitedChunksByDimension = new HashMap<>();
    private String activeChunkDimension = "";
    private long activeChunkKey = Long.MIN_VALUE;

    public double getFatigue() {
        return fatigue;
    }

    public long getCurrentJourneyTicks() {
        return currentJourneyTicks;
    }

    public double getLastSecondGain() {
        return lastSecondGain;
    }

    public double getLastBaseGain() {
        return lastBaseGain;
    }

    public double getLastActivityGain() {
        return lastActivityGain;
    }

    public double getLastTimePressureGain() {
        return lastTimePressureGain;
    }

    // ── 客户端同步快照（由 S2CFatigueSyncPacket 写入） ──

    public double getClientFatigue() {
        return clientFatigue;
    }

    public int getClientStage() {
        return clientStage;
    }

    public double getClientLastSecondGain() {
        return clientLastSecondGain;
    }

    public double getClientDimensionMultiplier() {
        return clientDimensionMultiplier;
    }

    public double getClientJourneyMinutes() {
        return clientJourneyMinutes;
    }

    public void applySyncSnapshot(
            double fatigue, int stage, double lastSecondGain,
            double dimensionMultiplier, double journeyMinutes) {
        this.clientFatigue = fatigue;
        this.clientStage = stage;
        this.clientLastSecondGain = lastSecondGain;
        this.clientDimensionMultiplier = dimensionMultiplier;
        this.clientJourneyMinutes = journeyMinutes;
    }

    public double getLastDimensionMultiplier() {
        return lastDimensionMultiplier;
    }

    public int getLastNotifiedStage() {
        return lastNotifiedStage;
    }

    public void setLastNotifiedStage(int lastNotifiedStage) {
        this.lastNotifiedStage = Math.max(0, lastNotifiedStage);
    }

    public void addFatigue(double delta) {
        if (delta <= 0.0D) {
            return;
        }
        fatigue = Mth.clamp(fatigue + delta, 0.0D, MAX_FATIGUE);
    }

    public void setFatigue(double fatigue) {
        this.fatigue = Mth.clamp(fatigue, 0.0D, MAX_FATIGUE);
    }

    public void reduceFatigue(double delta) {
        if (delta <= 0.0D) {
            return;
        }
        fatigue = Mth.clamp(fatigue - delta, 0.0D, MAX_FATIGUE);
    }

    public void tickJourney(boolean outsideTown) {
        if (outsideTown) {
            currentJourneyTicks += 20L;
        } else {
            currentJourneyTicks = 0L;
        }
    }

    public boolean updateExplorationChunk(ServerPlayer player, boolean outsideTown) {
        if (!outsideTown) {
            commitActiveChunk();
            clearActiveChunk();
            return false;
        }

        ResourceLocation dimensionId = player.serverLevel().dimension().location();
        String dimensionKey = dimensionId.toString();
        ChunkPos chunkPos = player.chunkPosition();
        long chunkKey = chunkPos.toLong();

        if (dimensionKey.equals(activeChunkDimension) && activeChunkKey == chunkKey) {
            return isChunkVisited(dimensionKey, chunkKey);
        }

        commitActiveChunk();
        activeChunkDimension = dimensionKey;
        activeChunkKey = chunkKey;
        return isChunkVisited(dimensionKey, chunkKey);
    }

    public void recordBlockBroken() {
        pendingBlocksBroken++;
    }

    public void recordBlockPlaced() {
        pendingBlocksPlaced++;
    }

    public void recordDamageTaken(double amount) {
        if (amount > 0.0D) {
            pendingDamageTaken += amount;
        }
    }

    public MovementSample sampleMovement(ServerPlayer player) {
        int walkCm = player.getStats().getValue(Stats.CUSTOM, Stats.WALK_ONE_CM);
        int sprintCm = player.getStats().getValue(Stats.CUSTOM, Stats.SPRINT_ONE_CM);
        int swimCm = player.getStats().getValue(Stats.CUSTOM, Stats.SWIM_ONE_CM);
        int climbCm = player.getStats().getValue(Stats.CUSTOM, Stats.CLIMB_ONE_CM);
        int jumpCount = player.getStats().getValue(Stats.CUSTOM, Stats.JUMP);

        MovementSample sample =
                new MovementSample(
                        toMeters(deltaAndStore(walkCm, MovementCounter.WALK)),
                        toMeters(deltaAndStore(sprintCm, MovementCounter.SPRINT)),
                        toMeters(deltaAndStore(swimCm, MovementCounter.SWIM)),
                        toMeters(deltaAndStore(climbCm, MovementCounter.CLIMB)),
                        Math.max(0, deltaAndStore(jumpCount, MovementCounter.JUMP)));

        lastWalkMeters = sample.walkMeters();
        lastSprintMeters = sample.sprintMeters();
        lastSwimMeters = sample.swimMeters();
        lastClimbMeters = sample.climbMeters();
        lastJumpDelta = sample.jumpCount();
        return sample;
    }

    public HungerSample sampleHunger(ServerPlayer player) {
        int currentFoodLevel = player.getFoodData().getFoodLevel();
        float currentSaturationLevel = player.getFoodData().getSaturationLevel();

        if (lastFoodLevel < 0) {
            lastFoodLevel = currentFoodLevel;
            lastSaturationLevel = currentSaturationLevel;
            return HungerSample.EMPTY;
        }

        int foodLoss = Math.max(0, lastFoodLevel - currentFoodLevel);
        double saturationLoss = Math.max(0.0D, lastSaturationLevel - currentSaturationLevel);

        lastFoodLevel = currentFoodLevel;
        lastSaturationLevel = currentSaturationLevel;
        lastFoodLoss = foodLoss;
        lastSaturationLoss = saturationLoss;
        return new HungerSample(foodLoss, saturationLoss);
    }

    public ActivitySample consumeActivitySample() {
        ActivitySample sample =
                new ActivitySample(pendingBlocksBroken, pendingBlocksPlaced, pendingDamageTaken);
        pendingBlocksBroken = 0;
        pendingBlocksPlaced = 0;
        pendingDamageTaken = 0.0D;
        lastBlocksBroken = sample.blocksBroken();
        lastBlocksPlaced = sample.blocksPlaced();
        lastDamageTaken = sample.damageTaken();
        return sample;
    }

    public void storeComputation(FatigueCalculator.FatigueComputation computation) {
        lastSecondGain = computation.totalGain();
        lastBaseGain = computation.baseGain();
        lastActivityGain = computation.activityGain();
        lastTimePressureGain = computation.timePressureGain();
        lastDimensionMultiplier = computation.dimensionMultiplier();
    }

    private static double toMeters(int centimeters) {
        return centimeters / 100.0D;
    }

    private int deltaAndStore(int currentValue, MovementCounter counter) {
        return switch (counter) {
            case WALK -> {
                int delta = computeDelta(lastWalkCm, currentValue);
                lastWalkCm = currentValue;
                yield delta;
            }
            case SPRINT -> {
                int delta = computeDelta(lastSprintCm, currentValue);
                lastSprintCm = currentValue;
                yield delta;
            }
            case SWIM -> {
                int delta = computeDelta(lastSwimCm, currentValue);
                lastSwimCm = currentValue;
                yield delta;
            }
            case CLIMB -> {
                int delta = computeDelta(lastClimbCm, currentValue);
                lastClimbCm = currentValue;
                yield delta;
            }
            case JUMP -> {
                int delta = computeDelta(lastJumpCount, currentValue);
                lastJumpCount = currentValue;
                yield delta;
            }
        };
    }

    private static int computeDelta(int previous, int current) {
        if (previous < 0) {
            return 0;
        }
        return Math.max(0, current - previous);
    }

    private boolean isChunkVisited(String dimensionKey, long chunkKey) {
        return visitedChunksByDimension.getOrDefault(dimensionKey, Set.of()).contains(chunkKey);
    }

    private void commitActiveChunk() {
        if (activeChunkDimension.isEmpty() || activeChunkKey == Long.MIN_VALUE) {
            return;
        }
        visitedChunksByDimension
                .computeIfAbsent(activeChunkDimension, key -> new HashSet<>())
                .add(activeChunkKey);
    }

    private void clearActiveChunk() {
        activeChunkDimension = "";
        activeChunkKey = Long.MIN_VALUE;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        commitActiveChunk();
        tag.putDouble("Fatigue", fatigue);
        tag.putLong("JourneyTicks", currentJourneyTicks);
        tag.putDouble("LastSecondGain", lastSecondGain);
        tag.putDouble("LastBaseGain", lastBaseGain);
        tag.putDouble("LastActivityGain", lastActivityGain);
        tag.putDouble("LastTimePressureGain", lastTimePressureGain);
        tag.putDouble("LastDimensionMultiplier", lastDimensionMultiplier);
        tag.putInt("LastNotifiedStage", lastNotifiedStage);
        CompoundTag visitedChunksTag = new CompoundTag();
        for (Map.Entry<String, Set<Long>> entry : visitedChunksByDimension.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                visitedChunksTag.putLongArray(
                        entry.getKey(), entry.getValue().stream().mapToLong(Long::longValue).toArray());
            }
        }
        tag.put("VisitedChunks", visitedChunksTag);
        tag.putString("ActiveChunkDimension", activeChunkDimension);
        tag.putLong("ActiveChunkKey", activeChunkKey);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        fatigue = Mth.clamp(tag.getDouble("Fatigue"), 0.0D, MAX_FATIGUE);
        currentJourneyTicks = Math.max(0L, tag.getLong("JourneyTicks"));
        lastSecondGain = Math.max(0.0D, tag.getDouble("LastSecondGain"));
        lastBaseGain = Math.max(0.0D, tag.getDouble("LastBaseGain"));
        lastActivityGain = Math.max(0.0D, tag.getDouble("LastActivityGain"));
        lastTimePressureGain = Math.max(0.0D, tag.getDouble("LastTimePressureGain"));
        lastDimensionMultiplier = Math.max(0.0D, tag.getDouble("LastDimensionMultiplier"));
        lastNotifiedStage = Math.max(0, tag.getInt("LastNotifiedStage"));
        visitedChunksByDimension.clear();
        CompoundTag visitedChunksTag = tag.getCompound("VisitedChunks");
        for (String dimensionKey : visitedChunksTag.getAllKeys()) {
            long[] chunkKeys = visitedChunksTag.getLongArray(dimensionKey);
            if (chunkKeys.length == 0) {
                continue;
            }
            Set<Long> visitedChunkSet = new HashSet<>();
            for (long chunkKey : chunkKeys) {
                visitedChunkSet.add(chunkKey);
            }
            visitedChunksByDimension.put(dimensionKey, visitedChunkSet);
        }
        activeChunkDimension = tag.getString("ActiveChunkDimension");
        activeChunkKey = tag.contains("ActiveChunkKey") ? tag.getLong("ActiveChunkKey") : Long.MIN_VALUE;

        lastWalkCm = -1;
        lastSprintCm = -1;
        lastSwimCm = -1;
        lastClimbCm = -1;
        lastJumpCount = -1;
        lastFoodLevel = -1;
        lastSaturationLevel = -1.0F;
        pendingBlocksBroken = 0;
        pendingBlocksPlaced = 0;
        pendingDamageTaken = 0.0D;
    }

    private enum MovementCounter {
        WALK,
        SPRINT,
        SWIM,
        CLIMB,
        JUMP
    }

    public record MovementSample(
            double walkMeters,
            double sprintMeters,
            double swimMeters,
            double climbMeters,
            int jumpCount) {
        public static final MovementSample EMPTY = new MovementSample(0.0D, 0.0D, 0.0D, 0.0D, 0);
    }

    public record HungerSample(int foodLoss, double saturationLoss) {
        public static final HungerSample EMPTY = new HungerSample(0, 0.0D);
    }

    public record ActivitySample(int blocksBroken, int blocksPlaced, double damageTaken) {
        public static final ActivitySample EMPTY = new ActivitySample(0, 0, 0.0D);
    }
}
