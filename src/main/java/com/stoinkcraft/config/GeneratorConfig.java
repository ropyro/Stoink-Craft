package com.stoinkcraft.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

/**
 * Configuration holder for generator-related settings.
 * Manages timing, spawn rates, and scaling for all generator types.
 */
public class GeneratorConfig {

    private final Logger logger;

    // Crop Generator
    private double cropBaseGrowthChance;

    // Passive Mob Generator
    private int passiveMobBaseSpawnInterval;
    private int passiveMobMinSpawnInterval;
    private int passiveMobBaseMaxMobs;
    private int passiveMobMobsPerCapacityLevel;
    private double passiveMobSpawnSpeedReductionPerLevel;

    // Honey Generator
    private int honeyMaxHoney;
    private int honeyBaseGenerationSeconds;
    private int honeyMinGenerationSeconds;
    private double honeySpeedReductionPercentage;

    // Mine Generator
    private double mineGeodeSpawnChance;
    private long mineBaseRegenIntervalSeconds;
    private long mineRegenSpeedReductionPerLevel;
    private long mineMinRegenIntervalSeconds;

    // Tombstone Generator
    private int tombstoneBaseSpawnInterval;
    private int tombstoneMinSpawnInterval;
    private double tombstoneSpawnSpeedReductionPerLevel;

    public GeneratorConfig(JavaPlugin plugin, File configFile) {
        this.logger = plugin.getLogger();
        load(configFile);
    }

    private void load(File configFile) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Crop Generator
        cropBaseGrowthChance = config.getDouble("generators.crop.base-growth-chance", 0.015);

        // Passive Mob Generator
        passiveMobBaseSpawnInterval = config.getInt("generators.passive-mob.base-spawn-interval", 10);
        passiveMobMinSpawnInterval = config.getInt("generators.passive-mob.min-spawn-interval", 2);
        passiveMobBaseMaxMobs = config.getInt("generators.passive-mob.base-max-mobs", 8);
        passiveMobMobsPerCapacityLevel = config.getInt("generators.passive-mob.mobs-per-capacity-level", 4);
        passiveMobSpawnSpeedReductionPerLevel = config.getDouble("generators.passive-mob.spawn-speed-reduction-per-level", 0.8);

        // Honey Generator
        honeyMaxHoney = config.getInt("generators.honey.max-honey", 5);
        honeyBaseGenerationSeconds = config.getInt("generators.honey.base-generation-seconds", 300);
        honeyMinGenerationSeconds = config.getInt("generators.honey.min-generation-seconds", 30);
        honeySpeedReductionPercentage = config.getDouble("generators.honey.speed-reduction-percentage", 0.08);

        // Mine Generator
        mineGeodeSpawnChance = config.getDouble("generators.mine.geode-spawn-chance", 0.025);
        mineBaseRegenIntervalSeconds = config.getLong("generators.mine.base-regen-interval-seconds", 7200);
        mineRegenSpeedReductionPerLevel = config.getLong("generators.mine.regen-speed-reduction-per-level", 600);
        mineMinRegenIntervalSeconds = config.getLong("generators.mine.min-regen-interval-seconds", 1800);

        // Tombstone Generator
        tombstoneBaseSpawnInterval = config.getInt("generators.tombstone.base-spawn-interval", 30);
        tombstoneMinSpawnInterval = config.getInt("generators.tombstone.min-spawn-interval", 8);
        tombstoneSpawnSpeedReductionPerLevel = config.getDouble("generators.tombstone.spawn-speed-reduction-per-level", 2.2);

        validate();

