package com.otherworldinn.compat.naturescompass;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class NaturesCompassDurabilityHelper {
    public static final ResourceLocation NATURES_COMPASS_ID =
            ResourceLocation.fromNamespaceAndPath("naturescompass", "naturescompass");
    private static final int MAX_SEARCH_USES = 8;

    private NaturesCompassDurabilityHelper() {}

    public static void onSearchSucceeded(ItemStack stack, Player player) {
        if (player == null || !isNatureCompass(stack)) {
            return;
        }

        stack.set(DataComponents.MAX_DAMAGE, MAX_SEARCH_USES);
        int currentDamage = Mth.clamp(stack.getOrDefault(DataComponents.DAMAGE, 0), 0, MAX_SEARCH_USES);
        int nextDamage = currentDamage + 1;
        if (nextDamage >= MAX_SEARCH_USES) {
            replaceWithVanillaCompass(player, stack);
            return;
        }

        stack.set(DataComponents.DAMAGE, nextDamage);
        player.getInventory().setChanged();
    }

    public static boolean isNatureCompass(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return NATURES_COMPASS_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private static void replaceWithVanillaCompass(Player player, ItemStack targetStack) {
        Inventory inventory = player.getInventory();
        ItemStack replacement = new ItemStack(Items.COMPASS);

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot) == targetStack) {
                inventory.setItem(slot, replacement);
                playBreakSound(player);
                inventory.setChanged();
                return;
            }
        }

        targetStack.shrink(1);
        if (!inventory.add(replacement)) {
            player.drop(replacement, false);
        }
        playBreakSound(player);
        inventory.setChanged();
    }

    private static void playBreakSound(Player player) {
        player.level()
                .playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.SHIELD_BREAK,
                        SoundSource.PLAYERS,
                        0.9F,
                        1.0F);
    }
}
