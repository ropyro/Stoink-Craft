package com.stoinkcraft.earning.jobsites.components.unlockable;

import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.components.JobSiteGenerator;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

public abstract class UnlockableGenerator extends JobSiteGenerator implements Unlockable {

    private final UnlockableConfig unlockConfig;

    protected UnlockableGenerator(
            JobSite parent,
            String id,
            String displayName,
            int requiredJobsiteLevel,
            long buildTimeMillis,
            IntSupplier costSupplier,
            Predicate<JobSite> unlockCondition
    ) {
        super(parent, false); // Start disabled until unlocked
        this.unlockConfig = new UnlockableConfig(
                id, displayName, requiredJobsiteLevel,
                buildTimeMillis, costSupplier, unlockCondition
        );
    }

    @Override
    public UnlockableConfig getUnlockConfig() {
        return unlockConfig;
    }

    @Override
    public JobSite getJobSite() {
        return getParent();
    }

    @Override
    public void tick() {
        tickUnlockProgress();

        // Only run generator logic if unlocked
        if (isUnlocked()) {
            super.tick();
        }
    }

    @Override
    public void onUnlockComplete() {
        setEnabled(true);
        onGeneratorUnlocked();
    }

    /** Called when the generator becomes available */
    protected void onGeneratorUnlocked() {}
}