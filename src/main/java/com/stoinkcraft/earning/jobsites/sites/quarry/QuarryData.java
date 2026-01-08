package com.stoinkcraft.earning.jobsites.sites.quarry;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteData;

/**
 * Serializable data for QuarrySite
 */
public class QuarryData extends JobSiteData {

    @Expose
    private long elapsedSeconds;

    @Expose
    private String currentOreSet;

    public QuarryData(boolean isBuilt, JobSite parent) {
        super(isBuilt, parent);
        this.elapsedSeconds = 0L;
        this.currentOreSet = OreSet.MINING_BASICS.name();
    }

    public long getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(long elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
    }

    public void incrementElapsedSeconds() {
        this.elapsedSeconds++;
    }

    public OreSet getCurrentOreSet() {
        try {
            return OreSet.valueOf(currentOreSet);
        } catch (Exception e) {
            return OreSet.MINING_BASICS;
        }
    }

    public void setCurrentOreSet(OreSet oreSet) {
        this.currentOreSet = oreSet.name();
    }
}