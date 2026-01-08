package com.stoinkcraft.earning.jobsites;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.earning.collections.CollectionRegistry;
import com.stoinkcraft.earning.collections.CollectionType;
import com.stoinkcraft.earning.jobsites.components.unlockable.Unlockable;
import com.stoinkcraft.earning.jobsites.components.unlockable.UnlockableProgress;
import com.stoinkcraft.earning.jobsites.components.unlockable.UnlockableState;

import java.util.*;

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

    /**
     * Get the current collection count for a specific collection type
     */
    public long getCollectionCount(String collectionId) {
        return collectionCounts.getOrDefault(collectionId, 0L);
    }

    /**
     * Get the current collection count for a specific collection type
     */
    public long getCollectionCount(CollectionType type) {
        return getCollectionCount(type.getId());
    }

    /**
     * Add progress to a collection and return any level-ups that occurred.
     *
     * @param collectionId The collection ID
     * @param amount Amount to add
     * @return List of levels that were achieved (empty if no level-ups)
     */
    public List<Integer> addCollectionProgress(String collectionId, long amount) {
        long oldCount = getCollectionCount(collectionId);
        int oldLevel = CollectionRegistry.getLevelFromCount(oldCount);

        long newCount = oldCount + amount;
        collectionCounts.put(collectionId, newCount);

        int newLevel = CollectionRegistry.getLevelFromCount(newCount);

        // Collect all levels achieved
        List<Integer> levelsAchieved = new ArrayList<>();
        for (int level = oldLevel + 1; level <= newLevel; level++) {
            levelsAchieved.add(level);
        }

        return levelsAchieved;
    }

    /**
     * Add progress to a collection and return any level-ups that occurred.
     */
    public List<Integer> addCollectionProgress(CollectionType type, long amount) {
        return addCollectionProgress(type.getId(), amount);
    }

    /**
     * Get the current level for a collection
     */
    public int getCollectionLevel(String collectionId) {
        return CollectionRegistry.getLevelFromCount(getCollectionCount(collectionId));
    }

    /**
     * Get the current level for a collection
     */
    public int getCollectionLevel(CollectionType type) {
        return getCollectionLevel(type.getId());
    }

    /**
     * Get all collection counts (for GUI display)
     */
    public Map<String, Long> getAllCollectionCounts() {
        return Collections.unmodifiableMap(collectionCounts);
    }


    // ==================== Parent ====================

    public JobSite getParent() {
        return this.parent;
    }

    public void setParent(JobSite parent) {
        this.parent = parent;
    }
}