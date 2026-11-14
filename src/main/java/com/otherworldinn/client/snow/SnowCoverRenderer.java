package com.otherworldinn.client.snow;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.compat.create.CreateSnowCoverCompat;
import com.otherworldinn.world.snow.SnowCoverMap;
import com.otherworldinn.world.snow.SnowCoverState;
import com.otherworldinn.world.snow.SnowHash;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.model.lighting.QuadLighter;

/**
 * 真实覆雪的渲染入口：在区块段重建时，把"可承接积雪的水平表面"变成 1~2px 厚的雪板几何。
 *
 * <p>表面推导见 {@link SnowSurfaceCache}；本类负责"每列取哪些方块"、"扣掉压在上面的东西"、
 * "把雪板写进段缓冲"。哪些列算有雪由 {@link SnowCoverState} 决定。
 *
 * <p>为什么挂在 {@code AddSectionGeometryEvent} 上：几何直接进原版区块网格，
 * 光照/遮挡/视锥/渲染批次全部复用原版，每帧零开销、零额外 draw call。
 *
 * <p>两种模式（见 {@code snowCover.force}）：
 * <ul>
 *   <li><b>强制覆雪</b>：无视天气，所有可覆雪表面一律铺雪，并按 60%→40%→20% 往遮蔽区渗透；</li>
 *   <li><b>天气驱动</b>（默认）：积雪与否由服务端按原版随机刻判定后同步过来
 *       （见 {@code SnowCoverServer} / {@code SnowCoverState}），不参与渗透，
 *       被屋顶压住的表面照旧不铺——原版那里本来也不会有雪。</li>
 * </ul>
 */
