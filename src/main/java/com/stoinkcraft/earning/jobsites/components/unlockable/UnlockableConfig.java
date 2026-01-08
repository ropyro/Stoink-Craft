package com.stoinkcraft.earning.jobsites.components.unlockable;

import com.stoinkcraft.earning.jobsites.JobSite;

import java.util.function.IntSupplier;
import java.util.function.Predicate;

public record UnlockableConfig(
        String id,
        String displayName,
        int requiredJobsiteLevel,
        long buildTimeMillis,
        IntSupplier costSupplier,
        Predicate<JobSite> unlockCondition
) {
    public int getCost() {
        return costSupplier.getAsInt();
    }

    public boolean meetsRequirements(JobSite site) {
        return unlockCondition.test(site)
                && site.getLevel() >= requiredJobsiteLevel;
    }
}
