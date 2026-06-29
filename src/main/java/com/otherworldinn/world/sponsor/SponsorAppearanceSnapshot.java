package com.otherworldinn.world.sponsor;

public record SponsorAppearanceSnapshot(
        String sponsorName,
        String displayName,
        String modelType,
        String skinUrl,
        SponsorProfileSourceType sourceType) {
    public SponsorAppearanceSnapshot {
        sponsorName = normalizeText(sponsorName);
        displayName = normalizeText(displayName);
        if (displayName.isBlank()) {
            displayName = sponsorName;
        }
        modelType = normalizeModelType(modelType);
        skinUrl = normalizeText(skinUrl);
        sourceType = sourceType == null ? SponsorProfileSourceType.LOCAL_FALLBACK : sourceType;
    }

    public static SponsorAppearanceSnapshot localFallback(String sponsorName) {
        return new SponsorAppearanceSnapshot(
                sponsorName,
                sponsorName,
                "default",
                "",
                SponsorProfileSourceType.LOCAL_FALLBACK);
    }

    public boolean hasSkinUrl() {
        return !this.skinUrl.isBlank();
    }

    private static String normalizeText(String text) {
        return text == null ? "" : text.trim();
    }

    private static String normalizeModelType(String modelType) {
        return "slim".equalsIgnoreCase(modelType) ? "slim" : "default";
    }
}
