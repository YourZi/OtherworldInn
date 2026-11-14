package com.otherworldinn.client.snow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 把方块拆成"可承接积雪的水平表面"的缓存。
 *
 * <p>形状和模型分工：
 * <ul>
 *   <li>{@code getShape} 决定"哪些区域算暴露的上表面"（遮挡扣除、四边围合、去重）；</li>
 *   <li>模型（{@link SnowModelHeight}）决定"这个表面到底在多高"以及"它挡住了下面多少"。</li>
 * </ul>
 *
 * <p>低于 2/16 的面（地毯、轨道）和面积小于 1/64 格的面（细杆）忽略；
 * 同一高度上互相重叠的面要删掉，否则会重合面 z-fighting。
 *
 * <p>键为 {@link BlockState}：方块状态已经把朝向/连接关系编码进去了，因此对绝大多数方块是稳定的。
 */
public final class SnowSurfaceCache {

    private static final float EPSILON = 1.0E-4F;
    /** 低于 2/16 的面不覆雪（地毯、铁轨之类）。 */
    private static final float MIN_SURFACE_Y = 2.0F / 16.0F;
    /** 面积小于 1/64 格的面不覆雪（细杆、告示牌立杆）。 */
    private static final float MIN_SURFACE_AREA = 1.0F / 64.0F;
    /** 判定"墙贴着边"的容差。 */
    private static final double EDGE_TOLERANCE = 1.0E-3;
    /**
     * 遮挡体底面和表面之间小于这个空隙就算"贴死"，雪不会钻进去。
     *
     * <p>雪板本身厚 2/16，缝比这还窄的话物理上也塞不进去；缝更宽就当作风雪从侧面灌进来了。
     */
    private static final float MIN_CLEARANCE = 2.0F / 16.0F;

    private static final Map<BlockState, List<SnowSurface>> CACHE = new ConcurrentHashMap<>();
    private static final Map<BlockState, VoxelShape> SHAPES = new ConcurrentHashMap<>();
    private static final Map<BlockState, float[]> SHAPE_OCCLUSIONS = new ConcurrentHashMap<>();

    private SnowSurfaceCache() {}

    /** 取某个方块状态的可覆雪表面；首次计算后进入缓存（工作线程安全）。 */
    public static List<SnowSurface> get(BlockState state, BlockGetter level, BlockPos pos) {
        List<SnowSurface> cached = CACHE.get(state);
        if (cached != null) {
            return cached;
        }
        return CACHE.computeIfAbsent(state, key -> compute(key, level, pos));
    }

    private static List<SnowSurface> compute(BlockState state, BlockGetter level, BlockPos pos) {
        return computeSurfaces(shapeOf(state, level, pos), SnowModelHeight.of(state));
    }

    /** 方块视觉形状，按方块状态缓存（工作线程安全）。 */
    public static VoxelShape shapeOf(BlockState state, BlockGetter level, BlockPos pos) {
        VoxelShape cached = SHAPES.get(state);
        if (cached != null) {
            return cached;
        }
        return SHAPES.computeIfAbsent(state, key -> key.getShape(level, pos, CollisionContext.empty()));
    }

    /** 上方某个方块对下方表面的遮挡：它所在的高度 + 每像素最低的几何面。 */
    public record Occluder(int blockY, float[] bottoms) {}

    /**
     * 判断"算不算被挡住"时允许的最大空隙。
     *
     * <p>门槛给得宽（8 格 ≈ 扫描深度），因为"是不是真的被遮住"由**覆盖比例**来决定：
     * 屋顶这种整格遮挡覆盖率接近 100%，栅栏只有 30% 左右。空隙只用来排除"差得离谱"的情况。
     */
    private static final float SHELTER_CLEARANCE = 8.0F;

