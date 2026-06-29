package com.otherworldinn.world.sponsor;

public enum SponsorProfileSourceType {
    MOJANG_ONLINE(0),
    LIVE_PLAYER(1),
    LOCAL_FALLBACK(2);

    private final int syncCode;

    SponsorProfileSourceType(int syncCode) {
        this.syncCode = syncCode;
    }

    public int syncCode() {
        return this.syncCode;
    }

    public static SponsorProfileSourceType fromSyncCode(int syncCode) {
        for (SponsorProfileSourceType value : values()) {
            if (value.syncCode == syncCode) {
                return value;
            }
        }
        return LOCAL_FALLBACK;
    }
}
