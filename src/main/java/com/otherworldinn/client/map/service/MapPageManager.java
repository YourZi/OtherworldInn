package com.otherworldinn.client.map.service;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.inn.facility.FacilityRegistry;
import com.otherworldinn.world.map.TownDataProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * 地图页面管理器
 *
 * <p>负责管理地图视图的分页逻辑和各页面包含的地图点。 采用网格坐标系统 (gridX, gridZ) 来索引页面，默认起始页为 (0, 0)。 页面在虚拟空间中的间距由 {@link
 * #PAGE_SPACING} 定义。
 */
public class MapPageManager {

    private static final MapPageManager INSTANCE = new MapPageManager();

    /** 页面在虚拟坐标系中的间距（方块单位） */
    public static final int PAGE_SPACING = 60;

    private final Set<PagePos> registeredPages = new HashSet<>();
    private final Map<PagePos, Set<ResourceLocation>> pagePoints = new HashMap<>();

    private MapPageManager() {
        initDefaultPages();
    }

    public static MapPageManager getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化默认的地图页面配置
     *
     * <p>包含初始页面 (0,0) 和示例扩展页面 (1,0)，以及各页面的地图点分配。
     */
    private void initDefaultPages() {
        // 注册第 0 页
        registerPage(0, 0);

        // 注册地图页面
        addPage(0, 0, Direction.EAST);
        addPage(0, -1, Direction.EAST);

        addPage(0, 0, Direction.SOUTH);

        addPage(0, 0, Direction.NORTH);


        // 主页面
        registerPoint(0, 0, ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "inn"));
        registerPoint(
                0, 0, ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "blacksmith"));
        registerPoint(0, 0, ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "town_gate"));
        registerPoint(
                0, 0, ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "magician_workshop"));
        registerPoint(
                0, 0, ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "market"));

        // 旅社方向页
        registerPoint(1, 0, ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "inn"));
        registerPoint(
                1, 0, ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "blacksmith"));
        registerPoint(
                1, 0, ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "boiler_room"));
        registerPoint(
                1, -1, ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "dock"));

        syncFacilityPointsToPages();
    }

    private void syncFacilityPointsToPages() {
        TownDataProvider.getPoints();
        for (FacilityRegistry.FacilityDefinition facility : FacilityRegistry.getAll()) {
            FacilityRegistry.FacilityMapPointConfig config = facility.mapPointConfig();
            registerPoint(config.pageX(), config.pageZ(), facility.mapPointId());
        }
    }

    /**
     * 注册一个新的地图页面
     *
     * @param gridX 网格 X 坐标
     * @param gridZ 网格 Z 坐标
     */
    public void registerPage(int gridX, int gridZ) {
        PagePos pos = new PagePos(gridX, gridZ);
        registeredPages.add(pos);
        pagePoints.putIfAbsent(pos, new HashSet<>());
    }

    /**
     * 将地图点关联到指定页面
     *
     * <p>如果目标页面尚未注册，会自动注册该页面。
     *
     * @param gridX 页面网格 X 坐标
     * @param gridZ 页面网格 Z 坐标
     * @param pointId 地图点的资源 ID
     */
    public void registerPoint(int gridX, int gridZ, ResourceLocation pointId) {
        PagePos pos = new PagePos(gridX, gridZ);
        if (!registeredPages.contains(pos)) {
            registerPage(gridX, gridZ);
        }
        pagePoints.get(pos).add(pointId);
    }

    /**
     * 获取指定页面包含的所有地图点 ID
     *
     * @param gridX 页面网格 X 坐标
     * @param gridZ 页面网格 Z 坐标
     * @return 包含地图点 ID 的不可变集合，如果页面不存在则返回空集合
     */
    public Set<ResourceLocation> getPointsForPage(int gridX, int gridZ) {
        syncFacilityPointsToPages();
        PagePos pos = new PagePos(gridX, gridZ);
        return pagePoints.getOrDefault(pos, Collections.emptySet());
    }

    /**
     * 在现有页面的指定方向上扩展新页面
     *
     * <p>例如：在 (0,0) 的 {@link Direction#WEST} 方向添加页面，会创建 (-1, 0) 页。
     *
     * @param sourceGridX 源页面网格 X 坐标
     * @param sourceGridZ 源页面网格 Z 坐标
     * @param direction 扩展方向 (NORTH, SOUTH, EAST, WEST)
     */
    public void addPage(int sourceGridX, int sourceGridZ, Direction direction) {
        int newX = sourceGridX + direction.getStepX();
        int newZ = sourceGridZ + direction.getStepZ();
        registerPage(newX, newZ);
    }

    /**
     * 检查指定坐标是否存在已注册的页面
     *
     * @param gridX 页面网格 X 坐标
     * @param gridZ 页面网格 Z 坐标
     * @return 如果页面存在返回 true，否则返回 false
     */
    public boolean hasPage(int gridX, int gridZ) {
        return registeredPages.contains(new PagePos(gridX, gridZ));
    }

    /**
     * 页面坐标记录类
     *
     * <p>用于作为 Map 的键或 Set 的元素，基于 (x, z) 坐标判定相等性。
     */
    public record PagePos(int x, int z) {}
}
