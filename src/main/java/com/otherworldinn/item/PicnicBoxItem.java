package com.otherworldinn.item;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.C2SPicnicBoxActionPacket;
import com.otherworldinn.world.event.listener.PlayerFatigueHandler;
import com.otherworldinn.world.fatigue.FatigueCalculator;
import com.otherworldinn.world.fatigue.FatigueData;
import com.otherworldinn.world.picnic.PicnicBoxData;
import com.otherworldinn.world.picnic.PicnicBoxSyncHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

public class PicnicBoxItem extends Item {
    private static final int PICNIC_COOLDOWN_TICKS = 15 * 20;
    private static final int DEFAULT_USE_DURATION = 32;
    private static final int MAX_BAR_WIDTH = 13;
    private static final String USE_TOOLTIP_KEY = "item.otherworldinn.picnic_box.tooltip.use";
    private static final String INSERT_TOOLTIP_KEY = "item.otherworldinn.picnic_box.tooltip.insert";
    private static final String CLEAR_TOOLTIP_KEY = "item.otherworldinn.picnic_box.tooltip.clear";
    private static final String EMPTY_TOOLTIP_KEY = "item.otherworldinn.picnic_box.tooltip.empty";
    private static final String CONTENTS_TOOLTIP_KEY = "item.otherworldinn.picnic_box.tooltip.contents";
    private static final String SLOT_TOOLTIP_KEY = "item.otherworldinn.picnic_box.tooltip.slot";
    private static final String EMPTY_KEY = "message.otherworldinn.picnic_box.empty";
    private static final String CANNOT_EAT_KEY = "message.otherworldinn.picnic_box.cannot_eat";

