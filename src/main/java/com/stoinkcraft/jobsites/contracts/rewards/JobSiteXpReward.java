package com.stoinkcraft.jobsites.contracts.rewards;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.contracts.ActiveContract;
import com.stoinkcraft.jobsites.sites.JobSiteType;

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