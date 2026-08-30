package com.otherworldinn.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.simibubi.create.content.redstone.deskBell.DeskBellBlockEntity;
import com.simibubi.create.foundation.gui.AllIcons;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeskBellIconRenderer {
    private static final Logger LOGGER = LogManager.getLogger();

    // 每个 DeskBell 动画开始的 GameTime，用于同步动画
    private static final Map<BlockPos, Long> ANIMATION_START_TICKS = new HashMap<>();

    public static void triggerAnimation(BlockPos pos, long gameTime) {
        ANIMATION_START_TICKS.put(pos, gameTime);
    }

    // 由 MixinDeskBellRenderer 调用
    public static void render(
            DeskBellBlockEntity blockEntity,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int light,
            int overlay) {
        Level level = blockEntity.getLevel();
        if (level == null || !level.isClientSide) return;

        BlockPos pos = blockEntity.getBlockPos();

        long gameTime = level.getGameTime();
        long startTime = ANIMATION_START_TICKS.getOrDefault(pos, -100L);
        if (gameTime - startTime > 20 || gameTime < startTime) {
            return;
        }

        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        if (team == null) return;

        List<TeamData.InnRegion> regions = team.getInnRegions();
        boolean inside = false;
        for (TeamData.InnRegion region : regions) {
            if (region.contains(pos)) {
                inside = true;
                break;
            }
        }
        if (!inside) return;

        InnData.InnState state = team.getInnData().getState();

        AllIcons icon;
        int color;

        switch (state) {
            case OPEN:
                icon = AllIcons.I_WHITELIST;
                color = ModColors.GREEN;
                break;
            case CLOSED:
                icon = AllIcons.I_BLACKLIST;
                color = ModColors.RED;
                break;
            default:
                return;
        }

        renderIcon(poseStack, bufferSource, pos, icon, color, level, partialTicks);
    }

    private static void renderIcon(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            BlockPos pos,
            AllIcons icon,
            int color,
            Level level,
            float partialTicks) {
        poseStack.pushPose();

        long gameTime = level.getGameTime();
        long startTime = ANIMATION_START_TICKS.getOrDefault(pos, gameTime);

        // 动画计算: 总周期 1.0s (20 ticks)
        float cycleTicks = 20.0f;
        float timePassed = (gameTime - startTime) + partialTicks;
        float t = Mth.clamp(timePassed / cycleTicks, 0.0f, 1.0f);

        float alpha;
        float yOffset;

        // 0.0 - 0.2: 淡入上滑
        float t1 = 0.2f;

        if (t < t1) {
            float progress = t / t1;
            float ease = 1 - (float) Math.pow(1 - progress, 3);
            alpha = ease;
            yOffset = -0.5f * (1 - ease);
        } else {
            alpha = 1.0f;
            yOffset = 0.0f;
        }

        // 移动到方块上方 1.2 格中心 (带偏移)
        poseStack.translate(0.5, 1.2 + yOffset, 0.5);

        // 面向玩家旋转 (仅水平旋转)
        Minecraft mc = Minecraft.getInstance();
        float yaw = mc.gameRenderer.getMainCamera().getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));

        float scale = 0.5f;
        poseStack.scale(scale, scale, scale);

        // 垂直翻转
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));

        // 居中
        poseStack.translate(-0.5, -0.5, 0);

        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (int) (alpha * 255);
        int finalColor = (a << 24) | (r << 16) | (g << 8) | b;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        icon.render(poseStack, bufferSource, finalColor);

        RenderSystem.disableBlend();

        poseStack.popPose();
    }
}
