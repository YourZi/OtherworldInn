package com.otherworldinn.client.snow;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 从**烘焙模型**里量出两张 16x16 的像素表：
 * <ul>
 *   <li>{@link #of} —— 每格"朝上的表面"有多高；</li>
 *   <li>{@link #bottoms} —— 每格被模型盖住时，遮挡体最**低**的面有多低（-1 表示该格没被盖住）。</li>
 * </ul>
 *
 * <p>为什么必须有这一层：形状（{@code getShape}）是体素近似，和模型差得很远。
 * 栅栏的连接臂在形状里是满长满高，而模型里横杆只有 2x9、顶面 15/16、底面 6/16；
 * 草的 {@code getShape} 是 12x12 的方块，模型只是两片十字薄面。
 *
 * <p>为什么要"底面高度"：现实里雪是斜着/侧向吹来的，只要遮挡体和表面之间有空隙，
 * 雪就能钻进去。所以判据不是"正上方有没有方块"，而是"挡在上面的东西离这个面多高"。
 *
 * <p>三种情况必须区分开：
 * <ul>
 *   <li>拿不到模型（方块实体渲染的箱子/床）→ {@code null}，由调用方退回形状判定；</li>
 *   <li>模型有面但**没有朝上的面**（梯子、锁链这类竖直薄片）→ 全 0 高度表，
 *       表示整个方块没有可积雪的顶面；</li>
 *   <li>正常模型 → 逐像素高度。</li>
 * </ul>
 * 结果按 {@link BlockState} 缓存；工作线程上调用是安全的（和区块网格构建读模型的时机一致）。
 */
public final class SnowModelHeight {

    private static final Direction[] ALL_DIRECTIONS = {
        Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null
    };

    private static final Maps NONE = new Maps(null, null);

    private static final Map<BlockState, Maps> CACHE = new ConcurrentHashMap<>();

    private SnowModelHeight() {}

    /**
     * @return 长度 256 的高度表（下标 {@code x + z * 16}，0 表示该像素没有朝上的模型面）；无可用模型时返回 null
     */
    public static float[] of(BlockState state) {
        Maps maps = maps(state);
        return maps == null ? null : maps.heights();
    }

    /**
     * @return 长度 256 的遮挡表：每格该方块最低的几何面高度，-1 表示该格没被遮挡；无可用模型时返回 null
     */
    public static float[] bottoms(BlockState state) {
        Maps maps = maps(state);
        return maps == null ? null : maps.bottoms();
    }

    private static Maps maps(BlockState state) {
        Maps cached = CACHE.get(state);
        if (cached != null) {
            return cached == NONE ? null : cached;
        }
        Maps computed = assemble(state);
        CACHE.put(state, computed == null ? NONE : computed);
        return computed;
    }

    private static Maps assemble(BlockState state) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return null;
        }
        BakedModel model = minecraft.getBlockRenderer().getBlockModel(state);
        if (model == null) {
            return null;
        }
        float[] heights = new float[256];
        float[] bottoms = new float[256];
        java.util.Arrays.fill(bottoms, -1.0F);
        // 注意区分两件事：
        //   模型根本没返回四边形（箱子/床这类方块实体渲染）→ 返回 null，由调用方退回形状；
        //   模型有四边形但投影面积全是零（十字面片、梯子这类竖直薄面）→ 这是"什么都不挡"，
        //   不能退回形状，否则草底下又会被那 12x12 的形状盒子吃光。
        boolean sawQuad = false;
        // 固定种子：多变体模型每次都挑同一个变体，保证结果稳定
        RandomSource random = RandomSource.create(42L);
        for (Direction direction : ALL_DIRECTIONS) {
            for (BakedQuad quad : model.getQuads(state, direction, random)) {
                sawQuad = true;
                rasterizeCoverage(quad, bottoms);
                if (quad.getDirection() == Direction.UP) {
                    rasterizeHeight(quad, heights);
                }
            }
        }
        if (!sawQuad) {
            return null;
        }
        // 注意这里**不能**在没有朝上的面时返回 null：null 的语义是"拿不到模型，退回形状判定"。
        // 梯子、锁链、旗帜、告示牌这类模型只有竖直薄面，形状却是个贴墙的盒子，
        // 退回形状就会在 y=1 的那条窄边浮一层雪。返回全 0 高度表 = 整个方块没有可积雪的顶面。
        return new Maps(heights, bottoms);
    }

    /** 朝上的面：按包围盒铺到像素格子，记录最高值。 */
    private static void rasterizeHeight(BakedQuad quad, float[] heights) {
        int[] vertices = quad.getVertices();
        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minZ = Float.MAX_VALUE;
        float maxZ = -Float.MAX_VALUE;
        float maxY = 0.0F;
        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            float x = Float.intBitsToFloat(vertices[offset]);
            float y = Float.intBitsToFloat(vertices[offset + 1]);
            float z = Float.intBitsToFloat(vertices[offset + 2]);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
            maxY = Math.max(maxY, y);
        }
        int x0 = Math.max(0, (int) Math.ceil(minX * 16.0F - 0.5F));
        int x1 = Math.min(15, (int) Math.floor(maxX * 16.0F - 0.5F));
        int z0 = Math.max(0, (int) Math.ceil(minZ * 16.0F - 0.5F));
        int z1 = Math.min(15, (int) Math.floor(maxZ * 16.0F - 0.5F));
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                int index = x + z * 16;
                heights[index] = Math.max(heights[index], maxY);
            }
        }
    }

    /**
     * 投影覆盖：把四边形投到 XZ 平面，落在多边形内的像素记下它最低的高度。
     *
     * <p>退化四边形（竖直薄面，比如草的十字面片、梯子）投影面积为零，**不算遮挡**——
     * 现实里雪会盖过一片草叶，只留下草尖露出来。
     *
     * @return 这个四边形是否真的贡献了面积
     */
    private static boolean rasterizeCoverage(BakedQuad quad, float[] bottoms) {
        int[] vertices = quad.getVertices();
        double[] xs = new double[4];
        double[] zs = new double[4];
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE;
        double maxZ = -Double.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        double area = 0.0;
        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            xs[i] = Float.intBitsToFloat(vertices[offset]);
            zs[i] = Float.intBitsToFloat(vertices[offset + 2]);
            minY = Math.min(minY, Float.intBitsToFloat(vertices[offset + 1]));
            minX = Math.min(minX, xs[i]);
            maxX = Math.max(maxX, xs[i]);
            minZ = Math.min(minZ, zs[i]);
            maxZ = Math.max(maxZ, zs[i]);
        }
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) & 3;
            area += xs[i] * zs[j] - xs[j] * zs[i];
        }
        if (Math.abs(area) < 1.0E-6) {
            return false;
        }
        boolean counterClockwise = area > 0.0;
        int x0 = Math.max(0, (int) Math.ceil(minX * 16.0F - 0.5F));
        int x1 = Math.min(15, (int) Math.floor(maxX * 16.0F - 0.5F));
        int z0 = Math.max(0, (int) Math.ceil(minZ * 16.0F - 0.5F));
        int z1 = Math.min(15, (int) Math.floor(maxZ * 16.0F - 0.5F));
        for (int x = x0; x <= x1; x++) {
            double px = (x + 0.5) / 16.0;
            for (int z = z0; z <= z1; z++) {
                double pz = (z + 0.5) / 16.0;
                boolean inside = true;
                for (int i = 0; i < 4; i++) {
                    int j = (i + 1) & 3;
                    double cross = (xs[j] - xs[i]) * (pz - zs[i]) - (zs[j] - zs[i]) * (px - xs[i]);
                    if (counterClockwise ? cross < -1.0E-9 : cross > 1.0E-9) {
                        inside = false;
                        break;
                    }
                }
                if (inside) {
                    int index = x + z * 16;
                    bottoms[index] = bottoms[index] < 0.0F ? minY : Math.min(bottoms[index], minY);
                }
            }
        }
        return true;
    }

    private record Maps(float[] heights, float[] bottoms) {}
}
