package com.stoinkcraft.jobs.jobsites.sites;

import com.sk89q.worldedit.regions.Region;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import org.bukkit.Location;

import java.io.File;

public class SkyriseSite extends JobSite {
    public SkyriseSite(Enterprise enterprise, Location spawnPoint, boolean isbuilt) {
        super(enterprise, JobSiteType.SKYRISE, spawnPoint, new File(StoinkCore.getInstance().getDataFolder(), "/schematics/building.schem"), isbuilt);
    }

    @Override
    public void initializeJobs() {

    }
}
