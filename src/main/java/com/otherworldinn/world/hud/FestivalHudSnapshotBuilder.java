package com.otherworldinn.world.hud;

import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.festival.FestivalDefinition;
import com.otherworldinn.world.festival.FestivalService;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class FestivalHudSnapshotBuilder {
    private FestivalHudSnapshotBuilder() {}

    public static CompoundTag buildForPlayer(ServerPlayer player) {
        if (player == null || player.getServer() == null) {
            return inactiveFestivalTag();
        }

        ServerLevel townLevel = player.getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) {
            return inactiveFestivalTag();
        }

        Optional<FestivalService.FestivalWindow> window = FestivalService.getActiveFestivalWindow(townLevel);
        if (window.isEmpty()) {
            return inactiveFestivalTag();
        }

        FestivalDefinition festival = window.get().festival();
        int daysRemaining = Math.max(1, window.get().lengthInDays() - window.get().dayIndex());
        CompoundTag tag = inactiveFestivalTag();
        tag.putBoolean("Active", true);
        tag.putString("NameKey", festival.translationKey());
        tag.putString("NameText", festival.zhName());
        tag.putInt("DaysRemaining", daysRemaining);
        return tag;
    }

    public static CompoundTag inactiveFestivalTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Active", false);
        tag.putString("NameKey", "");
        tag.putString("NameText", "");
        tag.putInt("DaysRemaining", 0);
        return tag;
    }
}
