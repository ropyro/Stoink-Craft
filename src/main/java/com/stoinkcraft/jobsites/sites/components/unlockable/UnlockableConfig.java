package com.stoinkcraft.jobsites.sites.components.unlockable;

import com.stoinkcraft.jobsites.sites.JobSite;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

public record UnlockableConfig(
        String id,
        String displayName,
        IntSupplier requiredLevelSupplier,
        LongSupplier buildTimeSupplier,
        IntSupplier costSupplier,
        Predicate<JobSite> unlockCondition
) {
    public int getRequiredJobsiteLevel() {
        return requiredLevelSupplier.getAsInt();
    }

    public long getBuildTimeMillis() {
        return buildTimeSupplier.getAsLong();
    }

    public int getCost() {
        return costSupplier.getAsInt();
    }

    public boolean meetsRequirements(JobSite site) {
        return unlockCondition.test(site)
                && site.getLevel() >= getRequiredJobsiteLevel();
    }
}
