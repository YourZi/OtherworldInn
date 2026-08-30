package com.otherworldinn.world.festival;

import net.minecraft.server.level.ServerLevel;

/** 节日运行上下文（{@code dayIndex} 为节日内第几天，0 起）。 */
public record FestivalContext(ServerLevel townLevel, FestivalDefinition festival, int dayIndex) {}
