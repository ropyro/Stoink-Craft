package com.stoinkcraft.jobs.jobsites.sites.skyrise;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.jobs.jobsites.JobSiteData;
import org.bukkit.util.Vector;

/**
 * Serializable data for SkyriseSite
 */
public class SkyriseData extends JobSiteData {
    @Expose
    private Vector entryHologramOffset;

    public SkyriseData(boolean isBuilt, Vector entryHologramOffset) {
        super(isBuilt);
        this.entryHologramOffset = entryHologramOffset;
    }

    public Vector getEntryHologramOffset() {
        return entryHologramOffset;
    }

    public void setEntryHologramOffset(Vector entryHologramOffset) {
        this.entryHologramOffset = entryHologramOffset;
    }
}