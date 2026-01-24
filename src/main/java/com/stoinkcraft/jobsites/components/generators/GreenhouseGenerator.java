package com.stoinkcraft.jobsites.components.generators;

import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.farmland.FarmlandData;
import com.stoinkcraft.jobsites.sites.farmland.FarmlandSite;
import org.bukkit.Location;

/**
 * A greenhouse generator that functions like a CropGenerator but has its own
 * individual upgrades and crop type selection. Each farmland has 3 greenhouses,
 * each unlocked at different levels.
 */
public class GreenhouseGenerator extends CropGenerator {

    private final int greenhouseIndex;

    public GreenhouseGenerator(Location corner1, Location corner2, JobSite parent, String regionName, int greenhouseIndex) {
        super(corner1, corner2, parent, regionName);
        this.greenhouseIndex = greenhouseIndex;
    }

    public int getGreenhouseIndex() {
        return greenhouseIndex;
    }

    /**
     * Returns the upgrade key for this greenhouse's growth speed.
     * e.g., "greenhouse_1_growth_speed", "greenhouse_2_growth_speed", etc.
     */
    public String getGrowthSpeedUpgradeKey() {
        return "greenhouse_" + greenhouseIndex + "_growth_speed";
    }

    /**
     * Returns the upgrade key for unlocking this greenhouse.
     * Greenhouse 1 is always unlocked, so this is mainly for 2 and 3.
     */
    public String getUnlockUpgradeKey() {
        return "unlock_greenhouse_" + greenhouseIndex;
    }

    /**
     * Checks if this greenhouse is unlocked.
     * Greenhouse 1 is always unlocked when the farmland is built.
     */
    public boolean isUnlocked() {
        if (greenhouseIndex == 1) {
            return true; // First greenhouse is always available
        }
        return getFarmlandData().getLevel(getUnlockUpgradeKey()) > 0;
    }

    @Override
    public void tick() {
        if (!isUnlocked()) {
            return; // Don't tick if not unlocked
        }
        super.tick();
    }

    @Override
    public void build() {
        if (!isUnlocked()) {
            return; // Don't build crops if not unlocked
        }
        super.build();
    }

    private FarmlandData getFarmlandData() {
        return ((FarmlandSite) getParent()).getData();
    }

    /**
     * Gets the growth speed level for this specific greenhouse.
     */
    @Override
    protected int getGrowthSpeedLevel() {
        return getFarmlandData().getLevel(getGrowthSpeedUpgradeKey());
    }

    /**
     * Gets the crop type for this specific greenhouse.
     */
    @Override
    protected CropGeneratorType getCropType() {
        return getFarmlandData().getGreenhouseCropType(greenhouseIndex);
    }

    /**
     * Sets the crop type for this specific greenhouse.
     */
    @Override
    public void setCropType(CropGeneratorType type) {
        getFarmlandData().setGreenhouseCropType(greenhouseIndex, type);
        if(isUnlocked())
            regenerateCrops();
    }

    /**
     * Rebuilds the greenhouse if it becomes unlocked.
     */
    public void onUnlock() {
        regenerateCrops();
    }
}
