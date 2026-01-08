package com.stoinkcraft.earning.jobsites.sites.graveyard;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteData;

import java.util.HashMap;
import java.util.Map;

public class GraveyardData extends JobSiteData {

    @Expose
    private int souls = 0;

    @Expose
    private int tombstonesPurchased = 4; // Start with 4 active

    @Expose
    private Map<Integer, UndeadMobType> tombstoneAttunements;

    public GraveyardData(boolean isBuilt, JobSite parent) {
        super(isBuilt, parent);
        this.tombstoneAttunements = new HashMap<>();
    }

    // ==================== Soul Methods ====================

    public int getSouls() {
        return souls;
    }

    public void setSouls(int souls) {
        this.souls = souls;
    }

    public void addSouls(int amount) {
        this.souls += amount;
    }

    public boolean spendSouls(int amount) {
        if (souls < amount) return false;
        souls -= amount;
        return true;
    }

    // ==================== Tombstone Methods ====================

    public int getTombstonesPurchased() {
        return tombstonesPurchased;
    }

    public void setTombstonesPurchased(int count) {
        this.tombstonesPurchased = count;
    }

    public void incrementTombstonesPurchased() {
        this.tombstonesPurchased++;
    }

    // ==================== Attunement Methods ====================

    private Map<Integer, UndeadMobType> getAttunementMap() {
        if (tombstoneAttunements == null) {
            tombstoneAttunements = new HashMap<>();
        }
        return tombstoneAttunements;
    }

    public UndeadMobType getAttunement(int tombstoneIndex) {
        return getAttunementMap().getOrDefault(tombstoneIndex, UndeadMobType.RANDOM);
    }

    public void setAttunement(int tombstoneIndex, UndeadMobType type) {
        if (type == UndeadMobType.RANDOM) {
            getAttunementMap().remove(tombstoneIndex);
        } else {
            getAttunementMap().put(tombstoneIndex, type);
        }
    }

    public Map<Integer, UndeadMobType> getAllAttunements() {
        return new HashMap<>(getAttunementMap());
    }

    public boolean isAttuned(int tombstoneIndex) {
        return getAttunementMap().containsKey(tombstoneIndex)
                && getAttunementMap().get(tombstoneIndex) != UndeadMobType.RANDOM;
    }
}
