package com.otherworldinn.world.inn.decoration;

/** 装饰提供的一条经营 buff。 */
public record InnDecorationBuff(InnDecorationBuffType type, double value) {
    public InnDecorationBuff {
        if (type == null) {
            throw new IllegalArgumentException("Decoration buff type cannot be null.");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Decoration buff value must be finite.");
        }
    }
}
