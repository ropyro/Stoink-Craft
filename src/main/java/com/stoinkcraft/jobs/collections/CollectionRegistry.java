package com.stoinkcraft.jobs.collections;

/**
 * Static registry for collection level thresholds and XP rewards.
 * All functions are centralized here for easy balancing adjustments.
 */
public final class CollectionRegistry {

    public static final int MAX_LEVEL = 27;

    // ==================== THRESHOLD CONFIGURATION ====================
    private static final long BASE_THRESHOLD = 150;
    private static final long MAX_THRESHOLD = 50_000;

    // ==================== XP REWARD CONFIGURATION ====================
    private static final int BASE_XP_REWARD = 100;
    private static final double XP_MULTIPLIER = 1.5; // Multiplier per level

    private CollectionRegistry() {
        // Static utility class
    }

    /**
     * Get the cumulative threshold required to reach a specific level.
     * Level 1 = 150, Level 27 = 50,000
     * Uses linear interpolation.
     *
     * @param level The target level (1-27)
     * @return Cumulative count required to reach this level
     */
    public static long getThresholdForLevel(int level) {
        if (level <= 0) return 0;
        if (level > MAX_LEVEL) level = MAX_LEVEL;

        // Linear interpolation: threshold = BASE + (level - 1) * step
        // Where step = (MAX - BASE) / (MAX_LEVEL - 1)
        double step = (double) (MAX_THRESHOLD - BASE_THRESHOLD) / (MAX_LEVEL - 1);
        return (long) (BASE_THRESHOLD + (level - 1) * step);
    }

    /**
     * Get the current level based on cumulative count.
     *
     * @param count The total items/kills collected
     * @return Current level (0 if below level 1 threshold, up to MAX_LEVEL)
     */
    public static int getLevelFromCount(long count) {
        if (count < BASE_THRESHOLD) return 0;

        for (int level = MAX_LEVEL; level >= 1; level--) {
            if (count >= getThresholdForLevel(level)) {
                return level;
            }
        }
        return 0;
    }

    /**
     * Get the XP reward for completing a specific level.
     * Scales with level for more rewarding late-game progression.
     *
     * @param level The level being completed (1-27)
     * @return XP reward for this level
     */
    public static int getXpRewardForLevel(int level) {
        if (level <= 0) return 0;
        if (level > MAX_LEVEL) level = MAX_LEVEL;

        // Exponential scaling: BASE_XP * (MULTIPLIER ^ (level - 1))
        // Level 1: 100, Level 10: ~3,844, Level 27: ~505,447
        //
        // Alternative linear scaling (uncomment if preferred):
         return BASE_XP_REWARD + (level - 1) * 50;

       // return (int) (BASE_XP_REWARD * Math.pow(XP_MULTIPLIER, level - 1));
    }

    /**
     * Get progress towards the next level as a percentage (0.0 - 1.0)
     *
     * @param count Current cumulative count
     * @return Progress percentage, or 1.0 if max level reached
     */
    public static double getProgressToNextLevel(long count) {
        int currentLevel = getLevelFromCount(count);

        if (currentLevel >= MAX_LEVEL) return 1.0;
        if (currentLevel == 0) {
            return (double) count / BASE_THRESHOLD;
        }

        long currentThreshold = getThresholdForLevel(currentLevel);
        long nextThreshold = getThresholdForLevel(currentLevel + 1);
        long progressInLevel = count - currentThreshold;
        long levelRange = nextThreshold - currentThreshold;

        return (double) progressInLevel / levelRange;
    }

    /**
     * Get the count needed to reach the next level
     *
     * @param count Current cumulative count
     * @return Remaining count needed, or 0 if max level reached
     */
    public static long getRemainingToNextLevel(long count) {
        int currentLevel = getLevelFromCount(count);

        if (currentLevel >= MAX_LEVEL) return 0;

        long nextThreshold = getThresholdForLevel(currentLevel + 1);
        return Math.max(0, nextThreshold - count);
    }
}