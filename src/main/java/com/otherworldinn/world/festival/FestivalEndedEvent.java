package com.otherworldinn.world.festival;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** 节日结束事件：窗口离开时广播 */
public class FestivalEndedEvent extends Event {
    private final ServerLevel townLevel;
    private final FestivalDefinition festival;

    public FestivalEndedEvent(ServerLevel townLevel, FestivalDefinition festival) {
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
