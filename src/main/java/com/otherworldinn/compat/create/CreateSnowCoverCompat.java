package com.otherworldinn.compat.create;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Create 兼容：动能方块不铺雪。
 *
 * <p>齿轮、传动轴、水车这类方块会旋转，但它们同时也是普通的 {@code RenderShape.MODEL} 方块，
 * 光看"有没有烘焙模型"区分不出来。雪一旦铺上去就是一层静止的板子，方块一转就浮在原地。
 * Create 的动能方块都继承 {@link KineticBlock}，直接按类型排除最稳。
 */
public final class CreateSnowCoverCompat {

    private CreateSnowCoverCompat() {}

    public static boolean ignoresSnowCover(BlockState state) {
        return state.getBlock() instanceof KineticBlock;
    }
}
