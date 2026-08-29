package com.otherworldinn.util;

import com.otherworldinn.world.event.runtime.DisappearManager;
import net.minecraft.world.entity.Entity;

/** 实体工具类。 */
public class EntityUtils {

    /** 随机等待 3-5 秒后移除实体并发出生物死亡粒子。 */
    public static void scheduleDisappear(Entity entity) {
        if (entity == null || entity.level().isClientSide) return;

        int delay = 60 + entity.level().random.nextInt(41);
        DisappearManager.schedule(entity, delay);
    }
}
