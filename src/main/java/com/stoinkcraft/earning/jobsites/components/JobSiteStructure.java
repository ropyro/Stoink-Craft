package com.stoinkcraft.earning.jobsites.components;

import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.components.unlockable.Unlockable;
import com.stoinkcraft.earning.jobsites.components.unlockable.UnlockableConfig;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

public abstract class JobSiteStructure implements JobSiteComponent, Unlockable {

    private final UnlockableConfig unlockConfig;
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
        this.unlockConfig = new UnlockableConfig(
                id, displayName, requiredJobsiteLevel,
                buildTimeMillis, costSupplier, unlockCondition
        );
        this.jobSite = jobSite;
    }

    @Override
    public UnlockableConfig getUnlockConfig() {
        return unlockConfig;
    }

    @Override
    public JobSite getJobSite() {
        return jobSite;
    }

    // ==================== JobSiteComponent Implementation ====================

    @Override
    public void tick() {
        tickUnlockProgress();
    }

    @Override
    public void build() {
        // Called when jobsite loads - subclasses override for built state handling
    }

    @Override
    public void disband() {
        // Cleanup logic - subclasses can override
    }

    @Override
    public void levelUp() {
        // React to jobsite level changes - subclasses can override
    }
}