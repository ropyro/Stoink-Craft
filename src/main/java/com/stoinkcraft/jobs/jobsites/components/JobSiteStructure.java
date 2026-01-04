package com.stoinkcraft.jobs.jobsites.components;

import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.StructureData;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

public class JobSiteStructure implements JobSiteComponent{

    private final String id;
    private final String displayName;
    private final int requiredJobsiteLevel;
    private final long buildTimeMillis;
    private final IntSupplier costSupplier;
    private final Predicate<JobSite> unlockCondition;
    private final JobSite jobSite;

    protected JobSiteStructure(
            String id,
            String displayName,
            int requiredJobsiteLevel,
            long buildTimeMillis,
            IntSupplier costSupplier,
            Predicate<JobSite> unlockCondition,
            JobSite jobSite
    ) {
        this.id = id;
        this.displayName = displayName;
        this.requiredJobsiteLevel = requiredJobsiteLevel;
        this.buildTimeMillis = buildTimeMillis;
        this.costSupplier = costSupplier;
        this.unlockCondition = unlockCondition;
        this.jobSite = jobSite;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public long getBuildTimeMillis() { return buildTimeMillis; }
    public int getCost() { return costSupplier.getAsInt(); }
    public int getRequiredJobsiteLevel() {
        return requiredJobsiteLevel;
    }
    public JobSite getJobSite() {
        return jobSite;
    }

    public boolean canUnlock(JobSite site) {
        return unlockCondition.test(site)
                && site.getLevel() >= requiredJobsiteLevel;
    }

    /* ===== Lifecycle hooks ===== */

    /** Called when purchase succeeds */
    public void onConstructionStart() {}

    /** Called when timer finishes */
    public void onConstructionComplete(){}

    /** Called every tick while BUILDING */
    public void onConstructionTick(long millisRemaining) {}

    /** Called when jobsite loads and structure already built */
    @Override
    public void build() {

    }

    @Override
    public void tick() {
        StructureData data = jobSite.getData().getStructure(getId());
        if (data.getState() == JobSiteStructure.StructureState.BUILDING) {
            onConstructionTick(data.getRemainingMillis());
            if (data.isFinished()) {
                data.markBuilt();
                onConstructionComplete();
            }
        }
    }

    @Override
    public void disband() {

    }

    @Override
    public void levelUp() {

    }

    public enum StructureState {
        LOCKED,
        BUILDING,
        BUILT
    }

}
