package com.otherworldinn.world.inn.decoration;

/** 单个全局装饰提供的经营属性。 */
public record InnDecorationStats(
        String id,
        double lodgingIncomeMultiplier,
        double diningIncomeMultiplier,
        double guestArrivalSpeedMultiplier,
        double reputationGainMultiplier,
        int maxInstances) {

    public InnDecorationStats {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Decoration id cannot be blank.");
        }
        if (!Double.isFinite(lodgingIncomeMultiplier)
                || !Double.isFinite(diningIncomeMultiplier)
                || !Double.isFinite(guestArrivalSpeedMultiplier)
                || !Double.isFinite(reputationGainMultiplier)) {
            throw new IllegalArgumentException("Decoration stat values must be finite.");
        }
        maxInstances = maxInstances == 0 ? 1 : maxInstances;
    }

    public int getEffectiveInstanceCount(int actualInstances) {
        if (actualInstances <= 0) {
            return 0;
        }
        if (maxInstances < 0) {
            return actualInstances;
        }
        return Math.min(actualInstances, maxInstances);
    }

    public double getValue(InnDecorationBuffType type) {
        if (type == null) {
            return 0.0D;
        }
        return switch (type) {
            case LODGING_INCOME_MULTIPLIER -> lodgingIncomeMultiplier;
            case DINING_INCOME_MULTIPLIER -> diningIncomeMultiplier;
            case GUEST_ARRIVAL_SPEED_MULTIPLIER -> guestArrivalSpeedMultiplier;
            case REPUTATION_GAIN_MULTIPLIER -> reputationGainMultiplier;
        };
    }
}
