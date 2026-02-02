package com.stoinkcraft.enterprise.reputation;

import com.google.common.util.concurrent.AtomicDouble;
import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.components.unlockable.Unlockable;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteUpgrade;

/**
 * Utility class for calculating reputation-based multipliers and net worth.
 *
 * Uses a sigmoid function to map reputation score to a multiplier range.
 * At the midpoint reputation, multiplier = 1.0.
 * As reputation increases, multiplier approaches max.
 * As reputation decreases, multiplier approaches min.
 */
public class ReputationCalculator {

    /**
     * Calculate the reputation multiplier using a sigmoid function.
     *
     * Formula: multiplier = min + (max - min) * sigmoid((rep - midpoint) * scalingFactor)
     *
     * @param reputation The enterprise's reputation score
     * @return The multiplier value (between min and max configured values)
     */
    public static double getMultiplier(double reputation) {
        double min = ConfigLoader.getEconomy().getReputationMinMultiplier();
        double max = ConfigLoader.getEconomy().getReputationMaxMultiplier();
        double midpoint = ConfigLoader.getEconomy().getReputationMidpoint();
        double scalingFactor = ConfigLoader.getEconomy().getReputationScalingFactor();

        // Sigmoid function: 1 / (1 + e^(-x))
        double x = (reputation - midpoint) * scalingFactor;
        double sigmoid = 1.0 / (1.0 + Math.exp(-x));

        // Scale sigmoid (0-1) to multiplier range (min-max)
        return min + (max - min) * sigmoid;
    }

    /**
     * Calculate effective net worth based on bank balance and reputation.
     *
     * @param enterprise The enterprise to calculate net worth for
     * @return The calculated net worth (grossValue * reputationMultiplier)
     */
    public static double calculateNetWorth(Enterprise enterprise) {
        double multiplier = getMultiplier(enterprise.getReputation());
        return calculateGrossWorth(enterprise) * multiplier;
    }

    /**
     * Calculate the gross value of all the enterprise's assets
     *
     * @param enterprise The enterprise to calculate gross value for
     * @return The calculated gross value (bank balance + unlockable costs + upgrade costs)
     */
    public static double calculateGrossWorth(Enterprise enterprise){
        double gross = enterprise.getBankBalance();
        for (JobSite js : enterprise.getJobSiteManager().getAllJobSites()) {
            for (Unlockable u : js.getUnlockables()) {
                if (u.isUnlocked()) {
                    gross += u.getCost();
                }
            }
            for (JobSiteUpgrade up : js.getUpgrades()) {
                int currentLevel = js.getData().getLevel(up.id());
                if (currentLevel <= 0) continue;
                if (currentLevel == 1) {
                    gross += up.cost(1);
                    continue;
                }
                for (int l = 1; l <= currentLevel; l++) {
                    gross += up.cost(l);
                }
            }
        }
        return gross;
    }

    /**
     * Get a formatted display string for the current multiplier.
     *
     * @param reputation The reputation score
     * @return Formatted string like "1.15x"
     */
    public static String getMultiplierDisplay(double reputation) {
        double multiplier = getMultiplier(reputation);
        return String.format("%.2fx", multiplier);
    }

    /**
     * Get the reputation points gained for completing a contract.
     *
     * @param weekly Whether the contract is a weekly contract
     * @return The reputation points to add
     */
    public static double getCompletionReputation(boolean weekly) {
        return weekly
                ? ConfigLoader.getEconomy().getWeeklyContractCompleteRep()
                : ConfigLoader.getEconomy().getDailyContractCompleteRep();
    }

    /**
     * Get the reputation points lost for an expired contract.
     *
     * @param weekly Whether the contract is a weekly contract
     * @return The reputation points to remove (positive value)
     */
    public static double getExpiryReputation(boolean weekly) {
        return Math.abs(weekly
                ? ConfigLoader.getEconomy().getWeeklyContractExpireRep()
                : ConfigLoader.getEconomy().getDailyContractExpireRep());
    }
}
