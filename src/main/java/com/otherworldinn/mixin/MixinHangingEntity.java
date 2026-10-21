package com.otherworldinn.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让挂载实体（物品展示框、荧光展示框、画）贴合被点击方块的实际表面，而不是整格边界平面。
 *
 * <p>原版把背面偏移硬编码为 0.46875（对齐到整格边界），贴在栅栏柱侧面、半砖顶面这类非满方块上会悬空。
 * 这里在 {@code recalculateBoundingBox} 之后按支撑方块朝该方向的最外沿平移包围盒——实体位置由包围盒中心推导，
 * 所以改包围盒即可同时修正渲染位置，无需额外同步或存档字段。
 */
@Mixin(HangingEntity.class)
public abstract class MixinHangingEntity {

    @Inject(method = "recalculateBoundingBox", at = @At("TAIL"))
    private void otherworldinn$snapToSupportSurface(CallbackInfo ci) {
        HangingEntity hanging = (HangingEntity) (Object) this;
        // 构造期 Entity 构造函数会调 setPos 触发一次重算，此时 direction 字段尚未初始化，
        // 原版靠 recalculateBoundingBox 开头的 null 判断静默跳过，这里必须同样跳过。
        Direction direction = hanging.getDirection();
        if (direction == null) {
            return;
        }

        BlockPos supportPos = hanging.getPos().relative(direction.getOpposite());
        Level level = hanging.level();
        BlockState supportState = level.getBlockState(supportPos);
        VoxelShape supportShape = supportState.getShape(level, supportPos);
        if (supportShape.isEmpty()) {
            return;
        }

        Direction.Axis axis = direction.getAxis();
        boolean positive = direction.getAxisDirection() == Direction.AxisDirection.POSITIVE;

        // 支撑方块朝该方向的最外沿（栅栏柱 0.375 / 半砖顶面 0.5 等）
        double surface = positive ? supportShape.max(axis) : supportShape.min(axis);
        // 与整格边界（正方向为 1.0，负方向为 0.0）的差值，>0 表示表面在整格内部
        double gap = positive ? 1.0D - surface : surface;
        if (Math.abs(gap) < 1.0E-4D) {
            return;
        }

        double offset = -gap;
        AABB box = hanging.getBoundingBox().move(
                direction.getStepX() * offset,
                direction.getStepY() * offset,
                direction.getStepZ() * offset);
        Vec3 center = box.getCenter();
        hanging.setPosRaw(center.x, center.y, center.z);
        hanging.setBoundingBox(box);
    }
}
