package com.stoinkcraft.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Configuration holder for collection progression system.
 * Manages thresholds and XP rewards for all 27 collection levels.
 */
public class CollectionConfig {

    private final Logger logger;

    private int maxLevel;
    private long[] thresholds;
    private int[] xpRewards;

    public CollectionConfig(JavaPlugin plugin, File configFile) {
        this.logger = plugin.getLogger();
        load(configFile);
    }

    private void load(File configFile) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Load max level
        maxLevel = config.getInt("collections.max-level", 27);

        // Load thresholds
        thresholds = new long[maxLevel + 1];
        ConfigurationSection thresholdsSection = config.getConfigurationSection("collections.thresholds");

        if (thresholdsSection != null) {
            thresholds[0] = 0; // Level 0 is always 0
            for (int i = 1; i <= maxLevel; i++) {
                thresholds[i] = thresholdsSection.getLong(String.valueOf(i), getDefaultThreshold(i));
            }
        } else {
            // Use defaults if section doesn't exist
            for (int i = 0; i <= maxLevel; i++) {
                thresholds[i] = getDefaultThreshold(i);
            }
        }

        // Load XP rewards
        xpRewards = new int[maxLevel + 1];
        ConfigurationSection xpSection = config.getConfigurationSection("collections.xp-rewards");

        if (xpSection != null) {
            xpRewards[0] = 0; // Level 0 is always 0
            for (int i = 1; i <= maxLevel; i++) {
                xpRewards[i] = xpSection.getInt(String.valueOf(i), getDefaultXpReward(i));
            }
        } else {
            // Use defaults if section doesn't exist
            for (int i = 0; i <= maxLevel; i++) {
                xpRewards[i] = getDefaultXpReward(i);
            }
        }

        validate();

        logger.info("Collection config loaded successfully (" + maxLevel + " levels)");
    }

    private void validate() {
        if (maxLevel < 1 || maxLevel > 100) {
            logger.severe("Invalid max-level: " + maxLevel + " (must be 1-100)");
            throw new IllegalArgumentException("max-level must be between 1 and 100");
        }

        // Validate thresholds are monotonically increasing
        for (int i = 1; i <= maxLevel; i++) {
            if (thresholds[i] <= thresholds[i - 1]) {
                logger.severe("Threshold for level " + i + " (" + thresholds[i] +
                            ") must be greater than level " + (i - 1) + " (" + thresholds[i - 1] + ")");
                throw new IllegalArgumentException("Collection thresholds must be monotonically increasing");
            }
        }

        // Validate XP rewards are non-negative
        for (int i = 1; i <= maxLevel; i++) {
            if (xpRewards[i] < 0) {
                logger.severe("XP reward for level " + i + " cannot be negative");
                throw new IllegalArgumentException("XP rewards must be >= 0");
            }
        }
    }

    // Getters

    public int getMaxLevel() {
        return maxLevel;
    }

    public long[] getThresholds() {
        return Arrays.copyOf(thresholds, thresholds.length);
    }

    public int[] getXpRewards() {
        return Arrays.copyOf(xpRewards, xpRewards.length);
    }

    public long getThreshold(int level) {
        if (level < 0 || level > maxLevel) return 0;
        return thresholds[level];
    }

    public int getXpReward(int level) {
        if (level < 0 || level > maxLevel) return 0;
        return xpRewards[level];
    }

    // Default values (matching original hardcoded values)

    private static final long[] DEFAULT_THRESHOLDS = {
            0, 100, 250, 450, 700, 1000, 1400, 1900, 2500, 3200, 4000, 5000, 6200, 7600,
            9200, 11000, 13200, 15800, 18800, 22200, 26000, 30500, 35500, 41000, 47500,
            55000, 64000, 75000
    };

    private static final int[] DEFAULT_XP_REWARDS = {
            0, 50, 75, 100, 130, 165, 200, 240, 285, 335, 400, 470, 550, 640, 740, 850,
            975, 1100, 1250, 1425, 1625, 1850, 2100, 2400, 2750, 3150, 3600, 5000
    };

    private long getDefaultThreshold(int level) {
        if (level < 0 || level >= DEFAULT_THRESHOLDS.length) return 0;
        return DEFAULT_THRESHOLDS[level];
    }

    private int getDefaultXpReward(int level) {
        if (level < 0 || level >= DEFAULT_XP_REWARDS.length) return 0;
        return DEFAULT_XP_REWARDS[level];
    }
}
