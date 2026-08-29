package com.otherworldinn.client.gui.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.quest.QuestMarkerProjector;
import com.otherworldinn.client.quest.QuestHudState;
import com.otherworldinn.mixin.MixinGameRendererAccessor;
import com.otherworldinn.util.ClientServices;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.Camera;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * 任务系统 HUD（主 mod 自渲染，不依赖 OtherworldInnHud），常驻屏幕左侧垂直居中。
 * 布局为槽位制：淡出中的块仍占槽位，槽位回收后其余块平滑移动；节点推进在同槽位交叉淡变。
 */
@EventBusSubscriber(
        modid = OtherworldInn.MODID,
        value = Dist.CLIENT,
        bus = EventBusSubscriber.Bus.MOD)
public final class QuestHudOverlay {
    private static final int LEFT_MARGIN = 6;
    private static final int TEXT_MAX_WIDTH = 150;
    private static final int BLOCK_PADDING_X = 6;
    private static final int BLOCK_PADDING_Y = 4;
    private static final int BLOCK_GAP = 6;
    private static final int LINE_GAP = 1;
    private static final int HUD_VERTICAL_OFFSET = -40;
    /** 目标行相对标题行的缩进（标题顶格，内容后缩）。 */
    private static final int REQUIREMENT_INDENT = 10;
    private static final int REQUIREMENT_OPEN_COLOR = 0xFFF0F0F0;
    private static final int REQUIREMENT_COMPLETE_COLOR = 0xFF8A8A8A;
    private static final int REQUIREMENT_ITEM_COMPLETE_COLOR = 0xFF72D66A;
    private static final int BACKGROUND = 0x66000000;
    private static final int PREFIX_MAIN_COLOR = 0xFFE8B45A;
    private static final int PREFIX_SIDE_COLOR = 0xFF8FC7C7;
    private static final int MARKER_EDGE_MARGIN = 24;
    private static final int MARKER_ICON_RADIUS = 4;
    private static final int MARKER_ARROW_RADIUS = 8;
    private static long lastMarkerDebugLogAt;

    private static final long APPEAR_DURATION_MS = 220L;
    private static final long DISAPPEAR_DURATION_MS = 260L;
    private static final long CROSSFADE_DURATION_MS = 220L;
    private static final int SLIDE_DISTANCE = 10;
    /** 垂直位置插值时间常数（毫秒），越小跟随越快。 */
    private static final long Y_SMOOTHING_MS = 90L;

    /** 首次出现时刻。 */
    private static final Map<String, Long> APPEARED_AT = new HashMap<>();
    /** 开始消失时刻（存在即表示正在淡出）。 */
    private static final Map<String, Long> DISAPPEARED_AT = new HashMap<>();
    /** 最近一次渲染的内容缓存，供消失动画期间继续绘制。 */
    private static final Map<String, List<DisplayLine>> LINE_CACHE = new HashMap<>();
    /** 槽位顺序：淡出中的块仍占位，保证其它块位置不动。 */
    private static final List<String> SLOT_ORDER = new ArrayList<>();
    /** 各块当前的动画垂直位置（像素，平滑趋近目标位）。 */
    private static final Map<String, Float> ANIM_Y = new HashMap<>();
    /** 各块最近一次的阶段签名（活跃节点集合），用于检测节点推进。 */
    private static final Map<String, String> STAGE_CACHE = new HashMap<>();
    /** 节点推进交叉淡变：旧内容 + 开始时刻。 */
    private static final Map<String, CrossFade> CROSS_FADE = new HashMap<>();
    private static long lastFrameMillis = 0L;

    private record DisplayLine(FormattedCharSequence text, int color, int indent) {}

    private record TaskBlock(String key, String stage, List<DisplayLine> lines) {}

