package com.otherworldinn.world.fatigue;

import com.otherworldinn.compat.ReskillableCompat;
import com.otherworldinn.world.dimension.TownDimensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public final class FatigueCalculator {
    private static final double BASE_GAIN_PER_SECOND = 0.010D;
    private static final double JOURNEY_PRESSURE_PER_MINUTE = 0.0016D;
    private static final double MAX_JOURNEY_PRESSURE = 0.120D;

    private static final double WALK_GAIN_PER_METER = 0.0015D;
    private static final double SPRINT_GAIN_PER_METER = 0.0050D;
    private static final double SWIM_GAIN_PER_METER = 0.0038D;
    private static final double CLIMB_GAIN_PER_METER = 0.0030D;
    private static final double JUMP_GAIN = 0.020D;
    private static final double BLOCK_BREAK_GAIN = 0.035D;
    private static final double BLOCK_PLACE_GAIN = 0.018D;
    private static final double DAMAGE_GAIN_PER_HEALTH = 0.045D;
    private static final double FOOD_LOSS_GAIN = 0.120D;
    private static final double SATURATION_LOSS_GAIN = 0.040D;
    private static final double MIN_TOWN_RECOVERY_PER_SECOND = 4.0D;
    private static final double MAX_TOWN_RECOVERY_PER_SECOND = 12.0D;
    private static final double EXPLORED_CHUNK_MULTIPLIER = 0.3D;

    /** 技能零级时主世界静止未探索的基准探索时间（秒） */
    private static final double SKILL_BASE_EXPLORE_SECONDS = 2400.0D;
    /** 四项技能满级后主世界静止未探索的目标探索时间（秒） */
    private static final double SKILL_MAX_EXPLORE_SECONDS = 5400.0D;

    private FatigueCalculator() {}

    public static FatigueComputation evaluateSecond(ServerPlayer player, FatigueData data) {
        FatigueData.MovementSample movement = data.sampleMovement(player);
        FatigueData.HungerSample hunger = data.sampleHunger(player);
        FatigueData.ActivitySample activity = data.consumeActivitySample();

        boolean outsideTown = player.level().dimension() != TownDimensions.TOWN_LEVEL;
        data.tickJourney(outsideTown);
        boolean inExploredChunk = data.updateExplorationChunk(player, outsideTown);

        if (!outsideTown || player.isCreative() || player.isSpectator()) {
            return FatigueComputation.ZERO;
        }

        double baseGain = BASE_GAIN_PER_SECOND;
        double activityGain = computeActivityGain(movement, hunger, activity);
        double timePressureGain = computeTimePressureGain(data.getCurrentJourneyTicks());
        double dimensionMultiplier = getDimensionMultiplier(player.level());
        double exploredChunkMultiplier = inExploredChunk ? EXPLORED_CHUNK_MULTIPLIER : 1.0D;
        double totalGain =
                (baseGain + activityGain + timePressureGain) * dimensionMultiplier * exploredChunkMultiplier;
        double skillMultiplier = getSkillFatigueMultiplier(player);
        totalGain *= skillMultiplier;

        return new FatigueComputation(
                totalGain,
                baseGain,
                activityGain,
                timePressureGain,
                dimensionMultiplier,
                exploredChunkMultiplier,
                skillMultiplier);
    }

    public static FatigueStage getStage(double fatigue) {
        if (fatigue >= 80.0D) {
            return FatigueStage._4;
        }
        if (fatigue >= 60.0D) {
            return FatigueStage._3;
        }
        if (fatigue >= 40.0D) {
            return FatigueStage._2;
        }
        if (fatigue >= 20.0D) {
            return FatigueStage._1;
        }
        return FatigueStage._0;
    }

    public static double getTownRecoveryPerSecond(double fatigue) {
        if (fatigue <= 0.0D) {
            return 0.0D;
        }
        double normalized = Math.min(1.0D, fatigue / FatigueData.MAX_FATIGUE);
        return MIN_TOWN_RECOVERY_PER_SECOND
                + (1.0D - normalized) * (MAX_TOWN_RECOVERY_PER_SECOND - MIN_TOWN_RECOVERY_PER_SECOND);
    }

    /**
     * 根据敏捷/挖掘/攻击/防御四项技能等级计算疲劳倍率。
     *
     * <p>用"先定目标时间再反推倍率"的方式，使技能进度和探索时间呈线性关系。
     * 四项满级 = 约 90 分钟主世界探索时间；无技能或 Reskillable 未加载时无减免（1.0）。
     */
    public static double getSkillFatigueMultiplier(ServerPlayer player) {
        if (!ReskillableCompat.isLoaded()) {
            return 1.0D;
        }

        int maxLevel = ReskillableCompat.getMaxLevel();
        if (maxLevel <= 0) {
            return 1.0D;
        }

        double agility = ReskillableCompat.getSkillLevel(player, "agility");
        double mining = ReskillableCompat.getSkillLevel(player, "mining");
        double attack = ReskillableCompat.getSkillLevel(player, "attack");
        double defense = ReskillableCompat.getSkillLevel(player, "defense");

        double avgProgress = (agility + mining + attack + defense) / (4.0D * maxLevel);
        avgProgress = Math.min(1.0D, Math.max(0.0D, avgProgress));

        // 线性时间目标: T(p) = BASE + (MAX-BASE) × p
        double targetSeconds = SKILL_BASE_EXPLORE_SECONDS
                + avgProgress * (SKILL_MAX_EXPLORE_SECONDS - SKILL_BASE_EXPLORE_SECONDS);
        // 静止时的积分: fatigue = multiplier × (base × T + pressurePerMinute × T² / 120)
        double denominator = computeStaticFatigueIntegral(targetSeconds);
        double multiplier = FatigueData.MAX_FATIGUE / denominator;
        return Math.min(1.0D, Math.max(0.05D, multiplier));
    }

    private static double computeActivityGain(
            FatigueData.MovementSample movement,
            FatigueData.HungerSample hunger,
            FatigueData.ActivitySample activity) {
        return movement.walkMeters() * WALK_GAIN_PER_METER
                + movement.sprintMeters() * SPRINT_GAIN_PER_METER
                + movement.swimMeters() * SWIM_GAIN_PER_METER
                + movement.climbMeters() * CLIMB_GAIN_PER_METER
                + movement.jumpCount() * JUMP_GAIN
                + activity.blocksBroken() * BLOCK_BREAK_GAIN
                + activity.blocksPlaced() * BLOCK_PLACE_GAIN
                + activity.damageTaken() * DAMAGE_GAIN_PER_HEALTH
                + hunger.foodLoss() * FOOD_LOSS_GAIN
                + hunger.saturationLoss() * SATURATION_LOSS_GAIN;
    }

    private static double computeTimePressureGain(long currentJourneyTicks) {
        double journeyMinutes = currentJourneyTicks / 1200.0D;
        return Math.min(MAX_JOURNEY_PRESSURE, journeyMinutes * JOURNEY_PRESSURE_PER_MINUTE);
    }

    private static double computeStaticFatigueIntegral(double seconds) {
        return BASE_GAIN_PER_SECOND * seconds
                + (JOURNEY_PRESSURE_PER_MINUTE * seconds * seconds / 120.0D);
    }

    private static double getDimensionMultiplier(Level level) {
        if (level.dimension() == Level.NETHER) {
            return 1.18D;
        }
        if (level.dimension() == Level.END) {
            return 1.25D;
        }
        if (level.dimension() == Level.OVERWORLD) {
            return 1.0D;
        }
        return 1.10D;
    }

    public record FatigueComputation(
            double totalGain,
            double baseGain,
            double activityGain,
            double timePressureGain,
            double dimensionMultiplier,
            double exploredChunkMultiplier,
            double skillMultiplier) {
        public static final FatigueComputation ZERO =
                new FatigueComputation(0.0D, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    }

    public enum FatigueStage {
        _0(-1, -1, -1, 0),
        _1(0, -1, 0, 0),
        _2(0, 0, 0, 0),
        _3(1, 1, 1, 30),
        _4(2, 2, 1, 60);

        private final int slownessAmplifier;
        private final int miningFatigueAmplifier;
        private final int weaknessAmplifier;
        private final int blindnessDuration;

        FatigueStage(
                int slownessAmplifier,
                int miningFatigueAmplifier,
                int weaknessAmplifier,
                int blindnessDuration) {
            this.slownessAmplifier = slownessAmplifier;
            this.miningFatigueAmplifier = miningFatigueAmplifier;
            this.weaknessAmplifier = weaknessAmplifier;
            this.blindnessDuration = blindnessDuration;
        }

        public int slownessAmplifier() {
            return slownessAmplifier;
        }

        public int miningFatigueAmplifier() {
            return miningFatigueAmplifier;
        }

        public int weaknessAmplifier() {
            return weaknessAmplifier;
        }

        public int blindnessDuration() {
            return blindnessDuration;
        }

        public int level() {
            return ordinal();
        }
    }
}
