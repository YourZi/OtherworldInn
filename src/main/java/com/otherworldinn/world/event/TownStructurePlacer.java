package com.otherworldinn.world.event;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.dimension.TownDimensions;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID)
public class TownStructurePlacer {

    private static final int CENTER_CHUNK_RADIUS = 2;

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level
                && level.dimension() == TownDimensions.TOWN_LEVEL) {
            // 常加载中心 5x5 区块，降低关键设施被卸载风险
            ensureCenterChunksAlwaysLoaded(level);
        }
    }

    private static void ensureCenterChunksAlwaysLoaded(ServerLevel level) {
        // 半径 2 个区块 => (2*2+1)^2 = 25 区块
        for (int chunkX = -CENTER_CHUNK_RADIUS; chunkX <= CENTER_CHUNK_RADIUS; chunkX++) {
            for (int chunkZ = -CENTER_CHUNK_RADIUS; chunkZ <= CENTER_CHUNK_RADIUS; chunkZ++) {
                level.setChunkForced(chunkX, chunkZ, true);
            }
        }
    }

    public static boolean placeStructureTemplate(
            ServerLevel level, ResourceLocation structureId, BlockPos origin) {
        return placeStructureTemplate(level, structureId, origin, 2);
    }

    /**
     * 放置结构，可控制是否产生方块掉落物。
     *
     * @param flags 标志位：2 = 方块更新，18 = 方块更新 + 不掉落
     */
    public static boolean placeStructureTemplate(
            ServerLevel level, ResourceLocation structureId, BlockPos origin, int flags) {
        StructureTemplateManager manager = level.getStructureManager();
        Optional<StructureTemplate> templateOptional = manager.get(structureId);
        if (templateOptional.isEmpty()) {
            OtherworldInn.LOGGER.warn("Facility structure not found: {}", structureId);
            return false;
        }

        StructureTemplate template = templateOptional.get();
        StructurePlaceSettings settings =
                new StructurePlaceSettings()
                        .setRotation(Rotation.NONE)
                        .setMirror(Mirror.NONE)
                        .setIgnoreEntities(false);
        return template.placeInWorld(level, origin, BlockPos.ZERO, settings, level.getRandom(), flags);
    }
}
