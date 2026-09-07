package com.otherworldinn.compat;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.block.HoneyCauldronBlock;
import com.otherworldinn.block.HoneyCauldronService;
import com.otherworldinn.init.ModBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.api.behaviour.spouting.CauldronSpoutingBehavior;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.fluids.RegisterCauldronFluidContentEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class HoneyCauldronCompat {
    private static boolean createRegistered;

    private HoneyCauldronCompat() {}

    public static void init() {
        if (createRegistered) {
            return;
        }
        createRegistered = true;
        CauldronSpoutingBehavior.CAULDRON_INFO.register(
                AllFluids.HONEY.get(),
                new CauldronSpoutingBehavior.CauldronInfo(
                        HoneyCauldronService.HONEY_PER_LEVEL,
                        HoneyCauldronService.withLevel(1)));
    }

    @SubscribeEvent
    public static void registerCauldronFluids(RegisterCauldronFluidContentEvent event) {
        event.register(
                ModBlocks.HONEY_CAULDRON.get(),
                AllFluids.HONEY.getSource(),
                HoneyCauldronService.MAX_HONEY_LEVEL,
                HoneyCauldronBlock.LEVEL);
    }
}
