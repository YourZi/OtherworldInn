package com.otherworldinn.entity.base;

import com.otherworldinn.world.economy.service.ItemSellPriceManager;
import com.otherworldinn.world.inn.GuestData;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.InnTodo;
import com.otherworldinn.util.AdvancementUtils;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;

public abstract class VipGuestEntity extends GuestEntity {
    private static final EntityDataAccessor<ItemStack> ORDERED_MEAL_ITEM =
            SynchedEntityData.defineId(VipGuestEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final int VIP_MIN_ORDER_TIMES = 1;
    private static final int VIP_MAX_ORDER_TIMES = 3;
    private static final int VIP_WAITING_TIMEOUT_TICKS = 6000;
    private static final int VIP_CHECKOUT_GRACE_TICKS = 200;
    private static final String VIP_GLOW_TEAM = "otherworldinn_vip_blue";
    private static final String TAG_ORDER_COUNT = "VipOrderCount";
    private static final String TAG_NEXT_ORDER_TIME = "VipNextOrderTime";
    private static final String TAG_WAITING = "VipWaiting";
    private static final String TAG_DEADLINE = "VipDeadline";
    private static final String TAG_PENDING_ITEM = "VipPendingItem";
    private static final String TAG_PENDING_TODO = "VipPendingTodo";
    private static final String TAG_PENDING_TODO_TEAM = "VipPendingTodoTeam";
    private static final String TAG_CHECKOUT_DEFERRED = "VipCheckoutDeferred";
    private static final String TAG_DEFERRED_CHECKOUT_TIME = "VipDeferredCheckoutTime";
    private static final String VIP_MEAL_TODO_KEY = "todo.otherworldinn.vip_meal_order";
    private static final String VIP_MEAL_TODO_ID_PREFIX = "vip_meal_order:";
    private int vipOrderCount = 0;
    private long vipNextOrderTime = 0L;
    private boolean vipWaitingForMeal = false;
    private long vipWaitingDeadline = 0L;
    private ResourceLocation vipPendingItemId = null;
    private String vipPendingTodoText = "";
    private UUID vipPendingTodoTeamId = null;
    private boolean vipCheckoutDeferred = false;
    private long vipDeferredCheckoutTime = 0L;
    private boolean wasCheckedIn = false;

    protected VipGuestEntity(EntityType<? extends PathfinderMob> type, net.minecraft.world.level.Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ORDERED_MEAL_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected boolean shouldRunBudgetDiningBehavior() {
        return false;
    }

    @Override
    protected boolean shouldGuestGlow() {
        return super.shouldGuestGlow() || this.vipWaitingForMeal;
    }

    @Override
    public ItemStack getHeadDisplayItem() {
        return this.entityData.get(ORDERED_MEAL_ITEM);
    }

    @Override
    public int getBudget() {
        return 0;
    }

    @Override
    public boolean canCheckOutNow(long currentTime) {
        if (!this.vipCheckoutDeferred) {
            if (this.vipWaitingForMeal && currentTime >= this.getGuestData().getCheckoutTime()) {
                this.vipCheckoutDeferred = true;
                return false;
            }
            return true;
        }
        if (this.vipWaitingForMeal) {
            return false;
        }
        if (this.vipDeferredCheckoutTime <= 0L) {
            this.vipDeferredCheckoutTime = currentTime + VIP_CHECKOUT_GRACE_TICKS;
            return false;
        }
        return currentTime >= this.vipDeferredCheckoutTime;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        GuestData.GuestState state = this.getGuestData().getState();
        boolean checkedIn = state == GuestData.GuestState.CHECKED_IN;
        if (!checkedIn) {
            this.wasCheckedIn = false;
            this.vipCheckoutDeferred = false;
            this.vipDeferredCheckoutTime = 0L;
            if (this.vipWaitingForMeal) {
                clearVipWaitingState(level, true);
            }
            applyBlueGlowTeam(level, false);
            return;
        }
        if (!this.wasCheckedIn) {
            this.wasCheckedIn = true;
            if (this.vipOrderCount < VIP_MIN_ORDER_TIMES) {
                this.vipNextOrderTime = level.getGameTime() + 200L;
            }
        }
        if (this.vipWaitingForMeal) {
            if (level.getGameTime() >= this.vipWaitingDeadline) {
                handleVipOrderTimeout(level);
            }
            applyBlueGlowTeam(level, true);
            return;
        }
        applyBlueGlowTeam(level, false);
        if (this.vipOrderCount >= VIP_MAX_ORDER_TIMES || level.getGameTime() < this.vipNextOrderTime) {
            return;
        }
        if (this.vipOrderCount == 0 || this.getRandom().nextFloat() < 0.35F) {
            startVipMealOrder(level);
        } else {
            scheduleNextVipOrder(level);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide
                && this.vipWaitingForMeal
                && this.getGuestData().getState() == GuestData.GuestState.CHECKED_IN
                && this.level() instanceof ServerLevel level) {
            ItemStack held = player.getItemInHand(hand);
            if (matchesOrderedMeal(held)) {
                TeamData team = resolveInnTeam(level);
                if (team == null) {
                    return InteractionResult.PASS;
                }
                int basePrice = ItemSellPriceManager.getConfiguredPrice(this.vipPendingItemId);
                if (basePrice <= 0) {
                    return InteractionResult.PASS;
                }
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                int basePayout = basePrice * 2;
                int reputationGain = 10 + this.getRandom().nextInt(21);
                InnData inn = team.getInnData();
                int payout = inn.calculateDiningIncomeAmount(basePayout);
                TeamManager.getInstance().addCoins(team, payout, level.getServer());
                inn.recordDiningIncome(payout, level);
                inn.addReputation(inn.scaleGuestReputationDelta(this, reputationGain));
                TeamManager.getInstance().syncTeam(team, level.getServer());
                level.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        this.getX(),
                        this.getY() + this.getBbHeight() + 0.6D,
                        this.getZ(),
                        12,
                        0.35D,
                        0.25D,
                        0.35D,
                        0.02D);
                level.playSound(
                        null, this.blockPosition(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.1F);
                if (player instanceof ServerPlayer serverPlayer) {
                    AdvancementUtils.award(serverPlayer, AdvancementUtils.SERVE_ONE_VIP);
                }
                clearVipWaitingState(level, true);
                scheduleNextVipOrder(level);
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, hand);
    }

    private void startVipMealOrder(ServerLevel level) {
        TeamData team = resolveInnTeam(level);
        if (team == null) {
            scheduleNextVipOrder(level);
            return;
        }
        ResourceLocation selected = pickVipMealItem(team);
        if (selected == null) {
            scheduleNextVipOrder(level);
            return;
        }
        Item item = BuiltInRegistries.ITEM.get(selected);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            scheduleNextVipOrder(level);
            return;
        }
        ItemStack mealStack = new ItemStack(item);
        this.vipPendingItemId = selected;
        this.entityData.set(ORDERED_MEAL_ITEM, mealStack.copy());
        this.vipWaitingForMeal = true;
        this.vipWaitingDeadline = level.getGameTime() + VIP_WAITING_TIMEOUT_TICKS;
        this.vipOrderCount++;
        this.vipPendingTodoTeamId = team.getTeamId();
        InnTodo todo = createVipMealTodo(mealStack);
        this.vipPendingTodoText = todo.fallbackText();
        team.getInnData().addTodo(level, team, todo);
    }

    private void handleVipOrderTimeout(ServerLevel level) {
        TeamData team = resolveInnTeam(level);
        if (team != null) {
            int reputationLoss = 10 + this.getRandom().nextInt(11);
            InnData inn = team.getInnData();
            inn.addReputation(inn.scaleGuestReputationDelta(this, -reputationLoss));
            TeamManager.getInstance().syncTeam(team, level.getServer());
        }
        level.sendParticles(
                ParticleTypes.ANGRY_VILLAGER,
                this.getX(),
                this.getY() + this.getBbHeight() + 0.6D,
                this.getZ(),
                10,
                0.4D,
                0.3D,
                0.4D,
                0.01D);
        level.playSound(
                null, this.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.NEUTRAL, 1.0F, 0.85F);
        clearVipWaitingState(level, true);
        scheduleNextVipOrder(level);
    }

    private void clearVipWaitingState(ServerLevel level, boolean removeTodo) {
        if (removeTodo && !this.vipPendingTodoText.isEmpty()) {
            removeVipTodo(level);
        }
        this.vipWaitingForMeal = false;
        this.vipWaitingDeadline = 0L;
        this.vipPendingItemId = null;
        this.vipPendingTodoText = "";
        this.vipPendingTodoTeamId = null;
        if (this.vipCheckoutDeferred && this.vipDeferredCheckoutTime <= 0L) {
            this.vipDeferredCheckoutTime = level.getDayTime() + VIP_CHECKOUT_GRACE_TICKS;
        }
        this.entityData.set(ORDERED_MEAL_ITEM, ItemStack.EMPTY);
        applyBlueGlowTeam(level, false);
    }

    private void removeVipTodo(ServerLevel level) {
        TeamManager manager = TeamManager.getInstance();
        if (this.vipPendingTodoTeamId != null) {
            TeamData exactTeam = manager.getData(level.getServer()).getTeams().get(this.vipPendingTodoTeamId);
            if (exactTeam != null) {
                exactTeam.getInnData().removeTodoByIdOrText(
                        level, exactTeam, vipMealTodoId(), this.vipPendingTodoText);
                return;
            }
        }
        TeamData team = resolveInnTeam(level);
        if (team != null) {
            team.getInnData().removeTodoByIdOrText(level, team, vipMealTodoId(), this.vipPendingTodoText);
            return;
        }
        for (TeamData fallbackTeam : manager.getData(level.getServer()).getTeams().values()) {
            fallbackTeam.getInnData().removeTodoByIdOrText(
                    level, fallbackTeam, vipMealTodoId(), this.vipPendingTodoText);
        }
    }

    private String vipMealTodoId() {
        return VIP_MEAL_TODO_ID_PREFIX + getUUID();
    }

    private InnTodo createVipMealTodo(ItemStack mealStack) {
        return InnTodo.translatable(vipMealTodoId(), VIP_MEAL_TODO_KEY, this.getName(), mealStack.getHoverName());
    }

    private void scheduleNextVipOrder(ServerLevel level) {
        if (this.vipOrderCount >= VIP_MAX_ORDER_TIMES) {
            this.vipNextOrderTime = Long.MAX_VALUE;
            return;
        }
        this.vipNextOrderTime = level.getGameTime() + 1200L + this.getRandom().nextInt(3601);
    }

    protected int getVipMealMinPriceExclusive() {
        return 20;
    }

    private ResourceLocation pickVipMealItem(TeamData team) {
        List<ResourceLocation> all = ItemSellPriceManager.getConfiguredItemsAbovePrice(
                getVipMealMinPriceExclusive() - 1);
        List<ResourceLocation> candidates = new ArrayList<>(all);
        if (candidates.isEmpty()) {
            candidates = ItemSellPriceManager.getTopPricedItems(5);
            candidates.removeIf(id -> "kaleidoscope_tavern".equals(id.getNamespace()));
        }
        if (candidates.isEmpty()) return null;
        return candidates.get(this.getRandom().nextInt(candidates.size()));
    }

    private boolean matchesOrderedMeal(ItemStack stack) {
        if (stack.isEmpty() || this.vipPendingItemId == null) {
            return false;
        }
        ResourceLocation heldId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return this.vipPendingItemId.equals(heldId);
    }

    private TeamData resolveInnTeam(ServerLevel level) {
        TeamData byPosition = TeamManager.getInstance().getTeamAt(this.blockPosition(), level.getServer());
        if (byPosition != null) {
            return byPosition;
        }
        return TeamManager.getInstance().getNearestInn(this.blockPosition(), level.getServer());
    }

    private void applyBlueGlowTeam(ServerLevel level, boolean enable) {
        ServerScoreboard scoreboard = level.getServer().getScoreboard();
        String playerKey = this.getStringUUID();
        if (!enable) {
            scoreboard.removePlayerFromTeam(playerKey);
            return;
        }
        PlayerTeam team = scoreboard.getPlayerTeam(VIP_GLOW_TEAM);
        if (team == null) {
            team = scoreboard.addPlayerTeam(VIP_GLOW_TEAM);
        }
        team.setColor(ChatFormatting.BLUE);
        scoreboard.addPlayerToTeam(playerKey, team);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(TAG_ORDER_COUNT, this.vipOrderCount);
        compound.putLong(TAG_NEXT_ORDER_TIME, this.vipNextOrderTime);
        compound.putBoolean(TAG_WAITING, this.vipWaitingForMeal);
        compound.putLong(TAG_DEADLINE, this.vipWaitingDeadline);
        if (this.vipPendingItemId != null) {
            compound.putString(TAG_PENDING_ITEM, this.vipPendingItemId.toString());
        }
        if (!this.vipPendingTodoText.isEmpty()) {
            compound.putString(TAG_PENDING_TODO, this.vipPendingTodoText);
        }
        if (this.vipPendingTodoTeamId != null) {
            compound.putUUID(TAG_PENDING_TODO_TEAM, this.vipPendingTodoTeamId);
        }
        compound.putBoolean(TAG_CHECKOUT_DEFERRED, this.vipCheckoutDeferred);
        compound.putLong(TAG_DEFERRED_CHECKOUT_TIME, this.vipDeferredCheckoutTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.vipOrderCount = Math.max(0, compound.getInt(TAG_ORDER_COUNT));
        this.vipNextOrderTime = compound.getLong(TAG_NEXT_ORDER_TIME);
        this.vipWaitingForMeal = compound.getBoolean(TAG_WAITING);
        this.vipWaitingDeadline = compound.getLong(TAG_DEADLINE);
        this.vipPendingTodoText = compound.getString(TAG_PENDING_TODO);
        this.vipPendingTodoTeamId =
                compound.hasUUID(TAG_PENDING_TODO_TEAM)
                        ? compound.getUUID(TAG_PENDING_TODO_TEAM)
                        : null;
        this.vipCheckoutDeferred = compound.getBoolean(TAG_CHECKOUT_DEFERRED);
        this.vipDeferredCheckoutTime = compound.getLong(TAG_DEFERRED_CHECKOUT_TIME);
        if (compound.contains(TAG_PENDING_ITEM)) {
            this.vipPendingItemId = ResourceLocation.tryParse(compound.getString(TAG_PENDING_ITEM));
        } else {
            this.vipPendingItemId = null;
        }
        if (this.vipPendingItemId != null) {
            Item item = BuiltInRegistries.ITEM.get(this.vipPendingItemId);
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                this.entityData.set(ORDERED_MEAL_ITEM, new ItemStack(item));
            } else {
                this.entityData.set(ORDERED_MEAL_ITEM, ItemStack.EMPTY);
            }
        } else {
            this.entityData.set(ORDERED_MEAL_ITEM, ItemStack.EMPTY);
        }
    }
}
