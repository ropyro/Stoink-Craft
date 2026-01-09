package com.stoinkcraft.utils;

import com.stoinkcraft.config.ConfigLoader;

import java.util.UUID;

public class SCConstants {

    public static final UUID serverCEO = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final String RELOAD_COMMAND = "stoinkcore.admin.reload";
    public static final String ROTATE_MARKET_COMMAND = "stoinkcore.admin.rotatemarket";
    public static final String SERVER_ENT_COMMAND = "stoinkcore.admin.serverenterprise";
    public static final String TOPCEO_COMMAND = "stoinkcore.admin.topceo";
    public static final String SERVER_ENT_COMMAND_SETWARP = "stoinkcore.admin.serverenterprise.setwarp";

    // Default values (used as fallbacks if config not loaded)
    public static final double DEFAULT_PLAYER_PAY_SPLIT_PERCENTAGE = 0.25;
    public static final double DEFAULT_ENTERPRISE_FOUNDING_COST = 10000;
    public static final double DEFAULT_ENTERPRISE_DAILY_TAX = 0.35;
    public static final int DEFAULT_MAX_SHARES = 100;
    public static final long DAY_MILLIS = 24 * 60 * 60 * 1000; // 1 day in milliseconds

    // Deprecated - kept for backward compatibility, but no longer used
    @Deprecated
    public static final double PRICE_BOOST = 2.25;

    // Backward-compatible fields (deprecated - use getters instead)
    @Deprecated
    public static double PLAYER_PAY_SPLIT_PERCENTAGE = DEFAULT_PLAYER_PAY_SPLIT_PERCENTAGE;
    @Deprecated
    public static double ENTERPRISE_FOUNDING_COST = DEFAULT_ENTERPRISE_FOUNDING_COST;
    @Deprecated
    public static double ENTERPRISE_DAILY_TAX = DEFAULT_ENTERPRISE_DAILY_TAX;
    @Deprecated
    public static int MAX_SHARES = DEFAULT_MAX_SHARES;

    /**
     * Get player pay split percentage. Checks config first, falls back to default.
     * @return The percentage of earnings that go to the player (0.0-1.0)
     */
    public static double getPlayerPaySplit() {
        if (ConfigLoader.isInitialized()) {
            return ConfigLoader.getEconomy().getPlayerPaySplit();
        }
        return DEFAULT_PLAYER_PAY_SPLIT_PERCENTAGE;
    }

    /**
     * Get enterprise founding cost. Checks config first, falls back to default.
     * @return The cost to create a new enterprise
     */
    public static double getEnterpriseFoundingCost() {
        if (ConfigLoader.isInitialized()) {
            return ConfigLoader.getEconomy().getEnterpriseFoundingCost();
        }
        return DEFAULT_ENTERPRISE_FOUNDING_COST;
    }

    /**
     * Get enterprise daily tax rate. Checks config first, falls back to default.
     * @return The daily tax rate on enterprise funds (0.0-1.0)
     */
    public static double getEnterpriseDailyTax() {
        if (ConfigLoader.isInitialized()) {
            return ConfigLoader.getEconomy().getEnterpriseDailyTax();
        }
        return DEFAULT_ENTERPRISE_DAILY_TAX;
    }

    /**
     * Get maximum shares per enterprise. Checks config first, falls back to default.
     * @return The maximum number of shares an enterprise can have
     */
    public static int getMaxShares() {
        if (ConfigLoader.isInitialized()) {
            return ConfigLoader.getEconomy().getMaxShares();
        }
        return DEFAULT_MAX_SHARES;
    }

}
