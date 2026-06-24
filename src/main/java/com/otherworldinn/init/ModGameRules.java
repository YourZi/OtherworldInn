package com.otherworldinn.init;

import net.minecraft.world.level.GameRules;

public final class ModGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> RULE_ENABLE_SPONSOR_GUEST =
            GameRules.register(
                    "enableSponsorGuest",
                    GameRules.Category.SPAWNING,
                    GameRules.BooleanValue.create(false));

    private ModGameRules() {}

    public static void init() {}
}
