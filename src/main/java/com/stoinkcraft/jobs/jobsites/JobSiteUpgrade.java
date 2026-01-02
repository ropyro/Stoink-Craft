package com.stoinkcraft.jobs.jobsites;

import com.stoinkcraft.jobs.jobsites.JobSite;

import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class JobSiteUpgrade {

    private final String id;
    private final String display;
    private final int maxLevel;
    private final int requiredJobsiteLevel; // ✅ NEW
    private final IntFunction<Integer> costFormula;
    private final Predicate<JobSite> unlockCondition;
    private final BiConsumer<JobSite, Integer> applyEffect;

    public JobSiteUpgrade(
            String id,
            String display,
            int maxLevel,
            int requiredJobsiteLevel,
            IntFunction<Integer> costFormula,
            Predicate<JobSite> unlockCondition,
            BiConsumer<JobSite, Integer> applyEffect
    ) {
        this.id = id;
        this.display = display;
        this.maxLevel = maxLevel;
        this.requiredJobsiteLevel = requiredJobsiteLevel;
        this.costFormula = costFormula;
        this.unlockCondition = unlockCondition;
        this.applyEffect = applyEffect;
    }

    public String id() { return id; }
    public String display() { return display; }
    public int maxLevel() { return maxLevel; }
    public int requiredJobsiteLevel() { return requiredJobsiteLevel; }
    public int cost(int level) { return costFormula.apply(level); }
    public boolean canUnlock(JobSite site) { return unlockCondition.test(site); }
    public void apply(JobSite site, int newLevel) { applyEffect.accept(site, newLevel); }
}