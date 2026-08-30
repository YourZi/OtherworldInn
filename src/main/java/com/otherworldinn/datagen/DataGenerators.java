package com.otherworldinn.datagen;

import com.otherworldinn.OtherworldInn;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * DataGen 入口：监听 GatherDataEvent，注册客户端（模型、语言）与服务端（标签、战利品表、配方）数据提供者
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();

        // 客户端数据提供者 (Client Providers)

        event.getGenerator()
                .addProvider(
                        event.includeClient(),
                        new ModBlockStateProvider(packOutput, existingFileHelper));

        event.getGenerator()
                .addProvider(
                        event.includeClient(),
                        new ModItemModelProvider(packOutput, existingFileHelper));

        event.getGenerator()
                .addProvider(event.includeClient(), new ModLanguageProvider(packOutput, "en_us"));

        event.getGenerator()
                .addProvider(event.includeClient(), new ModLanguageProvider(packOutput, "zh_cn"));

        // 服务端数据提供者 (Server Providers)

        ModBlockTagProvider blockTagProvider =
                new ModBlockTagProvider(packOutput, lookupProvider, existingFileHelper);
        event.getGenerator().addProvider(event.includeServer(), blockTagProvider);

        event.getGenerator()
                .addProvider(
                        event.includeServer(),
                        new ModItemTagProvider(
                                packOutput,
                                lookupProvider,
                                blockTagProvider.contentsGetter(),
                                existingFileHelper));

        event.getGenerator()
                .addProvider(
                        event.includeServer(),
                        new ModLootTableProvider(packOutput, lookupProvider));

        event.getGenerator()
                .addProvider(
                        event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
    }
}
