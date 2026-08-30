package com.otherworldinn.world.event.listener;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.dimension.TownDimensions;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

/** 魔法空间常加载区块管理器。 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class MagicSpaceChunkLoader {

    /** 7*7 区块 */
    private static final int CHUNK_RADIUS = 3;

    private MagicSpaceChunkLoader() {}

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.dimension() != TownDimensions.MAGIC_SPACE_LEVEL) return;

        for (int cx = -CHUNK_RADIUS; cx <= CHUNK_RADIUS; cx++) {
            for (int cz = -CHUNK_RADIUS; cz <= CHUNK_RADIUS; cz++) {
                serverLevel.setChunkForced(cx, cz, true);
            }
        }
    }
}