    private record CrossFade(List<DisplayLine> oldLines, long startedAt) {}

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.EXPERIENCE_BAR,
                ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "quest_hud_overlay"),
                QuestHudOverlay::render);
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            clearAnimations();
            QuestHudState.clear();
            return;
        }
        Font font = mc.font;
        long now = Util.getMillis();
        long dt = Mth.clamp(now - lastFrameMillis, 0L, 100L);
        lastFrameMillis = now;

        // 构建当前帧的块（快照顺序：任务在前，委托/故事委托在后）
        List<TaskBlock> current = new ArrayList<>();
        List<int[]> activeMarkers = new ArrayList<>();
        for (QuestHudState.HudTask task : QuestHudState.getTasks()) {
            activeMarkers.addAll(task.markers());
            List<DisplayLine> lines = new ArrayList<>();
            int titleColor = "main".equals(task.category()) ? PREFIX_MAIN_COLOR : PREFIX_SIDE_COLOR;
            appendWrapped(lines, font, withCategoryPrefix(task), titleColor, 0);
            for (QuestHudState.HudRequirement requirement : task.requirements()) {
                MutableComponent line = requirement.text().copy();
                int color = requirement.complete() ? REQUIREMENT_COMPLETE_COLOR : REQUIREMENT_OPEN_COLOR;
                if ("item".equals(requirement.type())) {
                    if (requirement.complete()) {
                        line = line.append(Component.literal(" \u221a"));
                        color = REQUIREMENT_ITEM_COMPLETE_COLOR;
                    } else if (requirement.required() > 1) {
                        line = line.append(Component.literal(
                                " (" + requirement.current() + "/" + requirement.required() + ")"));
                    }
                } else if (requirement.required() > 1 && !requirement.complete()) {
                    line = line.append(Component.literal(
                            " (" + requirement.current() + "/" + requirement.required() + ")"));
                }
                appendWrapped(
                        lines,
                        font,
                        line,
                        color,
                        REQUIREMENT_INDENT);
            }
            if (!lines.isEmpty()) {
                current.add(new TaskBlock(task.key(), task.stage(), lines));
            }
        }
        if (current.isEmpty() && DISAPPEARED_AT.isEmpty() && LINE_CACHE.isEmpty()) {
            clearAnimations();
            return;
        }

        updateAnimationState(current, now);

        // 淡出完成的块先过期（连同内容缓存），再回收其槽位
        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, Long> entry : DISAPPEARED_AT.entrySet()) {
            if (now - entry.getValue() >= DISAPPEAR_DURATION_MS) {
                expired.add(entry.getKey());
            }
        }
        for (String key : expired) {
            DISAPPEARED_AT.remove(key);
            LINE_CACHE.remove(key);
            STAGE_CACHE.remove(key);
        }
        SLOT_ORDER.removeIf(key -> !isRenderable(key, current));

        // 交叉淡变过期
        CROSS_FADE.values().removeIf(fade -> now - fade.startedAt() >= CROSSFADE_DURATION_MS);

        // 各块高度（淡出块用缓存行测）
        Map<String, Integer> heights = new HashMap<>();
        Map<String, List<DisplayLine>> drawable = new HashMap<>();
        for (TaskBlock block : current) {
            drawable.put(block.key(), block.lines());
            heights.put(block.key(), measureBlockHeight(font, block.lines()));
        }
        for (Map.Entry<String, List<DisplayLine>> entry : LINE_CACHE.entrySet()) {
            if (DISAPPEARED_AT.containsKey(entry.getKey()) && !drawable.containsKey(entry.getKey())) {
                drawable.put(entry.getKey(), entry.getValue());
                heights.put(entry.getKey(), measureBlockHeight(font, entry.getValue()));
            }
        }

        // 目标布局：按槽位顺序自上而下累加，整体垂直居中
        int totalHeight = 0;
        for (String key : SLOT_ORDER) {
            Integer h = heights.get(key);
            if (h != null) {
                totalHeight += h;
            }
        }
        totalHeight += BLOCK_GAP * Math.max(0, countDrawable(SLOT_ORDER, heights) - 1);
        int top = Math.max(BLOCK_GAP, (guiGraphics.guiHeight() - totalHeight) / 2 + HUD_VERTICAL_OFFSET);
        Map<String, Float> targetY = new HashMap<>();
        float cursor = top;
        for (String key : SLOT_ORDER) {
            Integer h = heights.get(key);
            if (h == null) {
                continue;
            }
            targetY.put(key, cursor);
            cursor += h + BLOCK_GAP;
        }

        // 垂直位置平滑插值（新块直接落在目标位，不做垂直滑移）
        float factor = 1.0F - (float) Math.exp(-dt / (float) Y_SMOOTHING_MS);
        for (Map.Entry<String, Float> entry : targetY.entrySet()) {
            Float currentY = ANIM_Y.get(entry.getKey());
            if (currentY == null) {
                ANIM_Y.put(entry.getKey(), entry.getValue());
            } else if (Math.abs(currentY - entry.getValue()) > 0.01F) {
                ANIM_Y.put(entry.getKey(), Mth.lerp(factor, currentY, entry.getValue()));
            }
        }

        // 绘制（alpha 极低时跳过：字体渲染会把 alpha≤3 的颜色强制变不透明，导致淡出末尾闪一帧）
        for (String key : SLOT_ORDER) {
            List<DisplayLine> lines = drawable.get(key);
            if (lines == null) {
                continue;
            }
            float alpha;
            int offsetX;
            Long appeared = APPEARED_AT.get(key);
            if (appeared != null) {
                float progress = Mth.clamp((now - appeared) / (float) APPEAR_DURATION_MS, 0.0F, 1.0F);
                alpha = easeOut(progress);
                offsetX = -(int) ((1.0F - progress) * SLIDE_DISTANCE);
            } else {
                alpha = 1.0F;
                offsetX = 0;
            }
            Long disappeared = DISAPPEARED_AT.get(key);
            if (disappeared != null) {
                float progress = Mth.clamp((now - disappeared) / (float) DISAPPEAR_DURATION_MS, 0.0F, 1.0F);
                alpha = 1.0F - easeOut(progress);
                offsetX = -(int) (easeOut(progress) * SLIDE_DISTANCE);
            }
            if (alpha < 0.03F) {
                continue;
            }

            // 节点推进交叉淡变：新内容淡入、旧内容淡出（同一槽位）
            List<DisplayLine> oldLines = null;
            float oldAlpha = 0.0F;
            float newAlpha = alpha;
            CrossFade fade = CROSS_FADE.get(key);
            if (fade != null) {
                float progress =
                        Mth.clamp((now - fade.startedAt()) / (float) CROSSFADE_DURATION_MS, 0.0F, 1.0F);
                newAlpha = alpha * easeOut(progress);
                oldLines = fade.oldLines();
                oldAlpha = alpha * (1.0F - easeOut(progress));
            }

            int width = 0;
            for (DisplayLine line : lines) {
                width = Math.max(width, font.width(line.text()) + line.indent());
            }
            if (oldLines != null) {
                for (DisplayLine line : oldLines) {
                    width = Math.max(width, font.width(line.text()));
                }
            }
            width += BLOCK_PADDING_X * 2;
            int x = LEFT_MARGIN + offsetX;

            float y = ANIM_Y.getOrDefault(key, targetY.getOrDefault(key, (float) top));
            int backgroundHeight = measureBlockHeight(font, lines);
            if (oldLines != null) {
                backgroundHeight = Math.max(backgroundHeight, measureBlockHeight(font, oldLines));
            }
            guiGraphics.fill(x, (int) y, x + width, (int) y + backgroundHeight, withAlpha(BACKGROUND, alpha));

            if (oldLines != null && oldAlpha >= 0.03F) {
                drawLines(guiGraphics, font, oldLines, x, (int) y, withAlphaBits(alpha * oldAlpha));
            }
            if (newAlpha >= 0.03F) {
                drawLines(guiGraphics, font, lines, x, (int) y, withAlphaBits(newAlpha));
            }
        }

        renderMarkers(guiGraphics, mc, deltaTracker, activeMarkers);
    }

    /**
     * 指示点渲染：只保留最近一个活跃任务点，投影为 HUD 2D 锚点。
     * 屏幕内按真实位置显示，离屏/身后时吸附到边缘并旋转箭头。
     */
    private static void renderMarkers(
            GuiGraphics guiGraphics, Minecraft mc, DeltaTracker deltaTracker, List<int[]> markers) {
        if (markers.isEmpty() || mc.player == null || mc.gameRenderer == null) {
            return;
        }
        if (!mc.player.level().dimension().equals(com.otherworldinn.world.dimension.TownDimensions.TOWN_LEVEL)) {
            return;
        }
        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) {
            return;
        }
        int[] marker = findNearestMarker(markers, camera.getPosition());
        if (marker == null) {
            return;
        }
        Vec3 camPos = camera.getPosition();
        Vec3 relative = new Vec3(marker[0] + 0.5D, marker[1] + 0.5D, marker[2] + 0.5D).subtract(camPos);
        var look = camera.getLookVector();
        var up = camera.getUpVector();
        var left = camera.getLeftVector();
        double localX = -(relative.x * left.x() + relative.y * left.y() + relative.z * left.z());
        double localY = relative.x * up.x() + relative.y * up.y() + relative.z * up.z();
        double localZ = relative.x * look.x() + relative.y * look.y() + relative.z * look.z();

        double verticalFov = resolveVerticalFovDegrees(mc, camera, deltaTracker);
        QuestMarkerProjector.Result result = QuestMarkerProjector.project(
                localX,
                localY,
                localZ,
                verticalFov,
                guiGraphics.guiWidth(),
                guiGraphics.guiHeight(),
                MARKER_EDGE_MARGIN);
        // #region debug-point A:quest-marker-client
        if (System.currentTimeMillis() - lastMarkerDebugLogAt >= 500L) {
            lastMarkerDebugLogAt = System.currentTimeMillis();
            try {
                java.net.http.HttpClient.newHttpClient()
                        .sendAsync(
                                java.net.http.HttpRequest.newBuilder(java.net.URI.create(readDebugServerUrl()))
                                        .header("Content-Type", "application/json")
                                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(
                                                "{\"sessionId\":\"" + readDebugSessionId()
                                                        + "\",\"runId\":\"post-fix\",\"hypothesisId\":\"A\",\"location\":\"QuestHudOverlay.renderMarkers\",\"msg\":\"[DEBUG] client marker projection\",\"data\":{\"markerX\":"
                                                        + marker[0] + ",\"markerY\":"
                                                        + marker[1] + ",\"markerZ\":"
                                                        + marker[2] + ",\"cameraX\":"
                                                        + camPos.x + ",\"cameraY\":"
                                                        + camPos.y + ",\"cameraZ\":"
                                                        + camPos.z + ",\"lookX\":"
                                                        + look.x() + ",\"lookY\":"
                                                        + look.y() + ",\"lookZ\":"
                                                        + look.z() + ",\"upX\":"
                                                        + up.x() + ",\"upY\":"
                                                        + up.y() + ",\"upZ\":"
                                                        + up.z() + ",\"leftX\":"
                                                        + left.x() + ",\"leftY\":"
                                                        + left.y() + ",\"leftZ\":"
                                                        + left.z() + ",\"localX\":"
                                                        + localX + ",\"localY\":"
                                                        + localY + ",\"localZ\":"
                                                        + localZ + ",\"verticalFov\":"
                                                        + verticalFov + ",\"screenX\":"
                                                        + result.screenX() + ",\"screenY\":"
                                                        + result.screenY() + ",\"clamped\":"
                                                        + result.clampedToEdge() + ",\"angle\":"
                                                        + result.angleDegrees() + ",\"guiWidth\":"
                                                        + guiGraphics.guiWidth() + ",\"guiHeight\":"
                                                        + guiGraphics.guiHeight() + "},\"ts\":"
                                                        + System.currentTimeMillis() + "}"))
                                        .build(),
                                java.net.http.HttpResponse.BodyHandlers.discarding())
                        .exceptionally(ex -> null);
            } catch (Exception ignored) {
            }
        }
        // #endregion
        if (result.clampedToEdge()) {
            drawMarkerArrow(guiGraphics, result.screenX(), result.screenY(), (float) result.angleDegrees());
        } else {
            drawMarkerDot(guiGraphics, result.screenX(), result.screenY());
        }
    }

    private static double resolveVerticalFovDegrees(Minecraft mc, Camera camera, DeltaTracker deltaTracker) {
        try {
            return ((MixinGameRendererAccessor) mc.gameRenderer)
                    .otherworldinn$invokeGetFov(camera, deltaTracker.getGameTimeDeltaPartialTick(true), true);
        } catch (Exception ignored) {
            return mc.options.fov().get();
        }
    }

    private static String readDebugServerUrl() {
        try {
            java.nio.file.Path path = java.nio.file.Path.of(".dbg", "quest-marker-position.env");
            for (String line : java.nio.file.Files.readAllLines(path)) {
                if (line.startsWith("DEBUG_SERVER_URL=")) {
                    return line.substring("DEBUG_SERVER_URL=".length()).trim();
                }
            }
        } catch (Exception ignored) {
        }
        return "http://127.0.0.1:7777/event";
    }

    private static String readDebugSessionId() {
        try {
            java.nio.file.Path path = java.nio.file.Path.of(".dbg", "quest-marker-position.env");
            for (String line : java.nio.file.Files.readAllLines(path)) {
                if (line.startsWith("DEBUG_SESSION_ID=")) {
                    return escapeDebug(line.substring("DEBUG_SESSION_ID=".length()).trim());
                }
            }
        } catch (Exception ignored) {
        }
        return "quest-marker-position";
    }

    private static String escapeDebug(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static int[] findNearestMarker(List<int[]> markers, Vec3 cameraPos) {
        int[] nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int[] marker : markers) {
            if (marker == null || marker.length < 3) {
                continue;
            }
            double distance = cameraPos.distanceToSqr(marker[0] + 0.5D, marker[1] + 0.5D, marker[2] + 0.5D);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = marker;
            }
        }
        return nearest;
    }

    /** 指示点图标纹理（8x8）：存在则优先使用，缺失回退为程序绘制的圆点。 */
    private static final ResourceLocation MARKER_ICON =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "textures/gui/quest_hud/marker_icon.png");
    /** 离屏箭头纹理（16x16）。 */
    private static final ResourceLocation MARKER_ARROW_ICON =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "textures/gui/quest_hud/marker_arrow.png");

    private static void drawMarkerDot(GuiGraphics guiGraphics, double cx, double cy) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float) cx, (float) cy, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(MARKER_ICON, -MARKER_ICON_RADIUS, -MARKER_ICON_RADIUS, 0, 0, 8, 8, 8, 8);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();
    }

    private static void drawMarkerArrow(GuiGraphics guiGraphics, double cx, double cy, float angleDegrees) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float) cx, (float) cy, 0.0F);
        guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(angleDegrees));
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.blit(
                MARKER_ARROW_ICON,
                -MARKER_ARROW_RADIUS,
                -MARKER_ARROW_RADIUS,
                0,
                0,
                16,
                16,
                16,
                16);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();
    }

    private static void drawLines(
            GuiGraphics guiGraphics, Font font, List<DisplayLine> lines, int x, int y, int baseColor) {
        int textY = y + BLOCK_PADDING_Y;
        for (DisplayLine line : lines) {
            guiGraphics.drawString(
                    font, line.text(), x + BLOCK_PADDING_X + line.indent(), textY, applyBaseColor(line.color(), baseColor), false);
            textY += font.lineHeight + LINE_GAP;
        }
    }

    /** baseColor 只提供 alpha 位，颜色仍由各行自带颜色决定（保持 [主线] 前缀等样式色）。 */
    private static int applyBaseColor(int lineColor, int baseColor) {
        return (baseColor & 0xFF000000) | (lineColor & 0x00FFFFFF);
    }

    private static int withAlphaBits(float alpha) {
        return (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F) << 24;
    }

    /** 标题加分类前缀：[主线] / [支线]（前缀与标题同行，整行用分类色绘制以参与淡入淡出）。 */
    private static Component withCategoryPrefix(QuestHudState.HudTask task) {
        boolean main = "main".equals(task.category());
        boolean zh = ClientServices.isChineseLocale();
        String prefix = main ? (zh ? "[主线] " : "[Main] ") : (zh ? "[支线] " : "[Side] ");
        return Component.literal(prefix).append(task.title());
    }

    /** 维护出现/消失时刻表、内容缓存与槽位顺序（新块按快照邻居关系插入槽位）。 */
    private static void updateAnimationState(List<TaskBlock> current, long now) {
        Set<String> currentKeys = new HashSet<>();
        for (TaskBlock block : current) {
            currentKeys.add(block.key());
        }
        for (String key : currentKeys) {
            DISAPPEARED_AT.remove(key);
            APPEARED_AT.putIfAbsent(key, now);
        }
        for (Map.Entry<String, List<DisplayLine>> entry : LINE_CACHE.entrySet()) {
            if (!currentKeys.contains(entry.getKey()) && !DISAPPEARED_AT.containsKey(entry.getKey())) {
                DISAPPEARED_AT.put(entry.getKey(), now);
            }
        }

        // 节点推进检测：阶段签名变化 → 记录旧内容开始交叉淡变（须在覆盖 LINE_CACHE 之前）
        for (int i = 0; i < current.size(); i++) {
            TaskBlock block = current.get(i);
            String previous = STAGE_CACHE.put(block.key(), block.stage());
            if (previous != null
                    && !previous.equals(block.stage())
                    && !CROSS_FADE.containsKey(block.key())) {
                List<DisplayLine> oldLines = LINE_CACHE.get(block.key());
                if (oldLines != null) {
                    CROSS_FADE.put(block.key(), new CrossFade(oldLines, now));
                }
            }
        }

        for (TaskBlock block : current) {
            LINE_CACHE.put(block.key(), block.lines());
        }
        APPEARED_AT.keySet().removeIf(key -> !currentKeys.contains(key) && !DISAPPEARED_AT.containsKey(key));

        // 槽位插入：按快照顺序，插到下一个"已在槽位表中"的兄弟之前；无兄弟则追加末尾
        for (int i = 0; i < current.size(); i++) {
            String key = current.get(i).key();
            if (SLOT_ORDER.contains(key)) {
                continue;
            }
            int insertAt = -1;
            for (int j = i + 1; j < current.size(); j++) {
                int existing = SLOT_ORDER.indexOf(current.get(j).key());
                if (existing >= 0) {
                    insertAt = existing;
                    break;
                }
            }
            if (insertAt >= 0) {
                SLOT_ORDER.add(insertAt, key);
            } else {
                SLOT_ORDER.add(key);
            }
        }
    }

    private static boolean isRenderable(String key, List<TaskBlock> current) {
        for (TaskBlock block : current) {
            if (block.key().equals(key)) {
                return true;
            }
        }
        return DISAPPEARED_AT.containsKey(key);
    }

    private static int countDrawable(List<String> keys, Map<String, Integer> heights) {
        int count = 0;
        for (String key : keys) {
            if (heights.containsKey(key)) {
                count++;
            }
        }
        return count;
    }

    private static int measureBlockHeight(Font font, List<DisplayLine> lines) {
        return BLOCK_PADDING_Y * 2 + lines.size() * (font.lineHeight + LINE_GAP) - LINE_GAP;
    }

    private static void clearAnimations() {
        APPEARED_AT.clear();
        DISAPPEARED_AT.clear();
        LINE_CACHE.clear();
        SLOT_ORDER.clear();
        ANIM_Y.clear();
        STAGE_CACHE.clear();
        CROSS_FADE.clear();
        lastFrameMillis = 0L;
    }

    private static void appendWrapped(
            List<DisplayLine> lines, Font font, Component text, int color, int indent) {
        for (FormattedCharSequence wrapped : ComponentRenderUtils.wrapComponents(text, TEXT_MAX_WIDTH - indent, font)) {
            lines.add(new DisplayLine(wrapped, color, indent));
        }
    }

    private static float easeOut(float progress) {
        return 1.0F - (1.0F - progress) * (1.0F - progress);
    }

    private static int withAlpha(int color, float alpha) {
        int alphaBits = (int) ((color >>> 24) * Mth.clamp(alpha, 0.0F, 1.0F));
        return (alphaBits << 24) | (color & 0x00FFFFFF);
    }
}
