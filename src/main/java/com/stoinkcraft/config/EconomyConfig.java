package com.stoinkcraft.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

/**
 * Configuration holder for economy-related settings.
 * Values include player pay splits, enterprise costs, taxes, and share limits.
 */
public class EconomyConfig {

    private final Logger logger;

    private double playerPaySplit;
    private double enterpriseFoundingCost;
    private double enterpriseDailyTax;
    private int maxShares;

    public EconomyConfig(JavaPlugin plugin, File configFile) {
        this.logger = plugin.getLogger();
        load(configFile);
    }

    private void load(File configFile) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Load values with fallbacks to hardcoded defaults
        playerPaySplit = config.getDouble("economy.player-pay-split-percentage", 0.25);
        enterpriseFoundingCost = config.getDouble("economy.enterprise-founding-cost", 10000.0);
        enterpriseDailyTax = config.getDouble("economy.enterprise-daily-tax", 0.35);
        maxShares = config.getInt("economy.max-shares", 100);

        validate();

        logger.info("Economy config loaded successfully");
    }

    private void validate() {
        if (playerPaySplit < 0 || playerPaySplit > 1) {
            logger.severe("Invalid player-pay-split-percentage: " + playerPaySplit + " (must be 0-1)");
            throw new IllegalArgumentException("player-pay-split-percentage must be between 0 and 1");
        }

        if (enterpriseFoundingCost < 0) {
            logger.severe("Invalid enterprise-founding-cost: " + enterpriseFoundingCost + " (must be >= 0)");
            throw new IllegalArgumentException("enterprise-founding-cost must be >= 0");
        }

        if (enterpriseDailyTax < 0 || enterpriseDailyTax > 1) {
            logger.severe("Invalid enterprise-daily-tax: " + enterpriseDailyTax + " (must be 0-1)");
            throw new IllegalArgumentException("enterprise-daily-tax must be between 0 and 1");
        }

        if (maxShares <= 0) {
            logger.severe("Invalid max-shares: " + maxShares + " (must be > 0)");
            throw new IllegalArgumentException("max-shares must be > 0");
        }
    }

    // Getters

    public double getPlayerPaySplit() {
        return playerPaySplit;
    }

    public double getEnterpriseFoundingCost() {
        return enterpriseFoundingCost;
    }

    public double getEnterpriseDailyTax() {
        return enterpriseDailyTax;
    }

    public int getMaxShares() {
        return maxShares;
    }
}
