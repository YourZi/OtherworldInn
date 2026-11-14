package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.command.HusbandryDebugCommand;
import com.otherworldinn.client.command.SnowCoverCommand;
import com.otherworldinn.command.PanoramixScreenshotCommands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ModClientCommandEvents {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        PanoramixScreenshotCommands.register(event.getDispatcher());
        HusbandryDebugCommand.register(event.getDispatcher());
        SnowCoverCommand.register(event.getDispatcher());
    }
}
