package com.stoinkcraft.earning.jobsites;

import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class JobSiteUpgrade {

    private final String id;
    private final String display;
    private final int maxLevel;
    private final int baseRequiredJobsiteLevel;
    private final int jobsiteLevelIncrement;
    private final IntFunction<Integer> costFormula;
    private final Predicate<JobSite> unlockCondition;
    private final BiConsumer<JobSite, Integer> applyEffect;

    /**
     * Creates a new JobSiteUpgrade with scaling level requirements.
     *
     * @param id                      Unique identifier
     * @param display                 Display name
     * @param maxLevel                Maximum upgrade level (1 for unlocks, 10 for scaling upgrades)
     * @param baseRequiredJobsiteLevel Jobsite level required for upgrade level 1
     * @param jobsiteLevelIncrement   Additional jobsite levels required per upgrade level
     * @param costFormula             Function: (upgradeLevel) -> cost
     * @param unlockCondition         Additional conditions (e.g., requires other upgrades)
     * @param applyEffect             Effect applied when upgraded
     */
    public JobSiteUpgrade(
            String id,
            String display,
            int maxLevel,
            int baseRequiredJobsiteLevel,
            int jobsiteLevelIncrement,
            IntFunction<Integer> costFormula,
            Predicate<JobSite> unlockCondition,
            BiConsumer<JobSite, Integer> applyEffect
    ) {
        this.id = id;
        this.display = display;
        this.maxLevel = maxLevel;
        this.baseRequiredJobsiteLevel = baseRequiredJobsiteLevel;
        this.jobsiteLevelIncrement = jobsiteLevelIncrement;
        this.costFormula = costFormula;
        this.unlockCondition = unlockCondition;
        this.applyEffect = applyEffect;
    }

    public String id() { return id; }
    public String display() { return display; }
    public int maxLevel() { return maxLevel; }
    public int baseRequiredJobsiteLevel() { return baseRequiredJobsiteLevel; }
    public int jobsiteLevelIncrement() { return jobsiteLevelIncrement; }

    /**
     * Get the required JobSite level for a specific upgrade level.
     *
     * Example: baseLevel=5, increment=2
     * - Upgrade Level 1 requires JobSite Level 5
     * - Upgrade Level 2 requires JobSite Level 7
     * - Upgrade Level 3 requires JobSite Level 9
     *
     * @param upgradeLevel The upgrade level being purchased (1-indexed)
     * @return Required JobSite level
     */
    public int getRequiredJobsiteLevel(int upgradeLevel) {
        if (upgradeLevel <= 1) {
            return baseRequiredJobsiteLevel;
        }
        return baseRequiredJobsiteLevel + ((upgradeLevel - 1) * jobsiteLevelIncrement);
    }

    /**
     * Get the maximum upgrade level achievable at a given jobsite level.
     *
     * @param jobsiteLevel Current jobsite level
     * @return Max achievable upgrade level (0 if not yet unlocked)
     */
    public int getMaxAchievableLevel(int jobsiteLevel) {
        if (jobsiteLevel < baseRequiredJobsiteLevel) {
            return 0;
        }
        if (jobsiteLevelIncrement == 0) {
            return maxLevel; // No scaling, all levels available once unlocked
        }
        int achievable = 1 + (jobsiteLevel - baseRequiredJobsiteLevel) / jobsiteLevelIncrement;
        return Math.min(achievable, maxLevel);
    }

    /**
     * Get the cost for a specific upgrade level.
     *
     * @param upgradeLevel The level being purchased
     * @return Cost in currency
     */
    public int cost(int upgradeLevel) {
        return costFormula.apply(upgradeLevel);
    }

    /**
     * Check if this upgrade can be purchased at the specified level.
     *
     * @param site The JobSite
     * @param targetUpgradeLevel The upgrade level being purchased
     * @return true if all conditions are met
     */
    public boolean canPurchase(JobSite site, int targetUpgradeLevel) {
        if (targetUpgradeLevel > maxLevel) return false;
        if (targetUpgradeLevel <= 0) return false;

        int currentLevel = site.getData().getLevel(id);
        if (targetUpgradeLevel != currentLevel + 1) return false; // Must purchase sequentially

        return unlockCondition.test(site)
                && site.getLevel() >= getRequiredJobsiteLevel(targetUpgradeLevel);
    }

    /**
     * Check if upgrade is visible (meets prerequisite conditions, regardless of level).
     */
    public boolean isVisible(JobSite site) {
        return unlockCondition.test(site);
    }

    /**
     * Check if the next level can be unlocked.
     */
    public boolean canPurchaseNext(JobSite site) {
        int currentLevel = site.getData().getLevel(id);
        if (currentLevel >= maxLevel) return false;
        return canPurchase(site, currentLevel + 1);
    }

    public void apply(JobSite site, int newLevel) {
        applyEffect.accept(site, newLevel);
    }

    // Backwards compatibility
    @Deprecated
    public int requiredJobsiteLevel() {
        return baseRequiredJobsiteLevel;
    }

    @Deprecated
    public boolean canUnlock(JobSite site) {
        return canPurchaseNext(site);
    }
}