package com.otherworldinn.world.inn.decoration;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 已解析的装饰模板。 */
public record InnDecorationTemplate(List<TemplateBlock> blocks) {
    public InnDecorationTemplate {
        blocks = List.copyOf(blocks);
    }

    public record TemplateBlock(BlockPos relativePos, BlockState state) {}
}
