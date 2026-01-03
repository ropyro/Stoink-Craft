package com.stoinkcraft.jobs.contracts;

public class ContractScaling {

    public static int dailyContractsForLevel(int level) {
        if (level < 5) return 3;
        if (level < 10) return 4;
        if (level < 20) return 5;
        if (level < 30) return 6;
        return 7;
    }

    public static int weeklyContractsForLevel(int level) {
        if (level < 5) return 3;
        if (level < 10) return 4;
        if (level < 20) return 5;
        if (level < 30) return 6;
        return 7;
    }
}
