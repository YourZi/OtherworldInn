package com.otherworldinn.world.festival;

import java.util.List;

/**
 * 节日定义
 *
 * @param id 唯一 ID（如 spring_festival）
 * @param enName 英文名
 * @param zhName 中文名
 * @param trigger 时间触发窗口
 * @param effects 效果集合（可为空）
 * @param translationKey 文案翻译键（festival.otherworldinn.{id}）
 */
public record FestivalDefinition(
        String id,
        String enName,
        String zhName,
        FestivalTrigger trigger,
        List<FestivalEffect> effects,
        String translationKey) {}
