package com.stoinkcraft.jobsites.sites;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.sites.farmland.FarmlandData;
import com.stoinkcraft.jobsites.sites.graveyard.GraveyardData;
import com.stoinkcraft.jobsites.sites.graveyard.GraveyardSite;
import com.stoinkcraft.jobsites.sites.quarry.QuarryData;
import com.stoinkcraft.jobsites.sites.skyrise.SkyriseData;
import com.stoinkcraft.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.jobsites.sites.quarry.QuarrySite;
import com.stoinkcraft.jobsites.sites.skyrise.SkyriseSite;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
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

    public void initializeJobSites(SkyriseData skyriseData, QuarryData quarryData, FarmlandData farmlandData, GraveyardData graveyardData, boolean isNewEnterprise) {
        if (plotIndex == -1) {
            plotIndex = StoinkCore.getInstance().getEnterprisePlotManager().getNextAvailablePlotIndex();
            enterprise.setPlotIndex(plotIndex);
        }
        plots = StoinkCore.getInstance().getEnterprisePlotManager().assignPlots(enterprise.getID(), plotIndex);

        Location skyriseLoc = plots.get(JobSiteType.SKYRISE);
        if (skyriseData == null) {
            skyriseData = createDefaultSkyriseData();
        }
        skyriseSite = new SkyriseSite(enterprise, skyriseLoc, skyriseData);

        Location farmlandLoc = plots.get(JobSiteType.FARMLAND);
        if (farmlandData == null) {
            farmlandData = createDefaultFarmlandData();
        }
        farmlandSite = new FarmlandSite(enterprise, farmlandLoc, farmlandData);

        Location quarryLoc = plots.get(JobSiteType.QUARRY);
        if (quarryData == null) {
            quarryData = createDefaultQuarryData();
        }
        quarrySite = new QuarrySite(enterprise, quarryLoc, quarryData);

        Location graveyardLoc = plots.get(JobSiteType.GRAVEYARD);
        if (graveyardData == null) {
            graveyardData = createDefaultGraveyardData();
        }
        graveyardSite = new GraveyardSite(enterprise, graveyardLoc, graveyardData);

    }

    public void initializeJobSites(SkyriseData skyriseData, QuarryData quarryData, FarmlandData farmlandData, GraveyardData graveyardData) {
        initializeJobSites(skyriseData, quarryData, farmlandData, graveyardData, false);
    }

    public void initializeJobSites() {
        initializeJobSites(null, null, null, null, true);
    }

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

    public List<JobSite> getAllJobSites(){
        return List.of(skyriseSite, quarrySite, farmlandSite, graveyardSite);
    }

    public void initializeNpcsFromRegistry() {
        getAllJobSites().forEach(JobSite::initializeNpcsFromRegistry);
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
            case GRAVEYARD -> {
                return graveyardSite;
            }
        }
        return null;
    }

    public JobSiteType resolveJobsite(Location location) {
        if (getFarmlandSite() != null && getFarmlandSite().isBuilt() &&
                getFarmlandSite().contains(location)) {
            return JobSiteType.FARMLAND;
        }
         if (getQuarrySite() != null && getQuarrySite().isBuilt() &&
                 getQuarrySite().contains(location)) {
             return JobSiteType.QUARRY;
         }
         if (getSkyriseSite() != null && getSkyriseSite().isBuilt() &&
                 getSkyriseSite().contains(location)) {
             return JobSiteType.SKYRISE;
         }
         if(getGraveyardSite() != null && getGraveyardSite().isBuilt() &&
                 getGraveyardSite().contains(location)){
             return JobSiteType.GRAVEYARD;
         }
        return null;
    }

    public int getPlotIndex() {
        return plotIndex;
    }


    public boolean isJobSiteUnlocked(JobSiteType type) {
        JobSite site = getJobSite(type);
        return site != null && site.isBuilt();
    }


    public boolean canPurchaseJobSite(JobSiteType type) {
        JobSiteRequirements req = JobSiteRequirements.forType(type);
        if (req == null) return false;

        if (isJobSiteUnlocked(type)) return false;

        if (req.getPrerequisite() != null && !isJobSiteUnlocked(req.getPrerequisite())) {
            return false;
        }

        if (req.getRequiredPreReqLevel() > 0 && req.getPrerequisite() != null) {
            JobSite prereqSite = getJobSite(req.getPrerequisite());
            if (prereqSite == null || prereqSite.getLevel() < req.getRequiredPreReqLevel()) {
                return false;
            }
        }

        return true;
    }

    public String getPurchaseBlockReason(JobSiteType type) {
        JobSiteRequirements req = JobSiteRequirements.forType(type);
        if (req == null) return "Unknown job site";

        if (isJobSiteUnlocked(type)) return "Already unlocked";

        if (req.getPrerequisite() != null && !isJobSiteUnlocked(req.getPrerequisite())) {
            return "Requires " + req.getPrerequisite().name() + " to be unlocked first";
        }

        if (req.getRequiredPreReqLevel() > 0 && req.getPrerequisite() != null) {
            JobSite prereqSite = getJobSite(req.getPrerequisite());
            if (prereqSite != null) {
                int currentLevel = prereqSite.getLevel();
                if (currentLevel < req.getRequiredPreReqLevel()) {
                    String prereqName = req.getPrerequisite().name().charAt(0) + req.getPrerequisite().name().substring(1).toLowerCase();
                    return "Requires " + prereqName + " Level " + req.getRequiredPreReqLevel() + " (current: " + currentLevel + ")";
                }
            }
        }

        return null;
    }

    public boolean purchaseJobSite(JobSiteType type, Player player) {
        if (!enterprise.hasManagementPermission(player.getUniqueId())) return false;

        JobSiteRequirements req = JobSiteRequirements.forType(type);
        if (req == null) return false;

        if (!canPurchaseJobSite(type)) return false;

        int cost = req.getCost();
        if (cost > 0 && enterprise.getBankBalance() < cost) {
            return false;
        }

        if (cost > 0) {
            enterprise.decreaseBankBalance(cost);
        }

        JobSite site = getJobSite(type);
        if (site != null) {
            site.build();
            return true;
        }

        return false;
    }

    public int getFarmlandLevel() {
        if (farmlandSite == null) return 0;
        return farmlandSite.getLevel();
    }
}