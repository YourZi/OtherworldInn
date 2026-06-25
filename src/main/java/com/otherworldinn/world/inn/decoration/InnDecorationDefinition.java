package com.otherworldinn.world.inn.decoration;

import java.util.List;

/**
 * 旅社装饰定义。
 *
 * <p>新版装饰系统改为单方块全局效果，不再依赖模板、旋转和手动激活。
 */
public record InnDecorationDefinition(
        String id,
        String enName,
        String zhName,
        String translationKey,
        List<InnDecorationBuff> buffs,
        int maxInstances) {

    public InnDecorationDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Decoration id cannot be blank.");
        }
        enName = enName == null || enName.isBlank() ? id : enName;
        zhName = zhName == null || zhName.isBlank() ? enName : zhName;
        translationKey =
                translationKey == null || translationKey.isBlank()
                        ? "inn_decoration.otherworldinn." + id
                        : translationKey;
        buffs = buffs == null ? List.of() : List.copyOf(buffs);
        maxInstances = maxInstances == 0 ? 1 : maxInstances;
    }

    public int getEffectiveInstanceCount(int actualInstances) {
        if (actualInstances <= 0) {
            return 0;
        }
        if (maxInstances < 0) {
            return actualInstances;
        }
        return Math.min(actualInstances, maxInstances);
    }
}
