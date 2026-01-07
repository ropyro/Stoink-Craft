package com.stoinkcraft.jobs.contracts.rewards;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.contracts.ActiveContract;
import com.stoinkcraft.jobs.jobsites.JobSiteType;

import java.util.List;

public class JobSiteXpReward implements DescribableReward {

    private final JobSiteType jobSiteType;
    private final int xp;

    public JobSiteXpReward(JobSiteType jobSiteType, int xp) {
        this.jobSiteType = jobSiteType;
        this.xp = xp;
    }

    public int getXp() {
        return xp;
    }

    @Override
    public void apply(Enterprise enterprise, ActiveContract contract) {
        enterprise.getJobSiteManager()
                .getJobSite(jobSiteType)
                .getData().incrementXp(xp);
    }

    @Override
    public List<String> getLore() {
        return List.of("§f+" + xp + " Jobsite XP");
    }
}