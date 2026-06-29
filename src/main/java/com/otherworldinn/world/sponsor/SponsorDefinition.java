package com.otherworldinn.world.sponsor;

import java.util.Locale;

public record SponsorDefinition(String playerName, int spawnWeight) {
    public SponsorDefinition {
        playerName = playerName == null ? "" : playerName.trim();
        spawnWeight = Math.max(1, spawnWeight);
    }

    public static SponsorDefinition of(String playerName) {
        return new SponsorDefinition(playerName, 1);
    }

    public String normalizedKey() {
        return this.playerName.toLowerCase(Locale.ROOT);
    }
}
