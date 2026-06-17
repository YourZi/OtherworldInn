package com.otherworldinn.world.inventory;

import com.otherworldinn.entity.store.WanderingTraderEntity;
import com.otherworldinn.init.ModMenuTypes;
import com.otherworldinn.world.economy.service.ItemRecyclePriceCalculator;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 游商回收菜单 — 3行（27格），像箱子一样放入物品，关闭时结算。
 *
 * <p>多人互斥：同一游商实体同时只允许一名玩家打开。
 */
public class WanderingTraderRecycleMenu extends AbstractContainerMenu {

    private static final int ROWS = 3;
    private static final int COLS = 9;
    public static final int SLOT_COUNT = ROWS * COLS;

    private final Player player;
    private final int traderEntityId;
    private final SimpleContainer container;

    public WanderingTraderRecycleMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, extraData.readInt());
    }

    public WanderingTraderRecycleMenu(int containerId, Inventory playerInventory, int traderEntityId) {
        super(ModMenuTypes.WANDERING_TRADER_RECYCLE.get(), containerId);
        this.player = playerInventory.player;
        this.traderEntityId = traderEntityId;
        this.container = new SimpleContainer(SLOT_COUNT);

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
        int total = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                int price = ItemRecyclePriceCalculator.getRecyclePrice(stack);
                total += price * stack.getCount();
                container.setItem(i, ItemStack.EMPTY);
            }
        }
        if (total > 0) {
            Entity entity = serverPlayer.level().getEntity(traderEntityId);
            if (entity instanceof WanderingTraderEntity trader) {
                trader.addFavorProgress(total);
            }
            TeamData team = TeamManager.getInstance().getPlayerTeam(serverPlayer);
            if (team != null) {
                team.addCoins(total, serverPlayer.server);
            }
        }
    }

    public int getTotalPrice() {
        int total = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.slots.get(i).getItem();
            if (!stack.isEmpty()) {
                total += ItemRecyclePriceCalculator.getRecyclePrice(stack) * stack.getCount();
            }
        }
        return total;
    }

    public int getTraderEntityId() {
        return traderEntityId;
    }
}
