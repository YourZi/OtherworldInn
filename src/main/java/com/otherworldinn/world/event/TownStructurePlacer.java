package com.otherworldinn.world.event;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.dimension.TownDimensions;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
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
    public static final int NO_DROPS_REPLACE_FLAGS =
            Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level
                && level.dimension() == TownDimensions.TOWN_LEVEL) {
            ensureCenterChunksAlwaysLoaded(level);
        }
    }

    private static void ensureCenterChunksAlwaysLoaded(ServerLevel level) {
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

    /**
     * 放置结构并抑制替换过程中的方块掉落物。
     */
    public static boolean placeStructureTemplateNoDrops(
            ServerLevel level, ResourceLocation structureId, BlockPos origin) {
        return placeStructureTemplate(level, structureId, origin, NO_DROPS_REPLACE_FLAGS);
    }
}