    public PicnicBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            player.stopUsingItem();
            return InteractionResultHolder.fail(stack);
        }

        int slot = findFirstOccupiedSlot(player);
        if (slot < 0) {
            player.stopUsingItem();
            if (!level.isClientSide) {
                showMessage(player, EMPTY_KEY);
            }
            return InteractionResultHolder.fail(stack);
        }

        ItemStack preview = getFoodInSlot(player, slot);
        boolean canConsume = canConsume(player, preview);
        if (!canConsume) {
            player.stopUsingItem();
            if (!level.isClientSide) {
                showMessage(player, CANNOT_EAT_KEY);
            }
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (level.isClientSide || !(livingEntity instanceof ServerPlayer player)) {
            return stack;
        }

        int slot = findFirstOccupiedSlot(player);
        if (slot < 0) {
            return stack;
        }

        ItemStack preview = getFoodInSlot(player, slot);
        if (!canConsume(player, preview)) {
            return stack;
        }

        PicnicBoxData picnicData = player.getData(ModAttachments.PLAYER_PICNIC_BOX);
        ItemStack consumed = picnicData.removeOneFromSlot(slot);
        if (consumed.isEmpty()) {
            return stack;
        }

        ItemStack result;
        try {
            result = consumed.finishUsingItem(level, livingEntity);
        } catch (Exception exception) {
            OtherworldInn.LOGGER.warn(
                    "Failed to consume picnic box food {}, returning it to player",
                    consumed.getItem(),
                    exception);
            safeGiveToPlayer(player, consumed);
            PicnicBoxSyncHelper.sync(player);
            return stack;
        }

        try {
            handleRemainder(player, consumed, result);
        } catch (Exception exception) {
            OtherworldInn.LOGGER.warn(
                    "Failed to resolve picnic box remainder for {}",
                    consumed.getItem(),
                    exception);
        }
        player.awardStat(Stats.ITEM_USED.get(consumed.getItem()));
        CriteriaTriggers.CONSUME_ITEM.trigger(player, consumed);

        FoodProperties food = consumed.get(DataComponents.FOOD);
        if (food != null) {
            FatigueData fatigue = player.getData(ModAttachments.PLAYER_FATIGUE);
            fatigue.reduceFatigue(computeFatigueRecovery(food));
            fatigue.setLastNotifiedStage(FatigueCalculator.getStage(fatigue.getFatigue()).level());
            PlayerFatigueHandler.syncCurrentState(player);
        }

        player.getCooldowns().addCooldown(this, PICNIC_COOLDOWN_TICKS);
        PicnicBoxSyncHelper.sync(player);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return DEFAULT_USE_DURATION;
        }

        int slot = findFirstOccupiedSlot(player);
        if (slot < 0) {
            return DEFAULT_USE_DURATION;
        }
        ItemStack food = getFoodInSlot(player, slot);
        return food.isEmpty() ? DEFAULT_USE_DURATION : food.getItem().getUseDuration(food, entity);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (!(entity instanceof Player player)) {
            return super.onEntitySwing(stack, entity, hand);
        }
        return tryClearWithSecondaryUse(player) || super.onEntitySwing(stack, entity, hand);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        if (tryClearWithSecondaryUse(player)) {
            return false;
        }
        return super.canAttackBlock(state, level, pos, player);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (tryClearWithSecondaryUse(player)) {
            return true;
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(USE_TOOLTIP_KEY).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable(INSERT_TOOLTIP_KEY).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable(CLEAR_TOOLTIP_KEY).withStyle(ChatFormatting.DARK_GRAY));

        Player player = getClientPlayer();
        if (player == null) {
            return;
        }

        PicnicBoxData data = getData(player);
        if (data.isEmpty()) {
            tooltipComponents.add(Component.translatable(EMPTY_TOOLTIP_KEY).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltipComponents.add(
                Component.translatable(
                                CONTENTS_TOOLTIP_KEY,
                                data.getTotalItemCount(),
                                PicnicBoxData.SLOT_COUNT * PicnicBoxData.MAX_ITEMS_PER_SLOT)
                        .withStyle(ChatFormatting.GOLD));

        for (int i = 0; i < PicnicBoxData.SLOT_COUNT; i++) {
            ItemStack slotStack = data.getItem(i);
            if (slotStack.isEmpty()) {
                continue;
            }
            tooltipComponents.add(
                    Component.translatable(
                                    SLOT_TOOLTIP_KEY, i + 1, slotStack.getHoverName(), slotStack.getCount())
                            .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        Player player = getClientPlayer();
        return player != null && !getData(player).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        Player player = getClientPlayer();
        if (player == null) {
            return 0;
        }
        int total = getData(player).getTotalItemCount();
        return Math.max(
                1,
                Math.round(
                        (float) total
                                * MAX_BAR_WIDTH
                                / (PicnicBoxData.SLOT_COUNT * PicnicBoxData.MAX_ITEMS_PER_SLOT)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xC89F45;
    }

    private static void handleRemainder(ServerPlayer player, ItemStack consumed, ItemStack result) {
        if (!result.isEmpty()) {
            safeGiveToPlayer(player, result);
            return;
        }
        ItemStack legacyRemainder = resolveLegacyRemainder(consumed);
        if (!legacyRemainder.isEmpty()) {
            safeGiveToPlayer(player, legacyRemainder);
        }
    }

    private static int computeFatigueRecovery(FoodProperties food) {
        return Math.min(12, Math.round(1.0F + food.nutrition() * 1.5F));
    }

    private static boolean canConsume(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        FoodProperties food = stack.get(DataComponents.FOOD);
        return food != null && player.canEat(food.canAlwaysEat());
    }

    private static int findFirstOccupiedSlot(Player player) {
        return getData(player).findFirstOccupiedSlot();
    }

    private static ItemStack getFoodInSlot(Player player, int slot) {
        return getData(player).getItem(slot);
    }

    private static ItemStack removeOne(Player player) {
        int slot = findFirstOccupiedSlot(player);
        return slot < 0 ? ItemStack.EMPTY : getData(player).removeOneFromSlot(slot);
    }

    private static boolean dropContents(Player player) {
        List<ItemStack> contents = getData(player).clearAndReturnContents();
        if (contents.isEmpty()) {
            return false;
        }
        for (ItemStack content : contents) {
            safeGiveToPlayer(player, content);
        }
        syncIfServer(player);
        return true;
    }

    private static boolean tryClearWithSecondaryUse(Player player) {
        if (!player.isSecondaryUseActive()) {
            return false;
        }
        if (player.level().isClientSide) {
            return !getData(player).isEmpty();
        }

        boolean changed = dropContents(player);
        if (changed) {
            playDropContentsSound(player);
        }
        return changed;
    }

    public static void handleStackedOnOtherEvent(ItemStackedOnOtherEvent event) {
        if (event.getClickAction() != ClickAction.SECONDARY) {
            return;
        }

        ItemStack carried = event.getCarriedItem();
        ItemStack stackedOn = event.getStackedOnItem();
        boolean carriedIsPicnicBox = carried.getItem() instanceof PicnicBoxItem;
        boolean stackedOnIsPicnicBox = stackedOn.getItem() instanceof PicnicBoxItem;
        if (!carriedIsPicnicBox && !stackedOnIsPicnicBox) {
            return;
        }

        Player rawPlayer = event.getPlayer();
        int slotIndex = resolveMenuSlotIndex(rawPlayer, event.getSlot());
        int action = resolveInventoryAction(carried, stackedOn);
        if (action < 0 || slotIndex < 0) {
            return;
        }
        event.setCanceled(true);
        if (!rawPlayer.level().isClientSide) {
            return;
        }
        ModMessages.sendToServer(new C2SPicnicBoxActionPacket(action, slotIndex));
    }

    private static int resolveInventoryAction(ItemStack carried, ItemStack stackedOn) {
        boolean carriedIsPicnicBox = carried.getItem() instanceof PicnicBoxItem;
        boolean stackedOnIsPicnicBox = stackedOn.getItem() instanceof PicnicBoxItem;
        if (carriedIsPicnicBox) {
            if (stackedOn.isEmpty()) {
                return C2SPicnicBoxActionPacket.REMOVE_TO_SLOT;
            }
            if (PicnicBoxData.canStore(stackedOn)) {
                return C2SPicnicBoxActionPacket.INSERT_FROM_SLOT;
            }
            return -1;
        }
        if (stackedOnIsPicnicBox) {
            if (carried.isEmpty()) {
                return C2SPicnicBoxActionPacket.REMOVE_TO_CARRIED;
            }
            if (PicnicBoxData.canStore(carried)) {
                return C2SPicnicBoxActionPacket.INSERT_FROM_CARRIED;
            }
        }
        return -1;
    }

    public static void handleServerInventoryAction(Player rawPlayer, int action, int slotIndex) {
        if (!(rawPlayer instanceof ServerPlayer player)) {
            return;
        }

        boolean changed =
                switch (action) {
                    case C2SPicnicBoxActionPacket.INSERT_FROM_SLOT -> insertFromSlot(player, slotIndex);
                    case C2SPicnicBoxActionPacket.REMOVE_TO_SLOT -> removeToSlot(player, slotIndex);
                    case C2SPicnicBoxActionPacket.INSERT_FROM_CARRIED -> insertFromCarried(player, slotIndex);
                    case C2SPicnicBoxActionPacket.REMOVE_TO_CARRIED -> removeToCarried(player, slotIndex);
                    default -> false;
                };

        if (changed) {
            player.containerMenu.broadcastChanges();
        }
        PicnicBoxSyncHelper.sync(player);
    }

    private static boolean insertFromSlot(ServerPlayer player, int slotIndex) {
        if (!(player.containerMenu.getCarried().getItem() instanceof PicnicBoxItem)) {
            return false;
        }

        Slot slot = getMenuSlot(player, slotIndex);
        if (slot == null) {
            return false;
        }

        ItemStack slotStack = slot.getItem();
        if (!PicnicBoxData.canStore(slotStack)) {
            return false;
        }

        PicnicBoxData data = getData(player);
        int transferable = Math.min(slotStack.getCount(), data.getInsertCapacity(slotStack));
        if (transferable <= 0) {
            return false;
        }

        int inserted = data.insert(slotStack, transferable);
        if (inserted <= 0) {
            return false;
        }

        slotStack.shrink(inserted);
        if (slotStack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        playInsertSound(player);
        return true;
    }

    private static boolean removeToSlot(ServerPlayer player, int slotIndex) {
        if (!(player.containerMenu.getCarried().getItem() instanceof PicnicBoxItem)) {
            return false;
        }

        Slot slot = getMenuSlot(player, slotIndex);
        if (slot == null || !slot.getItem().isEmpty()) {
            return false;
        }

        ItemStack removed = removeOne(player);
        if (removed.isEmpty()) {
            return false;
        }

        playRemoveOneSound(player);
        ItemStack leftover = slot.safeInsert(removed);
        if (!leftover.isEmpty()) {
            safeGiveToPlayer(player, leftover);
        }
        slot.setChanged();
        return true;
    }

    private static boolean insertFromCarried(ServerPlayer player, int slotIndex) {
        Slot slot = getMenuSlot(player, slotIndex);
        if (slot == null || !(slot.getItem().getItem() instanceof PicnicBoxItem)) {
            return false;
        }

        ItemStack carried = player.containerMenu.getCarried();
        if (!PicnicBoxData.canStore(carried)) {
            return false;
        }

        PicnicBoxData data = getData(player);
        int inserted = data.insert(carried, carried.getCount());
        if (inserted <= 0) {
            return false;
        }

        ItemStack updated = carried.copy();
        updated.shrink(inserted);
        player.containerMenu.setCarried(updated.isEmpty() ? ItemStack.EMPTY : updated);
        playInsertSound(player);
        return true;
    }

    private static boolean removeToCarried(ServerPlayer player, int slotIndex) {
        Slot slot = getMenuSlot(player, slotIndex);
        if (slot == null || !(slot.getItem().getItem() instanceof PicnicBoxItem)) {
            return false;
        }
        if (!player.containerMenu.getCarried().isEmpty()) {
            return false;
        }

        ItemStack removed = removeOne(player);
        if (removed.isEmpty()) {
            return false;
        }

        playRemoveOneSound(player);
        player.containerMenu.setCarried(removed);
        return true;
    }

    private static Slot getMenuSlot(ServerPlayer player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= player.containerMenu.slots.size()) {
            return null;
        }
        return player.containerMenu.getSlot(slotIndex);
    }

    private static int resolveMenuSlotIndex(Player player, Slot slot) {
        return player.containerMenu.slots.indexOf(slot);
    }

    private static PicnicBoxData getData(Player player) {
        return player.getData(ModAttachments.PLAYER_PICNIC_BOX);
    }

    private static void safeGiveToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static void syncIfServer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PicnicBoxSyncHelper.sync(serverPlayer);
        }
    }

    private static ItemStack resolveLegacyRemainder(ItemStack consumed) {
        ItemStack fromStack =
                tryInvokeRemainderMethod(
                        consumed,
                        consumed,
                        "getCraftingRemainingItem",
                        "getCraftingRemainder",
                        "getRecipeRemainder");
        if (!fromStack.isEmpty()) {
            return fromStack;
        }
        return tryInvokeRemainderMethod(
                consumed.getItem(),
                consumed,
                "getCraftingRemainingItem",
                "getCraftingRemainder",
                "getRecipeRemainder",
                "getContainerItem");
    }

    private static ItemStack tryInvokeRemainderMethod(
            Object target, ItemStack consumed, String... candidateNames) {
        for (String candidate : candidateNames) {
            for (Method method : target.getClass().getMethods()) {
                if (!method.getName().equals(candidate)) {
                    continue;
                }
                try {
                    Object value;
                    if (method.getParameterCount() == 0) {
                        value = method.invoke(target);
                    } else if (method.getParameterCount() == 1
                            && method.getParameterTypes()[0] == ItemStack.class) {
                        value = method.invoke(target, consumed.copy());
                    } else {
                        continue;
                    }

                    if (value instanceof ItemStack remainder && !remainder.isEmpty()) {
                        return remainder.copy();
                    }
                } catch (IllegalAccessException | InvocationTargetException ignored) {
                    continue;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    private static void showMessage(Player player, String key) {
        player.displayClientMessage(
                Component.translatable(key).withStyle(style -> style.withColor(ModColors.ERROR)),
                true);
    }

    private static void playRemoveOneSound(Player player) {
        player.playSound(
                SoundEvents.BUNDLE_REMOVE_ONE,
                0.8F,
                0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playInsertSound(Player player) {
        player.playSound(
                SoundEvents.BUNDLE_INSERT,
                0.8F,
                0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }

    private static void playDropContentsSound(Player player) {
        player.playSound(
                SoundEvents.BUNDLE_DROP_CONTENTS,
                0.8F,
                0.8F + player.level().getRandom().nextFloat() * 0.4F);
    }
}
