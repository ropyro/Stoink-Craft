package com.stoinkcraft.jobs.jobsites.data;

import com.google.gson.annotations.Expose;
import org.bukkit.util.Vector;

public class FarmlandData {

    @Expose
    private boolean isBuilt;


    public FarmlandData(boolean isBuilt) {
        this.isBuilt = isBuilt;
    }

    public boolean isBuilt() {
        return isBuilt;
    }

    public void setBuilt(boolean built) {
        isBuilt = built;
    }
}
