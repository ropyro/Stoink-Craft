package com.stoinkcraft.earning.jobsites.components.generators;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.earning.jobsites.JobSite;
import org.bukkit.Location;

public class GreenhouseGenerator extends CropGenerator{

    @Expose
    public CropGeneratorType currentType = CropGeneratorType.WHEAT;

    public GreenhouseGenerator(Location corner1, Location corner2, JobSite parent, String regionName) {
        super(corner1, corner2, parent, regionName);
    }
}
