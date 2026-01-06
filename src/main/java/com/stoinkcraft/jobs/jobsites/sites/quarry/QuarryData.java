package com.stoinkcraft.jobs.jobsites.sites.quarry;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteData;
import org.bukkit.util.Vector;

/**
 * Serializable data for QuarrySite
 */
public class QuarryData extends JobSiteData {

    @Expose
    private long regenIntervalSeconds;

    @Expose
    private long elapsedSeconds;

    public QuarryData(boolean isBuilt, JobSite parent) {
        super(isBuilt, parent);
        this.regenIntervalSeconds = QuarrySite.DEFAULT_REGEN_INTERVAL_SECONDS;
        this.elapsedSeconds = 0L;
    }

    public long getRegenIntervalSeconds() {
        return regenIntervalSeconds;
    }

    public void setRegenIntervalSeconds(long regenIntervalSeconds) {
        this.regenIntervalSeconds = regenIntervalSeconds;
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }
}