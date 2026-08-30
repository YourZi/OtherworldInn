package com.otherworldinn.client.quest;

import net.minecraft.util.Mth;

/**
 * 把相机局部空间中的目标位置映射为 HUD 上的 2D 锚点；不依赖渲染投影矩阵，屏外或身后的目标吸附到屏幕边缘并给出箭头角度。
 */
public final class QuestMarkerProjector {
    private static final double EPSILON = 1.0E-4D;

    private QuestMarkerProjector() {}

    public record Result(double screenX, double screenY, double angleDegrees, boolean clampedToEdge) {}

    public static Result project(
            double localX,
            double localY,
            double localZ,
            double verticalFovDegrees,
            int screenWidth,
            int screenHeight,
            int edgeMargin) {
        double safeVerticalFov = Mth.clamp(verticalFovDegrees, 1.0D, 179.0D);
        double halfVerticalRadians = Math.toRadians(safeVerticalFov * 0.5D);
        double aspect = Math.max(1.0D, (double) screenWidth / Math.max(1, screenHeight));
        double halfHorizontalRadians = Math.atan(Math.tan(halfVerticalRadians) * aspect);

        double normalizedX = localX / Math.max(Math.abs(localZ), EPSILON) / Math.tan(halfHorizontalRadians);
        double normalizedY = -localY / Math.max(Math.abs(localZ), EPSILON) / Math.tan(halfVerticalRadians);

        if (localZ <= EPSILON) {
            normalizedX = -normalizedX;
            normalizedY = -normalizedY;
        }

        boolean insideScreen = localZ > EPSILON && Math.abs(normalizedX) <= 1.0D && Math.abs(normalizedY) <= 1.0D;
        if (insideScreen) {
            return new Result(
                    screenWidth * (0.5D + normalizedX * 0.5D),
                    screenHeight * (0.5D + normalizedY * 0.5D),
                    0.0D,
                    false);
        }

        if (Math.abs(normalizedX) < EPSILON && Math.abs(normalizedY) < EPSILON) {
            normalizedY = -1.0D;
        }

        // 离屏或身后都必须强制推到边缘，而不是只在超过 1 时才钳制。
        double clampScale = 1.0D / Math.max(Math.abs(normalizedX), Math.abs(normalizedY));
        double edgeX = normalizedX * clampScale;
        double edgeY = normalizedY * clampScale;
        double halfWidth = screenWidth * 0.5D - edgeMargin;
        double halfHeight = screenHeight * 0.5D - edgeMargin;
        double screenX = screenWidth * 0.5D + edgeX * halfWidth;
        double screenY = screenHeight * 0.5D + edgeY * halfHeight;
        double angle = Math.toDegrees(Math.atan2(edgeX, -edgeY));
        return new Result(screenX, screenY, angle, true);
    }
}
