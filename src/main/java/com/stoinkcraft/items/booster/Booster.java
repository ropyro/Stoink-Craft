package com.stoinkcraft.items.booster;

import com.google.gson.annotations.Expose;

/**
 * Represents an active booster on an Enterprise.
 * Tracks multiplier, duration, and start time for persistence.
 */
public class Booster {

    @Expose
    private final double multiplier;

    @Expose
    private final long durationMillis;

    @Expose
    private final long startTime;

    @Expose
    private final BoosterTier tier;

    public Booster(BoosterTier tier) {
        this.tier = tier;
        this.multiplier = tier.getMultiplier();
        this.durationMillis = tier.getDurationMillis();
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Reconstruction constructor for loading from save data.
     */
    public Booster(BoosterTier tier, double multiplier, long durationMillis, long startTime) {
        this.tier = tier;
        this.multiplier = multiplier;
        this.durationMillis = durationMillis;
        this.startTime = startTime;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public long getStartTime() {
        return startTime;
    }

    public BoosterTier getTier() {
        return tier;
    }

    public long getTimeRemainingMillis() {
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, durationMillis - elapsed);
    }

    public long getTimeRemainingTicks() {
        return getTimeRemainingMillis() / 50; // 50ms per tick
    }

    public boolean isExpired() {
        return getTimeRemainingMillis() <= 0;
    }

    /**
     * Formats remaining time as a human-readable string.
     * e.g., "4m 30s" or "45s"
     */
    public String getFormattedTimeRemaining() {
        long seconds = getTimeRemainingMillis() / 1000;

        if (seconds <= 0) {
            return "Expired";
        }

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (minutes > 0) {
            return minutes + "m " + remainingSeconds + "s";
        } else {
            return remainingSeconds + "s";
        }
    }
}