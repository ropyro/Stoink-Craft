package com.stoinkcraft.jobs.jobsites;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.data.QuarryData;
import com.stoinkcraft.jobs.jobsites.data.SkyriseData;
import com.stoinkcraft.jobs.jobsites.sites.QuarrySite;
import com.stoinkcraft.jobs.jobsites.sites.SkyriseSite;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Map;

public class JobSiteManager {

    private Enterprise enterprise;
    private SkyriseSite skyriseSite;
    private QuarrySite quarrySite;
    private Map<JobSiteType, Location> plots;
    private int plotIndex;

    public JobSiteManager(Enterprise enterprise, int plotIndex) {
        this.enterprise = enterprise;
        this.plotIndex = plotIndex;
    }

    public void onEnterpriseDisband() {
        if (skyriseSite != null) skyriseSite.disband();
        if (quarrySite != null) quarrySite.disband();
    }

    /**
     * Initialize job sites with provided data (from JSON load)
     */
    public void initializeJobSites(SkyriseData skyriseData, QuarryData quarryData) {
        if (plotIndex == -1) {
            plotIndex = StoinkCore.getEnterprisePlotManager().getNextAvailablePlotIndex();
            enterprise.setPlotIndex(plotIndex);
        }
        plots = StoinkCore.getEnterprisePlotManager().assignPlots(enterprise.getID(), plotIndex);

        // Initialize Skyrise
        Location skyriseLoc = plots.get(JobSiteType.SKYRISE);
        if (skyriseData == null) {
            skyriseData = createDefaultSkyriseData();
        }
        skyriseSite = new SkyriseSite(enterprise, skyriseLoc, skyriseData);

        // Initialize Quarry
        Location quarryLoc = plots.get(JobSiteType.QUARRY);
        if (quarryData == null) {
            quarryData = createDefaultQuarryData();
        }
        quarrySite = new QuarrySite(enterprise, quarryLoc, quarryData);
    }

    /**
     * Initialize with default values (new enterprise)
     */
    public void initializeJobSites() {
        initializeJobSites(null, null);
    }

    // Default data factories
    private SkyriseData createDefaultSkyriseData() {
        return new SkyriseData(false, new Vector(-5.5, 4, 0.5));
    }

    private QuarryData createDefaultQuarryData() {
        return new QuarryData(
                false,
                new Vector(-3.5, 3, 0.5),
                new Vector(-4, -1, -4),
                new Vector(-25, -21, 17),
                300L,
                0L
        );
    }

    // Getters for serialization
    public SkyriseData getSkyriseData() {
        if (skyriseSite == null) return null;
        return skyriseSite.getData();
    }

    public QuarryData getQuarryData() {
        if (quarrySite == null) return null;
        return quarrySite.getData();
    }

    public SkyriseSite getSkyriseSite() {
        return skyriseSite;
    }

    public QuarrySite getQuarrySite() {
        return quarrySite;
    }

    public int getPlotIndex() {
        return plotIndex;
    }
}