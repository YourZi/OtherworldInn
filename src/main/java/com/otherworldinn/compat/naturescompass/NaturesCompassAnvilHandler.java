package com.otherworldinn.compat.naturescompass;

import com.otherworldinn.OtherworldInn;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class NaturesCompassAnvilHandler {
    private NaturesCompassAnvilHandler() {}

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (!NaturesCompassDurabilityHelper.isNatureCompass(left) || !right.is(ItemTags.LOGS)) {
            return;
        }

        NaturesCompassDurabilityHelper.ensureNatureCompassState(left);
        int currentDamage = NaturesCompassDurabilityHelper.getDamage(left);
        if (currentDamage <= 0) {
            return;
        }

        int repairedDamage = Math.min(currentDamage, right.getCount());
        if (repairedDamage <= 0) {
            return;
        }

        ItemStack output = event.getOutput().copy();
        if (output.isEmpty()) {
            output = left.copy();
        }
        NaturesCompassDurabilityHelper.ensureNatureCompassState(output);
        output.set(DataComponents.DAMAGE, currentDamage - repairedDamage);

        event.setOutput(output);
        event.setMaterialCost(repairedDamage);
        event.setCost(Math.max(event.getCost(), repairedDamage));
    }
}
