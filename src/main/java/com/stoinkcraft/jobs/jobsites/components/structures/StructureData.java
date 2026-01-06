package com.stoinkcraft.jobs.jobsites.components.structures;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.jobs.jobsites.components.JobSiteStructure;

public class StructureData {

    @Expose
    private JobSiteStructure.StructureState state;
    @Expose
    private long buildFinishTime;

    public JobSiteStructure.StructureState getState() {
        return state == null ? JobSiteStructure.StructureState.LOCKED : state;
    }

    public void startBuilding(long durationMillis) {
        this.state = JobSiteStructure.StructureState.BUILDING;
        this.buildFinishTime = System.currentTimeMillis() + durationMillis;
    }

    public boolean isFinished() {
        return System.currentTimeMillis() >= buildFinishTime;
    }

    public long getRemainingMillis() {
        return Math.max(0, buildFinishTime - System.currentTimeMillis());
    }

    public void markBuilt() {
        this.state = JobSiteStructure.StructureState.BUILT.BUILT;
        this.buildFinishTime = 0;
    }
}