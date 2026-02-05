package com.stoinkcraft.jobsites.sites;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.jobsites.collections.CollectionRegistry;
import com.stoinkcraft.jobsites.collections.CollectionType;
import com.stoinkcraft.jobsites.components.unlockable.Unlockable;
import com.stoinkcraft.jobsites.components.unlockable.UnlockableProgress;
import com.stoinkcraft.jobsites.components.unlockable.UnlockableState;

import java.util.*;

public class JobSiteData {

    private transient JobSite parent;

    @Expose
    private boolean isBuilt;
    @Expose
    private final Map<String, Integer> upgrades;
    @Expose
    private final Map<String, Integer> npcs;
    @Expose
    private final Map<String, UnlockableProgress> unlockables;
    @Expose
    private final Map<String, Long> collectionCounts = new HashMap<>();
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


    public int getNpcId(String name) {
        return npcs.getOrDefault(name, -1);
    }

    public void setNpc(String name, int id) {
        npcs.put(name, id);
    }


    public int getLevel(String upgradeId) {
        return upgrades.getOrDefault(upgradeId, 0);
    }

    public void setLevel(String upgradeId, int level) {
        upgrades.put(upgradeId, level);
    }

    public Map<String, Integer> getUpgrades() {
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

    public void incrementXp(int increment) {
        int oldLevel = JobsiteLevelHelper.getLevelFromXp(xp);
        xp += increment;
        int newLevel = JobsiteLevelHelper.getLevelFromXp(xp);

        if (newLevel > oldLevel) {
            getParent().levelUp();
        }
    }

    public long getCollectionCount(String collectionId) {
        return collectionCounts.getOrDefault(collectionId, 0L);
    }

    public long getCollectionCount(CollectionType type) {
        return getCollectionCount(type.getId());
    }


    public List<Integer> addCollectionProgress(String collectionId, long amount) {
        long oldCount = getCollectionCount(collectionId);
        int oldLevel = CollectionRegistry.getLevelFromCount(oldCount);

        long newCount = oldCount + amount;
        collectionCounts.put(collectionId, newCount);

        int newLevel = CollectionRegistry.getLevelFromCount(newCount);

        List<Integer> levelsAchieved = new ArrayList<>();
        for (int level = oldLevel + 1; level <= newLevel; level++) {
            levelsAchieved.add(level);
        }

        return levelsAchieved;
    }

    public List<Integer> addCollectionProgress(CollectionType type, long amount) {
        return addCollectionProgress(type.getId(), amount);
    }


    public int getCollectionLevel(String collectionId) {
        return CollectionRegistry.getLevelFromCount(getCollectionCount(collectionId));
    }


    public int getCollectionLevel(CollectionType type) {
        return getCollectionLevel(type.getId());
    }

    public Map<String, Long> getAllCollectionCounts() {
        return Collections.unmodifiableMap(collectionCounts);
    }


    public JobSite getParent() {
        return this.parent;
    }

    public void setParent(JobSite parent) {
        this.parent = parent;
    }
}