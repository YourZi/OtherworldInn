package com.otherworldinn.world.festival;

import java.util.List;

/** 节日定义（translationKey 形如 {@code festival.otherworldinn.{id}}）。 */
public record FestivalDefinition(
        String id,
        String enName,
        String zhName,
        FestivalTrigger trigger,
        List<FestivalEffect> effects,
        String translationKey) {}
