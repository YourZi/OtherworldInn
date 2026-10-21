package com.otherworldinn.world.festival;

import java.util.Map;

/** 故事客人刷新加成：把指定客人的刷新权重提升配置值（0.5 = 提升 50%）；键 {@code ""} 为全局加成，具体客人 id 优先。 */
public class GuestSpawnBoostEffect implements FestivalEffect {

    /** storyGuestId → 刷新权重加成；"" 键表示全局加成 */
    private final Map<String, Double> guestSpawnBoosts;

    public GuestSpawnBoostEffect(Map<String, Double> guestSpawnBoosts) {
        this.guestSpawnBoosts = Map.copyOf(guestSpawnBoosts);
    }

    @Override
    public double queryValue(String key, Object... args) {
        if (!KEY_GUEST_SPAWN_BOOST.equals(key) || args.length == 0 || args[0] == null) {
            return 0.0D;
        }
        String guestId = args[0].toString();
        Double specific = guestSpawnBoosts.get(guestId);
        if (specific != null) {
            return specific;
        }
        return guestSpawnBoosts.getOrDefault("", 0.0D);
    }
}
