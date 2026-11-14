package com.otherworldinn.client.snow;

/**
 * 方块局部坐标系（0~1）下的一个水平表面：一个 XZ 矩形 + 它所在的高度。
 *
 * <p>由 {@link SnowSurfaceCache} 从方块的视觉形状（{@code BlockState#getShape}）推导出来，
 * {@link SnowCoverRenderer} 会把每个表面变成一块 2/16 厚的雪板。
 */
public record SnowSurface(float x0, float z0, float x1, float z1, float y) {

    /** 表面面积（以方块边长为 1）。 */
    public float area() {
        return (this.x1 - this.x0) * (this.z1 - this.z0);
    }
}
