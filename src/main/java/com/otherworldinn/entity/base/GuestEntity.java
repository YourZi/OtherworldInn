package com.otherworldinn.entity.base;

import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import com.otherworldinn.entity.guest.AdvancedVipGuestEntity;
import com.otherworldinn.entity.guest.HeavyPackGuestEntity;
import com.otherworldinn.entity.guest.OrdinaryGuestEntity;
import com.otherworldinn.entity.guest.OrdinaryVipGuestEntity;
import com.otherworldinn.entity.guest.RichGuestEntity;
import com.otherworldinn.entity.guest.SponsorGuestEntity;
import com.otherworldinn.entity.guest.UltraRichGuestEntity;
import com.otherworldinn.util.BlockEntitySearchUtils;
import com.otherworldinn.util.service.GuestNameManager;
import com.otherworldinn.world.dialogue.DialogueService;
import com.otherworldinn.world.economy.service.ItemSellPriceManager;
import com.otherworldinn.world.inn.GuestData;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.simibubi.create.content.redstone.deskBell.DeskBellBlockEntity;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 旅客实体
 *
 * <p>抽象父类实体，存储旅客数据。
 */
public abstract class GuestEntity extends PathfinderMob {

    private static final EntityDataAccessor<Integer> SKIN_VARIANT =
            SynchedEntityData.defineId(GuestEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GUEST_STATE =
            SynchedEntityData.defineId(GuestEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GUEST_BUDGET =
            SynchedEntityData.defineId(GuestEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GUEST_COMFORT_PREF =
            SynchedEntityData.defineId(GuestEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GUEST_LIGHT_PREF =
            SynchedEntityData.defineId(GuestEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GUEST_HUMIDITY_PREF =
            SynchedEntityData.defineId(GuestEntity.class, EntityDataSerializers.INT);
    // 搜索“餐台”的范围：以旅客为中心 32 格
    private static final int DINING_SEARCH_RADIUS = 32;
    // 平均一天触发 3 次：24000 / 3 = 8000 tick
    private static final int DINING_AVERAGE_INTERVAL = 8000;
    private static final int MIN_DAILY_DINING_ATTEMPTS = 3;
    private static final int NAVIGATION_STUCK_TIMEOUT_TICKS = 200;
    private static final double NAVIGATION_PROGRESS_THRESHOLD_SQR = 0.0625D;
    private static final ResourceLocation CREATE_DEPOT_ID =
            ResourceLocation.fromNamespaceAndPath("create", "depot");
    private static final String TAG_ASSIGNED_DIALOGUE_ID = "AssignedDialogueId";

    /** 旅客数据 */
    @Getter private GuestData guestData;

    /** 导航目标 */
    private BlockPos navigationTarget;

    /** 生成延迟计数器 */
    private int spawnDelay = 0;

    @Getter private int budget;
    private String assignedDialogueId = "";
    private int navigationStuckTicks = 0;
    private double lastNavigationDistanceSqr = Double.MAX_VALUE;
    private long diningPlanDay = Long.MIN_VALUE;
    private int dailySpendTarget = 0;
    private int dailySpentCoins = 0;
    private int dailyPurchaseTarget = 0;
    private int dailyPurchaseCount = 0;
    private int dailyPurchaseAttemptCount = 0;
    // 下一次尝试“用餐购买”的时间戳（游戏刻）
    private long nextDiningAttemptTime = 0L;
    // 行为树活动状态（对齐原版 Activity 概念）
    private Activity activeActivity = Activity.IDLE;

    @Nullable
    // 当前已经锁定、正在前往的餐台坐标
    private BlockPos pendingDiningTarget;

    protected GuestEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        long currentTime = level.getGameTime();
        this.guestData = new GuestData(this.getUUID(), currentTime + getStayDuration());
        this.initRewardItems();
        this.budget = this.generateInitialBudget();
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);

        // 如果没有自定义名称，则设置一个随机名称
        if (!this.hasCustomName()) {
            this.setCustomName(GuestNameManager.getRandomName(this.getRandom()));
        }
        assignRandomDialogueIfAbsent();

        this.initGuestPreferences();

        this.entityData.set(
                GUEST_COMFORT_PREF,
                packPreference(
                        this.guestData.getComfortPreference().min(),
                        this.guestData.getComfortPreference().max()));
        this.entityData.set(
                GUEST_LIGHT_PREF,
                packPreference(
                        this.guestData.getLightPreference().min(),
                        this.guestData.getLightPreference().max()));
        this.entityData.set(
                GUEST_HUMIDITY_PREF,
                packPreference(
                        this.guestData.getHumidityPreference().min(),
                        this.guestData.getHumidityPreference().max()));

        return spawnData;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanOpenDoors(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    private class MoveToTargetGoal extends Goal {
        private int recalculateDelay;

        public MoveToTargetGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return GuestEntity.this.navigationTarget != null && !GuestEntity.this.isSleeping();
        }

        @Override
        public void start() {
            this.recalculateDelay = 0;
            moveToTarget();
        }

        @Override
        public void tick() {
            if (GuestEntity.this.navigationTarget != null) {
                double tgtX = GuestEntity.this.navigationTarget.getX() + 0.5;
                double tgtY = GuestEntity.this.navigationTarget.getY();
                double tgtZ = GuestEntity.this.navigationTarget.getZ() + 0.5;
                double hDistSqr =
                        (GuestEntity.this.getX() - tgtX) * (GuestEntity.this.getX() - tgtX)
                                + (GuestEntity.this.getZ() - tgtZ)
                                        * (GuestEntity.this.getZ() - tgtZ);
                double vDist = Math.abs(GuestEntity.this.getY() - tgtY);

                if (hDistSqr < 4.0D && vDist < 2.0D) {
                    if (GuestEntity.this.guestData.getState() == GuestData.GuestState.WAITING) {
                        GuestEntity.this.getNavigation().stop();
                    } else {
                        GuestEntity.this.clearNavigationTarget();
                    }
                    return;
                }
                if (GuestEntity.this.lastNavigationDistanceSqr - hDistSqr
                        > NAVIGATION_PROGRESS_THRESHOLD_SQR) {
                    GuestEntity.this.navigationStuckTicks = 0;
                } else {
                    GuestEntity.this.navigationStuckTicks++;
                    if (GuestEntity.this.getNavigation().isDone()) {
                        GuestEntity.this.navigationStuckTicks += 2;
                    }
                }
                GuestEntity.this.lastNavigationDistanceSqr = hDistSqr;
                if (GuestEntity.this.navigationStuckTicks >= NAVIGATION_STUCK_TIMEOUT_TICKS) {
                    GuestEntity.this.clearNavigationTarget();
                    return;
                }

                if (--this.recalculateDelay <= 0) {
                    this.recalculateDelay = 20;
                    moveToTarget();
                }
            }
        }

        private void moveToTarget() {
            if (GuestEntity.this.navigationTarget != null) {
                GuestEntity.this
                        .getNavigation()
                        .moveTo(
                                GuestEntity.this.navigationTarget.getX() + 0.5,
                                GuestEntity.this.navigationTarget.getY(),
                                GuestEntity.this.navigationTarget.getZ() + 0.5,
                                1.0D);
            }
        }
    }

    private class SleepAtNightGoal extends Goal {
        private BlockPos targetBedHead;
        private int pathRecalcDelay;
        private int stuckTicks;
        private double lastDistanceSqr = Double.MAX_VALUE;
        private static final int STUCK_TIMEOUT_TICKS = 200;
        private static final double PROGRESS_THRESHOLD_SQR = 0.0625D;

        public SleepAtNightGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!(GuestEntity.this.level() instanceof ServerLevel level)) return false;
            if (!level.isNight()) return false;
            if (GuestEntity.this.guestData.getState() != GuestData.GuestState.CHECKED_IN)
                return false;
            BlockPos assignedBed = GuestEntity.this.guestData.getAssignedBedPos();
            if (assignedBed == null) return false;
            BlockPos bedHeadPos = normalizeBedHeadPos(level, assignedBed);
            if (bedHeadPos == null) return false;
            if (GuestEntity.this.isSleeping()) {
                if (GuestEntity.this.getSleepingPos().isPresent()
                        && bedHeadPos.equals(GuestEntity.this.getSleepingPos().get())) {
                    return false;
                }
                GuestEntity.this.stopSleeping();
            }
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (!(GuestEntity.this.level() instanceof ServerLevel level)) return false;
            if (!level.isNight()) return false;
            if (GuestEntity.this.guestData.getState() != GuestData.GuestState.CHECKED_IN)
                return false;
            BlockPos assignedBed = GuestEntity.this.guestData.getAssignedBedPos();
            if (assignedBed == null) return false;
            BlockPos bedHeadPos = normalizeBedHeadPos(level, assignedBed);
            if (bedHeadPos == null) return false;
            if (GuestEntity.this.isSleeping()) {
                return bedHeadPos.equals(GuestEntity.this.getSleepingPos().orElse(null));
            }
            if (this.stuckTicks >= STUCK_TIMEOUT_TICKS) {
                return false;
            }
            return this.targetBedHead != null && this.targetBedHead.equals(bedHeadPos);
        }

        @Override
        public void start() {
            this.pathRecalcDelay = 0;
            this.stuckTicks = 0;
            this.lastDistanceSqr = Double.MAX_VALUE;
            if (GuestEntity.this.level() instanceof ServerLevel level) {
                BlockPos assignedBed = GuestEntity.this.guestData.getAssignedBedPos();
                if (assignedBed != null) {
                    BlockPos bedHeadPos = normalizeBedHeadPos(level, assignedBed);
                    if (bedHeadPos != null) {
                        this.targetBedHead = bedHeadPos;
                        GuestEntity.this.getNavigation().stop();
                        moveToBedApproach(level, bedHeadPos);
                    }
                }
            }
        }

        @Override
        public void tick() {
            if (!(GuestEntity.this.level() instanceof ServerLevel level)) return;
            if (this.targetBedHead == null) return;

            double currentDistSqr =
                    GuestEntity.this.distanceToSqr(
                            this.targetBedHead.getX() + 0.5D,
                            this.targetBedHead.getY(),
                            this.targetBedHead.getZ() + 0.5D);

            if (currentDistSqr <= 3.0D) {
                GuestEntity.this.startSleeping(this.targetBedHead);
                GuestEntity.this.getNavigation().stop();
                return;
            }

            if (this.lastDistanceSqr - currentDistSqr > PROGRESS_THRESHOLD_SQR) {
                this.stuckTicks = 0;
            } else {
                this.stuckTicks++;
                if (GuestEntity.this.getNavigation().isDone()) {
                    this.stuckTicks += 2;
                }
            }
            this.lastDistanceSqr = currentDistSqr;

            if (--this.pathRecalcDelay <= 0) {
                this.pathRecalcDelay = 20;
                moveToBedApproach(level, this.targetBedHead);
            }
        }

        @Override
        public void stop() {
            if (GuestEntity.this.isSleeping()) {
                GuestEntity.this.stopSleeping();
            }
            this.targetBedHead = null;
            GuestEntity.this.getNavigation().stop();
        }

        private void moveToBedApproach(ServerLevel level, BlockPos bedHeadPos) {
            BlockPos approachPos = findBedApproachPos(level, bedHeadPos);
            if (approachPos != null) {
                GuestEntity.this
                        .getNavigation()
                        .moveTo(
                                approachPos.getX() + 0.5,
                                approachPos.getY(),
                                approachPos.getZ() + 0.5,
                                1.0D);
            }
        }
    }

    /**
     * 获取旅客皮肤纹理
     *
     * <p>子类必须实现此方法以提供特定的纹理。
     *
     * @return 纹理资源位置
     */
    public abstract ResourceLocation getSkinTexture();

    /**
     * 获取模型类型
     *
     * <p>返回 "default" (Steve) 或 "slim" (Alex)。 默认为 "default"。
     *
     * @return 模型类型字符串
     */
    public String getModelType() {
        return "default";
    }

    /**
     * 创建旅客属性
     *
     * @return 属性构建器
     */
    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 64.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    /**
     * 获取旅客停留时长（ticks）
     *
     * <p>默认为 1 Minecraft 天 (24000 ticks)。 子类可覆盖此方法以设定特定的停留时间。
     *
     * @return 停留时长 (ticks)
     */
    protected long getStayDuration() {
        return 24000L;
    }

    /**
     * 初始化旅客偏好
     *
     * <p>子类可覆盖此方法以设定特定的房间偏好。 默认所有属性偏好均为 0-100 (无限制)。
     */
    protected void initGuestPreferences() {
        GuestProfile profile = this.getGuestProfile();
        applyPreferenceRange(profile.comfortRange(), this.guestData::setComfortPreference);
        applyPreferenceRange(profile.lightRange(), this.guestData::setLightPreference);
        applyPreferenceRange(profile.humidityRange(), this.guestData::setHumidityPreference);
    }

    protected GuestProfile getGuestProfile() {
        return new GuestProfile(
                new PreferenceRangeProfile(new GuestData.IntRange(0, 0), new GuestData.IntRange(100, 100)),
                new PreferenceRangeProfile(new GuestData.IntRange(0, 0), new GuestData.IntRange(100, 100)),
                new PreferenceRangeProfile(new GuestData.IntRange(5, 20), new GuestData.IntRange(100, 100)),
                new GuestData.IntRange(6, 42),
                1.0D);
    }

    public double getReputationMultiplier() {
        return Math.max(0.0D, this.getGuestProfile().reputationMultiplier());
    }

    /**
     * 初始化奖励物品
     *
     * <p>子类可覆盖此方法以添加特定的奖励物品。 默认无奖励。 示例：this.guestData.addRewardItem(Items.EMERALD, 1, 3);
     */
    protected void initRewardItems() {
        // 默认无奖励，由子类实现
    }

    public void setNavigationTarget(BlockPos pos) {
        if (this.isSleeping()) {
            return;
        }
        this.navigationTarget = pos;
        this.navigationStuckTicks = 0;
        this.lastNavigationDistanceSqr = Double.MAX_VALUE;
    }

    private void clearNavigationTarget() {
        this.navigationTarget = null;
        this.navigationStuckTicks = 0;
        this.lastNavigationDistanceSqr = Double.MAX_VALUE;
        this.getNavigation().stop();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new SleepAtNightGoal());
        this.goalSelector.addGoal(1, new MoveToTargetGoal());
        // 优先级 1: 开门
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        // 优先级 2: 寻找旅社 (生成后)
        this.goalSelector.addGoal(2, new FindInnGoal());
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.6D));
    }

    private class FindInnGoal extends Goal {
        private BlockPos targetInnPos;
        private int recalculatePathDelay;

        public FindInnGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // 延迟执行
            if (GuestEntity.this.spawnDelay < 60) return false;

            // 只有处于空闲状态且没有导航目标时才寻找旅社
            if (GuestEntity.this.guestData.getState() != GuestData.GuestState.IDLE) return false;
            if (GuestEntity.this.navigationTarget != null) return false;

            // 如果已经在旅社范围内，则不需要寻找
            if (isInInnRange()) return false;

            // 查找最近的队伍旅社
            if (targetInnPos == null
                    && GuestEntity.this.level() instanceof ServerLevel serverLevel) {
                findNearestInn(serverLevel);
            }
            return targetInnPos != null;
        }

        private void findNearestInn(ServerLevel serverLevel) {
            TeamData team =
                    TeamManager.getInstance()
                            .getNearestInn(
                                    GuestEntity.this.blockPosition(), serverLevel.getServer());
            if (team != null && !team.getInnRegions().isEmpty()) {
                // 取第一个区域的中心作为目标
                TeamData.InnRegion region = team.getInnRegions().get(0);
                targetInnPos =
                        new BlockPos(
                                (region.minX() + region.maxX()) / 2,
                                64,
                                (region.minZ() + region.maxZ()) / 2);
            }
        }

        @Override
        public void start() {
            if (targetInnPos != null) {
                GuestEntity.this
                        .getNavigation()
                        .moveTo(
                                targetInnPos.getX(),
                                targetInnPos.getY(),
                                targetInnPos.getZ(),
                                1.0D);
                this.recalculatePathDelay = 0;
            }
        }

        @Override
        public boolean canContinueToUse() {
            // 如果已经在范围内，或者状态不再是 IDLE，停止
            if (isInInnRange()
                    || GuestEntity.this.guestData.getState() != GuestData.GuestState.IDLE) {
                return false;
            }
            return targetInnPos != null;
        }

        @Override
        public void tick() {
            // 每5tick检查一次范围
            if (GuestEntity.this.tickCount % 5 == 0) {
                // 检查是否进入了旅社范围
                if (isInInnRange()) {
                    // 停止移动
                    GuestEntity.this.getNavigation().stop();
                    targetInnPos = null;
                    return;
                }
            }

            // 重新计算路径逻辑 (每 40 tick / 2秒)
            if (--this.recalculatePathDelay <= 0) {
                this.recalculatePathDelay = 40;
                if (targetInnPos != null) {
                    if (GuestEntity.this.getNavigation().isDone()) {
                        // 如果导航完成了但还没到，尝试重新寻找目标并移动
                        if (GuestEntity.this.level() instanceof ServerLevel serverLevel) {
                            findNearestInn(serverLevel);
                        }
                    }
                    GuestEntity.this
                            .getNavigation()
                            .moveTo(
                                    targetInnPos.getX(),
                                    targetInnPos.getY(),
                                    targetInnPos.getZ(),
                                    1.0D);
                }
            }
        }

        private boolean isInInnRange() {
            if (GuestEntity.this.level() instanceof ServerLevel serverLevel) {
                TeamData team =
                        TeamManager.getInstance()
                                .getTeamAt(
                                        GuestEntity.this.blockPosition(), serverLevel.getServer());
                return team != null;
            }
            return false;
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        // 仅放行：创造模式攻击 / 虚空伤害；其余一律免疫（含药水等）
        return !source.isCreativePlayer() && !source.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            // 获取当前位置的队伍/旅社
            TeamData team =
                    TeamManager.getInstance()
                            .getTeamAt(this.blockPosition(), serverLevel.getServer());
            if (team != null) {
                InnData innData = team.getInnData();
                // 强制退房，标记为非正常退房（不支付房费）
                innData.checkOut(this.getUUID(), serverLevel, false);
            }
        }
    }

    private void scheduleNextDiningAttempt(ServerLevel level) {
        int intervalBase = this.getBudgetBasedDiningInterval();
        int next = intervalBase / 2 + this.getRandom().nextInt(intervalBase + 1);
        this.nextDiningAttemptTime = level.getGameTime() + next;
    }

    private boolean isDiningDisplay(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return CREATE_DEPOT_ID.equals(id);
    }

    @Nullable
    private IItemHandler getDisplayItemHandler(ServerLevel level, BlockPos pos, BlockState state) {
        // 统一通过方块物品能力读取 Create 置物台/弹射置物台上的展示物
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, blockEntity, null);
    }

    private boolean hasSellableItem(ServerLevel level, BlockPos pos, BlockState state) {
        IItemHandler itemHandler = getDisplayItemHandler(level, pos, state);
        if (itemHandler == null) {
            return false;
        }
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && ItemSellPriceManager.getPrice(stack) > 0) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private BlockPos findNearbyDiningDisplay(ServerLevel level) {
        List<BlockPos> candidates = new ArrayList<>();
        BlockPos origin = this.blockPosition();
        int minX = origin.getX() - DINING_SEARCH_RADIUS;
        int maxX = origin.getX() + DINING_SEARCH_RADIUS;
        int minZ = origin.getZ() - DINING_SEARCH_RADIUS;
        int maxZ = origin.getZ() + DINING_SEARCH_RADIUS;
        int minY = Math.max(level.getMinBuildHeight(), origin.getY() - 4);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, origin.getY() + 4);
        BlockEntitySearchUtils.forEachInBlockRange(
                level,
                minX,
                maxX,
                minY,
                maxY,
                minZ,
                maxZ,
                blockEntity -> {
                    BlockPos pos = blockEntity.getBlockPos();
                    if (origin.distSqr(pos) > DINING_SEARCH_RADIUS * DINING_SEARCH_RADIUS) {
                        return;
                    }
                    BlockState state = blockEntity.getBlockState();
                    if (isDiningDisplay(state) && hasSellableItem(level, pos, state)) {
                        candidates.add(pos.immutable());
                    }
                });
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(this.getRandom().nextInt(candidates.size()));
    }

    private boolean tryPurchaseFromDisplay(ServerLevel level, BlockPos pos) {
        // 以餐台位置归属队伍，避免旅客站位边界导致入账队伍错误
        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());
        if (team == null) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        IItemHandler itemHandler = getDisplayItemHandler(level, pos, state);
        if (itemHandler == null) {
            return false;
        }

        List<Integer> sellableSlots = new ArrayList<>();
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            int unitPrice = ItemSellPriceManager.getPrice(stack);
            if (unitPrice > 0 && unitPrice <= this.budget) {
                sellableSlots.add(slot);
            }
        }
        if (sellableSlots.isEmpty()) {
            return false;
        }

        int slot = sellableSlots.get(this.getRandom().nextInt(sellableSlots.size()));
        ItemStack slotStack = itemHandler.getStackInSlot(slot);
        int unitPrice = ItemSellPriceManager.getPrice(slotStack);
        if (unitPrice <= 0 || unitPrice > this.budget) {
            return false;
        }
        int remainingToday = Math.max(1, this.dailySpendTarget - this.dailySpentCoins);
        int desiredSpend = Math.min(this.getBudgetBasedDesiredSpend(), remainingToday + unitPrice);
        int maxCountByBudget = this.budget / unitPrice;
        int maxCountByDailyTarget = Math.max(1, (remainingToday + unitPrice - 1) / unitPrice);
        int desiredCount = Math.max(1, desiredSpend / unitPrice);
        int buyCount =
                Math.min(
                        slotStack.getCount(),
                        Math.min(Math.min(maxCountByBudget, maxCountByDailyTarget), desiredCount));
        if (buyCount <= 0) {
            return false;
        }
        ItemStack simulated = itemHandler.extractItem(slot, buyCount, true);
        if (simulated.isEmpty()) {
            return false;
        }

        ItemStack purchased = itemHandler.extractItem(slot, simulated.getCount(), false);
        if (purchased.isEmpty()) {
            return false;
        }

        int paidUnitPrice = ItemSellPriceManager.getPrice(purchased);
        if (paidUnitPrice <= 0) {
            return false;
        }

        int totalCost = paidUnitPrice * purchased.getCount();
        team.addCoins(totalCost, level.getServer());
        team.getInnData().recordDiningIncome(totalCost, level);
        TeamManager.getInstance().syncTeam(team, level.getServer());
        this.dailySpentCoins += totalCost;
        this.dailyPurchaseCount += 1;
        return true;
    }

    private void handleDiningPurchase(ServerLevel level) {
        if (this.guestData.getState() != GuestData.GuestState.CHECKED_IN) {
            this.pendingDiningTarget = null;
            return;
        }
        this.ensureDailyDiningPlan(level);
        if (this.hasReachedDailyDiningTargets()) {
            this.pendingDiningTarget = null;
            this.navigationTarget = null;
            this.getNavigation().stop();
            long nextDayStart = (this.diningPlanDay + 1L) * 24000L;
            this.nextDiningAttemptTime =
                    Math.max(
                            this.nextDiningAttemptTime,
                            nextDayStart + this.getRandom().nextInt(200));
            return;
        }

        if (this.pendingDiningTarget != null) {
            double distSqr =
                    this.distanceToSqr(
                            this.pendingDiningTarget.getX() + 0.5,
                            this.pendingDiningTarget.getY(),
                            this.pendingDiningTarget.getZ() + 0.5);
            if (distSqr <= 4.0D) {
                // 到达餐台后立即尝试购买一次（成功或失败都进入下一轮冷却）
                this.dailyPurchaseAttemptCount += 1;
                tryPurchaseFromDisplay(level, this.pendingDiningTarget);
                this.pendingDiningTarget = null;
                this.clearNavigationTarget();
                scheduleNextDiningAttempt(level);
            } else if (this.navigationTarget == null) {
                this.pendingDiningTarget = null;
                this.nextDiningAttemptTime =
                        level.getGameTime() + this.getBudgetBasedRetryInterval();
            }
            return;
        }

        if (this.nextDiningAttemptTime == 0L) {
            this.nextDiningAttemptTime = level.getGameTime() + this.getInitialDiningAttemptDelay();
            return;
        }
        // 避免在已有导航任务时插入餐饮任务，减少行为冲突
        if (level.getGameTime() < this.nextDiningAttemptTime || this.navigationTarget != null) {
            return;
        }
        if (!this.shouldAttemptDiningPurchaseNow()) {
            this.nextDiningAttemptTime = level.getGameTime() + this.getBudgetBasedRetryInterval();
            return;
        }

        BlockPos target = findNearbyDiningDisplay(level);
        if (target == null) {
            this.nextDiningAttemptTime = level.getGameTime() + this.getBudgetBasedRetryInterval();
            return;
        }

        this.pendingDiningTarget = target;
        this.setNavigationTarget(target);
    }

    private boolean handleSleepBehavior(ServerLevel level) {
        if (this.guestData.getState() != GuestData.GuestState.CHECKED_IN) {
            if (this.isSleeping()) {
                this.stopSleeping();
            }
            return false;
        }
        if (!level.isNight()) {
            if (this.isSleeping()) {
                this.stopSleeping();
            }
            return false;
        }
        BlockPos assignedBed = this.guestData.getAssignedBedPos();
        if (assignedBed == null) {
            return true;
        }
        BlockPos bedHeadPos = normalizeBedHeadPos(level, assignedBed);
        if (bedHeadPos == null) {
            return true;
        }
        if (this.isSleeping()) {
            if (this.getSleepingPos().isPresent() && bedHeadPos.equals(this.getSleepingPos().get())) {
                // 睡眠维持态：确保没有残留导航导致位置漂移
                if (this.navigationTarget != null || !this.getNavigation().isDone()) {
                    this.clearNavigationTarget();
                }
                return true;
            }
            this.stopSleeping();
        }
        double distSqr =
                this.distanceToSqr(
                        bedHeadPos.getX() + 0.5D, bedHeadPos.getY(), bedHeadPos.getZ() + 0.5D);
        if (distSqr <= 3.0D) {
            this.startSleeping(bedHeadPos);
            this.clearNavigationTarget();
            return true;
        }
        BlockPos approachPos = findBedApproachPos(level, bedHeadPos);
        if (approachPos != null && (this.navigationTarget == null || !this.navigationTarget.equals(approachPos))) {
            this.setNavigationTarget(approachPos);
        }
        return true;
    }

    private Activity resolveBehaviorActivity(ServerLevel level) {
        if (this.guestData.getState() == GuestData.GuestState.CHECKED_IN && level.isNight()) {
            BlockPos assignedBed = this.guestData.getAssignedBedPos();
            if (assignedBed != null && normalizeBedHeadPos(level, assignedBed) != null) {
                return Activity.REST;
            }
        }
        return Activity.IDLE;
    }

    private void syncBrainActivity(Activity targetActivity) {
        // 不直接依赖具体 Brain API 签名，避免版本差异导致编译失败
        Object brain = this.getBrain();
        if (brain == null) {
            return;
        }
        try {
            java.lang.reflect.Method setActive =
                    brain.getClass().getMethod("setActiveActivityIfPossible", Activity.class);
            setActive.invoke(brain, targetActivity);
            return;
        } catch (ReflectiveOperationException ignored) {
            // 继续尝试兼容其他签名
        }
        try {
            java.lang.reflect.Method setDefault =
                    brain.getClass().getMethod("setDefaultActivity", Activity.class);
            setDefault.invoke(brain, targetActivity);
        } catch (ReflectiveOperationException ignored) {
            // 若 Brain API 变动，则退化为仅使用本地 activity 状态
        }
    }

    private void tickBehaviorTree(ServerLevel level) {
        Activity nextActivity = resolveBehaviorActivity(level);
        if (nextActivity != this.activeActivity) {
            this.activeActivity = nextActivity;
            syncBrainActivity(nextActivity);
            if (nextActivity != Activity.REST && this.isSleeping()) {
                this.stopSleeping();
            }
        }

        if (this.activeActivity == Activity.REST) {
            return;
        }

        if (shouldRunBudgetDiningBehavior()) {
            handleDiningPurchase(level);
        }
    }

    @Nullable
    private BlockPos normalizeBedHeadPos(ServerLevel level, BlockPos bedPos) {
        BlockState state = level.getBlockState(bedPos);
        if (!(state.getBlock() instanceof BedBlock) || !state.hasProperty(BedBlock.PART)) {
            return null;
        }
        if (state.getValue(BedBlock.PART) == BedPart.HEAD) {
            return bedPos.immutable();
        }
        if (!state.hasProperty(BedBlock.FACING)) {
            return null;
        }
        return bedPos.relative(state.getValue(BedBlock.FACING)).immutable();
    }

    @Nullable
    private BlockPos findBedApproachPos(ServerLevel level, BlockPos bedHeadPos) {
        BlockState headState = level.getBlockState(bedHeadPos);
        if (!(headState.getBlock() instanceof BedBlock)
                || !headState.hasProperty(BedBlock.FACING)
                || !headState.hasProperty(BedBlock.PART)
                || headState.getValue(BedBlock.PART) != BedPart.HEAD) {
            return null;
        }
        net.minecraft.core.Direction facing = headState.getValue(BedBlock.FACING);
        BlockPos footPos = bedHeadPos.relative(facing.getOpposite());
        BlockPos[] candidates =
                new BlockPos[] {
                    footPos.relative(facing.getOpposite()),
                    footPos.relative(facing.getClockWise()),
                    footPos.relative(facing.getCounterClockWise()),
                    bedHeadPos.relative(facing),
                    footPos
                };
        for (BlockPos candidate : candidates) {
            BlockState feetState = level.getBlockState(candidate);
            BlockState headAboveState = level.getBlockState(candidate.above());
            BlockState groundState = level.getBlockState(candidate.below());
            if (!feetState.isSolid() && !headAboveState.isSolid() && groundState.isSolid()) {
                return candidate.immutable();
            }
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            spawnDelay++;

            // 检测是否进入旅社范围并触发登记
            if (this.tickCount % 20 == 0
                    && this.guestData.getState() == GuestData.GuestState.IDLE) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    TeamData team =
                            TeamManager.getInstance()
                                    .getTeamAt(this.blockPosition(), serverLevel.getServer());
                    if (team != null) {
                        // 旅客在旅社范围内，触发进入旅社逻辑
                        team.getInnData().addGuest(this, team, serverLevel);
                        BlockPos bellPos = this.findNearestDeskBellInInn(serverLevel, team);
                        if (bellPos != null) {
                            this.setNavigationTarget(bellPos);
                        } else {
                            this.getNavigation().stop();
                        }
                    }
                }
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                tickBehaviorTree(serverLevel);
            }

            // 同步状态到 SynchedEntityData
            int currentStateOrdinal = this.guestData.getState().ordinal();
            if (this.entityData.get(GUEST_STATE) != currentStateOrdinal) {
                this.entityData.set(GUEST_STATE, currentStateOrdinal);
            }
            if (this.entityData.get(GUEST_BUDGET) != this.budget) {
                this.entityData.set(GUEST_BUDGET, this.budget);
            }

            int packedComfort = packPreference(
                    this.guestData.getComfortPreference().min(),
                    this.guestData.getComfortPreference().max());
            if (this.entityData.get(GUEST_COMFORT_PREF) != packedComfort) {
                this.entityData.set(GUEST_COMFORT_PREF, packedComfort);
            }
            int packedLight = packPreference(
                    this.guestData.getLightPreference().min(),
                    this.guestData.getLightPreference().max());
            if (this.entityData.get(GUEST_LIGHT_PREF) != packedLight) {
                this.entityData.set(GUEST_LIGHT_PREF, packedLight);
            }
            int packedHumidity = packPreference(
                    this.guestData.getHumidityPreference().min(),
                    this.guestData.getHumidityPreference().max());
            if (this.entityData.get(GUEST_HUMIDITY_PREF) != packedHumidity) {
                this.entityData.set(GUEST_HUMIDITY_PREF, packedHumidity);
            }

            // 发光逻辑：等待入住时发光
            if (this.shouldGuestGlow()) {
                if (!this.hasGlowingTag()) {
                    this.setGlowingTag(true);
                }
            } else {
                if (this.hasGlowingTag()) {
                    this.setGlowingTag(false);
                }
            }
        } else {
            // 客户端：从 SynchedEntityData 更新 GuestData 状态
            int syncedStateOrdinal = this.entityData.get(GUEST_STATE);
            if (syncedStateOrdinal >= 0
                    && syncedStateOrdinal < GuestData.GuestState.values().length) {
                GuestData.GuestState syncedState =
                        GuestData.GuestState.values()[syncedStateOrdinal];
                if (this.guestData.getState() != syncedState) {
                    this.guestData.setState(syncedState);
                }
            }
            GuestData.IntRange budgetRange = getBudgetRange();
            this.budget = Mth.clamp(this.entityData.get(GUEST_BUDGET), budgetRange.min(), budgetRange.max());

            int packedComfort = this.entityData.get(GUEST_COMFORT_PREF);
            this.guestData.setComfortPreference(
                    unpackMin(packedComfort), unpackMax(packedComfort));
            int packedLight = this.entityData.get(GUEST_LIGHT_PREF);
            this.guestData.setLightPreference(
                    unpackMin(packedLight), unpackMax(packedLight));
            int packedHumidity = this.entityData.get(GUEST_HUMIDITY_PREF);
            this.guestData.setHumidityPreference(
                    unpackMin(packedHumidity), unpackMax(packedHumidity));

            // 客户端发光逻辑 (虽然 glowing tag 会自动同步，但这里双重保险或用于其他客户端效果)
            // 注意：setGlowingTag 主要由服务端控制，客户端设置可能只在本地生效
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SKIN_VARIANT, 0);
        builder.define(GUEST_STATE, GuestData.GuestState.IDLE.ordinal());
        builder.define(GUEST_BUDGET, getBudgetRange().min());
        builder.define(GUEST_COMFORT_PREF, packPreference(0, 100));
        builder.define(GUEST_LIGHT_PREF, packPreference(0, 100));
        builder.define(GUEST_HUMIDITY_PREF, packPreference(0, 100));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!this.level().isClientSide
                && stack.is(InitItems.BROOM.get())
                && this.guestData.getState() == GuestData.GuestState.WAITING
                && this.level() instanceof ServerLevel serverLevel) {
            TeamData innTeam =
                    TeamManager.getInstance().getTeamAt(this.blockPosition(), serverLevel.getServer());
            if (innTeam == null) {
                return InteractionResult.PASS;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                TeamData playerTeam = TeamManager.getInstance().getPlayerTeam(serverPlayer);
                if (playerTeam == null || !playerTeam.getTeamId().equals(innTeam.getTeamId())) {
                    return InteractionResult.FAIL;
                }
            }
            InnData innData = innTeam.getInnData();
            innData.handleGuestDeparture(this.getUUID(), true, serverLevel, innTeam);
            innData.removeGuest(this.getUUID());
            TeamManager.getInstance().syncTeam(innTeam, serverLevel.getServer());
            return InteractionResult.SUCCESS;
        }
        if (stack.isEmpty()
                && player.isShiftKeyDown()
                && !this.level().isClientSide
                && player.isCreative()) {
            // 调试信息
            player.sendSystemMessage(Component.literal("--- Guest Debug Info ---"));
            player.sendSystemMessage(Component.literal("UUID: " + this.getUUID()));
            player.sendSystemMessage(Component.literal("State: " + this.guestData.getState()));
            player.sendSystemMessage(
                    Component.literal("Synced State: " + this.entityData.get(GUEST_STATE)));
            player.sendSystemMessage(Component.literal("Room ID: " + this.guestData.getRoomId()));

            TeamData team =
                    TeamManager.getInstance()
                            .getTeamAt(this.blockPosition(), this.level().getServer());
            player.sendSystemMessage(Component.literal("In Inn Range: " + (team != null)));
            if (team != null) {
                player.sendSystemMessage(Component.literal("Team ID: " + team.getTeamId()));
            }

            player.sendSystemMessage(
                    Component.literal(
                            "Navigation Target: "
                                    + (this.navigationTarget != null
                                            ? this.navigationTarget.toShortString()
                                            : "null")));
            player.sendSystemMessage(Component.literal("Spawn Delay: " + this.spawnDelay));

            return InteractionResult.SUCCESS;
        }
        InteractionResult fallback = super.mobInteract(player, hand);
        if (fallback.consumesAction()) {
            return fallback;
        }
        if (!this.level().isClientSide
                && hand == InteractionHand.MAIN_HAND
                && stack.isEmpty()
                && !player.isShiftKeyDown()
                && player instanceof ServerPlayer serverPlayer
                && DialogueService.tryStartDialogue(serverPlayer, this)) {
            return InteractionResult.SUCCESS;
        }
        return fallback;
    }

    public int getSkinVariant() {
        return this.entityData.get(SKIN_VARIANT);
    }

    public void setSkinVariant(int variant) {
        this.entityData.set(SKIN_VARIANT, variant);
    }

    protected void setBudget(int budget) {
        GuestData.IntRange budgetRange = getBudgetRange();
        this.budget = Mth.clamp(budget, budgetRange.min(), budgetRange.max());
        if (!this.level().isClientSide) {
            this.entityData.set(GUEST_BUDGET, this.budget);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("SkinVariant", this.getSkinVariant());
        compound.putInt("Budget", this.budget);
        compound.putLong("DiningPlanDay", this.diningPlanDay);
        compound.putInt("DailySpendTarget", this.dailySpendTarget);
        compound.putInt("DailySpentCoins", this.dailySpentCoins);
        compound.putInt("DailyPurchaseTarget", this.dailyPurchaseTarget);
        compound.putInt("DailyPurchaseCount", this.dailyPurchaseCount);
        compound.putInt("DailyPurchaseAttemptCount", this.dailyPurchaseAttemptCount);
        // 将 GuestData 保存到 NBT 中
        CompoundTag guestTag = new CompoundTag();
        this.guestData.save(guestTag);
        compound.put("GuestData", guestTag);
        if (!this.assignedDialogueId.isBlank()) {
            compound.putString(TAG_ASSIGNED_DIALOGUE_ID, this.assignedDialogueId);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.assignedDialogueId = compound.getString(TAG_ASSIGNED_DIALOGUE_ID);
        if (this.assignedDialogueId.isBlank() && !this.level().isClientSide) {
            assignRandomDialogueIfAbsent();
        }
        if (compound.contains("SkinVariant")) {
            this.setSkinVariant(compound.getInt("SkinVariant"));
        }
        if (compound.contains("Budget")) {
            GuestData.IntRange budgetRange = getBudgetRange();
            this.budget =
                    Mth.clamp(compound.getInt("Budget"), budgetRange.min(), budgetRange.max());
        } else {
            this.budget = this.generateInitialBudget();
        }
        this.entityData.set(GUEST_BUDGET, this.budget);
        this.diningPlanDay =
                compound.contains("DiningPlanDay")
                        ? compound.getLong("DiningPlanDay")
                        : Long.MIN_VALUE;
        this.dailySpendTarget = Math.max(0, compound.getInt("DailySpendTarget"));
        this.dailySpentCoins = Math.max(0, compound.getInt("DailySpentCoins"));
        this.dailyPurchaseTarget = Math.max(0, compound.getInt("DailyPurchaseTarget"));
        this.dailyPurchaseCount = Math.max(0, compound.getInt("DailyPurchaseCount"));
        this.dailyPurchaseAttemptCount = Math.max(0, compound.getInt("DailyPurchaseAttemptCount"));
        // 从 NBT 加载 GuestData
        if (compound.contains("GuestData")) {
            CompoundTag guestTag = compound.getCompound("GuestData");
            this.guestData = GuestData.load(guestTag);
            // 初始同步状态
            this.entityData.set(GUEST_STATE, this.guestData.getState().ordinal());
        }
    }

    private int generateInitialBudget() {
        return randomInRange(getBudgetRange());
    }

    @Nullable
    public String getAssignedDialogueId() {
        return this.assignedDialogueId.isBlank() ? null : this.assignedDialogueId;
    }

    private void assignRandomDialogueIfAbsent() {
        if (!this.assignedDialogueId.isBlank()) {
            return;
        }
        String picked = GuestDialogueRandomPool.pick(this);
        if (picked != null) {
            this.assignedDialogueId = picked;
        }
    }

    private int getBudgetBasedDiningInterval() {
        float factor = Mth.clamp(18.0f / (float) this.budget, 0.45f, 1.9f);
        return Math.max(1200, (int) (DINING_AVERAGE_INTERVAL * factor));
    }

    private int getBudgetBasedRetryInterval() {
        int base = Math.max(300, this.getBudgetBasedDiningInterval() / 4);
        return base + this.getRandom().nextInt(base + 1);
    }

    private int getBudgetBasedDesiredSpend() {
        int minSpend = Math.max(1, this.budget / 3);
        int variable = Math.max(1, this.budget - minSpend + 1);
        return minSpend + this.getRandom().nextInt(variable);
    }

    private boolean shouldAttemptDiningPurchaseNow() {
        if (this.dailyPurchaseAttemptCount < MIN_DAILY_DINING_ATTEMPTS
                && !this.hasReachedDailyDiningTargets()) {
            return true;
        }
        float normalized =
                (this.budget - getBudgetRange().min())
                        / (float) Math.max(1, getBudgetRange().max() - getBudgetRange().min());
        float spendProgress =
                this.dailySpendTarget <= 0
                        ? 0.0f
                        : Mth.clamp(
                                this.dailySpentCoins / (float) this.dailySpendTarget, 0.0f, 1.0f);
        float remainingIntent = 1.0f - spendProgress;
        float desire = (0.3f + normalized * 0.7f) * (0.35f + remainingIntent * 0.65f);
        return this.getRandom().nextFloat() < desire;
    }

    private void ensureDailyDiningPlan(ServerLevel level) {
        long currentDay = level.getGameTime() / 24000L;
        if (this.diningPlanDay == currentDay) {
            return;
        }
        this.diningPlanDay = currentDay;
        this.dailySpentCoins = 0;
        this.dailyPurchaseCount = 0;
        this.dailyPurchaseAttemptCount = 0;

        float spendRatio = (float) (0.7 + this.getRandom().nextGaussian() * 0.15);
        spendRatio = Mth.clamp(spendRatio, 0.25f, 1.0f);
        this.dailySpendTarget = Math.max(1, Math.round(this.budget * spendRatio));

        float normalized =
                (this.budget - getBudgetRange().min())
                        / (float) Math.max(1, getBudgetRange().max() - getBudgetRange().min());
        float meanCount = 1.2f + normalized * 3.2f;
        int sampledCount =
                Math.max(
                        1,
                        Math.round((float) (meanCount + this.getRandom().nextGaussian() * 0.9f)));
        this.dailyPurchaseTarget = Math.max(1, Math.min(this.dailySpendTarget, sampledCount));
    }

    private boolean hasReachedDailyDiningTargets() {
        if (this.dailyPurchaseAttemptCount < MIN_DAILY_DINING_ATTEMPTS) {
            return false;
        }
        return this.dailySpentCoins >= this.dailySpendTarget
                || this.dailyPurchaseCount >= this.dailyPurchaseTarget;
    }

    private int getInitialDiningAttemptDelay() {
        float normalized =
                (this.budget - getBudgetRange().min())
                        / (float) Math.max(1, getBudgetRange().max() - getBudgetRange().min());
        int base = Mth.floor(Mth.lerp(1.0f - normalized, 200.0f, 900.0f));
        return base + this.getRandom().nextInt(161);
    }

    protected boolean shouldRunBudgetDiningBehavior() {
        return true;
    }

    protected boolean shouldGuestGlow() {
        return this.guestData.getState() == GuestData.GuestState.WAITING;
    }

    public ItemStack getHeadDisplayItem() {
        return ItemStack.EMPTY;
    }

    public boolean canCheckOutNow(long currentTime) {
        return true;
    }

    private void applyPreferenceRange(
            PreferenceRangeProfile profile, BiConsumer<Integer, Integer> rangeSetter) {
        GuestData.IntRange minRange = normalizeRange(profile.minRange(), 0, 100);
        GuestData.IntRange maxRange = normalizeRange(profile.maxRange(), 0, 100);
        int min = randomInRange(minRange);
        int max = randomInRange(maxRange);
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        }
        rangeSetter.accept(min, max);
    }

    private static int packPreference(int min, int max) {
        return (min & 0xFF) | ((max & 0xFF) << 8);
    }

    private static int unpackMin(int packed) {
        return packed & 0xFF;
    }

    private static int unpackMax(int packed) {
        return (packed >> 8) & 0xFF;
    }

    private GuestData.IntRange getBudgetRange() {
        return normalizeRange(this.getGuestProfile().budgetRange(), 1, Integer.MAX_VALUE);
    }

    private GuestData.IntRange normalizeRange(GuestData.IntRange range, int floor, int ceiling) {
        int normalizedMin = Mth.clamp(range.min(), floor, ceiling);
        int normalizedMax = Mth.clamp(range.max(), floor, ceiling);
        if (normalizedMin > normalizedMax) {
            int temp = normalizedMin;
            normalizedMin = normalizedMax;
            normalizedMax = temp;
        }
        return new GuestData.IntRange(normalizedMin, normalizedMax);
    }

    private int randomInRange(GuestData.IntRange range) {
        if (range.min() >= range.max()) {
            return range.min();
        }
        return range.min() + this.getRandom().nextInt(range.max() - range.min() + 1);
    }

    private static final class GuestDialogueRandomPool {
        private static final Map<Class<? extends GuestEntity>, List<String>> DIALOGUES_BY_GUEST_CLASS =
                Map.of(
                        OrdinaryGuestEntity.class,
                        List.of(
                                "guest_ordinary_welcome",
                                "guest_ordinary_weather",
                                "guest_ordinary_checkout"),
                        RichGuestEntity.class,
                        List.of("guest_rich_service", "guest_rich_wine", "guest_rich_tip"),
                        HeavyPackGuestEntity.class,
                        List.of(
                                "guest_heavy_pack_route",
                                "guest_heavy_pack_storage",
                                "guest_heavy_pack_food"),
                        UltraRichGuestEntity.class,
                        List.of(
                                "guest_ultra_rich_suite",
                                "guest_ultra_rich_privacy",
                                "guest_ultra_rich_guard"),
                        OrdinaryVipGuestEntity.class,
                        List.of(
                                "guest_vip_ordinary_schedule",
                                "guest_vip_ordinary_tea",
                                "guest_vip_ordinary_review"),
                        AdvancedVipGuestEntity.class,
                        List.of(
                                "guest_vip_advanced_security",
                                "guest_vip_advanced_order",
                                "guest_vip_advanced_reward"),
                        SponsorGuestEntity.class,
                        List.of(
                                "guest_sponsor_photo",
                                "guest_sponsor_renovation",
                                "guest_sponsor_support"));

        @Nullable
        private static String pick(GuestEntity guest) {
            List<String> pool = DIALOGUES_BY_GUEST_CLASS.get(guest.getClass());
            if (pool == null || pool.isEmpty()) {
                return null;
            }
            return pool.get(guest.getRandom().nextInt(pool.size()));
        }
    }

    public record GuestProfile(
            PreferenceRangeProfile comfortRange,
            PreferenceRangeProfile lightRange,
            PreferenceRangeProfile humidityRange,
            GuestData.IntRange budgetRange,
            double reputationMultiplier) {}

    public record PreferenceRangeProfile(GuestData.IntRange minRange, GuestData.IntRange maxRange) {}

    @Nullable
    private BlockPos findNearestDeskBellInInn(ServerLevel level, TeamData team) {
        BlockPos origin = this.blockPosition();
        int nearMinY = Math.max(level.getMinBuildHeight(), origin.getY() - 16);
        int nearMaxY = Math.min(level.getMaxBuildHeight() - 1, origin.getY() + 16);
        BlockPos near = this.findNearestDeskBellInInn(level, team, nearMinY, nearMaxY);
        if (near != null) {
            return near;
        }
        return this.findNearestDeskBellInInn(
                level, team, level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
    }

    @Nullable
    private BlockPos findNearestDeskBellInInn(
            ServerLevel level, TeamData team, int minY, int maxY) {
        BlockPos origin = this.blockPosition();
        BlockPos[] bestPos = new BlockPos[1];
        double[] bestDist = new double[] {Double.MAX_VALUE};
        int minChunkX = Integer.MAX_VALUE;
        int maxChunkX = Integer.MIN_VALUE;
        int minChunkZ = Integer.MAX_VALUE;
        int maxChunkZ = Integer.MIN_VALUE;

        for (TeamData.InnRegion region : team.getInnRegions()) {
            minChunkX = Math.min(minChunkX, region.minX() >> 4);
            maxChunkX = Math.max(maxChunkX, region.maxX() >> 4);
            minChunkZ = Math.min(minChunkZ, region.minZ() >> 4);
            maxChunkZ = Math.max(maxChunkZ, region.maxZ() >> 4);
        }

        if (minChunkX == Integer.MAX_VALUE) {
            return null;
        }

        BlockEntitySearchUtils.forEachInChunkRange(
                level,
                minChunkX,
                maxChunkX,
                minChunkZ,
                maxChunkZ,
                blockEntity -> {
                    if (!(blockEntity instanceof DeskBellBlockEntity)) {
                        return;
                    }
                    BlockPos pos = blockEntity.getBlockPos();
                    int y = pos.getY();
                    if (y < minY || y > maxY) {
                        return;
                    }
                    if (!TeamData.isInGlobalMaxInnZone(pos)) {
                        return;
                    }
                    double dist = origin.distSqr(pos);
                    if (dist < bestDist[0]) {
                        bestDist[0] = dist;
                        bestPos[0] = pos.immutable();
                    }
                });
        return bestPos[0];
    }
}
