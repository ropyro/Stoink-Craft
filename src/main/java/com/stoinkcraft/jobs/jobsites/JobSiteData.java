package com.stoinkcraft.jobs.jobsites;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;

public class JobSiteData {

    private JobSite parent;
    @Expose
    private boolean isBuilt;
    @Expose
    private final Map<String, Integer> upgrades;
    @Expose
    private final Map<String, StructureData> structures;
    @Expose
    private int xp;

    public JobSiteData(boolean isBuilt, JobSite parent){
        this.isBuilt = isBuilt;
        this.upgrades = new HashMap<>();
        this.structures = new HashMap<>();
        this.xp = 0;
        this.parent = parent;
    }

    public StructureData getStructure(String id) {
        return structures.computeIfAbsent(id, k -> new StructureData());
    }
    public int getLevel(String upgradeId) {
        return upgrades.getOrDefault(upgradeId, 0);
    }

    public void setLevel(String upgradeId, int level) {
        upgrades.put(upgradeId, level);
    }

    public Map<String, Integer> getUpgrades(){
        return upgrades;
    }

    public boolean isBuilt() {
        return isBuilt;
    }

    public void setBuilt(boolean built) {
        isBuilt = built;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void incrementXp(int increment){
        if(JobsiteLevelHelper.getLevelFromXp(xp + increment) > JobsiteLevelHelper.getLevelFromXp(xp) ){
            getParent().levelUp();
        }
        xp += increment;
    }

    private JobSite getParent(){
        return this.parent;
    }

    public void setParent(JobSite parent){
        this.parent = parent;
    }
}
