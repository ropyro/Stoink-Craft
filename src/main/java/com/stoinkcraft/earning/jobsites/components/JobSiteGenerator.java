package com.stoinkcraft.earning.jobsites.components;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.stoinkcraft.earning.jobsites.JobSite;
import org.bukkit.Location;

public class JobSiteGenerator implements JobSiteComponent {

    private JobSite parent;
    private boolean enabled;

    public JobSiteGenerator(JobSite parent){
        this.parent = parent;
        enabled = true;
    }
    public JobSiteGenerator(JobSite parent, boolean enabled){
        this.parent = parent;
        this.enabled = enabled;
    }

    @Override
    public void tick() {
        if(!enabled) return;
    }

    @Override
    public void build() {

    }

    @Override
    public void disband() {

    }

    @Override
    public void levelUp() {

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
