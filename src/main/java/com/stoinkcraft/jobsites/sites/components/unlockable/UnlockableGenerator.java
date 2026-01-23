package com.stoinkcraft.jobsites.sites.components.unlockable;

import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.components.JobSiteGenerator;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

public abstract class UnlockableGenerator extends JobSiteGenerator implements Unlockable {

    private final UnlockableConfig unlockConfig;

    protected UnlockableGenerator(
            JobSite parent,
            String id,
            String displayName,
            IntSupplier requiredLevelSupplier,
            LongSupplier buildTimeSupplier,
            IntSupplier costSupplier,
            Predicate<JobSite> unlockCondition
    ) {
        super(parent, false); // Start disabled until unlocked
        this.unlockConfig = new UnlockableConfig(
                id, displayName, requiredLevelSupplier,
                buildTimeSupplier, costSupplier, unlockCondition
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