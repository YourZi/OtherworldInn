package com.otherworldinn.world.map;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.inn.facility.FacilityRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

/**
 * 城镇地图数据提供者
 *
 * <p>负责管理城镇中的所有地图点（POI）。 当前使用静态数据
 */
public class TownDataProvider {

    private static final List<MapPoint> POINTS = new ArrayList<>();
    private static final Map<ResourceLocation, MapPoint> POINT_INDEX = new LinkedHashMap<>();

    static {
        // 初始化城镇关键点
        // 坐标和屏幕偏移为当前配置值

        // 旅社 (Inn)
        registerPoint(
                new MapPoint(
                        ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "inn"),
                        new Vec3(27, 71, 0),
                        new Vec2(-20, -40),
                        MapIconAtlas.ATLAS_TEXTURE,
                        MapIconAtlas.SLOT_INN,
                        Component.translatable("map_point.otherworldinn.inn"),
                        MapPoint.MapPointType.SHOP,
                        null // 默认解锁
                        ));

        // 铁匠铺 (Blacksmith)
        registerPoint(
                new MapPoint(
                        ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "blacksmith"),
                        new Vec3(17, 71, 3),
                        new Vec2(0, -40),
                        MapIconAtlas.ATLAS_TEXTURE,
                        MapIconAtlas.SLOT_BLACKSMITH,
                        Component.translatable("map_point.otherworldinn.blacksmith"),
                        MapPoint.MapPointType.SHOP,
                        null // 默认解锁
                        ));

        // 魔女工坊 (Magician Workshop)
        registerPoint(
                new MapPoint(
                        ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "magician_workshop"),
                        new Vec3(-1, 71, 16),
                        new Vec2(20, -40),
                        MapIconAtlas.ATLAS_TEXTURE,
                        MapIconAtlas.SLOT_MAGICIAN_WORKSHOP,
                        Component.translatable("map_point.otherworldinn.magician_workshop"),
                        MapPoint.MapPointType.SHOP,
                        null // 默认解锁
                        ));

        // 集市 (Market)
        registerPoint(
                new MapPoint(
                        ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "market"),
                        new Vec3(3, 71, -21),
                        new Vec2(10, -10),
                        MapIconAtlas.ATLAS_TEXTURE,
                        MapIconAtlas.SLOT_MARKET,
                        Component.translatable("map_point.otherworldinn.market"),
                        MapPoint.MapPointType.SHOP,
                        null // 默认解锁
                        ));

        // 城镇大门 (Town Gate)
        registerPoint(
                new MapPoint(
                        ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "town_gate"),
                        new Vec3(-30, 71, 0),
                        new Vec2(0, 0),
                        MapIconAtlas.ATLAS_TEXTURE,
                        MapIconAtlas.SLOT_TOWN_GATE,
                        Component.translatable("map_point.otherworldinn.town_gate"),
                        MapPoint.MapPointType.EXIT_GATE,
                        null // 默认解锁
                        ));

        // 码头 (Dock)
        registerPoint(
                new MapPoint(
                        ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "dock"),
                        new Vec3(70, 74, -80),
                        new Vec2(70, 30),
                        MapIconAtlas.ATLAS_TEXTURE,
                        MapIconAtlas.SLOT_DOCK,
                        Component.translatable("map_point.otherworldinn.dock"),
                        MapPoint.MapPointType.LANDMARK,
                        null));

        syncFacilityPoints();
    }

    private static void registerPoint(MapPoint point) {
        if (point == null || point.id() == null) {
            return;
        }
        if (POINT_INDEX.containsKey(point.id())) {
            return;
        }
        POINT_INDEX.put(point.id(), point);
        POINTS.add(point);
    }

    private static void syncFacilityPoints() {
        for (FacilityRegistry.FacilityDefinition facility : FacilityRegistry.getAll()) {
            ResourceLocation pointId = facility.mapPointId();
            if (POINT_INDEX.containsKey(pointId)) {
                continue;
            }
            FacilityRegistry.FacilityMapPointConfig config = facility.mapPointConfig();
            registerPoint(
                    new MapPoint(
                            pointId,
                            config.worldPosition(),
                            config.screenOffset(),
                            MapIconAtlas.ATLAS_TEXTURE,
                            MapIconAtlas.slotForPointId(pointId),
                            Component.translatable(facility.translationKey()),
                            config.type(),
                            "facility_locked"));
        }
    }

    /**
     * 获取所有地图点
     *
     * @return 地图点列表
     */
    public static List<MapPoint> getPoints() {
        syncFacilityPoints();
        return POINTS;
    }

    /**
     * 根据 ID 获取地图点
     *
     * @param id 地图点 ID
     * @return Optional 地图点
     */
    public static Optional<MapPoint> getPoint(ResourceLocation id) {
        syncFacilityPoints();
        return Optional.ofNullable(POINT_INDEX.get(id));
    }
}
