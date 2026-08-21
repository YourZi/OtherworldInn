package com.otherworldinn.world.festival;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** 节日开始事件：窗口首次激活时广播（每年同季同期再次触发） */
public class FestivalStartedEvent extends Event {
    private final ServerLevel townLevel;
    private final FestivalDefinition festival;

    public FestivalStartedEvent(ServerLevel townLevel, FestivalDefinition festival) {
        this.townLevel = townLevel;
        this.festival = festival;
    }

    public ServerLevel getTownLevel() {
        return townLevel;
    }

    public FestivalDefinition getFestival() {
        return festival;
    }
}
