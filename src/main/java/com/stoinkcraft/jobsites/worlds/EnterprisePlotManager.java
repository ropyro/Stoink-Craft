package com.stoinkcraft.jobsites.worlds;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import org.bukkit.Location;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnterprisePlotManager {

    private final EnterpriseWorldManager worldManager;
    private final int plotSpacing = 500;
    private final int jobOffset = 250; // Distance between each schematic

    private int nextIndex = 0;

    public EnterprisePlotManager(EnterpriseWorldManager worldManager) {
        this.worldManager = worldManager;
    }

    public int getNextAvailablePlotIndex() {
        return nextIndex++;
    }

    // Optional: on load, find the max assigned index and start from there
    public void resetNextIndex(Collection<Enterprise> enterprises) {
        int max = enterprises.stream()
                .mapToInt(e -> e.getPlotIndex())
                .filter(i -> i >= 0)
                .max()
                .orElse(0);
        nextIndex = max + 1;
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
