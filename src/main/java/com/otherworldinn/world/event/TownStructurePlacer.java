package com.otherworldinn.world.event;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.event.listener.TownZonePolicyService;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.core.Vec3i;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID)
public class TownStructurePlacer {

    /** 城镇中心常加载区块半径（结构贴图要求目标区块已加载，装饰区域选点须落在该范围内） */
    public static final int CENTER_CHUNK_RADIUS = 2;
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
        return placeStructureTemplate(
                level,
                structureId,
                origin,
                NO_DROPS_REPLACE_FLAGS,
                TownZonePolicyService.ProtectionBypassReason.FACILITY_UPGRADE);
    }

    /**
     * 放置结构，可控制是否产生方块掉落物。
     */
    public static boolean placeStructureTemplate(
            ServerLevel level, ResourceLocation structureId, BlockPos origin, int flags) {
        return placeStructureTemplate(
                level,
                structureId,
                origin,
                flags,
                TownZonePolicyService.ProtectionBypassReason.FACILITY_UPGRADE);
    }

    public static boolean placeStructureTemplate(
            ServerLevel level,
            ResourceLocation structureId,
            BlockPos origin,
            int flags,
            TownZonePolicyService.ProtectionBypassReason bypassReason) {
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
        try (TownZonePolicyService.ProtectionBypassScope ignored =
                TownZonePolicyService.beginProtectionBypass(
                        bypassReason,
                        collectBoundingBoxPositions(origin, template.getSize()))) {
            return template.placeInWorld(level, origin, BlockPos.ZERO, settings, level.getRandom(), flags);
        }
    }

    /**
     * 放置结构并抑制替换过程中的方块掉落物。
     */
    public static boolean placeStructureTemplateNoDrops(
            ServerLevel level, ResourceLocation structureId, BlockPos origin) {
        return placeStructureTemplate(
                level,
                structureId,
                origin,
                NO_DROPS_REPLACE_FLAGS,
                TownZonePolicyService.ProtectionBypassReason.TRADER_SHIP_REBUILD);
    }

    private static Set<BlockPos> collectBoundingBoxPositions(BlockPos origin, Vec3i size) {
        java.util.Set<BlockPos> positions = new java.util.HashSet<>();
        int maxX = origin.getX() + Math.max(0, size.getX() - 1);
        int maxY = origin.getY() + Math.max(0, size.getY() - 1);
        int maxZ = origin.getZ() + Math.max(0, size.getZ() - 1);
        for (BlockPos pos : BlockPos.betweenClosed(origin, new BlockPos(maxX, maxY, maxZ))) {
            positions.add(pos.immutable());
        }
        return positions;
    }
}
