package com.stoinkcraft.jobs.jobsites.sites.quarry;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.jobs.jobsites.JobSiteData;
import org.bukkit.util.Vector;

/**
 * Serializable data for QuarrySite
 */
public class QuarryData extends JobSiteData {

    @Expose
    private Vector entryHologramOffset;

    @Expose
    private Vector mineCorner1Offset;

    @Expose
    private Vector mineCorner2Offset;

    @Expose
    private long regenIntervalSeconds;

    @Expose
    private long tickCounter;

    public QuarryData(boolean isBuilt, Vector entryHologramOffset,
                      Vector mineCorner1Offset, Vector mineCorner2Offset,
                      long regenIntervalSeconds, long tickCounter, QuarrySite parent) {
        super(isBuilt, parent);
        this.entryHologramOffset = entryHologramOffset;
        this.mineCorner1Offset = mineCorner1Offset;
        this.mineCorner2Offset = mineCorner2Offset;
        this.regenIntervalSeconds = regenIntervalSeconds;
        this.tickCounter = tickCounter;
    }

    // Getters and setters
    public Vector getEntryHologramOffset() {
        return entryHologramOffset;
    }

    public void setEntryHologramOffset(Vector entryHologramOffset) {
        this.entryHologramOffset = entryHologramOffset;
    }

    public Vector getMineCorner1Offset() {
        return mineCorner1Offset;
    }

    public void setMineCorner1Offset(Vector mineCorner1Offset) {
        this.mineCorner1Offset = mineCorner1Offset;
    }

    public Vector getMineCorner2Offset() {
        return mineCorner2Offset;
    }

    public void setMineCorner2Offset(Vector mineCorner2Offset) {
        this.mineCorner2Offset = mineCorner2Offset;
    }

    public long getRegenIntervalSeconds() {
        return regenIntervalSeconds;
    }

    public void setRegenIntervalSeconds(long regenIntervalSeconds) {
        this.regenIntervalSeconds = regenIntervalSeconds;
    }

    public long getTickCounter() {
        return tickCounter;
    }

    public void setTickCounter(long tickCounter) {
        this.tickCounter = tickCounter;
    }
}