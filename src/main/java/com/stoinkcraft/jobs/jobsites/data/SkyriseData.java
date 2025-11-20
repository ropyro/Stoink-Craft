package com.stoinkcraft.jobs.jobsites.data;

import com.google.gson.annotations.Expose;
import org.bukkit.util.Vector;

/**
 * Serializable data for SkyriseSite
 */
public class SkyriseData {
    @Expose
    private boolean isBuilt;

    @Expose
    private Vector entryHologramOffset;

    public SkyriseData(boolean isBuilt, Vector entryHologramOffset) {
        this.isBuilt = isBuilt;
        this.entryHologramOffset = entryHologramOffset;
    }

    public boolean isBuilt() {
        return isBuilt;
    }

    public void setBuilt(boolean built) {
        isBuilt = built;
    }

    public Vector getEntryHologramOffset() {
        return entryHologramOffset;
    }

    public void setEntryHologramOffset(Vector entryHologramOffset) {
        this.entryHologramOffset = entryHologramOffset;
    }
}