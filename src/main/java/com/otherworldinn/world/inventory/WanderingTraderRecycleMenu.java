package com.otherworldinn.world.inventory;

import com.otherworldinn.entity.store.WanderingTraderEntity;
import com.otherworldinn.init.ModMenuTypes;
import com.otherworldinn.world.economy.service.ItemRecyclePriceCalculator;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 游商回收菜单 — 3行（27格），像箱子一样放入物品，关闭时结算。
 *
 * <p>多人互斥：同一游商实体同时只允许一名玩家打开。
 */
public class WanderingTraderRecycleMenu extends AbstractContainerMenu {
    private static final int FULL_PRICE_LIMIT = 16;
    private static final int HALF_PRICE_LIMIT = 32;
    private static final int LOW_PRICE_LIMIT = 64;
    private static final int RECYCLE_FAVOR_CAP = 100;
    private static final int RECYCLE_COIN_CAP = 256;

    private static final int ROWS = 3;
    private static final int COLS = 9;
    public static final int SLOT_COUNT = ROWS * COLS;

    private final Player player;
    private final int traderEntityId;
    private final SimpleContainer container;
    private final Map<Item, Integer> initialRecycledCounts;
    private final int initialRecycleFavorGained;
    private final int initialRecycleCoinGained;

    public WanderingTraderRecycleMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(
                containerId,
                playerInventory,
                extraData.readInt(),
                extraData.readVarInt(),
                extraData.readVarInt(),
                readRecycledCounts(extraData));
    }

    public WanderingTraderRecycleMenu(int containerId, Inventory playerInventory, WanderingTraderEntity trader) {
        this(
                containerId,
                playerInventory,
                trader.getId(),
                trader.getRecycleFavorGained(),
                trader.getRecycleCoinGained(),
                trader.copyRecycledItemCounts());
    }

    private WanderingTraderRecycleMenu(
            int containerId,
            Inventory playerInventory,
            int traderEntityId,
            int initialRecycleFavorGained,
            int initialRecycleCoinGained,
            Map<Item, Integer> initialRecycledCounts) {
        super(ModMenuTypes.WANDERING_TRADER_RECYCLE.get(), containerId);
        this.player = playerInventory.player;
        this.traderEntityId = traderEntityId;
        this.container = new SimpleContainer(SLOT_COUNT);
        this.initialRecycleFavorGained = Math.max(0, initialRecycleFavorGained);
        this.initialRecycleCoinGained = Math.max(0, initialRecycleCoinGained);
        this.initialRecycledCounts = new HashMap<>(initialRecycledCounts);

        // 回收物品槽位（3行）
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int index = row * COLS + col;
                this.addSlot(new Slot(container, index, 8 + col * 18, 18 + row * 18));
            }
        }

        // 玩家背包（底部3行）+ 快捷栏
        int playerInvY = 84;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvY + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return result;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < SLOT_COUNT) {
            // 从回收区移到背包
            if (!this.moveItemStackTo(stack, SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // 从背包移到回收区
            if (!this.moveItemStackTo(stack, 0, SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);
    }

    @Override
    public boolean stillValid(Player player) {
        Entity entity = player.level().getEntity(traderEntityId);
        return entity instanceof WanderingTraderEntity trader
                && trader.isAlive()
                && trader.getCurrentRecyclePlayer() == player;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // 清除游商的占用状态
        Entity entity = player.level().getEntity(traderEntityId);
        if (entity instanceof WanderingTraderEntity trader) {
            if (trader.getCurrentRecyclePlayer() == player) {
                trader.setCurrentRecyclePlayer(null);
            }
        }

        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            settle(serverPlayer);
        } else {
            // 客户端：丢弃回收区的物品
            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack stack = this.container.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    player.drop(stack, false);
                }
            }
        }
    }

    private void settle(ServerPlayer serverPlayer) {
        RecycleQuote quote;
        Entity entity = serverPlayer.level().getEntity(traderEntityId);
        if (entity instanceof WanderingTraderEntity trader) {
            quote = calculateRecycleQuote(
                    this.container,
                    trader.copyRecycledItemCounts(),
                    trader.getRecycleFavorGained(),
                    trader.getRecycleCoinGained());
            trader.applyRecycleVisitProgress(quote.addedCounts(), quote.favorToAdd(), quote.totalCoins());
            if (quote.favorToAdd() > 0) {
                trader.addFavorProgress(quote.favorToAdd());
            }
        } else {
            quote = calculateRecycleQuote(
                    this.container,
                    this.initialRecycledCounts,
                    this.initialRecycleFavorGained,
                    this.initialRecycleCoinGained);
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!container.getItem(i).isEmpty()) {
                container.setItem(i, ItemStack.EMPTY);
            }
        }
        if (quote.totalCoins() > 0) {
            TeamData team = TeamManager.getInstance().getPlayerTeam(serverPlayer);
            if (team != null) {
                TeamManager.getInstance().addCoins(team, quote.totalCoins(), serverPlayer.server);
            }
        }
    }

    public int getTotalPrice() {
        return calculateRecycleQuote(
                        this.container,
                        this.initialRecycledCounts,
                        this.initialRecycleFavorGained,
                        this.initialRecycleCoinGained)
                .totalCoins();
    }

    public boolean isRecycleCoinCapReached() {
        return this.initialRecycleCoinGained >= RECYCLE_COIN_CAP;
    }

    private static Map<Item, Integer> readRecycledCounts(FriendlyByteBuf extraData) {
        int size = extraData.readVarInt();
        Map<Item, Integer> counts = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Item item = BuiltInRegistries.ITEM.byId(extraData.readVarInt());
            int count = Math.max(0, extraData.readVarInt());
            if (item != null && count > 0) {
                counts.put(item, count);
            }
        }
        return counts;
    }

    private static RecycleQuote calculateRecycleQuote(
            Container container,
            Map<Item, Integer> baseCounts,
            int currentFavorGained,
            int currentCoinGained) {
        int theoreticalTotal = 0;
        Map<Item, Integer> simulatedCounts = new HashMap<>(baseCounts);
        Map<Item, Integer> addedCounts = new HashMap<>();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                Item item = stack.getItem();
                int unitPrice = ItemRecyclePriceCalculator.getRecyclePrice(stack);
                int alreadyRecycled = simulatedCounts.getOrDefault(item, 0);
                int stackCount = stack.getCount();
                theoreticalTotal += calculateSegmentedValue(unitPrice, alreadyRecycled, stackCount);
                simulatedCounts.put(item, alreadyRecycled + stackCount);
                addedCounts.merge(item, stackCount, Integer::sum);
            }
        }
        int remainingCoins = Math.max(0, RECYCLE_COIN_CAP - Math.max(0, currentCoinGained));
        int total = Math.min(theoreticalTotal, remainingCoins);
        int remainingFavor = Math.max(0, RECYCLE_FAVOR_CAP - Math.max(0, currentFavorGained));
        int favorToAdd = Math.min(total / 10, remainingFavor);
        return new RecycleQuote(total, favorToAdd, Collections.unmodifiableMap(addedCounts));
    }

    private static int calculateSegmentedValue(int unitPrice, int alreadyRecycled, int stackCount) {
        if (stackCount <= 0 || unitPrice <= 0) {
            return 0;
        }

        int total = 0;
        int cursor = Math.max(0, alreadyRecycled);
        int remaining = stackCount;
        while (remaining > 0) {
            int rangeEnd = nextRangeEnd(cursor);
            int countInRange = Math.min(remaining, rangeEnd - cursor);
            total += applyRate(unitPrice, countInRange, ratePercentFor(cursor));
            cursor += countInRange;
            remaining -= countInRange;
        }
        return total;
    }

    private static int nextRangeEnd(int alreadyRecycled) {
        if (alreadyRecycled < FULL_PRICE_LIMIT) {
            return FULL_PRICE_LIMIT;
        }
        if (alreadyRecycled < HALF_PRICE_LIMIT) {
            return HALF_PRICE_LIMIT;
        }
        if (alreadyRecycled < LOW_PRICE_LIMIT) {
            return LOW_PRICE_LIMIT;
        }
        return Integer.MAX_VALUE;
    }

    private static int ratePercentFor(int alreadyRecycled) {
        if (alreadyRecycled < FULL_PRICE_LIMIT) {
            return 100;
        }
        if (alreadyRecycled < HALF_PRICE_LIMIT) {
            return 50;
        }
        if (alreadyRecycled < LOW_PRICE_LIMIT) {
            return 20;
        }
        return 0;
    }

    private static int applyRate(int unitPrice, int count, int ratePercent) {
        if (count <= 0 || ratePercent <= 0) {
            return 0;
        }
        return unitPrice * count * ratePercent / 100;
    }

    private record RecycleQuote(int totalCoins, int favorToAdd, Map<Item, Integer> addedCounts) {}

    public int getRecycleFavorCap() {
        return RECYCLE_FAVOR_CAP;
    }

    public int getRecycleCoinCap() {
        return RECYCLE_COIN_CAP;
    }

    public int getTraderEntityId() {
        return traderEntityId;
    }
}
