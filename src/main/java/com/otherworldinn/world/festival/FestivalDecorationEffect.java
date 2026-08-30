package com.otherworldinn.world.festival;

import com.otherworldinn.world.festival.FestivalDecorationService.DecorationRegion;
import java.util.List;
import net.minecraft.server.level.ServerLevel;

/** 节日装饰效果：开始时贴节日版结构快照，结束时贴还原版。 */
public class FestivalDecorationEffect implements FestivalEffect {
    private final List<DecorationRegion> regions;

    public FestivalDecorationEffect(List<DecorationRegion> regions) {
        this.regions = List.copyOf(regions);
    }

    public List<DecorationRegion> regions() {
        return regions;
    }

    @Override
    public void onFestivalStart(ServerLevel townLevel, FestivalContext ctx) {
        FestivalDecorationService.apply(townLevel, ctx.festival().id(), regions);
    }

    @Override
    public void onFestivalEnd(ServerLevel townLevel, FestivalContext ctx) {
        FestivalDecorationService.restore(townLevel, regions);
    }
}
