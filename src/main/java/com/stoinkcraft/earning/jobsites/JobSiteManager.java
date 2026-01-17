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

    /**
     * Initialize job sites with provided data (from JSON load)
     * @param isNewEnterprise If true, only Farmland and Skyrise will be auto-built.
     *                        If false (loading existing), sites are restored based on their saved built state.
     */
    public void initializeJobSites(SkyriseData skyriseData, QuarryData quarryData, FarmlandData farmlandData, GraveyardData graveyardData, boolean isNewEnterprise) {
        if (plotIndex == -1) {
            plotIndex = StoinkCore.getInstance().getEnterprisePlotManager().getNextAvailablePlotIndex();
            enterprise.setPlotIndex(plotIndex);
        }
        plots = StoinkCore.getInstance().getEnterprisePlotManager().assignPlots(enterprise.getID(), plotIndex);

        // Initialize Skyrise (always free/default)
        Location skyriseLoc = plots.get(JobSiteType.SKYRISE);
        if (skyriseData == null) {
            skyriseData = createDefaultSkyriseData();
        }
        skyriseSite = new SkyriseSite(enterprise, skyriseLoc, skyriseData);

        // Initialize Farmland (free/default starting jobsite)
        Location farmlandLoc = plots.get(JobSiteType.FARMLAND);
        if (farmlandData == null) {
            farmlandData = createDefaultFarmlandData();
        }
        farmlandSite = new FarmlandSite(enterprise, farmlandLoc, farmlandData);

        // Initialize Quarry (requires purchase)
        Location quarryLoc = plots.get(JobSiteType.QUARRY);
        if (quarryData == null) {
            quarryData = createDefaultQuarryData();
        }
        quarrySite = new QuarrySite(enterprise, quarryLoc, quarryData);

        // Initialize Graveyard (requires purchase)
        Location graveyardLoc = plots.get(JobSiteType.GRAVEYARD);
        if (graveyardData == null) {
            graveyardData = createDefaultGraveyardData();
        }
        graveyardSite = new GraveyardSite(enterprise, graveyardLoc, graveyardData);

        // For new enterprises, only auto-build Farmland and Skyrise
        // Quarry and Graveyard must be purchased
        if (isNewEnterprise) {
            // Skyrise and Farmland are free - build them automatically
            // Note: build() checks isBuilt flag, so it won't rebuild if already built
        }
        // For existing enterprises, the build state is preserved from the loaded data
    }

    /**
     * Initialize job sites with provided data (from JSON load - existing enterprise)
     */
    public void initializeJobSites(SkyriseData skyriseData, QuarryData quarryData, FarmlandData farmlandData, GraveyardData graveyardData) {
        initializeJobSites(skyriseData, quarryData, farmlandData, graveyardData, false);
    }

    /**
     * Initialize with default values (new enterprise)
     * Only Farmland and Skyrise will be auto-built.
     */
    public void initializeJobSites() {
        initializeJobSites(null, null, null, null, true);
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

    public List<JobSite> getAllJobSites(){
        return List.of(skyriseSite, quarrySite, farmlandSite, graveyardSite);
    }

    /**
     * Initialize all NPCs from Citizens registry for all job sites.
     * Call this after CitizensEnableEvent has fired.
     */
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

    // ==================== Job Site Purchase System ====================

    /**
     * Check if a job site is unlocked (purchased/built)
     */
    public boolean isJobSiteUnlocked(JobSiteType type) {
        JobSite site = getJobSite(type);
        return site != null && site.isBuilt();
    }

    /**
     * Check if requirements are met to purchase a job site
     */
    public boolean canPurchaseJobSite(JobSiteType type) {
        JobSiteRequirements req = JobSiteRequirements.forType(type);
        if (req == null) return false;

        // Already unlocked
        if (isJobSiteUnlocked(type)) return false;

        // Check prerequisite is unlocked
        if (req.getPrerequisite() != null && !isJobSiteUnlocked(req.getPrerequisite())) {
            return false;
        }

        // Check prerequisite level requirement
        if (req.getRequiredPreReqLevel() > 0 && req.getPrerequisite() != null) {
            JobSite prereqSite = getJobSite(req.getPrerequisite());
            if (prereqSite == null || prereqSite.getLevel() < req.getRequiredPreReqLevel()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get the reason why a job site cannot be purchased
     */
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

        return null; // No blocking reason
    }

    /**
     * Purchase and build a job site
     * @return true if purchase was successful
     */
    public boolean purchaseJobSite(JobSiteType type, Player player) {
        JobSiteRequirements req = JobSiteRequirements.forType(type);
        if (req == null) return false;

        // Check if can purchase
        if (!canPurchaseJobSite(type)) return false;

        // Check funds
        int cost = req.getCost();
        if (cost > 0 && !StoinkCore.getEconomy().has(player, cost)) {
            return false;
        }

        // Withdraw funds
        if (cost > 0) {
            StoinkCore.getEconomy().withdrawPlayer(player, cost);
        }

        // Build the job site
        JobSite site = getJobSite(type);
        if (site != null) {
            site.build();
            return true;
        }

        return false;
    }

    /**
     * Get the current Farmland level (used for requirement checks)
     */
    public int getFarmlandLevel() {
        if (farmlandSite == null) return 0;
        return farmlandSite.getLevel();
    }
}