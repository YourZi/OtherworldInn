package com.otherworldinn.world.event.listener;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.dimension.TownDimensions;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * 城镇常加载区块管理器
 *
 * <p>保持以 (0,0) 为中心、半径 3 范围内的区块常加载，使女仆等实体在玩家离开城镇后仍能继续工作。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class TownChunkLoader {

    /** 以 (0,0) 为中心、半径 3 的区块范围（7*7 区块） */
    private static final int CHUNK_RADIUS = 3;

    private TownChunkLoader() {}

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.dimension() != TownDimensions.TOWN_LEVEL) return;

        for (int cx = -CHUNK_RADIUS; cx <= CHUNK_RADIUS; cx++) {
            for (int cz = -CHUNK_RADIUS; cz <= CHUNK_RADIUS; cz++) {
                serverLevel.setChunkForced(cx, cz, true);
            }
        }
    }
}
