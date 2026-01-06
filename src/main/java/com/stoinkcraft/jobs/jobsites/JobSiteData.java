package com.stoinkcraft.jobs.jobsites;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.jobs.jobsites.components.structures.StructureData;
import com.stoinkcraft.jobs.jobsites.components.unlockable.Unlockable;
import com.stoinkcraft.jobs.jobsites.components.unlockable.UnlockableProgress;
import com.stoinkcraft.jobs.jobsites.components.unlockable.UnlockableState;

import java.util.HashMap;
import java.util.Map;

public class JobSiteData {

    private transient JobSite parent; // transient = not serialized by Gson

    @Expose
    private boolean isBuilt;
    @Expose
    private final Map<String, Integer> upgrades;
    @Expose
    private final Map<String, Integer> npcs;
    @Expose
    private final Map<String, UnlockableProgress> unlockables;
    @Expose
    private int xp;

    public JobSiteData(boolean isBuilt, JobSite parent) {
        this.isBuilt = isBuilt;
        this.upgrades = new HashMap<>();
        this.npcs = new HashMap<>();
        this.unlockables = new HashMap<>();
        this.xp = 0;
        this.parent = parent;
    }

    // ==================== Unlockable Methods ====================

    public UnlockableProgress getUnlockableProgress(String id) {
        return unlockables.computeIfAbsent(id, k -> new UnlockableProgress());
    }

    public UnlockableState getUnlockableState(String id) {
        return getUnlockableProgress(id).getState();
    }

    public void startUnlock(Unlockable unlockable) {
        UnlockableProgress progress = getUnlockableProgress(unlockable.getUnlockableId());
        progress.startBuilding(unlockable.getBuildTimeMillis());
        unlockable.onUnlockStart();
    }

    // ==================== NPC Methods ====================

    public int getNpcId(String name) {
        return npcs.getOrDefault(name, -1);
    }

    public void setNpc(String name, int id) {
        npcs.put(name, id);
    }

    // ==================== Upgrade Methods ====================

    public int getLevel(String upgradeId) {
        return upgrades.getOrDefault(upgradeId, 0);
    }

    public void setLevel(String upgradeId, int level) {
        upgrades.put(upgradeId, level);
    }

    public Map<String, Integer> getUpgrades() {
        return upgrades;
    }

    // ==================== Build State ====================

    public boolean isBuilt() {
        return isBuilt;
    }

    public void setBuilt(boolean built) {
        isBuilt = built;
    }

    // ==================== XP ====================

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void incrementXp(int increment) {
        int oldLevel = JobsiteLevelHelper.getLevelFromXp(xp);
        xp += increment;
        int newLevel = JobsiteLevelHelper.getLevelFromXp(xp);

        if (newLevel > oldLevel) {
            getParent().levelUp();
        }
    }

    // ==================== Parent ====================

    public JobSite getParent() {
        return this.parent;
    }

    public void setParent(JobSite parent) {
        this.parent = parent;
    }
}