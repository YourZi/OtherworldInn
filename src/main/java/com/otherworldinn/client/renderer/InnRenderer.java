package com.otherworldinn.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.control.CameraHandler;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.team.TeamData;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public class InnRenderer {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!CameraHandler.isMapMode()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        // 移动到世界坐标原点
        Vec3 cameraPos = event.getCamera().getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // TODO: 地图视图下的旅社范围渲染暂时禁用，renderInnZones 保留待启用
        poseStack.popPose();
    }

    private static void renderInnZones(
            PoseStack poseStack, List<TeamData.InnRegion> regions, InnData.InnState state) {
        Tesselator tesselator = Tesselator.getInstance();

        // 渲染设置
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableCull();

        float y = 71.01f; // 假设地面在 70

        // 颜色设置
        float red, green, blue;
        if (state == InnData.InnState.CLOSED) {
            red = 1.0f;
            green = 0.416f;
            blue = 0.416f;
        } else {
            // 营业模式：绿色 #00FF7F
            red = 0.0f;
            green = 1.0f;
            blue = 0.498f;
        }
        float alpha = 0.2f;

        Matrix4f matrix = poseStack.last().pose();

        // 1. 批量绘制所有填充
        try {
            BufferBuilder buffer =
                    tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

            for (TeamData.InnRegion region : regions) {
                float minX = region.minX();
                float maxX = region.maxX() + 1; // +1 覆盖完整方块
                float minZ = region.minZ();
                float maxZ = region.maxZ() + 1;

                buffer.addVertex(matrix, minX, y, minZ).setColor(red, green, blue, alpha);
                buffer.addVertex(matrix, minX, y, maxZ).setColor(red, green, blue, alpha);
                buffer.addVertex(matrix, maxX, y, maxZ).setColor(red, green, blue, alpha);
                buffer.addVertex(matrix, maxX, y, minZ).setColor(red, green, blue, alpha);
            }

            BufferUploader.drawWithShader(buffer.buildOrThrow());
        } catch (Exception e) {
            // 忽略可能的异常 (API 变动)
        }

        // 2. 绘制边框 (仅绘制外轮廓)
        RenderSystem.lineWidth(2.0f);
        alpha = 0.8f;

        try {
            // 使用 DEBUG_LINES 模式绘制所有线段
            BufferBuilder lineBuffer =
                    tesselator.begin(
                            VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

            for (TeamData.InnRegion region : regions) {
                // North (z = minZ)
                drawEdge(
                        lineBuffer,
                        matrix,
                        region.minX(),
                        region.maxX() + 1,
                        region.minZ(),
                        regions,
                        (r) -> r.maxZ() + 1 == region.minZ(),
                        (r) -> new Interval(r.minX(), r.maxX() + 1),
                        true,
                        red,
                        green,
                        blue,
                        alpha,
                        y);

                // South (z = maxZ + 1)
                drawEdge(
                        lineBuffer,
                        matrix,
                        region.minX(),
                        region.maxX() + 1,
                        region.maxZ() + 1,
                        regions,
                        (r) -> r.minZ() == region.maxZ() + 1,
                        (r) -> new Interval(r.minX(), r.maxX() + 1),
                        true,
                        red,
                        green,
                        blue,
                        alpha,
                        y);

                // West (x = minX)
                drawEdge(
                        lineBuffer,
                        matrix,
                        region.minZ(),
                        region.maxZ() + 1,
                        region.minX(),
                        regions,
                        (r) -> r.maxX() + 1 == region.minX(),
                        (r) -> new Interval(r.minZ(), r.maxZ() + 1),
                        false,
                        red,
                        green,
                        blue,
                        alpha,
                        y);

                // East (x = maxX + 1)
                drawEdge(
                        lineBuffer,
                        matrix,
                        region.minZ(),
                        region.maxZ() + 1,
                        region.maxX() + 1,
                        regions,
                        (r) -> r.minX() == region.maxX() + 1,
                        (r) -> new Interval(r.minZ(), r.maxZ() + 1),
                        false,
                        red,
                        green,
                        blue,
                        alpha,
                        y);
            }

            BufferUploader.drawWithShader(lineBuffer.buildOrThrow());
        } catch (Exception e) {
            // 忽略
        }

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
    }

    private record Interval(int start, int end) {
        boolean intersects(Interval other) {
            return this.start < other.end && this.end > other.start;
        }
    }

    private static void drawEdge(
            BufferBuilder buffer,
            Matrix4f matrix,
            int start,
            int end,
            int constantCoord,
            List<TeamData.InnRegion> allRegions,
            java.util.function.Predicate<TeamData.InnRegion> isNeighbor,
            java.util.function.Function<TeamData.InnRegion, Interval> getNeighborInterval,
            boolean isXAxis,
            float r,
            float g,
            float b,
            float a,
            float y) {

        java.util.List<Interval> segments = new java.util.ArrayList<>();
        segments.add(new Interval(start, end));

        for (TeamData.InnRegion region : allRegions) {
            if (isNeighbor.test(region)) {
                segments = subtract(segments, getNeighborInterval.apply(region));
                if (segments.isEmpty()) return;
            }
        }

        for (Interval seg : segments) {
            if (isXAxis) {
                buffer.addVertex(matrix, seg.start, y, constantCoord).setColor(r, g, b, a);
                buffer.addVertex(matrix, seg.end, y, constantCoord).setColor(r, g, b, a);
            } else {
                buffer.addVertex(matrix, constantCoord, y, seg.start).setColor(r, g, b, a);
                buffer.addVertex(matrix, constantCoord, y, seg.end).setColor(r, g, b, a);
            }
        }
    }

    private static java.util.List<Interval> subtract(
            java.util.List<Interval> current, Interval remove) {
        java.util.List<Interval> result = new java.util.ArrayList<>();
        for (Interval i : current) {
            // No intersection
            if (!i.intersects(remove)) {
                result.add(i);
                continue;
            }

            // Left part
            if (i.start < remove.start) {
                result.add(new Interval(i.start, remove.start));
            }

            // Right part
            if (i.end > remove.end) {
                result.add(new Interval(remove.end, i.end));
            }
        }
        return result;
    }
}
