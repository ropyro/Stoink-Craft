package com.stoinkcraft.jobs.jobsites.sites.graveyard;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteData;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import org.bukkit.Location;

import java.io.File;

public class GraveyardSite extends JobSite {
    public GraveyardSite(Enterprise enterprise, JobSiteType type, Location spawnPoint, File schematic, JobSiteData data, boolean isBuilt) {
        super(enterprise, type, spawnPoint, schematic, data, isBuilt);
    }

    @Override
    public void initializeBuild() {

    }

    @Override
    public void tick() {

    }
}
