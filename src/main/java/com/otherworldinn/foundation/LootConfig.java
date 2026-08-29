package com.otherworldinn.foundation;

import java.util.ArrayList;
import java.util.List;

/** 战利品表配置（entries 仅在 type 为 CUSTOM 时使用）。 */
public record LootConfig(LootType type, List<LootEntry> entries, boolean silkTouchDropSelf) {
    public static final LootConfig DEFAULT =
            new LootConfig(LootType.DROP_SELF, new ArrayList<>(), false);
    public static final LootConfig EMPTY =
            new LootConfig(LootType.DROP_NOTHING, new ArrayList<>(), false);

    public enum LootType {
        DROP_SELF,
        DROP_NOTHING,
        CUSTOM
    }

    /** 单个战利品条目（chance 取值 0.0-1.0，itemId 形如 namespace:id）。 */
    public record LootEntry(
            String itemId, float chance, int minCount, int maxCount, boolean requiresSilkTouch) {}
}
