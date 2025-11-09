package com.stoinkcraft.jobs.jobsites.resourcegenerators;

import com.stoinkcraft.jobs.jobsites.JobSite;

public abstract class ResourceGenerator {

    private JobSite parent;
    private long tickCounter = 0;

    public ResourceGenerator(JobSite parent){
        this.parent = parent;
    }

    public void tick() {
        tickCounter++;
        onTick();
    }

    public JobSite getParent(){
        return parent;
    }

    public long getTickCounter() {
        return tickCounter;
    }

    protected abstract void onTick();

}
