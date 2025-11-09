package com.stoinkcraft.jobs.jobsites.sites;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import org.bukkit.Location;

import java.io.File;

public class GraveyardSite extends JobSite {
    public GraveyardSite(Enterprise enterprise, JobSiteType type, Location spawnPoint, File schematic, boolean isBuilt) {
        super(enterprise, type, spawnPoint, schematic, isBuilt);
    }

    @Override
    public void initializeJobs() {

    }

    @Override
    public void initializeBuild() {

    }
}
