package com.otherworldinn.init;

import net.minecraft.world.level.GameRules;

public final class ModGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> RULE_ENABLE_SPONSOR_GUEST =
            GameRules.register(
                    "enableSponsorGuest",
                    GameRules.Category.SPAWNING,
                    GameRules.BooleanValue.create(false));

    /** 是否允许原版动物繁殖（小麦/胡萝卜等直接生育）。默认关闭，改由养殖系统接管。 */
    public static final GameRules.Key<GameRules.BooleanValue> RULE_ALLOW_VANILLA_BREEDING =
            GameRules.register(
                    "allowVanillaBreeding",
                    GameRules.Category.SPAWNING,
                    GameRules.BooleanValue.create(false));

    private ModGameRules() {}

    public static void init() {}
}
