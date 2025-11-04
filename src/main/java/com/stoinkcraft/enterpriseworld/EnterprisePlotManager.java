package com.stoinkcraft.enterpriseworld;

import com.stoinkcraft.jobs.jobsites.JobSiteType;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnterprisePlotManager {

    private final EnterpriseWorldManager worldManager;
    private final int plotSpacing = 500;
    private final int jobOffset = 250; // Distance between each schematic

    public EnterprisePlotManager(EnterpriseWorldManager worldManager) {
        this.worldManager = worldManager;
    }

    public Map<JobSiteType, Location> assignPlots(UUID enterpriseId, int index) {
        Map<JobSiteType, Location> locations = new HashMap<>();

        int baseX = 0;
        int baseZ = index * plotSpacing;

        locations.put(JobSiteType.SKYRISE, new Location(worldManager.getWorld(), baseX, 64, baseZ));
        locations.put(JobSiteType.QUARRY, new Location(worldManager.getWorld(), baseX + jobOffset, 64, baseZ));
        locations.put(JobSiteType.FARMLAND, new Location(worldManager.getWorld(), baseX + 2 * jobOffset, 64, baseZ));
        locations.put(JobSiteType.GRAVEYARD, new Location(worldManager.getWorld(), baseX + 3 * jobOffset, 64, baseZ));

        return locations;
    }
}
