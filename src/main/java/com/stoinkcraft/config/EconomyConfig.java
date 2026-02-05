package com.stoinkcraft.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class EconomyConfig {

    private final Logger logger;

    private double playerPaySplit;
    private double enterpriseFoundingCost;
    private double enterpriseDailyTax;
    private int maxShares;

    // Reputation settings
    private double reputationMinMultiplier;
    private double reputationMaxMultiplier;
    private double reputationMidpoint;
    private double reputationScalingFactor;
    private double reputationMinValue;
    private double reputationMaxValue;
    private double dailyContractCompleteRep;
    private double dailyContractExpireRep;
    private double weeklyContractCompleteRep;
    private double weeklyContractExpireRep;

    public EconomyConfig(JavaPlugin plugin, File configFile) {
        this.logger = plugin.getLogger();
        load(configFile);
    }

    private void load(File configFile) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        playerPaySplit = config.getDouble("economy.player-pay-split-percentage", 0.25);
        enterpriseFoundingCost = config.getDouble("economy.enterprise-founding-cost", 10000.0);
        enterpriseDailyTax = config.getDouble("economy.enterprise-daily-tax", 0.0);
        maxShares = config.getInt("economy.max-shares", 100);

        reputationMinMultiplier = config.getDouble("economy.reputation.min-multiplier", 0.75);
        reputationMaxMultiplier = config.getDouble("economy.reputation.max-multiplier", 1.25);
        reputationMidpoint = config.getDouble("economy.reputation.midpoint", 100.0);
        reputationScalingFactor = config.getDouble("economy.reputation.scaling-factor", 0.01);
        reputationMinValue = config.getDouble("economy.reputation.min-value", 0.0);
        reputationMaxValue = config.getDouble("economy.reputation.max-value", 200.0);
        dailyContractCompleteRep = config.getDouble("economy.reputation.daily-contract.complete", 10.0);
        dailyContractExpireRep = config.getDouble("economy.reputation.daily-contract.expire", -15.0);
        weeklyContractCompleteRep = config.getDouble("economy.reputation.weekly-contract.complete", 25.0);
        weeklyContractExpireRep = config.getDouble("economy.reputation.weekly-contract.expire", -35.0);

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

    public double getReputationMinMultiplier() {
        return reputationMinMultiplier;
    }

    public double getReputationMaxMultiplier() {
        return reputationMaxMultiplier;
    }

    public double getReputationMidpoint() {
        return reputationMidpoint;
    }

    public double getReputationScalingFactor() {
        return reputationScalingFactor;
    }

    public double getReputationMinValue() {
        return reputationMinValue;
    }

    public double getReputationMaxValue() {
        return reputationMaxValue;
    }

    public double getDailyContractCompleteRep() {
        return dailyContractCompleteRep;
    }

    public double getDailyContractExpireRep() {
        return dailyContractExpireRep;
    }

    public double getWeeklyContractCompleteRep() {
        return weeklyContractCompleteRep;
    }

    public double getWeeklyContractExpireRep() {
        return weeklyContractExpireRep;
    }
}
