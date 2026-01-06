package com.stoinkcraft.jobs.jobsites.components.generators;

import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.components.JobSiteGenerator;
import com.stoinkcraft.jobs.jobsites.components.JobSiteHologram;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.List;

public class PowerCellGenerator extends JobSiteGenerator {

    private boolean initialized = false;

    private final Location hiveLocation;
    private final World world;

    private JobSiteHologram hologram;
    private final String hologramId;
    private static final Vector HOLOGRAM_OFFSET = new Vector(0.5, 1.6, 0.5);

    public PowerCellGenerator(Location hiveLocation, JobSite parent) {
        super(parent);
        this.hiveLocation = hiveLocation.clone();
        this.world = hiveLocation.getWorld();

        this.hologramId =
                parent.getEnterprise().getID() + "_honey_" +
                        hiveLocation.getBlockX() + "_" +
                        hiveLocation.getBlockY() + "_" +
                        hiveLocation.getBlockZ();

        this.hologram = new JobSiteHologram(
                parent,
                hologramId,
                hiveLocation.clone().subtract(parent.getSpawnPoint().clone()).add(HOLOGRAM_OFFSET).toVector(),
                List.of()
        );

        parent.addComponent(hologram);
    }


}
