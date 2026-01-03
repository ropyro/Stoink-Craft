package com.stoinkcraft.jobs.jobsites;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.sites.farmland.FarmlandData;
import com.stoinkcraft.jobs.jobsites.sites.quarry.QuarryData;
import com.stoinkcraft.jobs.jobsites.sites.skyrise.SkyriseData;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.CropGenerator;
import com.stoinkcraft.jobs.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.jobs.jobsites.sites.quarry.QuarrySite;
import com.stoinkcraft.jobs.jobsites.sites.skyrise.SkyriseSite;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Map;

public class JobSiteManager {

    private Enterprise enterprise;
    private SkyriseSite skyriseSite;
    private QuarrySite quarrySite;
    private FarmlandSite farmlandSite;
    private Map<JobSiteType, Location> plots;
    private int plotIndex;

    public JobSiteManager(Enterprise enterprise, int plotIndex) {
        this.enterprise = enterprise;
        this.plotIndex = plotIndex;
    }

    public void onEnterpriseDisband() {
        if (skyriseSite != null) skyriseSite.disband();
        if (quarrySite != null) quarrySite.disband();
        if (farmlandSite != null) farmlandSite.disband();
    }

    /**
     * Initialize job sites with provided data (from JSON load)
     */
    public void initializeJobSites(SkyriseData skyriseData, QuarryData quarryData, FarmlandData farmlandData) {
        if (plotIndex == -1) {
            plotIndex = StoinkCore.getInstance().getEnterprisePlotManager().getNextAvailablePlotIndex();
            enterprise.setPlotIndex(plotIndex);
        }
        plots = StoinkCore.getInstance().getEnterprisePlotManager().assignPlots(enterprise.getID(), plotIndex);

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

        //Initialize Farmland
        Location farmlandLoc = plots.get(JobSiteType.FARMLAND);
        if(farmlandData == null){
            farmlandData = createDefaultFarmlandData();
        }
        farmlandSite = new FarmlandSite(enterprise, farmlandLoc, farmlandData);
    }

    /**
     * Initialize with default values (new enterprise)
     */
    public void initializeJobSites() {
        initializeJobSites(null, null, null);
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

    private FarmlandData createDefaultFarmlandData(){
        return new FarmlandData(false);
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

    public FarmlandData getFarmlandData(){
        if(farmlandSite == null) return null;
        return farmlandSite.getData();
    }

    public SkyriseSite getSkyriseSite() {
        return skyriseSite;
    }

    public QuarrySite getQuarrySite() {
        return quarrySite;
    }

    public FarmlandSite getFarmlandSite(){
        return farmlandSite;
    }

    public JobSite getJobSite(JobSiteType jobSiteType){
        switch (jobSiteType){
            case FARMLAND -> {
                return farmlandSite;
            }
            case QUARRY -> {
                return quarrySite;
            }
            case SKYRISE -> {
                return skyriseSite;
            }
        }
        return null;
    }

    public JobSiteType resolveJobsite(Location location) {

        // FARMLAND
        if (getFarmlandSite() != null &&
                getFarmlandSite().contains(location)) {
            return JobSiteType.FARMLAND;
        }

        // QUARRY (future)
        // if (manager.getQuarrySite() != null &&
        //         manager.getQuarrySite().contains(location)) {
        //     return JobSiteType.QUARRY;
        // }

        // SKYRISE (future)
        // if (manager.getSkyriseSite() != null &&
        //         manager.getSkyriseSite().contains(location)) {
        //     return JobSiteType.SKYRISE;
        // }

        return null;
    }

    public int getPlotIndex() {
        return plotIndex;
    }
}