package com.stoinkcraft.earning.jobsites;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.earning.jobsites.sites.farmland.FarmlandData;
import com.stoinkcraft.earning.jobsites.sites.graveyard.GraveyardData;
import com.stoinkcraft.earning.jobsites.sites.graveyard.GraveyardSite;
import com.stoinkcraft.earning.jobsites.sites.quarry.QuarryData;
import com.stoinkcraft.earning.jobsites.sites.skyrise.SkyriseData;
import com.stoinkcraft.earning.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.earning.jobsites.sites.quarry.QuarrySite;
import com.stoinkcraft.earning.jobsites.sites.skyrise.SkyriseSite;
import org.bukkit.Location;

import java.util.Map;

public class JobSiteManager {

    private Enterprise enterprise;

    private SkyriseSite skyriseSite;
    private QuarrySite quarrySite;
    private FarmlandSite farmlandSite;
    private GraveyardSite graveyardSite;

    private Map<JobSiteType, Location> plots;
    private int plotIndex;

    public JobSiteManager(Enterprise enterprise, int plotIndex) {
        this.enterprise = enterprise;
        this.plotIndex = plotIndex;
    }

    public void disbandJobSites() {
        if (skyriseSite != null) skyriseSite.disband();
        if (quarrySite != null) quarrySite.disband();
        if (farmlandSite != null) farmlandSite.disband();
        if (graveyardSite != null) graveyardSite.disband();
    }

    /**
     * Initialize job sites with provided data (from JSON load)
     */
    public void initializeJobSites(SkyriseData skyriseData, QuarryData quarryData, FarmlandData farmlandData, GraveyardData graveyardData) {
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

        //Initialize Farmland
        Location farmlandLoc = plots.get(JobSiteType.FARMLAND);
        if(farmlandData == null){
            farmlandData = createDefaultFarmlandData();
        }
        farmlandSite = new FarmlandSite(enterprise, farmlandLoc, farmlandData);

        // Initialize Quarry
        Location quarryLoc = plots.get(JobSiteType.QUARRY);
        if (quarryData == null) {
            quarryData = createDefaultQuarryData();
        }
        quarrySite = new QuarrySite(enterprise, quarryLoc, quarryData);

        //Initialize Graveyard
        Location graveyardLoc = plots.get(JobSiteType.GRAVEYARD);
        if(graveyardData == null){
            graveyardData = createDefaultGraveyardData();
        }
        graveyardSite = new GraveyardSite(enterprise, graveyardLoc, graveyardData);

    }

    /**
     * Initialize with default values (new enterprise)
     */
    public void initializeJobSites() {
        initializeJobSites(null, null, null, null);
    }

    // Default data factories
    private SkyriseData createDefaultSkyriseData() {
        return new SkyriseData(false, skyriseSite);
    }

    private QuarryData createDefaultQuarryData() {
        return new QuarryData(false, quarrySite);
    }

    private FarmlandData createDefaultFarmlandData(){
        return new FarmlandData(false, farmlandSite);
    }

    private GraveyardData createDefaultGraveyardData() {return new GraveyardData(false, graveyardSite);}

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

    public GraveyardData getGraveyardData(){
        if(graveyardSite == null) return null;
        return graveyardSite.getData();
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

    public GraveyardSite getGraveyardSite() {return graveyardSite;}

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
            case GRAVEYARD -> {
                return graveyardSite;
            }
        }
        return null;
    }

    public JobSiteType resolveJobsite(Location location) {
        if (getFarmlandSite() != null &&
                getFarmlandSite().contains(location)) {
            return JobSiteType.FARMLAND;
        }
         if (getQuarrySite() != null &&
                 getQuarrySite().contains(location)) {
             return JobSiteType.QUARRY;
         }
         if (getSkyriseSite() != null &&
                 getSkyriseSite().contains(location)) {
             return JobSiteType.SKYRISE;
         }
         if(getGraveyardSite() != null &&
                 getGraveyardSite().contains(location)){
             return JobSiteType.GRAVEYARD;
         }
        return null;
    }

    public int getPlotIndex() {
        return plotIndex;
    }
}