package com.otherworldinn.foundation;

import java.util.ArrayList;
import java.util.List;

/** 物品的数据生成配置信息（modelType 可选 generated/handheld）。 */
public record ItemDataGenInfo(
        boolean generateModel,
        String modelType,
        String enName,
        String cnName,
        List<String> enTooltips,
        List<String> cnTooltips) {
    public static final ItemDataGenInfo DEFAULT =
            new ItemDataGenInfo(true, "generated", "", "", new ArrayList<>(), new ArrayList<>());
}
