package com.stoinkcraft.jobs.jobsites.resourcegenerators;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.stoinkcraft.jobs.jobsites.JobSite;
import org.bukkit.Location;

public abstract class ResourceGenerator {

    private JobSite parent;
    private long tickCounter = 0;

    private boolean enabled;

    public ResourceGenerator(JobSite parent){
        this.parent = parent;
        enabled = true;
    }
    public ResourceGenerator(JobSite parent, boolean enabled){
        this.parent = parent;
        this.enabled = enabled;
    }

    public void tick() {
        if(!enabled) return;
        tickCounter++;
        onTick();
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public JobSite getParent(){
        return parent;
    }

    public long getTickCounter() {
        return tickCounter;
    }

    public void setTickCounter(long ticks){
        this.tickCounter = ticks;
    }

    protected abstract void onTick();

    public abstract void init();

    public CuboidRegion getRegion(Location corner1, Location corner2) {
        BlockVector3 min = BlockVector3.at(
                Math.min(corner1.getBlockX(), corner2.getBlockX()),
                Math.min(corner1.getBlockY(), corner2.getBlockY()),
                Math.min(corner1.getBlockZ(), corner2.getBlockZ())
        );
        BlockVector3 max = BlockVector3.at(
                Math.max(corner1.getBlockX(), corner2.getBlockX()),
                Math.max(corner1.getBlockY(), corner2.getBlockY()),
                Math.max(corner1.getBlockZ(), corner2.getBlockZ())
        );
        return new CuboidRegion(min, max);
    }

}
