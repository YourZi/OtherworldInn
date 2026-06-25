package com.otherworldinn.entity.base;

import com.otherworldinn.world.dialogue.DialogueService;
import com.otherworldinn.world.inventory.StoreMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 商店实体抽象父类
 *
 * <p>特性： 1. 右键打开商店界面 (子类实现) 2. 原地不动，无 AI 3. 不受重力影响 4. 仅允许创造模式攻击和虚空伤害生效 5. 无受伤变红效果 6.
 * 持续播放循环动画 (客户端逻辑) 7. 存储商品列表 (物品、数量、售价)
 */
public abstract class StoreEntity extends PathfinderMob {
    private static final int MAX_FAVOR_LEVEL = 10;
    private static final int COINS_PER_FAVOR_LEVEL = 200;
    private static final double MAX_LEVEL_DISCOUNT_RATE = 0.7D;

    public static int getMaxFavorLevelValue() {
        return MAX_FAVOR_LEVEL;
    }

    public static int getCoinsPerFavorLevelValue() {
        return COINS_PER_FAVOR_LEVEL;
    }

    protected int getCoinsPerFavorLevel() {
        return COINS_PER_FAVOR_LEVEL;
    }

    public int getFavorCoinsPerLevelValue() {
        return this.getCoinsPerFavorLevel();
    }

