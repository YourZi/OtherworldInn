package com.otherworldinn.world.picnic;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class PicnicBoxData implements INBTSerializable<CompoundTag> {
    public static final int SLOT_COUNT = 2;
    public static final int MAX_ITEMS_PER_SLOT = 4;

    private final NonNullList<ItemStack> slots = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return slots.get(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        if (stack.isEmpty()) {
            slots.set(slot, ItemStack.EMPTY);
            return;
        }
        ItemStack normalized = stack.copy();
        normalized.setCount(Math.min(MAX_ITEMS_PER_SLOT, normalized.getCount()));
        slots.set(slot, normalized);
    }

    public boolean isEmpty() {
        return findFirstOccupiedSlot() < 0;
    }

    public int getTotalItemCount() {
        int total = 0;
        for (ItemStack slot : slots) {
            total += slot.getCount();
        }
        return total;
    }

    public int findFirstOccupiedSlot() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!slots.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public int findSlotForInsertion(ItemStack stack) {
        if (!canStore(stack)) {
            return -1;
        }

        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack existing = slots.get(i);
            if (!existing.isEmpty()
                    && ItemStack.isSameItemSameComponents(existing, stack)
                    && existing.getCount() < MAX_ITEMS_PER_SLOT) {
                return i;
            }
        }

        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slots.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public int insert(ItemStack stack, int amount) {
        if (!canStore(stack) || amount <= 0) {
            return 0;
        }

        int remaining = Math.min(amount, stack.getCount());
        int inserted = 0;

        while (remaining > 0) {
            int slot = findSlotForInsertion(stack);
            if (slot < 0) {
                break;
            }

            ItemStack existing = slots.get(slot);
            if (existing.isEmpty()) {
                int moved = Math.min(MAX_ITEMS_PER_SLOT, remaining);
                ItemStack placed = stack.copy();
                placed.setCount(moved);
                slots.set(slot, placed);
                inserted += moved;
                remaining -= moved;
                continue;
            }

            int space = MAX_ITEMS_PER_SLOT - existing.getCount();
            if (space <= 0) {
                break;
            }
            int moved = Math.min(space, remaining);
            existing.grow(moved);
            inserted += moved;
            remaining -= moved;
        }

        return inserted;
    }

    public int getInsertCapacity(ItemStack stack) {
        if (!canStore(stack)) {
            return 0;
        }

        int capacity = 0;
        for (ItemStack existing : slots) {
            if (existing.isEmpty()) {
                capacity += MAX_ITEMS_PER_SLOT;
                continue;
            }
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                capacity += MAX_ITEMS_PER_SLOT - existing.getCount();
            }
        }
        return capacity;
    }

    public ItemStack removeOneFromSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slots.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack extracted = stack.copy();
        extracted.setCount(1);
        stack.shrink(1);
        if (stack.isEmpty()) {
            slots.set(slot, ItemStack.EMPTY);
        }
        return extracted;
    }

    public ItemStack removeAllFromSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slots.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        slots.set(slot, ItemStack.EMPTY);
        return stack;
    }

    public List<ItemStack> clearAndReturnContents() {
        List<ItemStack> removed = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = removeAllFromSlot(i);
            if (!stack.isEmpty()) {
                removed.add(stack);
            }
        }
        return removed;
    }

    public List<ItemStack> copyItems() {
        List<ItemStack> copies = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            copies.add(slots.get(i).copy());
        }
        return copies;
    }

    public void replaceAll(List<ItemStack> items) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = i < items.size() ? items.get(i) : ItemStack.EMPTY;
            setItem(i, stack);
        }
    }

    public static boolean canStore(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.has(DataComponents.FOOD) && stack.getItem().canFitInsideContainerItems();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag itemsTag = new ListTag();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = slots.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            CompoundTag entry = new CompoundTag();
            entry.putInt("Slot", i);
            if (stack.save(provider) instanceof CompoundTag stackTag) {
                entry.put("Item", stackTag);
            }
            itemsTag.add(entry);
        }
        tag.put("Items", itemsTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots.set(i, ItemStack.EMPTY);
        }
        ListTag itemsTag = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < itemsTag.size(); i++) {
            CompoundTag entry = itemsTag.getCompound(i);
            int slot = entry.getInt("Slot");
            if (!entry.contains("Item", Tag.TAG_COMPOUND)) {
                continue;
            }
            ItemStack stack =
                    ItemStack.parse(provider, entry.getCompound("Item")).orElse(ItemStack.EMPTY);
            if (slot >= 0 && slot < SLOT_COUNT) {
                setItem(slot, stack);
                continue;
            }
            insert(stack, stack.getCount());
        }
    }
}
