package com.otherworldinn.foundation;

import java.util.ArrayList;
import java.util.List;

/** 方块的数据生成配置信息（renderType 可选 solid/cutout/translucent）。 */
public record BlockDataGenInfo(
        boolean generateModel,
        String renderType,
        LootConfig lootConfig,
        ToolType toolType,
        MiningLevel miningLevel,
        String enName,
        String cnName,
        List<String> enTooltips,
        List<String> cnTooltips) {
    public static final BlockDataGenInfo DEFAULT =
            new BlockDataGenInfo(
                    true,
                    "solid",
                    LootConfig.DEFAULT,
                    ToolType.NONE,
                    MiningLevel.NONE,
                    "",
                    "",
                    new ArrayList<>(),
                    new ArrayList<>());

    public enum ToolType {
        PICKAXE,
        AXE,
        SHOVEL,
        HOE,
        NONE
    }

    public enum MiningLevel {
        NONE,
        STONE,
        IRON,
        DIAMOND,
        NETHERITE
    }
}