        logger.info("Generator config loaded successfully");
    }

    private void validate() {
        // Crop Generator validation
        if (cropBaseGrowthChance < 0 || cropBaseGrowthChance > 1) {
            logger.severe("Invalid crop base-growth-chance: " + cropBaseGrowthChance + " (must be 0-1)");
            throw new IllegalArgumentException("crop base-growth-chance must be between 0 and 1");
        }

        // Passive Mob Generator validation
        if (passiveMobBaseSpawnInterval <= 0) {
            logger.severe("Invalid passive-mob base-spawn-interval: " + passiveMobBaseSpawnInterval + " (must be > 0)");
            throw new IllegalArgumentException("passive-mob base-spawn-interval must be > 0");
        }
        if (passiveMobMinSpawnInterval <= 0 || passiveMobMinSpawnInterval > passiveMobBaseSpawnInterval) {
            logger.severe("Invalid passive-mob min-spawn-interval: " + passiveMobMinSpawnInterval);
            throw new IllegalArgumentException("passive-mob min-spawn-interval must be > 0 and <= base-spawn-interval");
        }
        if (passiveMobBaseMaxMobs <= 0) {
            logger.severe("Invalid passive-mob base-max-mobs: " + passiveMobBaseMaxMobs + " (must be > 0)");
            throw new IllegalArgumentException("passive-mob base-max-mobs must be > 0");
        }

        // Honey Generator validation
        if (honeyMaxHoney <= 0) {
            logger.severe("Invalid honey max-honey: " + honeyMaxHoney + " (must be > 0)");
            throw new IllegalArgumentException("honey max-honey must be > 0");
        }
        if (honeyBaseGenerationSeconds <= 0) {
            logger.severe("Invalid honey base-generation-seconds: " + honeyBaseGenerationSeconds + " (must be > 0)");
            throw new IllegalArgumentException("honey base-generation-seconds must be > 0");
        }
        if (honeyMinGenerationSeconds <= 0 || honeyMinGenerationSeconds > honeyBaseGenerationSeconds) {
            logger.severe("Invalid honey min-generation-seconds: " + honeyMinGenerationSeconds);
            throw new IllegalArgumentException("honey min-generation-seconds must be > 0 and <= base-generation-seconds");
        }

        // Mine Generator validation
        if (mineGeodeSpawnChance < 0 || mineGeodeSpawnChance > 1) {
            logger.severe("Invalid mine geode-spawn-chance: " + mineGeodeSpawnChance + " (must be 0-1)");
            throw new IllegalArgumentException("mine geode-spawn-chance must be between 0 and 1");
        }
        if (mineBaseRegenIntervalSeconds <= 0) {
            logger.severe("Invalid mine base-regen-interval-seconds: " + mineBaseRegenIntervalSeconds + " (must be > 0)");
            throw new IllegalArgumentException("mine base-regen-interval-seconds must be > 0");
        }
        if (mineMinRegenIntervalSeconds <= 0 || mineMinRegenIntervalSeconds > mineBaseRegenIntervalSeconds) {
            logger.severe("Invalid mine min-regen-interval-seconds: " + mineMinRegenIntervalSeconds);
            throw new IllegalArgumentException("mine min-regen-interval-seconds must be > 0 and <= base-regen-interval-seconds");
        }

        // Tombstone Generator validation
        if (tombstoneBaseSpawnInterval <= 0) {
            logger.severe("Invalid tombstone base-spawn-interval: " + tombstoneBaseSpawnInterval + " (must be > 0)");
            throw new IllegalArgumentException("tombstone base-spawn-interval must be > 0");
        }
        if (tombstoneMinSpawnInterval <= 0 || tombstoneMinSpawnInterval > tombstoneBaseSpawnInterval) {
            logger.severe("Invalid tombstone min-spawn-interval: " + tombstoneMinSpawnInterval);
            throw new IllegalArgumentException("tombstone min-spawn-interval must be > 0 and <= base-spawn-interval");
        }
    }

    // =============================================
    // CROP GENERATOR GETTERS
    // =============================================

    public double getCropBaseGrowthChance() {
        return cropBaseGrowthChance;
    }

    // =============================================
    // PASSIVE MOB GENERATOR GETTERS
    // =============================================

    public int getPassiveMobBaseSpawnInterval() {
        return passiveMobBaseSpawnInterval;
    }

    public int getPassiveMobMinSpawnInterval() {
        return passiveMobMinSpawnInterval;
    }

    public int getPassiveMobBaseMaxMobs() {
        return passiveMobBaseMaxMobs;
    }

    public int getPassiveMobMobsPerCapacityLevel() {
        return passiveMobMobsPerCapacityLevel;
    }

    public double getPassiveMobSpawnSpeedReductionPerLevel() {
        return passiveMobSpawnSpeedReductionPerLevel;
    }

    // =============================================
    // HONEY GENERATOR GETTERS
    // =============================================

    public int getHoneyMaxHoney() {
        return honeyMaxHoney;
    }

    public int getHoneyBaseGenerationSeconds() {
        return honeyBaseGenerationSeconds;
    }

    public int getHoneyMinGenerationSeconds() {
        return honeyMinGenerationSeconds;
    }

    public double getHoneySpeedReductionPercentage() {
        return honeySpeedReductionPercentage;
    }

    // =============================================
    // MINE GENERATOR GETTERS
    // =============================================

    public double getMineGeodeSpawnChance() {
        return mineGeodeSpawnChance;
    }

    public long getMineBaseRegenIntervalSeconds() {
        return mineBaseRegenIntervalSeconds;
    }

    public long getMineRegenSpeedReductionPerLevel() {
        return mineRegenSpeedReductionPerLevel;
    }

    public long getMineMinRegenIntervalSeconds() {
        return mineMinRegenIntervalSeconds;
    }

    // =============================================
    // TOMBSTONE GENERATOR GETTERS
    // =============================================

    public int getTombstoneBaseSpawnInterval() {
        return tombstoneBaseSpawnInterval;
    }

    public int getTombstoneMinSpawnInterval() {
        return tombstoneMinSpawnInterval;
    }

    public double getTombstoneSpawnSpeedReductionPerLevel() {
        return tombstoneSpawnSpeedReductionPerLevel;
    }
}
