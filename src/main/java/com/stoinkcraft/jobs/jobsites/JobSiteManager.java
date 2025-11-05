package com.stoinkcraft.jobs.jobsites;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.sites.FarmlandSite;
import com.stoinkcraft.jobs.jobsites.sites.GraveyardSite;
import com.stoinkcraft.jobs.jobsites.sites.QuarrySite;
import com.stoinkcraft.jobs.jobsites.sites.SkyriseSite;
import org.bukkit.Location;

import java.util.Map;

public class JobSiteManager {

    private Enterprise enterprise;
    private SkyriseSite skyriseSite;
    private FarmlandSite farmlandSite;
    private QuarrySite quarrySite;
    private GraveyardSite graveyardSite;
    private Map<JobSiteType, Location> plots;
    private int plotIndex;

    public JobSiteManager(Enterprise enterprise, int plotIndex){
        this.enterprise = enterprise;
        this.plotIndex = plotIndex;
    }

    public void onEnterpriseDisband(){
        skyriseSite.disband();
        quarrySite.disband();
    }

    public void initializeJobSites(boolean skyriseIsBuilt, boolean quarryIsBuilt){
        if (plotIndex == -1) {
            plotIndex = StoinkCore.getEnterprisePlotManager().getNextAvailablePlotIndex();
        }
        plots = StoinkCore.getEnterprisePlotManager().assignPlots(enterprise.getID(), plotIndex);
        skyriseSite = new SkyriseSite(enterprise, plots.get(JobSiteType.SKYRISE), skyriseIsBuilt);
        quarrySite = new QuarrySite(enterprise, plots.get(JobSiteType.QUARRY), quarryIsBuilt);
    }

    public SkyriseSite getSkyriseSite() {
        return skyriseSite;
    }

    public FarmlandSite getFarmlandSite() {
        return farmlandSite;
    }

    public QuarrySite getQuarrySite() {
        return quarrySite;
    }

    public GraveyardSite getGraveyardSite() {
        return graveyardSite;
    }
}
