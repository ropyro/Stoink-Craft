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

    public static final double DEFAULT_PLAYER_PAY_SPLIT_PERCENTAGE = 0.25;
    public static final double DEFAULT_ENTERPRISE_FOUNDING_COST = 10000;
    public static final double DEFAULT_ENTERPRISE_DAILY_TAX = 0.35;
    public static final int DEFAULT_MAX_SHARES = 100;
    public static final long DAY_MILLIS = 24 * 60 * 60 * 1000;

    @Deprecated
    public static final double PRICE_BOOST = 2.25;

    @Deprecated
    public static double PLAYER_PAY_SPLIT_PERCENTAGE = DEFAULT_PLAYER_PAY_SPLIT_PERCENTAGE;
    @Deprecated
    public static double ENTERPRISE_FOUNDING_COST = DEFAULT_ENTERPRISE_FOUNDING_COST;
    @Deprecated
    public static double ENTERPRISE_DAILY_TAX = DEFAULT_ENTERPRISE_DAILY_TAX;
    @Deprecated
    public static int MAX_SHARES = DEFAULT_MAX_SHARES;

    public static double getPlayerPaySplit() {
        if (ConfigLoader.isInitialized()) {
            return ConfigLoader.getEconomy().getPlayerPaySplit();
        }
        return DEFAULT_PLAYER_PAY_SPLIT_PERCENTAGE;
    }

    public static double getEnterpriseFoundingCost() {
        if (ConfigLoader.isInitialized()) {
            return ConfigLoader.getEconomy().getEnterpriseFoundingCost();
        }
        return DEFAULT_ENTERPRISE_FOUNDING_COST;
    }

    public static double getEnterpriseDailyTax() {
        if (ConfigLoader.isInitialized()) {
            return ConfigLoader.getEconomy().getEnterpriseDailyTax();
        }
        return DEFAULT_ENTERPRISE_DAILY_TAX;
    }

    public static int getMaxShares() {
        if (ConfigLoader.isInitialized()) {
            return ConfigLoader.getEconomy().getMaxShares();
        }
        return DEFAULT_MAX_SHARES;
    }

}
