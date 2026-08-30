package com.otherworldinn.world.festival;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.data.TownSavedData;
import com.otherworldinn.world.event.TownStructurePlacer;
import com.otherworldinn.world.event.listener.TownZonePolicyService;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

/**
 * 节日装饰服务：成对结构快照（节日版/还原版，含空气全量覆盖、天然幂等）的世界写入与状态对账。
 * 还原版会抹掉装饰期间区域内的一切变化（城镇保护下公共区域不可被玩家改动，可接受）；装饰结构不携带实体。
 * 状态持久化于 {@link TownSavedData#getDecoratedFestivalId()}，服务器启动后首个有效 tick 执行 {@link #reconcile} 强制到目标态（崩溃/中断自愈）。
 */
public final class FestivalDecorationService {
    private FestivalDecorationService() {}

    /**
     * 一个装饰区域：节日版/还原版快照对 + 一组放置原点。
     *
     * <p>同一对快照可复用于多个原点（如灯串段贴满主街）。两个快照必须同原点、同尺寸导出。
     */
    public record DecorationRegion(
            ResourceLocation decoratedStructure,
            ResourceLocation baseStructure,
            List<BlockPos> origins) {
        public DecorationRegion(
                ResourceLocation decoratedStructure, ResourceLocation baseStructure, BlockPos origin) {
            this(decoratedStructure, baseStructure, List.of(origin));
        }

        public DecorationRegion {
            origins = List.copyOf(origins);
        }
    }

    /** 节日开始：全部原点贴节日版快照，并记录装饰状态 */
    public static void apply(ServerLevel townLevel, String festivalId, List<DecorationRegion> regions) {
        for (DecorationRegion region : regions) {
            for (BlockPos origin : region.origins()) {
                if (isPlacementAllowed(region, origin)) {
                    TownStructurePlacer.placeStructureTemplate(
                            townLevel,
                            region.decoratedStructure(),
                            origin,
                            TownStructurePlacer.NO_DROPS_REPLACE_FLAGS,
                            TownZonePolicyService.ProtectionBypassReason.FESTIVAL_DECORATION);
                }
            }
        }
        TownSavedData.get(townLevel).setDecoratedFestivalId(festivalId);
    }

    /** 节日结束：全部原点贴还原版快照，并清除装饰状态 */
    public static void restore(ServerLevel townLevel, List<DecorationRegion> regions) {
        for (DecorationRegion region : regions) {
            for (BlockPos origin : region.origins()) {
                if (isPlacementAllowed(region, origin)) {
                    TownStructurePlacer.placeStructureTemplate(
                            townLevel,
                            region.baseStructure(),
                            origin,
                            TownStructurePlacer.NO_DROPS_REPLACE_FLAGS,
                            TownZonePolicyService.ProtectionBypassReason.FESTIVAL_DECORATION);
                }
            }
        }
        TownSavedData.get(townLevel).setDecoratedFestivalId(null);
    }

    /**
     * 启动对账：当前激活节日（含调试开关）与持久化装饰态不一致时强制到目标态。
     * 节日已从注册表注销时仅清除状态。
     */
    public static void reconcile(ServerLevel townLevel) {
        TownSavedData data = TownSavedData.get(townLevel);
        String decorated = data.getDecoratedFestivalId();
        String activeId = FestivalService.getActiveFestival(townLevel)
                .map(FestivalDefinition::id)
                .orElse(null);
        if (Objects.equals(activeId, decorated)) {
            return;
        }
        if (decorated != null) {
            List<DecorationRegion> regions = findRegions(decorated);
            if (regions != null) {
                restore(townLevel, regions);
            } else {
                data.setDecoratedFestivalId(null);
            }
        }
        if (activeId != null) {
            List<DecorationRegion> regions = findRegions(activeId);
            if (regions != null) {
                apply(townLevel, activeId, regions);
            }
        }
    }

    /** 从注册表反查节日的装饰区域；节日存在但未挂装饰 Effect 时返回空列表，节日不存在返回 null */
    @Nullable
    private static List<DecorationRegion> findRegions(String festivalId) {
        for (FestivalDefinition festival : FestivalRegistry.getAll()) {
            if (festival.id().equals(festivalId)) {
                for (FestivalEffect effect : festival.effects()) {
                    if (effect instanceof FestivalDecorationEffect decoration) {
                        return decoration.regions();
                    }
                }
                return List.of();
            }
        }
        return null;
    }

    /** 放置前提：原点须在城镇常加载区块范围内（快照贴图要求目标区块已加载） */
    private static boolean isPlacementAllowed(DecorationRegion region, BlockPos origin) {
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        if (Math.abs(chunkX) > TownStructurePlacer.CENTER_CHUNK_RADIUS
                || Math.abs(chunkZ) > TownStructurePlacer.CENTER_CHUNK_RADIUS) {
            OtherworldInn.LOGGER.warn(
                    "Festival decoration {} skipped: origin {} outside always-loaded chunks",
                    region.decoratedStructure(),
                    origin.toShortString());
            return false;
        }
        return true;
    }
}
