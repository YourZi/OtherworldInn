package com.otherworldinn.world.inn.decoration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.Rotation;

/**
 * 旅社装饰注册表。
 *
 * <p>基础框架阶段直接用代码注册少量样例模板，后续可迁移为数据驱动。
 */
public final class InnDecorationRegistry {
    private static final Map<String, InnDecorationDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final List<Rotation> ALL_HORIZONTAL_ROTATIONS =
            List.of(Rotation.NONE, Rotation.CLOCKWISE_90, Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90);

    static {
        registerDefaults();
    }

    private InnDecorationRegistry() {}

    public static InnDecorationDefinition register(
            String id,
            String enName,
            String zhName,
            String templateResourcePath,
            List<InnDecorationBuff> buffs,
            List<Rotation> allowedRotations,
            int maxInstances) {
        InnDecorationDefinition definition =
                new InnDecorationDefinition(
                        id,
                        enName,
                        zhName,
                        "inn_decoration.otherworldinn." + id,
                        templateResourcePath,
                        buffs,
                        allowedRotations,
                        maxInstances);
        DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    public static InnDecorationDefinition get(String id) {
        return DEFINITIONS.get(id);
    }

    public static Collection<InnDecorationDefinition> getAll() {
        return DEFINITIONS.values();
    }

    private static void registerDefaults() {
        register(
                "coral_rockery",
                "Coral Rockery",
                "珊瑚假山",
                "data/otherworldinn/inn_decorations/coral_rockery.nbt",
                List.of(new InnDecorationBuff(InnDecorationBuffType.LODGING_INCOME_MULTIPLIER, 0.10D)),
                ALL_HORIZONTAL_ROTATIONS,
                1);
    }
}