    /**
     * 这些表面有多大比例被上方的遮挡盖住（0~1）。
     *
     * <p>用来区分"真的被屋顶压着"和"只是上面有根栅栏/栏杆"：
     * 栅栏只占 16x16 里的十来格（约 30%），不该把整列判成遮蔽。
     */
    public static float coveredRatio(List<SnowSurface> surfaces, List<Occluder> occluders, int surfaceBlockY) {
        if (surfaces.isEmpty() || occluders.isEmpty()) {
            return 0.0F;
        }
        float[][] grid = new float[16][16];
        for (SnowSurface surface : surfaces) {
            fillGrid(grid, surface);
        }
        boolean[] hasSurface = new boolean[256];
        int total = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (grid[x][z] > 0.0F) {
                    hasSurface[x + z * 16] = true;
                    total++;
                }
            }
        }
        if (total == 0) {
            return 0.0F;
        }
        boolean[] covered = new boolean[256];
        for (Occluder occluder : occluders) {
            int offset = occluder.blockY() - surfaceBlockY;
            for (int index = 0; index < 256; index++) {
                if (!hasSurface[index]) {
                    continue;
                }
                float bottom = occluder.bottoms()[index];
                if (bottom < 0.0F) {
                    continue;
                }
                float surfaceY = grid[index & 15][index >> 4];
                if (offset + bottom - surfaceY < SHELTER_CLEARANCE) {
                    covered[index] = true;
                }
            }
        }
        int count = 0;
        for (int index = 0; index < 256; index++) {
            if (covered[index]) {
                count++;
            }
        }
        return count / (float) total;
    }

    /**
     * 这个方块压在下面时，每像素最低的几何面有多低。
     *
     * <p>优先用模型投影（草的十字面片投影面积为零 → 不算遮挡；
     * 栅栏横杆底面在 6/16，于是横杆底下的雪会保留）。没有模型（方块实体渲染）时退回形状的 AABB。
     */
    public static float[] bottomsOf(BlockState state, BlockGetter level, BlockPos pos) {
        float[] model = SnowModelHeight.bottoms(state);
        if (model != null) {
            return model;
        }
        float[] cached = SHAPE_OCCLUSIONS.get(state);
        if (cached != null) {
            return cached;
        }
        float[] rasterized = rasterizeShape(shapeOf(state, level, pos));
        SHAPE_OCCLUSIONS.put(state, rasterized);
        return rasterized;
    }

    private static float[] rasterizeShape(VoxelShape shape) {
        float[] bottoms = new float[256];
        java.util.Arrays.fill(bottoms, -1.0F);
        for (AABB box : shape.toAabbs()) {
            int x0 = Math.max(0, (int) Math.ceil(box.minX * 16.0 - 0.5));
            int x1 = Math.min(15, (int) Math.floor(box.maxX * 16.0 - 0.5));
            int z0 = Math.max(0, (int) Math.ceil(box.minZ * 16.0 - 0.5));
            int z1 = Math.min(15, (int) Math.floor(box.maxZ * 16.0 - 0.5));
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    int index = x + z * 16;
                    bottoms[index] = bottoms[index] < 0.0F
                            ? (float) box.minY
                            : Math.min(bottoms[index], (float) box.minY);
                }
            }
        }
        return bottoms;
    }

    /**
     * 去掉被"贴死"的遮挡盖住的像素，剩下的按高度重新合并成矩形。
     *
     * <p>关键点：不是"上方有东西就挖掉"，而是看**上方那东西的底面离这个表面多高**。
     * 缝隙大于一个雪板厚度就认为风雪能从侧面灌进来，雪照铺；贴着面的（整块方块、地毯、
     * 底面半砖、栅栏立柱）才真正挖洞。
     */
    public static List<SnowSurface> cutMasks(
            List<SnowSurface> surfaces, List<Occluder> occluders, int surfaceBlockY) {
        if (surfaces.isEmpty() || occluders.isEmpty()) {
            return surfaces;
        }
        float[][] grid = new float[16][16];
        for (SnowSurface surface : surfaces) {
            fillGrid(grid, surface);
        }
        float[] surfaceHeights = new float[256];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                surfaceHeights[x + z * 16] = grid[x][z];
            }
        }
        for (Occluder occluder : occluders) {
            int offset = occluder.blockY() - surfaceBlockY;
            for (int index = 0; index < 256; index++) {
                float bottom = occluder.bottoms()[index];
                if (bottom < 0.0F) {
                    continue;
                }
                float surfaceY = surfaceHeights[index];
                if (surfaceY <= 0.0F) {
                    continue;
                }
                if (offset + bottom - surfaceY < MIN_CLEARANCE) {
                    grid[index & 15][index >> 4] = 0.0F;
                }
            }
        }
        return mergeCells(grid);
    }

    /** 纯几何部分：只按形状拆表面（便于脱离游戏单独验证）。 */
    static List<SnowSurface> computeSurfaces(VoxelShape shape) {
        return computeSurfaces(shape, null);
    }

    /**
     * 形状 + 模型高度表。
     *
     * @param heights 见 {@link SnowModelHeight#of}，为 null 时纯按形状
     */
    static List<SnowSurface> computeSurfaces(VoxelShape shape, float[] heights) {
        if (shape.isEmpty()) {
            return List.of();
        }
        List<AABB> boxes = shape.toAabbs();
        List<SnowSurface> candidates = new ArrayList<>(boxes.size());
        for (AABB box : boxes) {
            float y = (float) Math.min(box.maxY, 1.0);
            if (y < MIN_SURFACE_Y) {
                continue;
            }
            if (box.getXsize() < EPSILON || box.getZsize() < EPSILON) {
                continue;
            }
            List<Rect> remaining = new ArrayList<>(2);
            remaining.add(new Rect((float) box.minX, (float) box.minZ, (float) box.maxX, (float) box.maxZ));
            for (AABB other : boxes) {
                if (other == box) {
                    continue;
                }
                // 只要"有几何在这块顶面上方"就要扣掉：既包括压在它上面的盒子（栅栏横杆之间的立柱），
                // 也包括从上方悬出来的部分（铁砧顶部那圈悬空的檐）。只比它低的盒子不影响。
                if (other.maxY <= y + EPSILON) {
                    continue;
                }
                remaining = subtract(remaining, new Rect(
                        (float) other.minX, (float) other.minZ, (float) other.maxX, (float) other.maxZ));
                if (remaining.isEmpty()) {
                    break;
                }
            }
            for (Rect rect : remaining) {
                if (rect.area() < MIN_SURFACE_AREA) {
                    continue;
                }
                if (isEnclosed(boxes, box, rect, y)) {
                    // 四边都被更高的盒子围住：这是容器内腔/凹槽底，不当作"承接雪的顶面"
                    continue;
                }
                candidates.add(new SnowSurface(rect.x0, rect.z0, rect.x1, rect.z1, y));
            }
        }
        // 先去重再定高会把"同一高度但区域重叠"的候选切成一像素宽的碎片，
        // 所以顺序必须是：先用模型把高度压准，再按高度去重。
        if (heights == null) {
            return dropOverlaps(candidates);
        }
        return dropOverlaps(applyModelHeights(candidates, heights, computeSunken(heights)));
    }

    /**
     * 用模型高度表把形状给出的表面"压"到真实高度上，并按格子重新合并。
     *
     * <p>形状只负责"这块区域算不算暴露的上表面"，高度一律以模型为准：
     * 模型在某格没有朝上的面，就在那一格不铺雪（宁可漏，不可浮空）。
     */
    private static List<SnowSurface> applyModelHeights(
            List<SnowSurface> surfaces, float[] heights, boolean[] sunken) {
        List<SnowSurface> out = new ArrayList<>(surfaces.size());
        for (SnowSurface surface : surfaces) {
            out.addAll(cellsToRects(surface, heights, sunken));
        }
        return out;
    }

    /**
     * 找出"陷在自家几何里的"像素：四个方向都被更高的模型几何挡住，直到方块边界。
     *
     * <p>典型例子是炼药锅/堆肥桶/各种储罐的内腔底——那是一个盆地，雪铺上去就是"容器里放了一块雪板"，
     * 装了液体的话还会浮在液面上。反过来，台阶的下级、铁砧的窄檐、栅栏横杆都能沿某个方向通到边界，
     * 不会被误判。
     */
    private static boolean[] computeSunken(float[] heights) {
        boolean[] sunken = new boolean[256];
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                float height = heights[x + z * 16];
                if (height <= 0.0F) {
                    continue;
                }
                if (blocked(heights, x, z, 1, 0, height)
                        && blocked(heights, x, z, -1, 0, height)
                        && blocked(heights, x, z, 0, 1, height)
                        && blocked(heights, x, z, 0, -1, height)) {
                    sunken[x + z * 16] = true;
                }
            }
        }
        return sunken;
    }

    private static boolean blocked(float[] heights, int x, int z, int stepX, int stepZ, float height) {
        int cursorX = x + stepX;
        int cursorZ = z + stepZ;
        while (cursorX >= 0 && cursorX <= 15 && cursorZ >= 0 && cursorZ <= 15) {
            if (heights[cursorX + cursorZ * 16] > height + EPSILON) {
                return true;
            }
            cursorX += stepX;
            cursorZ += stepZ;
        }
        return false;
    }

    /** 把一块表面按 16x16 的高度表切分，并把同高度的相邻格子贪心合并成矩形。 */
    private static List<SnowSurface> cellsToRects(SnowSurface surface, float[] heights, boolean[] sunken) {
        float[][] grid = new float[16][16];
        fillGrid(grid, surface, heights, sunken);
        List<SnowSurface> merged = mergeCells(grid);
        List<SnowSurface> out = new ArrayList<>(merged.size());
        for (SnowSurface piece : merged) {
            // 对栅格没有对齐的矩形做一次夹取
            float x0 = Math.max(surface.x0(), piece.x0());
            float x1 = Math.min(surface.x1(), piece.x1());
            float z0 = Math.max(surface.z0(), piece.z0());
            float z1 = Math.min(surface.z1(), piece.z1());
            if ((x1 - x0) * (z1 - z0) < MIN_SURFACE_AREA) {
                continue;
            }
            out.add(new SnowSurface(x0, z0, x1, z1, piece.y()));
        }
        return out;
    }

    private static void fillGrid(float[][] grid, SnowSurface surface) {
        fillGrid(grid, surface, null, null);
    }

    private static void fillGrid(float[][] grid, SnowSurface surface, float[] heights, boolean[] sunken) {
        for (int x = 0; x < 16; x++) {
            float centerX = (x + 0.5F) / 16.0F;
            if (centerX < surface.x0() - EPSILON || centerX > surface.x1() + EPSILON) {
                continue;
            }
            for (int z = 0; z < 16; z++) {
                float centerZ = (z + 0.5F) / 16.0F;
                if (centerZ < surface.z0() - EPSILON || centerZ > surface.z1() + EPSILON) {
                    continue;
                }
                if (sunken != null && sunken[x + z * 16]) {
                    continue;
                }
                float height = heights == null ? surface.y() : heights[x + z * 16];
                grid[x][z] = Math.max(grid[x][z], height);
            }
        }
    }

    /** 16x16 的格子图贪心合并成矩形。 */
    private static List<SnowSurface> mergeCells(float[][] grid) {
        List<SnowSurface> out = new ArrayList<>(4);
        boolean[][] used = new boolean[16][16];
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                if (used[x][z] || grid[x][z] <= 0.0F) {
                    continue;
                }
                float height = grid[x][z];
                int width = 1;
                while (x + width < 16 && !used[x + width][z] && grid[x + width][z] == height) {
                    width++;
                }
                int depth = 1;
                boolean extend = true;
                while (extend && z + depth < 16) {
                    for (int offset = 0; offset < width; offset++) {
                        if (used[x + offset][z + depth] || grid[x + offset][z + depth] != height) {
                            extend = false;
                            break;
                        }
                    }
                    if (extend) {
                        depth++;
                    }
                }
                for (int dz = 0; dz < depth; dz++) {
                    for (int dx = 0; dx < width; dx++) {
                        used[x + dx][z + dz] = true;
                    }
                }
                if (width * depth / 256.0F < MIN_SURFACE_AREA) {
                    continue;
                }
                out.add(new SnowSurface(
                        x / 16.0F, z / 16.0F, (x + width) / 16.0F, (z + depth) / 16.0F, height));
            }
        }
        return out;
    }

    /**
     * 判断这个面是不是"四面被围住"的内腔底。
     *
     * <p>典型例子：炼药锅、堆肥桶、漏斗的内腔——形状上它们确实有一个朝上的平面，
     * 但那是容器内部，铺雪会像是在容器里放了一块雪板，直接跳过。
     */
    private static boolean isEnclosed(List<AABB> boxes, AABB self, Rect rect, float y) {
        return hasWall(boxes, self, rect, y, 0, -1) // 西
                && hasWall(boxes, self, rect, y, 0, 1) // 东
                && hasWall(boxes, self, rect, y, -1, 0) // 北
                && hasWall(boxes, self, rect, y, 1, 0); // 南
    }

    private static boolean hasWall(List<AABB> boxes, AABB self, Rect rect, float y, int axis, int sign) {
        for (AABB box : boxes) {
            if (box == self || box.maxY <= y + EPSILON) {
                continue;
            }
            if (axis == 0) {
                // 东西两侧的墙：贴着 rect 的 x 边，并且 z 向覆盖整条边
                double edge = sign < 0 ? rect.x0 : rect.x1;
                double boxEdge = sign < 0 ? box.maxX : box.minX;
                if (Math.abs(boxEdge - edge) > EDGE_TOLERANCE) {
                    continue;
                }
                if (box.minZ <= rect.z0 + EDGE_TOLERANCE && box.maxZ >= rect.z1 - EDGE_TOLERANCE) {
                    return true;
                }
            } else {
                // 南北两侧的墙：贴着 rect 的 z 边，并且 x 向覆盖整条边
                double edge = sign < 0 ? rect.z0 : rect.z1;
                double boxEdge = sign < 0 ? box.maxZ : box.minZ;
                if (Math.abs(boxEdge - edge) > EDGE_TOLERANCE) {
                    continue;
                }
                if (box.minX <= rect.x0 + EDGE_TOLERANCE && box.maxX >= rect.x1 - EDGE_TOLERANCE) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 同高度的面互相重叠时，只保留先到（面积大）的那一份。 */
    private static List<SnowSurface> dropOverlaps(List<SnowSurface> surfaces) {
        if (surfaces.size() <= 1) {
            return List.copyOf(surfaces);
        }
        List<SnowSurface> sorted = new ArrayList<>(surfaces);
        sorted.sort(Comparator.comparingDouble(SnowSurface::y)
                .thenComparing(Comparator.comparingDouble(SnowSurface::area).reversed()));
        List<SnowSurface> accepted = new ArrayList<>(sorted.size());
        for (SnowSurface candidate : sorted) {
            List<Rect> pieces = new ArrayList<>(2);
            pieces.add(new Rect(candidate.x0(), candidate.z0(), candidate.x1(), candidate.z1()));
            for (SnowSurface other : accepted) {
                if (Math.abs(other.y() - candidate.y()) > EPSILON) {
                    continue;
                }
                pieces = subtract(pieces, new Rect(other.x0(), other.z0(), other.x1(), other.z1()));
                if (pieces.isEmpty()) {
                    break;
                }
            }
            for (Rect piece : pieces) {
                if (piece.area() < MIN_SURFACE_AREA) {
                    continue;
                }
                accepted.add(new SnowSurface(piece.x0, piece.z0, piece.x1, piece.z1, candidate.y()));
            }
        }
        return List.copyOf(accepted);
    }

    private static List<Rect> subtract(List<Rect> rects, Rect cutter) {
        List<Rect> out = new ArrayList<>(rects.size() + 2);
        for (Rect rect : rects) {
            out.addAll(subtract(rect, cutter));
        }
        return out;
    }

    private static List<Rect> subtract(Rect rect, Rect cutter) {
        if (!rect.intersects(cutter)) {
            return List.of(rect);
        }
        List<Rect> out = new ArrayList<>(4);
        if (cutter.z0 > rect.z0) {
            out.add(new Rect(rect.x0, rect.z0, rect.x1, cutter.z0));
        }
        if (cutter.z1 < rect.z1) {
            out.add(new Rect(rect.x0, cutter.z1, rect.x1, rect.z1));
        }
        float z0 = Math.max(rect.z0, cutter.z0);
        float z1 = Math.min(rect.z1, cutter.z1);
        if (cutter.x0 > rect.x0) {
            out.add(new Rect(rect.x0, z0, cutter.x0, z1));
        }
        if (cutter.x1 < rect.x1) {
            out.add(new Rect(cutter.x1, z0, rect.x1, z1));
        }
        return out;
    }

    private record Rect(float x0, float z0, float x1, float z1) {

        float area() {
            return Math.max(0.0F, this.x1 - this.x0) * Math.max(0.0F, this.z1 - this.z0);
        }

        boolean intersects(Rect other) {
            return this.x1 > other.x0 + EPSILON
                    && this.x0 < other.x1 - EPSILON
                    && this.z1 > other.z0 + EPSILON
                    && this.z0 < other.z1 - EPSILON;
        }
    }
}
