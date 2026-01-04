package com.stoinkcraft.jobs.jobsites;

import java.util.function.BiConsumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

public abstract class JobSiteStructure {

    private final String id;
    private final String displayName;
    private final int requiredJobsiteLevel;
    private final long buildTimeMillis;
    private final IntSupplier costSupplier;
    private final Predicate<JobSite> unlockCondition;

    protected JobSiteStructure(
            String id,
            String displayName,
            int requiredJobsiteLevel,
            long buildTimeMillis,
            IntSupplier costSupplier,
            Predicate<JobSite> unlockCondition
    ) {
        this.id = id;
        this.displayName = displayName;
        this.requiredJobsiteLevel = requiredJobsiteLevel;
        this.buildTimeMillis = buildTimeMillis;
        this.costSupplier = costSupplier;
        this.unlockCondition = unlockCondition;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public long getBuildTimeMillis() { return buildTimeMillis; }
    public int getCost() { return costSupplier.getAsInt(); }

    public int getRequiredJobsiteLevel() {
        return requiredJobsiteLevel;
    }

    public boolean canUnlock(JobSite site) {
        return unlockCondition.test(site)
                && site.getLevel() >= requiredJobsiteLevel;
    }

    /* ===== Lifecycle hooks ===== */

    /** Called when purchase succeeds */
    public void onBuildStart(JobSite site) {}

    /** Called when timer finishes */
    public abstract void onBuildComplete(JobSite site);

    /** Called when jobsite loads and structure already built */
    public void onLoad(JobSite site) {}

    /** Called every tick while BUILDING */
    public void onBuildTick(JobSite site, long millisRemaining) {}
    public enum StructureState {
        LOCKED,
        BUILDING,
        BUILT
    }
}
