package com.stoinkcraft.jobs.jobsites.sites.skyrise;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteData;
import org.bukkit.util.Vector;

/**
 * Serializable data for SkyriseSite
 */
public class SkyriseData extends JobSiteData {

    public SkyriseData(boolean isBuilt, SkyriseSite parent) {
        super(isBuilt, parent);
    }
}