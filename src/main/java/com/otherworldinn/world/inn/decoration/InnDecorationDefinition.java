package com.otherworldinn.world.inn.decoration;

import java.util.List;
import net.minecraft.world.level.block.Rotation;

/**
 * 旅社装饰定义。
 *
 * <p>基础框架阶段只关心模板来源、允许旋转和最大生效数量。
 */
public record InnDecorationDefinition(
        String id,
        String enName,
        String zhName,
        String translationKey,
        String templateResourcePath,
        List<InnDecorationBuff> buffs,
        List<Rotation> allowedRotations,
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
        if (templateResourcePath == null || templateResourcePath.isBlank()) {
            throw new IllegalArgumentException("Decoration templateResourcePath cannot be blank.");
        }
        buffs = buffs == null ? List.of() : List.copyOf(buffs);
        allowedRotations =
                allowedRotations == null || allowedRotations.isEmpty()
                        ? List.of(Rotation.NONE)
                        : List.copyOf(allowedRotations);
        maxInstances = Math.max(1, maxInstances);
    }
}
