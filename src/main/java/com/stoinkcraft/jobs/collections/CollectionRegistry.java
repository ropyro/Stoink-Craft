package com.stoinkcraft.jobs.collections;

/**
 * Static registry for collection level thresholds and XP rewards.
 * All functions are centralized here for easy balancing adjustments.
 */
public final class CollectionRegistry {

    public static final int MAX_LEVEL = 27;

    // Threshold scaling - designed for collaborative play
    // Solo: ~2-3 hours per level early, ~8-10 hours per level late
    // 5-player: ~30 min per level early, ~2 hours per level late

    private static final long[] THRESHOLDS = {
            0,       // Level 0 (not achieved)
            100,     // Level 1  - Tutorial
            250,     // Level 2  - Getting started
            450,     // Level 3
            700,     // Level 4
            1000,    // Level 5  - Early milestone
            1400,    // Level 6
            1900,    // Level 7
            2500,    // Level 8
            3200,    // Level 9
            4000,    // Level 10 - Mid-early milestone
            5000,    // Level 11
            6200,    // Level 12
            7600,    // Level 13
            9200,    // Level 14
            11000,   // Level 15 - Midgame
            13200,   // Level 16
            15800,   // Level 17
            18800,   // Level 18
            22200,   // Level 19
            26000,   // Level 20 - Late midgame
            30500,   // Level 21
            35500,   // Level 22
            41000,   // Level 23
            47500,   // Level 24
            55000,   // Level 25 - Endgame
            64000,   // Level 26
            75000,   // Level 27 - Mastery
    };

    // XP rewards scale meaningfully but not explosively
    private static final int[] XP_REWARDS = {
            0,      // Level 0
            50,     // Level 1
            75,     // Level 2
            100,    // Level 3
            130,    // Level 4
            165,    // Level 5
            200,    // Level 6
            240,    // Level 7
            285,    // Level 8
            335,    // Level 9
            400,    // Level 10
            470,    // Level 11
            550,    // Level 12
            640,    // Level 13
            740,    // Level 14
            850,    // Level 15
            975,    // Level 16
            1100,   // Level 17
            1250,   // Level 18
            1425,   // Level 19
            1625,   // Level 20
            1850,   // Level 21
            2100,   // Level 22
            2400,   // Level 23
            2750,   // Level 24
            3150,   // Level 25
            3600,   // Level 26
            5000,   // Level 27 - Mastery bonus
    };

    private CollectionRegistry() {}

    public static long getThresholdForLevel(int level) {
        if (level <= 0) return 0;
        if (level > MAX_LEVEL) level = MAX_LEVEL;
        return THRESHOLDS[level];
    }

    public static int getLevelFromCount(long count) {
        for (int level = MAX_LEVEL; level >= 1; level--) {
            if (count >= THRESHOLDS[level]) {
                return level;
            }
        }
        return 0;
    }

    public static int getXpRewardForLevel(int level) {
        if (level <= 0) return 0;
        if (level > MAX_LEVEL) level = MAX_LEVEL;
        return XP_REWARDS[level];
    }

    public static double getProgressToNextLevel(long count) {
        int currentLevel = getLevelFromCount(count);

        if (currentLevel >= MAX_LEVEL) return 1.0;
        if (currentLevel == 0) {
            return (double) count / THRESHOLDS[1];
        }

        long currentThreshold = THRESHOLDS[currentLevel];
        long nextThreshold = THRESHOLDS[currentLevel + 1];
        long progressInLevel = count - currentThreshold;
        long levelRange = nextThreshold - currentThreshold;

        return (double) progressInLevel / levelRange;
    }

    public static long getRemainingToNextLevel(long count) {
        int currentLevel = getLevelFromCount(count);

        if (currentLevel >= MAX_LEVEL) return 0;

        long nextThreshold = THRESHOLDS[currentLevel + 1];
        return Math.max(0, nextThreshold - count);
    }

    /**
     * Get the amount needed for just this level (not cumulative)
     */
    public static long getAmountForLevel(int level) {
        if (level <= 1) return THRESHOLDS[1];
        return THRESHOLDS[level] - THRESHOLDS[level - 1];
    }
}