    public static int getDiscountedPriceForFavorLevel(int basePrice, int favorLevel) {
        if (favorLevel >= MAX_FAVOR_LEVEL) {
            return Math.max(1, (int) Math.floor(basePrice * MAX_LEVEL_DISCOUNT_RATE));
        }
        return basePrice;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D) // 不动
                .add(Attributes.KNOCKBACK_RESISTANCE, 25565.0D); // 抗击退
    }

    /** 商品列表 (合并了固定商品和随机商品) */
    protected final List<StoreItem> storeItems = new ArrayList<>();

    /**
     * 固定商品起始索引
     *
     * <p>在刷新库存时，保留索引在此之前的商品，移除之后的随机商品并重新生成。
     */
    protected int fixedItemsCount = 0;

    /** 上次进货的日期 (DayTime / 24000) */
    private long lastRestockDay = 0;

    private int totalSpentCoins = 0;
    private int favorLevel = 1;

    /** 子类设为 true 可禁止每日自动补货 — 用于由外部管理器控制生命周期的实体（如游商） */
    protected boolean suppressAutoRestock = false;
    private final List<FavorStoreItemData> favorStoreItems = new ArrayList<>();
    private boolean positionLockInitialized = false;
    private double lockedX;
    private double lockedY;
    private double lockedZ;

    /**
     * 待机/循环动画状态
     *
     * <p>子类模型需要使用此状态来播放动画。
     */
    public final AnimationState idleAnimationState = new AnimationState();

    protected StoreEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setNoAi(true); // 无 AI (虽然 setNoAi 已经处理了大部分逻辑，但 PathfinderMob 还是会有寻路相关的初始化)
        this.setNoGravity(true); // 无重力
        this.setPersistenceRequired(); // 防止自然消失
    }

    @Override
    public boolean requiresCustomPersistence() {
        // 强制持久化，避免被常规生物清理流程移除
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // 无论玩家距离多远都不允许自动卸载
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        // 显式声明商店实体应写入区块，避免因普通实体保存判定导致区块卸载后丢失。
        return true;
    }

    @Override
    public void tick() {
        // 客户端动画逻辑
        if (this.level().isClientSide) {
            // 确保持续播放待机动画
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.enforceLockedPosition();
            if (!this.suppressAutoRestock) {
                long currentDay = this.level().getDayTime() / 24000L;
                if (currentDay != this.lastRestockDay) {
                    this.restockAll();
                    this.lastRestockDay = currentDay;
                }
            }
        }
        super.tick();
    }

    // --- 交互逻辑 ---

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        // 只有主手交互生效，防止触发两次
        if (hand == InteractionHand.MAIN_HAND) {
            if (!this.level().isClientSide) {
                if (player instanceof ServerPlayer serverPlayer
                        && DialogueService.tryStartDialogue(serverPlayer, this)) {
                    return InteractionResult.SUCCESS;
                }
                // 兜底：未匹配到对话定义时直接打开商店
                this.openStoreScreen(player);
            }
            // 客户端返回 SUCCESS 播放交互动作 (挥手)
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * 打开商店界面
     *
     * <p>默认实现尝试打开 MenuProvider。 子类可以覆盖此方法以自定义打开逻辑。
     *
     * @param player 交互的玩家
     */
    protected void openStoreScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            MenuProvider menuProvider =
                    new SimpleMenuProvider(
                            (id, inventory, p) -> new StoreMenu(id, inventory, this),
                            this.getDisplayName());
            serverPlayer.openMenu(
                    menuProvider,
                    (buf) -> {
                        buf.writeInt(this.getId()); // 传递实体 ID 以便客户端获取实体
                        buf.writeResourceLocation(
                                BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()));
                        buf.writeInt(this.favorLevel);
                        buf.writeInt(this.totalSpentCoins);
                        buf.writeInt(this.getCoinsPerFavorLevel());

                        // 序列化商品列表
                        buf.writeInt(this.storeItems.size());
                        for (StoreItem item : this.storeItems) {
                            buf.writeNbt(item.saveForNetwork(this.registryAccess(), serverPlayer, this));
                        }
                    });
        }
    }

    public void openStoreForPlayer(Player player) {
        this.openStoreScreen(player);
    }

    public void playOpenStoreSound() {
        if (this.level().isClientSide) {
            return;
        }
        SoundEvent openSound = this.getOpenStoreSound();
        if (openSound != null) {
            this.level()
                    .playSound(
                            null,
                            this.blockPosition(),
                            openSound,
                            this.getSoundSource(),
                            1.0F,
                            1.0F);
        }
    }

    /**
     * 获取商店背景纹理
     *
     * <p>子类可以覆盖此方法以自定义背景。 默认为 "textures/gui/store.png"
     *
     * @return 背景纹理 ResourceLocation
     */
    public ResourceLocation getStoreBackground() {
        return ResourceLocation.fromNamespaceAndPath("otherworldinn", "textures/gui/store.png");
    }

    @Nullable
    protected SoundEvent getOpenStoreSound() {
        return SoundEvents.UI_BUTTON_CLICK.value();
    }

    // --- 免疫与物理逻辑 ---

    @Override
    public boolean isPushable() {
        return false; // 不可被推动
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public void knockback(double strength, double x, double z) {
    }

    @Override
    public void push(double x, double y, double z) {
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.isCreativePlayer() || source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return super.hurt(source, amount);
        }

        return false;
    }

    @Override
    protected void pushEntities() {
        // 不推动其他实体
    }

    // 覆盖此方法以确保无重力效果生效，并防止因受击导致的位移
    @Override
    public void travel(net.minecraft.world.phys.Vec3 travelVector) {
        if (this.isNoGravity()) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.travel(travelVector);
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
    }

    //传送到指定位置并同步刷新位置锁，避免下一 tick 被旧锁定坐标拉回
    public void moveToAndLockPosition(double x, double y, double z, float yRot, float xRot) {
        this.positionLockInitialized = true;
        this.lockedX = x;
        this.lockedY = y;
        this.lockedZ = z;
        this.moveTo(x, y, z, yRot, xRot);
    }

    private void enforceLockedPosition() {
        if (!this.positionLockInitialized) {
            this.positionLockInitialized = true;
            this.lockedX = this.getX();
            this.lockedY = this.getY();
            this.lockedZ = this.getZ();
        }
        if (this.getX() != this.lockedX || this.getY() != this.lockedY || this.getZ() != this.lockedZ) {
            this.setPos(this.lockedX, this.lockedY, this.lockedZ);
        }
        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = false;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 2) {
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    protected @Nullable net.minecraft.sounds.SoundEvent getDeathSound() {
        return null; // 无死亡音效
    }

    // --- 商品管理 ---

    /**
     * 添加固定商品 (通过 ResourceLocation)
     *
     * @param itemId 物品 ID (例如 "minecraft:apple" 或 "create:zinc_ingot")
     * @param price 价格
     * @param maxStock 最大库存
     */
    public void addStoreItem(String itemId, int price, int maxStock) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl != null) {
            BuiltInRegistries.ITEM
                    .getOptional(rl)
                    .ifPresent(item -> this.addStoreItem(new ItemStack(item), price, maxStock));
        }
    }

    /**
     * 添加固定商品 (通过 ResourceLocation, 带修改器)
     *
     * @param itemId 物品 ID
     * @param price 价格
     * @param maxStock 最大库存
     * @param modifier 修改器
     */
    public void addStoreItem(String itemId, int price, int maxStock, Consumer<ItemStack> modifier) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl != null) {
            BuiltInRegistries.ITEM
                    .getOptional(rl)
                    .ifPresent(
                            item ->
                                    this.addStoreItem(
                                            new ItemStack(item), price, maxStock, modifier));
        }
    }

    /**
     * 添加固定商品
     *
     * <p>这些商品在刷新时不会被移除，只会补充库存。
     *
     * @param item 物品
     * @param price 价格
     * @param maxStock 最大库存 (-1 表示无限)
     */
    public void addStoreItem(ItemStack item, int price, int maxStock) {
        this.storeItems.add(new StoreItem(item, price, maxStock));
        this.fixedItemsCount = this.storeItems.size(); // 更新固定商品数量
    }

    public void addLimitedStoreItem(String itemId, int price, int maxStock) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl != null) {
            BuiltInRegistries.ITEM
                    .getOptional(rl)
                    .ifPresent(
                            item -> this.addLimitedStoreItem(new ItemStack(item), price, maxStock));
        }
    }

    public void addLimitedStoreItem(
            String itemId, int price, int maxStock, Consumer<ItemStack> modifier) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl != null) {
            BuiltInRegistries.ITEM
                    .getOptional(rl)
                    .ifPresent(
                            item ->
                                    this.addLimitedStoreItem(
                                            new ItemStack(item), price, maxStock, modifier));
        }
    }

    public void addLimitedStoreItem(ItemStack item, int price, int maxStock) {
        this.storeItems.add(new StoreItem(item, price, maxStock, maxStock, 1, false));
        this.fixedItemsCount = this.storeItems.size();
    }

    public void addLimitedStoreItem(
            ItemStack item, int price, int maxStock, Consumer<ItemStack> modifier) {
        ItemStack copy = item.copy();
        if (modifier != null) {
            modifier.accept(copy);
        }
        this.addLimitedStoreItem(copy, price, maxStock);
    }

    /**
     * 添加固定商品 (带自定义设置)
     *
     * @param item 物品
     * @param price 价格
     * @param maxStock 最大库存
     * @param modifier 对物品栈的自定义修改操作 (例如设置耐久、附魔等)
     */
    public void addStoreItem(
            ItemStack item, int price, int maxStock, Consumer<ItemStack> modifier) {
        ItemStack copy = item.copy();
        if (modifier != null) {
            modifier.accept(copy);
        }
        this.addStoreItem(copy, price, maxStock);
    }

    public int getFavorLevel() {
        return this.favorLevel;
    }

    public int getTotalSpentCoins() {
        return this.totalSpentCoins;
    }

    public boolean canPurchase(StoreItem item) {
        return item.getRequiredFavorLevel() <= this.favorLevel;
    }

    public boolean canPurchase(Player player, StoreItem item) {
        if (!canPurchase(item)) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            return isProgressRequirementMet(serverPlayer, item);
        }
        return true;
    }

    public boolean isProgressRequirementMet(ServerPlayer player, StoreItem item) {
        String advancementId = item.getRequiredAdvancementId();
        if (advancementId == null || advancementId.isBlank()) {
            return true;
        }
        ResourceLocation id = ResourceLocation.tryParse(advancementId);
        if (id == null) {
            return true;
        }
        AdvancementHolder advancement = player.server.getAdvancements().get(id);
        if (advancement == null) {
            return true;
        }
        return player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public int getPurchasePrice(StoreItem item) {
        return getDiscountedPriceForFavorLevel(item.getPrice(), this.favorLevel);
    }

    /**
     * 增加商店好感进度（公共入口，可由外部系统调用）。
     *
     * <p>当前好感进度与历史消费共享同一计量单位，因此该方法会同步累加 totalSpentCoins。
     *
     * @param favorProgress 要增加的进度值，<= 0 时忽略
     */
    public void addFavorProgress(int favorProgress) {
        if (favorProgress <= 0) {
            return;
        }
        this.totalSpentCoins += favorProgress;
        int newLevel = this.calculateFavorLevel(this.totalSpentCoins);
        if (newLevel != this.favorLevel) {
            this.favorLevel = newLevel;
            this.unlockFavorStoreItems();
        }
    }

    /**
     * 增加消费累计（兼容旧调用）。
     *
     * <p>消费会转换为同等好感进度。
     */
    public void addSpentCoins(int spentCoins) {
        this.addFavorProgress(spentCoins);
    }

    public void addFavorStoreItem(int requiredFavorLevel, ItemStack item, int price, int maxStock) {
        this.addFavorStoreItem(requiredFavorLevel, item, price, maxStock, null);
    }

    public void addFavorStoreItem(
            int requiredFavorLevel,
            ItemStack item,
            int price,
            int maxStock,
            Consumer<ItemStack> modifier) {
        if (requiredFavorLevel < 2 || requiredFavorLevel > MAX_FAVOR_LEVEL) {
            return;
        }
        ItemStack copy = item.copy();
        if (modifier != null) {
            modifier.accept(copy);
        }
        this.favorStoreItems.add(new FavorStoreItemData(requiredFavorLevel, copy, price, maxStock));
        if (this.hasFixedItem(copy, price, maxStock, requiredFavorLevel, null, null)) {
            return;
        }
        int insertIndex = Math.min(this.fixedItemsCount, this.storeItems.size());
        this.storeItems.add(
                insertIndex,
                new StoreItem(copy, price, maxStock, maxStock, requiredFavorLevel, true, null, null));
        this.fixedItemsCount++;
    }

    public void addAchievementsStoreItem(
            ItemStack item,
            int price,
            int maxStock,
            ResourceLocation requiredAdvancementId,
            String requiredAdvancementTitleKey) {
        if (requiredAdvancementId == null) {
            return;
        }
        ItemStack copy = item.copy();
        String advancementId = requiredAdvancementId.toString();
        String titleKey =
                (requiredAdvancementTitleKey == null || requiredAdvancementTitleKey.isBlank())
                        ? buildAdvancementTitleKey(requiredAdvancementId)
                        : requiredAdvancementTitleKey;
        if (this.hasFixedItem(copy, price, maxStock, 1, advancementId, titleKey)) {
            return;
        }
        int insertIndex = Math.min(this.fixedItemsCount, this.storeItems.size());
        this.storeItems.add(
                insertIndex,
                new StoreItem(
                        copy,
                        price,
                        maxStock,
                        maxStock,
                        1,
                        true,
                        advancementId,
                        titleKey));
        this.fixedItemsCount++;
    }

    public void addAchievementsStoreItem(
            ItemStack item, int price, int maxStock, ResourceLocation requiredAdvancementId) {
        this.addAchievementsStoreItem(item, price, maxStock, requiredAdvancementId, null);
    }

    public void addAchievementsStoreItem(
            String itemId,
            int price,
            int maxStock,
            ResourceLocation requiredAdvancementId,
            String requiredAdvancementTitleKey) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl != null) {
            BuiltInRegistries.ITEM
                    .getOptional(rl)
                    .ifPresent(
                            item ->
                                    this.addAchievementsStoreItem(
                                            new ItemStack(item),
                                            price,
                                            maxStock,
                                            requiredAdvancementId,
                                            requiredAdvancementTitleKey));
        }
    }

    public void addAchievementsStoreItem(
            String itemId, int price, int maxStock, ResourceLocation requiredAdvancementId) {
        this.addAchievementsStoreItem(itemId, price, maxStock, requiredAdvancementId, null);
    }

    private static String buildAdvancementTitleKey(ResourceLocation advancementId) {
        String namespacePrefix =
                "minecraft".equals(advancementId.getNamespace())
                        ? ""
                        : advancementId.getNamespace() + ".";
        return "advancements."
                + namespacePrefix
                + advancementId.getPath().replace('/', '.')
                + ".title";
    }

    public void addFavorStoreItem(int requiredFavorLevel, String itemId, int price, int maxStock) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl != null) {
            BuiltInRegistries.ITEM
                    .getOptional(rl)
                    .ifPresent(
                            item ->
                                    this.addFavorStoreItem(
                                            requiredFavorLevel,
                                            new ItemStack(item),
                                            price,
                                            maxStock));
        }
    }

    public void addFavorStoreItem(
            int requiredFavorLevel,
            String itemId,
            int price,
            int maxStock,
            Consumer<ItemStack> modifier) {
        ResourceLocation rl = ResourceLocation.tryParse(itemId);
        if (rl != null) {
            BuiltInRegistries.ITEM
                    .getOptional(rl)
                    .ifPresent(
                            item ->
                                    this.addFavorStoreItem(
                                            requiredFavorLevel,
                                            new ItemStack(item),
                                            price,
                                            maxStock,
                                            modifier));
        }
    }

    public List<StoreItem> getStoreItems() {
        return this.storeItems;
    }

    private int calculateFavorLevel(int spentCoins) {
        int coinsPerFavorLevel = Math.max(1, this.getCoinsPerFavorLevel());
        int level = 1 + (spentCoins / coinsPerFavorLevel);
        if (level < 1) {
            return 1;
        }
        return Math.min(level, MAX_FAVOR_LEVEL);
    }

    private void unlockFavorStoreItems() {
        for (FavorStoreItemData favorItem : this.favorStoreItems) {
            if (this.hasFixedItem(
                    favorItem.itemStack(),
                    favorItem.price(),
                    favorItem.maxStock(),
                    favorItem.requiredFavorLevel(),
                    null,
                    null)) {
                continue;
            }
            int insertIndex = Math.min(this.fixedItemsCount, this.storeItems.size());
            this.storeItems.add(
                    insertIndex,
                    new StoreItem(
                            favorItem.itemStack().copy(),
                            favorItem.price(),
                            favorItem.maxStock(),
                            favorItem.maxStock(),
                            favorItem.requiredFavorLevel(),
                            true,
                            null,
                            null));
            this.fixedItemsCount++;
        }
    }

    private boolean hasFixedItem(
            ItemStack itemStack,
            int price,
            int maxStock,
            int requiredFavorLevel,
            @Nullable String requiredAdvancementId,
            @Nullable String requiredAdvancementTitleKey) {
        for (int i = 0; i < this.fixedItemsCount && i < this.storeItems.size(); i++) {
            StoreItem existing = this.storeItems.get(i);
            if (ItemStack.isSameItemSameComponents(existing.getItemStack(), itemStack)
                    && existing.getPrice() == price
                    && existing.getMaxStock() == maxStock
                    && existing.isRestockable()
                    && existing.getRequiredFavorLevel() == requiredFavorLevel
                    && java.util.Objects.equals(
                            existing.getRequiredAdvancementId(), requiredAdvancementId)
                    && java.util.Objects.equals(
                            existing.getRequiredAdvancementTitleKey(), requiredAdvancementTitleKey)) {
                return true;
            }
        }
        return false;
    }

    /** 补货所有商品并刷新随机商品 */
    public void restockAll() {
        for (int i = 0; i < this.fixedItemsCount && i < this.storeItems.size(); i++) {
            this.storeItems.get(i).restock();
        }
        this.refreshRandomItems();
    }

    /**
     * 刷新随机商品
     *
     * <p>默认实现移除所有随机商品。子类覆盖此方法以生成新的随机商品。
     */
    protected void refreshRandomItems() {
        // 移除所有非固定商品
        if (this.storeItems.size() > this.fixedItemsCount) {
            this.storeItems.subList(this.fixedItemsCount, this.storeItems.size()).clear();
        }
    }

    /**
     * 调试用：重置商店 NPC 的可持久化状态，并按代码默认配置重建数据。
     *
     * <p>会清空历史存档中残留的商品/好感等状态，随后调用子类重新写入默认装备与商品配置。
     */
    public final void debugResetToCodeDefaults() {
        if (this.level().isClientSide) {
            return;
        }
        this.totalSpentCoins = 0;
        this.favorLevel = 1;
        this.lastRestockDay = this.level().getDayTime() / 24000L;
        this.storeItems.clear();
        this.favorStoreItems.clear();
        this.fixedItemsCount = 0;
        this.clearAllEquipmentForReset();
        this.applyCodeDefaultsAfterDebugReset();
        this.restockAll();
    }

    protected abstract void applyCodeDefaultsAfterDebugReset();

    private void clearAllEquipmentForReset() {
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
        this.setDropChance(EquipmentSlot.LEGS, 0.0F);
        this.setDropChance(EquipmentSlot.FEET, 0.0F);
    }

    /**
     * 生成随机商品
     *
     * @param pool 商品池
     * @param minTypes 最少抽取的商品种类数量
     * @param maxTypes 最多抽取的商品种类数量
     */
    protected void generateRandomItems(List<RandomItemData> pool, int minTypes, int maxTypes) {
        if (pool == null || pool.isEmpty()) return;

        net.minecraft.util.RandomSource random = this.getRandom();

        // 随机选择 minTypes 到 maxTypes 种商品
        int count = minTypes + random.nextInt(Math.max(1, maxTypes - minTypes + 1));
        List<RandomItemData> poolCopy = new ArrayList<>(pool);

        for (int i = 0; i < count; i++) {
            if (poolCopy.isEmpty()) break;

            // 按权重随机选择
            int totalWeight = poolCopy.stream().mapToInt(e -> e.weight).sum();
            int roll = random.nextInt(totalWeight);
            int current = 0;
            RandomItemData selected = null;

            for (RandomItemData entry : poolCopy) {
                current += entry.weight;
                if (roll < current) {
                    selected = entry;
                    break;
                }
            }

            if (selected != null) {
                poolCopy.remove(selected); // 避免重复
                this.addRandomStoreItem(
                        new ItemStack(selected.item),
                        selected.minPrice,
                        selected.maxPrice,
                        selected.minStock,
                        selected.maxStock,
                        selected.modifier);
            }
        }
    }

    /**
     * 添加随机商品 (带范围随机)
     *
     * @param item 物品
     * @param minPrice 最小价格
     * @param maxPrice 最大价格
     * @param minStock 最小库存
     * @param maxStock 最大库存
     */
    protected void addRandomStoreItem(
            ItemStack item, int minPrice, int maxPrice, int minStock, int maxStock) {
        this.addRandomStoreItem(item, minPrice, maxPrice, minStock, maxStock, null);
    }

    /**
     * 添加随机商品 (带范围随机和修改器)
     *
     * @param item 物品
     * @param minPrice 最小价格
     * @param maxPrice 最大价格
     * @param minStock 最小库存
     * @param maxStock 最大库存
     * @param modifier 修改器
     */
    protected void addRandomStoreItem(
            ItemStack item,
            int minPrice,
            int maxPrice,
            int minStock,
            int maxStock,
            java.util.function.Consumer<ItemStack> modifier) {
        net.minecraft.util.RandomSource random = this.getRandom();
        int price = minPrice + random.nextInt(Math.max(1, maxPrice - minPrice + 1));
        int stock = minStock + random.nextInt(Math.max(1, maxStock - minStock + 1));

        ItemStack stack = item.copy();
        if (modifier != null) {
            modifier.accept(stack);
        }
        this.storeItems.add(new StoreItem(stack, price, stock));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putLong("LastRestockDay", this.lastRestockDay);
        compound.putInt("FixedItemsCount", this.fixedItemsCount);
        compound.putInt("FavorSpentCoins", this.totalSpentCoins);
        compound.putInt("FavorLevel", this.favorLevel);
        ListTag itemsTag = new ListTag();
        HolderLookup.Provider registryAccess = this.registryAccess();
        for (StoreItem storeItem : this.storeItems) {
            itemsTag.add(storeItem.save(registryAccess));
        }
        compound.put("StoreItems", itemsTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("LastRestockDay")) {
            this.lastRestockDay = compound.getLong("LastRestockDay");
        }
        if (compound.contains("FixedItemsCount")) {
            this.fixedItemsCount = compound.getInt("FixedItemsCount");
        }
        if (compound.contains("FavorSpentCoins")) {
            this.totalSpentCoins = compound.getInt("FavorSpentCoins");
        }
        if (compound.contains("FavorLevel")) {
            this.favorLevel = compound.getInt("FavorLevel");
        } else {
            this.favorLevel = this.calculateFavorLevel(this.totalSpentCoins);
        }
        this.favorLevel = Math.max(1, Math.min(MAX_FAVOR_LEVEL, this.favorLevel));
        if (compound.contains("StoreItems", Tag.TAG_LIST)) {
            ListTag itemsTag = compound.getList("StoreItems", Tag.TAG_COMPOUND);
            HolderLookup.Provider registryAccess = this.registryAccess();
            this.storeItems.clear();
            for (Tag tag : itemsTag) {
                if (tag instanceof CompoundTag itemTag) {
                    this.storeItems.add(StoreItem.load(registryAccess, itemTag));
                }
            }
        }
        this.fixedItemsCount = Math.min(this.fixedItemsCount, this.storeItems.size());
        this.unlockFavorStoreItems();
    }

    /** 随机商品池条目 */
    public record RandomItemData(
            net.minecraft.world.item.Item item,
            int minPrice,
            int maxPrice,
            int minStock,
            int maxStock,
            int weight,
            @Nullable java.util.function.Consumer<ItemStack> modifier) {
        public RandomItemData(
                net.minecraft.world.item.Item item,
                int minPrice,
                int maxPrice,
                int minStock,
                int maxStock,
                int weight) {
            this(item, minPrice, maxPrice, minStock, maxStock, weight, null);
        }
    }

    public record FavorStoreItemData(
            int requiredFavorLevel, ItemStack itemStack, int price, int maxStock) {}

    /** 商品条目内部类 */
    public static class StoreItem {
        private final ItemStack itemStack;
        private int price;
        private int maxStock;
        private int currentStock;
        private final int requiredFavorLevel;
        private final boolean restockable;
        @Nullable private final String requiredAdvancementId;
        @Nullable private final String requiredAdvancementTitleKey;
        private final boolean viewerLocked;

        public StoreItem(ItemStack itemStack, int price) {
            this(itemStack, price, -1, -1, 1, true, null, null, false);
        }

        public StoreItem(ItemStack itemStack, int price, int maxStock) {
            this(itemStack, price, maxStock, maxStock, 1, true, null, null, false);
        }

        public StoreItem(ItemStack itemStack, int price, int maxStock, int currentStock) {
            this(itemStack, price, maxStock, currentStock, 1, true, null, null, false);
        }

        public StoreItem(
                ItemStack itemStack,
                int price,
                int maxStock,
                int currentStock,
                int requiredFavorLevel) {
            this(
                    itemStack,
                    price,
                    maxStock,
                    currentStock,
                    requiredFavorLevel,
                    true,
                    null,
                    null,
                    false);
        }

        public StoreItem(
                ItemStack itemStack,
                int price,
                int maxStock,
                int currentStock,
                int requiredFavorLevel,
                boolean restockable) {
            this(
                    itemStack,
                    price,
                    maxStock,
                    currentStock,
                    requiredFavorLevel,
                    restockable,
                    null,
                    null,
                    false);
        }

        public StoreItem(
                ItemStack itemStack,
                int price,
                int maxStock,
                int currentStock,
                int requiredFavorLevel,
                boolean restockable,
                @Nullable String requiredAdvancementId,
                @Nullable String requiredAdvancementTitleKey) {
            this(
                    itemStack,
                    price,
                    maxStock,
                    currentStock,
                    requiredFavorLevel,
                    restockable,
                    requiredAdvancementId,
                    requiredAdvancementTitleKey,
                    false);
        }

        public StoreItem(
                ItemStack itemStack,
                int price,
                int maxStock,
                int currentStock,
                int requiredFavorLevel,
                boolean restockable,
                @Nullable String requiredAdvancementId,
                @Nullable String requiredAdvancementTitleKey,
                boolean viewerLocked) {
            this.itemStack = itemStack;
            this.price = price;
            this.maxStock = maxStock;
            this.currentStock = currentStock;
            this.requiredFavorLevel = Math.max(1, requiredFavorLevel);
            this.restockable = restockable;
            this.requiredAdvancementId =
                    requiredAdvancementId == null || requiredAdvancementId.isBlank()
                            ? null
                            : requiredAdvancementId;
            this.requiredAdvancementTitleKey =
                    requiredAdvancementTitleKey == null || requiredAdvancementTitleKey.isBlank()
                            ? null
                            : requiredAdvancementTitleKey;
            this.viewerLocked = viewerLocked;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public int getPrice() {
            return price;
        }

        public void setPrice(int price) {
            this.price = price;
        }

        public int getMaxStock() {
            return maxStock;
        }

        public int getCurrentStock() {
            return currentStock;
        }

        public void setCurrentStock(int currentStock) {
            this.currentStock = currentStock;
        }

        public int getRequiredFavorLevel() {
            return this.requiredFavorLevel;
        }

        public boolean isRestockable() {
            return this.restockable;
        }

        @Nullable
        public String getRequiredAdvancementId() {
            return this.requiredAdvancementId;
        }

        @Nullable
        public String getRequiredAdvancementTitleKey() {
            return this.requiredAdvancementTitleKey;
        }

        public boolean isViewerLocked() {
            return this.viewerLocked;
        }

        /** 是否无限库存 */
        public boolean isInfinite() {
            return maxStock < 0;
        }

        /** 是否售罄 */
        public boolean isSoldOut() {
            return !isInfinite() && currentStock <= 0;
        }

        /**
         * 尝试购买（扣减库存）
         *
         * @return true 如果购买成功（有库存），false 如果售罄
         */
        public boolean tryPurchase() {
            if (isInfinite()) return true;
            if (currentStock > 0) {
                currentStock--;
                return true;
            }
            return false;
        }

        /** 补货 */
        public void restock() {
            if (this.restockable && !isInfinite()) {
                this.currentStock = this.maxStock;
            }
        }

        public CompoundTag save(HolderLookup.Provider provider) {
            CompoundTag tag = new CompoundTag();
            if (!itemStack.isEmpty()) {
                tag.put("Item", itemStack.save(provider, new CompoundTag()));
            }
            tag.putInt("Price", price);
            tag.putInt("MaxStock", maxStock);
            tag.putInt("CurrentStock", currentStock);
            tag.putInt("RequiredFavorLevel", requiredFavorLevel);
            tag.putBoolean("Restockable", restockable);
            if (requiredAdvancementId != null) {
                tag.putString("RequiredAdvancementId", requiredAdvancementId);
            }
            if (requiredAdvancementTitleKey != null) {
                tag.putString("RequiredAdvancementTitleKey", requiredAdvancementTitleKey);
            }
            return tag;
        }

        public CompoundTag saveForNetwork(
                HolderLookup.Provider provider, ServerPlayer viewer, StoreEntity storeEntity) {
            CompoundTag tag = save(provider);
            tag.putBoolean("ViewerLocked", !storeEntity.isProgressRequirementMet(viewer, this));
            return tag;
        }

        public static StoreItem load(HolderLookup.Provider provider, CompoundTag tag) {
            ItemStack stack = ItemStack.EMPTY;
            if (tag.contains("Item")) {
                stack = ItemStack.parse(provider, tag.getCompound("Item")).orElse(ItemStack.EMPTY);
            }
            int price = tag.getInt("Price");
            int maxStock = tag.contains("MaxStock") ? tag.getInt("MaxStock") : -1;
            int currentStock = tag.contains("CurrentStock") ? tag.getInt("CurrentStock") : maxStock;
            int requiredFavorLevel =
                    tag.contains("RequiredFavorLevel") ? tag.getInt("RequiredFavorLevel") : 1;
            boolean restockable = !tag.contains("Restockable") || tag.getBoolean("Restockable");
            String requiredAdvancementId =
                    tag.contains("RequiredAdvancementId", Tag.TAG_STRING)
                            ? tag.getString("RequiredAdvancementId")
                            : null;
            String requiredAdvancementTitleKey =
                    tag.contains("RequiredAdvancementTitleKey", Tag.TAG_STRING)
                            ? tag.getString("RequiredAdvancementTitleKey")
                            : null;
            boolean viewerLocked = tag.contains("ViewerLocked") && tag.getBoolean("ViewerLocked");
            return new StoreItem(
                    stack,
                    price,
                    maxStock,
                    currentStock,
                    requiredFavorLevel,
                    restockable,
                    requiredAdvancementId,
                    requiredAdvancementTitleKey,
                    viewerLocked);
        }
    }
}
