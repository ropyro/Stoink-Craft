package com.stoinkcraft.jobsites.contracts;

public class ContractScaling {

    public static final int DAILY_CONTRACT_COUNT = 7;
    public static final int WEEKLY_CONTRACT_COUNT = 7;

    public static int dailyContractsForLevel(int level) {
        return DAILY_CONTRACT_COUNT;
    }

    public static int weeklyContractsForLevel(int level) {
        return WEEKLY_CONTRACT_COUNT;
    }
}
