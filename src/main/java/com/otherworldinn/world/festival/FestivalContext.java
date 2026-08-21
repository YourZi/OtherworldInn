package com.otherworldinn.world.festival;

import net.minecraft.server.level.ServerLevel;

/**
 * 节日运行上下文
 *
 * @param townLevel 城镇维度
 * @param festival 当前节日定义
 * @param dayIndex 节日第几天（0 起）
 */
public record FestivalContext(ServerLevel townLevel, FestivalDefinition festival, int dayIndex) {}