@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class SnowCoverRenderer {

    /** 雪板厚度：1px 或 2px，按"方块位置 + 表面高度"稳定哈希挑一个（多端一致、重载不变）。 */
    private static final float THICKNESS_THIN = 1.0F / 16.0F;
    private static final float THICKNESS_THICK = 2.0F / 16.0F;
    private static final float SURFACE_EPSILON = 1.0E-3F;
    /**
     * 一列往下最多走多少层（含中间的空格）。
     *
     * <p>遇到空气不能停：悬空结构（C 字、拱门、檐口）下面隔着空气，底下的平台照样该积雪。
     * 深度给到 24 是为了盖住"高屋檐"（实测有 13 格高的檐口），而且代价可控：
     * 只有空气间隙才会往下走，遇到实心堆叠第一层就 break；深处的雪再由渗透规则限制范围，
     * 不会把屋里灌满。
     */
    private static final int MAX_DEPTH = 24;

    private static final ResourceLocation SNOW_SPRITE = ResourceLocation.withDefaultNamespace("block/snow");
    /** Create 是否在场（静态读一次，避免每格都查 mod 列表）。 */
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");
    /** 顶面覆雪时，光照按"这一格放了一块雪层"来算，跟原版完全对齐。 */
    private static final BlockState SNOW_STATE = Blocks.SNOW.defaultBlockState();

    private SnowCoverRenderer() {}

    @SubscribeEvent
    public static void onAddSectionGeometry(AddSectionGeometryEvent event) {
        if (!SnowCoverController.isEnabled()) {
            return;
        }
        if (!(event.getLevel() instanceof ClientLevel level)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != level) {
            // 指南书之类的外部 level，不参与
            return;
        }
        BlockPos origin = event.getSectionOrigin();
        SectionCaps section = collect(level, origin);
        if (section == null) {
            return;
        }
        TextureAtlasSprite sprite = minecraft.getModelManager()
                .getAtlas(TextureAtlas.LOCATION_BLOCKS)
                .getSprite(SNOW_SPRITE);
        event.addRenderer(context -> emit(context, section, origin, sprite));
    }

    /** 本段要渲染的雪板，以及"某个方块上到底有没有雪"的查询表（供邻接侧面判定用）。 */
    private record SectionCaps(List<SnowCap> caps, Map<BlockPos, List<SnowSurface>> presence) {}

    /** 渗透：紧挨着"有雪方块"的遮蔽方块有 60% 几率改成覆雪。 */
    private static final int SPREAD_CHANCE = 60;
    /** 每传递一次，几率下降 20 个百分点（60% → 40% → 20% → 0%）。 */
    private static final int SPREAD_DECAY = 20;
    /** 传递的最大跳数。 */
    private static final int SPREAD_MAX_HOPS = 3;
    /** 判定"同一个高度层"的容差（地面台阶差一格也算连着）。 */
    private static final float SPREAD_HEIGHT_TOLERANCE = 1.0F;
    /** 收集邻接列时要往外扩多少格（必须 >= 传递跳数，否则段边界会出现一道缺雪的缝）。 */
    private static final int SPREAD_RING = SPREAD_MAX_HOPS;

    /**
     * 收集本段内的雪板。主线程执行：这里要查高度图，渲染器（工作线程）只拿结果。
     *
     * <p>除了本段 16x16 列，还要把外面一圈也算一遍：省侧面时必须看"邻居**实际**有没有雪"，
     * 而不是"邻居理论上有没有表面"——邻居的雪可能被它上方的方块挖掉了。
     *
     * <p>两种模式共用这套收集：强制覆雪时用 60/40/20 渗透把雪往遮蔽区里带；
     * 天气驱动时只保留露天雪面（被屋顶压住的本来就不该有雪）。
     */
    private static SectionCaps collect(ClientLevel level, BlockPos origin) {
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
        if (chunk == null) {
            return null;
        }
        int sectionMinY = origin.getY();
        int sectionMaxY = sectionMinY + 15;
        int minBuildHeight = level.getMinBuildHeight();
        boolean forced = SnowCoverController.isForced();
        // 一个区块的位图只取一次，逐列用位测试（否则每列都要查一次 map）
        long[] ownMask = forced ? null : SnowCoverState.view(chunkX, chunkZ);
        if (!forced && ownMask == null) {
            // 天气模式下没有数据的区块根本不会出雪：直接跳过，省掉 256 次高度图读取
            return null;
        }
        List<SnowSurfaceCache.Occluder> occluders = new ArrayList<>(MAX_DEPTH);
        List<SnowCap> caps = null;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                if (!forced && !SnowCoverMap.isSet(ownMask, x, z)) {
                    continue;
                }
                List<SnowCap> column = collectColumn(
                        level, chunk, x, z, minBuildHeight, sectionMinY, sectionMaxY, occluders);
                if (column == null) {
                    continue;
                }
                if (caps == null) {
                    caps = new ArrayList<>(64);
                }
                caps.addAll(column);
            }
        }
        if (caps == null) {
            return null;
        }
        // 外圈只用于"邻居实际有没有雪"（省侧面）：天气模式不需要传递，一圈就够
        int ring = forced ? SPREAD_RING : 1;
        Map<Long, long[]> ringMasks = forced ? null : new HashMap<>();
        List<SnowCap> ringCaps = new ArrayList<>();
        for (int dz = -ring; dz <= 15 + ring; dz++) {
            for (int dx = -ring; dx <= 15 + ring; dx++) {
                if (dx >= 0 && dx <= 15 && dz >= 0 && dz <= 15) {
                    continue;
                }
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int neighbourX = SectionPos.blockToSectionCoord(x);
                int neighbourZ = SectionPos.blockToSectionCoord(z);
                LevelChunk neighbour = level.getChunkSource().getChunk(neighbourX, neighbourZ, false);
                if (neighbour == null) {
                    continue;
                }
                if (!forced) {
                    long[] mask = ringMasks.computeIfAbsent(
                            ((long) neighbourX << 32) ^ (neighbourZ & 0xFFFFFFFFL),
                            ignored -> SnowCoverState.view(neighbourX, neighbourZ));
                    if (!SnowCoverMap.isSet(mask, x, z)) {
                        continue;
                    }
                }
                List<SnowCap> column = collectColumn(level, neighbour, x, z, minBuildHeight,
                        sectionMinY - 1, sectionMaxY + 1, occluders);
                if (column == null) {
                    continue;
                }
                ringCaps.addAll(column);
            }
        }
        // 强制覆雪时用 60/40/20 渗透把雪往遮蔽区里渗透；
        // 天气驱动时不参与渗透——被遮住的地方在原版里本来就不会有雪。
        Set<SnowCap> approved = forced ? spreadSnow(caps, ringCaps) : openOnly(caps, ringCaps);
        List<SnowCap> visible = new ArrayList<>(caps.size());
        List<SnowCap> approvedRing = new ArrayList<>(ringCaps.size());
        for (SnowCap cap : caps) {
            if (approved.contains(cap)) {
                visible.add(cap);
            }
        }
        if (visible.isEmpty()) {
            return null;
        }
        for (SnowCap cap : ringCaps) {
            if (approved.contains(cap)) {
                approvedRing.add(cap);
            }
        }
        Map<BlockPos, List<SnowSurface>> presence = new HashMap<>();
        addPresence(presence, visible);
        addPresence(presence, approvedRing);
        return new SectionCaps(visible, presence);
    }

    private static long columnKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    private static void addPresence(Map<BlockPos, List<SnowSurface>> presence, List<SnowCap> caps) {
        for (SnowCap cap : caps) {
            presence.computeIfAbsent(cap.pos(), key -> new ArrayList<>(2)).add(cap.surface());
        }
    }

    /**
     * 覆雪"渗透"：本来该没雪的方块，只要贴着有雪的方块，就有几率也长出雪来。
     *
     * <p>规则：第一跳 40%，之后每传递一次下降 20 个百分点（40% → 20% → 0%），所以最多传两圈；
     * 八邻域传递（含对角），并且只在同一高度层之间传（差一格以内）。
     * 几率用位置哈希掷骰，因此结果是稳定的——同一次构建、不同客户端看到的都一样。
     */
    private static Set<SnowCap> spreadSnow(List<SnowCap> caps, List<SnowCap> ringCaps) {
        Map<Long, List<SnowCap>> byColumn = new HashMap<>();
        index(byColumn, caps);
        index(byColumn, ringCaps);

        Set<SnowCap> approved = new HashSet<>();
        List<SnowCap> frontier = new ArrayList<>();
        for (SnowCap cap : caps) {
            if (!cap.sheltered()) {
                approved.add(cap);
                frontier.add(cap);
            }
        }
        for (SnowCap cap : ringCaps) {
            if (!cap.sheltered()) {
                approved.add(cap);
                frontier.add(cap);
            }
        }

        for (int hop = 1; hop <= SPREAD_MAX_HOPS && !frontier.isEmpty(); hop++) {
            int chance = SPREAD_CHANCE - SPREAD_DECAY * (hop - 1);
            if (chance <= 0) {
                break;
            }
            List<SnowCap> next = new ArrayList<>();
            for (SnowCap parent : frontier) {
                float parentY = parent.pos().getY() + parent.surface().y();
                int baseX = parent.pos().getX();
                int baseZ = parent.pos().getZ();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) {
                            continue;
                        }
                        List<SnowCap> neighbours = byColumn.get(columnKey(baseX + dx, baseZ + dz));
                        if (neighbours == null) {
                            continue;
                        }
                        for (SnowCap neighbour : neighbours) {
                            if (!neighbour.sheltered() || approved.contains(neighbour)) {
                                continue;
                            }
                            float neighbourY = neighbour.pos().getY() + neighbour.surface().y();
                            if (Math.abs(neighbourY - parentY) > SPREAD_HEIGHT_TOLERANCE) {
                                continue;
                            }
                            if (rollChance(neighbour, chance)) {
                                approved.add(neighbour);
                                next.add(neighbour);
                            }
                        }
                    }
                }
            }
            frontier = next;
        }
        return approved;
    }

    /** 天气模式：只保留露天的雪面，不做任何传递。 */
    private static Set<SnowCap> openOnly(List<SnowCap> caps, List<SnowCap> ringCaps) {
        Set<SnowCap> approved = new HashSet<>();
        for (SnowCap cap : caps) {
            if (!cap.sheltered()) {
                approved.add(cap);
            }
        }
        for (SnowCap cap : ringCaps) {
            if (!cap.sheltered()) {
                approved.add(cap);
            }
        }
        return approved;
    }

    private static void index(Map<Long, List<SnowCap>> byColumn, List<SnowCap> caps) {
        for (SnowCap cap : caps) {
            byColumn.computeIfAbsent(columnKey(cap.pos().getX(), cap.pos().getZ()), key -> new ArrayList<>(2))
                    .add(cap);
        }
    }

    /** 稳定掷骰：同一块雪每次算出来的结果一样。 */
    private static boolean rollChance(SnowCap cap, int percent) {
        long hash = SnowHash.surface(cap.pos(), cap.surface().y());
        return Math.floorMod(hash >>> 17, 100L) < percent;
    }

    /**
     * 处理一列：从最高方块往下最多 {@link #MAX_DEPTH} 层。
     *
     * <p>每个方块的顶面都要先扣掉"压在它上面那些方块"的投影——这才是"完整方块上放栅栏，
     * 没被栅栏盖住的顶部也该有雪"的来源。
     */
    private static List<SnowCap> collectColumn(
            ClientLevel level,
            LevelChunk chunk,
            int x,
            int z,
            int minBuildHeight,
            int sectionMinY,
            int sectionMaxY,
            List<SnowSurfaceCache.Occluder> occluders) {
        // WORLD_SURFACE = 这一列最高的非空气方块（MOTION_BLOCKING 会漏掉半砖/箱子这类"不挡运动"的方块）
        int topY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        if (topY < sectionMinY || topY - MAX_DEPTH > sectionMaxY) {
            return null;
        }
        List<SnowCap> caps = null;
        // 上面已经有一块"会积雪的方块"了 → 本列更下面的面算被遮住（屋檐、棚子……）
        boolean cappedAbove = false;
        occluders.clear();
        for (int depth = 0; depth <= MAX_DEPTH; depth++) {
            int y = topY - depth;
            if (y < minBuildHeight) {
                break;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (depth == 0 && isSnowLike(chunk.getBlockState(pos.above()))) {
                // 头顶还压着原版雪层（高度图没同步等情况）：整列让给原版，绝不重叠
                return null;
            }
            BlockState state = chunk.getBlockState(pos);
            if (state.isAir()) {
                // 空气既不遮挡也不积雪，但下面可能还有"悬空结构底下"的平台
                continue;
            }
            // 水面/岩浆面、隐形方块、已有的雪层都会把这一列"封顶"，下面的东西不算暴露
            if (!state.getFluidState().isEmpty()
                    || state.getRenderShape() == RenderShape.INVISIBLE
                    || isSnowLike(state)) {
                break;
            }
            // 方块实体渲染的方块（箱子、床、以及齿轮/水车这类会动的模组方块）：不铺雪。
            // 它们的模型是渲染器现场画的，拿不到四边形，形状也只是粗略包络；
            // 一旦它动起来，静态的雪盖就会浮在原地。原版对这些方块本来也不积雪。
            boolean modelRendered = state.getRenderShape() == RenderShape.MODEL;
            if (modelRendered
                    && !ignoresSnowCover(state)
                    && !state.getCollisionShape(chunk, pos, CollisionContext.empty()).isEmpty()) {
                // 上面压着的东西按"模型底面离表面多高"决定要不要挖洞：
                // 贴死的（立柱、整块方块）挖，悬空的（横杆、吊灯、薄面片）不挖
                List<SnowSurface> base = SnowSurfaceCache.get(state, chunk, pos);
                List<SnowSurface> surfaces = SnowSurfaceCache.cutMasks(base, occluders, y);
                if (!surfaces.isEmpty()) {
                    // 上面有雪 ≠ 被遮住：栅栏只占 30% 的格子，底下照样该有雪。
                    // 只有"覆盖过半"才算真正被屋顶压着，才会被算作 sheltered（不参与渗透）。
                    boolean sheltered = cappedAbove && SnowSurfaceCache.coveredRatio(base, occluders, y) >= 0.5F;
                    if (y >= sectionMinY && y <= sectionMaxY) {
                        if (caps == null) {
                            caps = new ArrayList<>(64);
                        }
                        for (SnowSurface surface : surfaces) {
                            caps.add(new SnowCap(pos, surface, sheltered));
                        }
                    }
                    cappedAbove = true;
                }
            }
            VoxelShape shape = SnowSurfaceCache.shapeOf(state, chunk, pos);
            if (shape.isEmpty()) {
                // 没有几何：不遮挡，也不影响更下面
                continue;
            }
            float[] bottoms = SnowSurfaceCache.bottomsOf(state, chunk, pos);
            if (hasAnyOcclusion(bottoms)) {
                occluders.add(new SnowSurfaceCache.Occluder(y, bottoms));
            }
            if (depth < MAX_DEPTH
                    && state.isCollisionShapeFullBlock(chunk, pos)
                    && !chunk.getBlockState(pos.below()).isAir()) {
                // 整格占满**并且直接压着**下面那块：再往下一定被完全盖住。
                // 悬空的整块（屋檐、雨棚）不能在这里停，否则它底下那块永远拿不到雪。
                break;
            }
        }
        return caps;
    }

    /**
     * 一块雪盖的厚度：稳定的伪随机 1px / 2px。
     *
     * <p>用位置 + 表面高度做哈希，保证同一块雪在任何客户端、任何次重建里都算得一样，
     * 也让"邻居的雪盖多厚"可以在不查表的情况下复算出来。
     */
    private static float thicknessOf(BlockPos pos, float surfaceY) {
        return (SnowHash.surface(pos, surfaceY) & 1L) == 0L ? THICKNESS_THIN : THICKNESS_THICK;
    }



    /**
     * 调试：把某一列的判定过程摊开，用于排查"这里为什么没雪"。
     */
    public static String debugColumn(ClientLevel level, BlockPos target) {
        int x = target.getX();
        int z = target.getZ();
        StringBuilder out = new StringBuilder();
        LevelChunk chunk = level.getChunkSource()
                .getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z), false);
        if (chunk == null) {
            return "区块未加载";
        }
        int topY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        out.append("列 ").append(x).append(',').append(z).append(" 顶端 y=").append(topY).append(' ')
                .append(chunk.getBlockState(new BlockPos(x, topY, z))).append('\n');
        for (int depth = 0; depth <= MAX_DEPTH; depth++) {
            int y = topY - depth;
            if (y < level.getMinBuildHeight()) {
                break;
            }
            out.append("  depth ").append(depth).append(" y=").append(y).append(" : ")
                    .append(chunk.getBlockState(new BlockPos(x, y, z))).append('\n');
        }
        List<SnowCap> column = collectColumn(level, chunk, x, z, level.getMinBuildHeight(),
                Integer.MIN_VALUE, Integer.MAX_VALUE, new ArrayList<>(MAX_DEPTH));
        if (column == null) {
            out.append("  → 这一列没有雪盖候选\n");
            return out.toString();
        }
        out.append("  雪盖候选 ").append(column.size()).append(" 块\n");
        for (SnowCap cap : column) {
            out.append(String.format("    y=%.3f 面积=%.4f 被遮=%s%n",
                    cap.pos().getY() + cap.surface().y(), cap.surface().area(), cap.sheltered()));
        }
        // 关键核对：这一列涉及的所有段，**真正**交给渲染的雪盖里有没有它
        int rendered = 0;
        int inColumn = 0;
        int inBlock = 0;
        int fromSection = SectionPos.blockToSectionCoord(topY - MAX_DEPTH);
        int toSection = SectionPos.blockToSectionCoord(topY);
        for (int sectionY = fromSection; sectionY <= toSection; sectionY++) {
            SectionCaps section = collect(level, new BlockPos(x & ~15, sectionY * 16, z & ~15));
            if (section == null) {
                continue;
            }
            rendered += section.caps().size();
            for (SnowCap cap : section.caps()) {
                if (cap.pos().getX() == x && cap.pos().getZ() == z) {
                    inColumn++;
                    if (cap.pos().getY() == target.getY()) {
                        inBlock++;
                    }
                }
            }
        }
        out.append("  → 该列涉及的段共渲染 ").append(rendered)
                .append(" 块；本列 ").append(inColumn)
                .append(" 块；目标块(y=").append(target.getY()).append(") ").append(inBlock).append(" 块\n");
        return out.toString();
    }

    /**
     * 不铺雪的方块：会动的方块（Create 动能件）、或数据包点名排除的方块。
     *
     * <p>和"模型判定"的分工：
     * <ul>
     *   <li><b>模型判定</b>（{@link SnowModelHeight}，自动）：模型没有朝上的面就不出雪，
     *       梯子、锁链这类竖直薄片不需要写进标签；</li>
     *   <li><b>本标签</b>（手动）：只收"有真实顶面、但语义上不该积雪"的方块，
     *       比如告示牌、灯笼、花盆、蜡烛——模型判定管不到它们。</li>
     * </ul>
     * 被排除的方块仍然是遮挡物，也仍然不让这一列断掉（雪会落到它下面那块）。 */
    private static boolean ignoresSnowCover(BlockState state) {
        if (state.is(OtherworldInn.SNOW_COVER_IGNORED)) {
            return true;
        }
        return CREATE_LOADED && CreateSnowCoverCompat.ignoresSnowCover(state);
    }

    private static boolean hasAnyOcclusion(float[] bottoms) {
        for (float bottom : bottoms) {
            if (bottom >= 0.0F) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSnowLike(BlockState state) {
        return state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.POWDER_SNOW);
    }

    /**
     * 把雪板写进段缓冲。工作线程执行，只能碰 {@code region}（段的只读快照）。
     */
    private static void emit(
            AddSectionGeometryEvent.SectionRenderingContext context,
            SectionCaps section,
            BlockPos origin,
            TextureAtlasSprite sprite) {
        List<SnowCap> caps = section.caps();
        Map<BlockPos, List<SnowSurface>> presence = section.presence();
        VertexConsumer buffer = context.getOrCreateChunkBuffer(RenderType.solid());
        BlockAndTintGetter region = context.getRegion();
        // 平滑光照 + 环境光遮蔽，和原版方块走同一套取样
        QuadLighter lighter = context.getQuadLighter(true);

        int originX = origin.getX();
        int originY = origin.getY();
        int originZ = origin.getZ();

        int[] scratch = new int[32];
        float[][] positions = new float[4][3];
        float[][] uvs = new float[4][2];

        for (SnowCap cap : caps) {
            BlockPos pos = cap.pos();
            SnowSurface surface = cap.surface();
            // 光照取样用的是"方块局部坐标"，必须相对光照中心来算。
            // 顶面（y=1）的几何其实落在上一格，所以把光照中心也挪上去、并换成雪层的状态，
            // 这样算出来的 AO / 平滑光照和原版真的在那一格放一块雪层完全一致。
            boolean atTop = surface.y() > 0.999F;
            BlockPos lightPos = atTop ? pos.above() : pos;
            BlockState lightState = atTop ? SNOW_STATE : region.getBlockState(pos);
            float yShift = atTop ? -1.0F : 0.0F;
            lighter.setup(region, lightPos, lightState);

            float relX = pos.getX() - originX;
            float relY = pos.getY() - originY;
            float relZ = pos.getZ() - originZ;
            float x0 = surface.x0();
            float x1 = surface.x1();
            float z0 = surface.z0();
            float z1 = surface.z1();
            // yb/yt 是给光照用的"局部坐标"（顶面挪到上一格后是 0~0.125）；
            // 渲染时要把这一格的差距补回去，否则顶层雪会被画到方块底部。
            float thickness = thicknessOf(pos, surface.y());
            float yb = surface.y() + yShift;
            float yt = yb + thickness;
            float emissionY = relY - yShift;
            // 底面/顶面的绝对 y，用来和邻居的雪盖比高低
            float baseAbs = pos.getY() + surface.y();
            float topAbs = baseAbs + thickness;

            // UV 按"世界像素"裁剪：每个顶点用自己的方块内坐标取贴图坐标，
            // 这样 2x9 的横杆雪盖只会用到贴图对应的 2x9 像素，不会把整张 16x16 拉上去。
            float ux0 = sprite.getU(x0);
            float ux1 = sprite.getU(x1);
            float uz0 = sprite.getU(z0);
            float uz1 = sprite.getU(z1);
            float vz0 = sprite.getV(z0);
            float vz1 = sprite.getV(z1);

            // 顶面
            setVertex(positions, uvs, 0, x0, yt, z0, ux0, vz0);
            setVertex(positions, uvs, 1, x0, yt, z1, ux0, vz1);
            setVertex(positions, uvs, 2, x1, yt, z1, ux1, vz1);
            setVertex(positions, uvs, 3, x1, yt, z0, ux1, vz0);
            emitQuad(buffer, lighter, scratch, positions, uvs, relX, emissionY, relZ, 0.0F, 1.0F, 0.0F);

            // 四个侧面：邻居的雪盖可能比本块薄（1px vs 2px），露出多少画多少
            float vTop = sprite.getV(1.0F - thickness);
            float northBottom = exposedBottom(presence, pos, Direction.NORTH, surface, baseAbs);
            if (northBottom < topAbs - SURFACE_EPSILON) {
                float low = yb + (northBottom - baseAbs);
                float vLow = sprite.getV(1.0F - (low - yb));
                setVertex(positions, uvs, 0, x1, yt, z0, ux1, vTop);
                setVertex(positions, uvs, 1, x1, low, z0, ux1, vLow);
                setVertex(positions, uvs, 2, x0, low, z0, ux0, vLow);
                setVertex(positions, uvs, 3, x0, yt, z0, ux0, vTop);
                emitQuad(buffer, lighter, scratch, positions, uvs, relX, emissionY, relZ, 0.0F, 0.0F, -1.0F);
            }
            float southBottom = exposedBottom(presence, pos, Direction.SOUTH, surface, baseAbs);
            if (southBottom < topAbs - SURFACE_EPSILON) {
                float low = yb + (southBottom - baseAbs);
                float vLow = sprite.getV(1.0F - (low - yb));
                setVertex(positions, uvs, 0, x0, yt, z1, ux0, vTop);
                setVertex(positions, uvs, 1, x0, low, z1, ux0, vLow);
                setVertex(positions, uvs, 2, x1, low, z1, ux1, vLow);
                setVertex(positions, uvs, 3, x1, yt, z1, ux1, vTop);
                emitQuad(buffer, lighter, scratch, positions, uvs, relX, emissionY, relZ, 0.0F, 0.0F, 1.0F);
            }
            float westBottom = exposedBottom(presence, pos, Direction.WEST, surface, baseAbs);
            if (westBottom < topAbs - SURFACE_EPSILON) {
                float low = yb + (westBottom - baseAbs);
                float vLow = sprite.getV(1.0F - (low - yb));
                setVertex(positions, uvs, 0, x0, yt, z0, uz0, vTop);
                setVertex(positions, uvs, 1, x0, low, z0, uz0, vLow);
                setVertex(positions, uvs, 2, x0, low, z1, uz1, vLow);
                setVertex(positions, uvs, 3, x0, yt, z1, uz1, vTop);
                emitQuad(buffer, lighter, scratch, positions, uvs, relX, emissionY, relZ, -1.0F, 0.0F, 0.0F);
            }
            float eastBottom = exposedBottom(presence, pos, Direction.EAST, surface, baseAbs);
            if (eastBottom < topAbs - SURFACE_EPSILON) {
                float low = yb + (eastBottom - baseAbs);
                float vLow = sprite.getV(1.0F - (low - yb));
                setVertex(positions, uvs, 0, x1, yt, z1, uz1, vTop);
                setVertex(positions, uvs, 1, x1, low, z1, uz1, vLow);
                setVertex(positions, uvs, 2, x1, low, z0, uz0, vLow);
                setVertex(positions, uvs, 3, x1, yt, z0, uz0, vTop);
                emitQuad(buffer, lighter, scratch, positions, uvs, relX, emissionY, relZ, 1.0F, 0.0F, 0.0F);
            }
        }
    }

    /**
     * 这一侧的侧面，被邻居的雪盖盖到了多高（返回绝对 y，盖子没挨着就返回本块雪盖的底面）。
     *
     * <p>三个条件都要满足才算"盖住"：
     * <ol>
     *   <li>这个面贴在方块边界上（柱顶、栏杆这种悬在格子中间的面的侧面永远看得见）；</li>
     *   <li>邻居在这一侧边界上有一块同高度的雪面；</li>
     *   <li>邻居那块雪面在边方向上把我的整条边都盖住。</li>
     * </ol>
     * 而且查的是**邻居实际渲染出来的雪盖**（{@code presence}），不是"理论上有没有表面"。
     *
     * <p>盖住的高度取邻居雪盖自己的厚度（1px / 2px）：本块 2px、邻居 1px 时，
     * 上面那 1px 依然要露出来。
     */
    private static float exposedBottom(
            Map<BlockPos, List<SnowSurface>> presence,
            BlockPos pos,
            Direction direction,
            SnowSurface surface,
            float capBottomAbs) {
        float plane = switch (direction) {
            case EAST -> surface.x1();
            case WEST -> surface.x0();
            case SOUTH -> surface.z1();
            case NORTH -> surface.z0();
            default -> -1.0F;
        };
        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            if (plane < 1.0F - SURFACE_EPSILON) {
                return capBottomAbs;
            }
        } else if (plane > SURFACE_EPSILON) {
            return capBottomAbs;
        }

        float absoluteY = pos.getY() + surface.y();
        int cellY = Mth.floor(absoluteY - SURFACE_EPSILON);
        BlockPos owner = pos.relative(direction).atY(cellY);
        List<SnowSurface> others = presence.get(owner);
        if (others == null) {
            return capBottomAbs;
        }
        float localY = absoluteY - owner.getY();
        float covered = capBottomAbs;
        for (SnowSurface other : others) {
            if (Math.abs(other.y() - localY) > SURFACE_EPSILON) {
                continue;
            }
            if (!touchesBoundary(other, direction) || !coversSpan(other, direction, surface)) {
                continue;
            }
            covered = Math.max(covered, owner.getY() + other.y() + thicknessOf(owner, other.y()));
        }
        return covered;
    }

    /** 邻居的雪面是否贴着我这一侧的边界。 */
    private static boolean touchesBoundary(SnowSurface other, Direction direction) {
        return switch (direction) {
            case EAST -> other.x0() <= SURFACE_EPSILON;
            case WEST -> other.x1() >= 1.0F - SURFACE_EPSILON;
            case SOUTH -> other.z0() <= SURFACE_EPSILON;
            case NORTH -> other.z1() >= 1.0F - SURFACE_EPSILON;
            default -> false;
        };
    }

    /** 邻居的雪面是否在边方向上覆盖了我的整条边。 */
    private static boolean coversSpan(SnowSurface other, Direction direction, SnowSurface mine) {
        return switch (direction) {
            case EAST, WEST -> other.z0() <= mine.z0() + SURFACE_EPSILON
                    && other.z1() >= mine.z1() - SURFACE_EPSILON;
            case NORTH, SOUTH -> other.x0() <= mine.x0() + SURFACE_EPSILON
                    && other.x1() >= mine.x1() - SURFACE_EPSILON;
            default -> false;
        };
    }

    private static void setVertex(
            float[][] positions, float[][] uvs, int index, float x, float y, float z, float u, float v) {
        positions[index][0] = x;
        positions[index][1] = y;
        positions[index][2] = z;
        uvs[index][0] = u;
        uvs[index][1] = v;
    }

    /**
     * 写一个四边形：先让 {@link QuadLighter} 按原版规则算每个顶点的亮度（含 AO 与方向明暗）和光照贴图，
     * 再按算出来的值写顶点。
     */
    private static void emitQuad(
            VertexConsumer buffer,
            QuadLighter lighter,
            int[] scratch,
            float[][] positions,
            float[][] uvs,
            float offsetX,
            float offsetY,
            float offsetZ,
            float normalX,
            float normalY,
            float normalZ) {
        int packedNormal = packNormal(normalX, normalY, normalZ);
        for (int i = 0; i < 4; i++) {
            int offset = i * 8;
            scratch[offset] = Float.floatToRawIntBits(positions[i][0]);
            scratch[offset + 1] = Float.floatToRawIntBits(positions[i][1]);
            scratch[offset + 2] = Float.floatToRawIntBits(positions[i][2]);
            scratch[offset + 3] = -1;
            scratch[offset + 4] = Float.floatToRawIntBits(uvs[i][0]);
            scratch[offset + 5] = Float.floatToRawIntBits(uvs[i][1]);
            scratch[offset + 6] = 0;
            scratch[offset + 7] = packedNormal;
        }
        lighter.computeLightingForQuad(scratch, true);
        float[] brightness = lighter.getComputedBrightness();
        int[] lightmap = lighter.getComputedLightmap();
        for (int i = 0; i < 4; i++) {
            int shade = Mth.clamp((int) (brightness[i] * 255.0F), 0, 255);
            int light = lightmap[i];
            buffer.addVertex(positions[i][0] + offsetX, positions[i][1] + offsetY, positions[i][2] + offsetZ)
                    .setColor(shade, shade, shade, 255)
                    .setUv(uvs[i][0], uvs[i][1])
                    .setUv2(light & 0xFFFF, light >> 16 & 0xFFFF)
                    .setNormal(normalX, normalY, normalZ);
        }
    }

    private static int packNormal(float x, float y, float z) {
        return ((int) (x * 127.0F) & 0xFF) | (((int) (y * 127.0F) & 0xFF) << 8) | (((int) (z * 127.0F) & 0xFF) << 16);
    }

    /** 一块待渲染的雪板：属于哪个方块、贴在它的哪个表面上。 */
    /** 一块待渲染的雪板：属于哪个方块、贴在它的哪个表面上、是否被上方结构遮住。 */
    private record SnowCap(BlockPos pos, SnowSurface surface, boolean sheltered) {}
}
