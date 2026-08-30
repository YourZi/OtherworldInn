package com.otherworldinn.compat.exposure.client;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.compat.ExposureCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * 拍照任务取景提示的客户端调度器（仅 tick 判定，渲染由 MixinExposureViewfinderOverlay 完成）。
 *
 * <p>本类禁止引用任何 Exposure 类：Exposure 是软依赖，@EventBusSubscriber 自动注册的监听器
 * 在 Exposure 缺失时也会被类加载，所有 Exposure 调用必须隔离在 {@link PhotoTargetHintService}
 * 中，由 {@link ExposureCompat#isLoaded()} 守门后按需加载。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public final class PhotoTargetHintClientHandler {
    private PhotoTargetHintClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ExposureCompat.isLoaded()) {
            PhotoTargetHintService.tick();
        }
    }
}
