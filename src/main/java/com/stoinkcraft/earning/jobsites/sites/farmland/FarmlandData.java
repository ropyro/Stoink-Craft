package com.stoinkcraft.earning.jobsites.sites.farmland;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteData;
import com.stoinkcraft.earning.jobsites.components.generators.CropGenerator;
import com.stoinkcraft.earning.jobsites.components.generators.PassiveMobGenerator;

import java.util.HashMap;
import java.util.Map;

public class FarmlandData extends JobSiteData {

    @Expose
    private CropGenerator.CropGeneratorType currentType = CropGenerator.CropGeneratorType.WHEAT;
    @Expose
    private PassiveMobGenerator.PassiveMobType currentMobType = PassiveMobGenerator.PassiveMobType.COW;

    /**
     * Stores the crop type for each greenhouse by index.
     * Greenhouse 1, 2, and 3 each have their own crop type.
     */
    @Expose
    private Map<Integer, CropGenerator.CropGeneratorType> greenhouseCropTypes = new HashMap<>();

    public FarmlandData(boolean isBuilt, JobSite parent) {
        super(isBuilt, parent);
        // Initialize default crop types for all greenhouses
        greenhouseCropTypes.put(1, CropGenerator.CropGeneratorType.WHEAT);
        greenhouseCropTypes.put(2, CropGenerator.CropGeneratorType.WHEAT);
        greenhouseCropTypes.put(3, CropGenerator.CropGeneratorType.WHEAT);
    }

    public PassiveMobGenerator.PassiveMobType getCurrentMobType() {
        return currentMobType;
    }

    public void setCurrentMobType(PassiveMobGenerator.PassiveMobType currentMobType) {
        this.currentMobType = currentMobType;
    }

    public CropGenerator.CropGeneratorType getCurrentCropType() {
        return currentType;
    }

    public void setCurrentCropType(CropGenerator.CropGeneratorType currentType) {
        this.currentType = currentType;
    }

    /**
     * Gets the crop type for a specific greenhouse.
     * @param greenhouseIndex The greenhouse index (1, 2, or 3)
     * @return The crop type for that greenhouse
     */
    public CropGenerator.CropGeneratorType getGreenhouseCropType(int greenhouseIndex) {
        // Handle null map for legacy data
        if (greenhouseCropTypes == null) {
            greenhouseCropTypes = new HashMap<>();
        }
        return greenhouseCropTypes.getOrDefault(greenhouseIndex, CropGenerator.CropGeneratorType.WHEAT);
    }

    /**
     * Sets the crop type for a specific greenhouse.
     * @param greenhouseIndex The greenhouse index (1, 2, or 3)
     * @param type The crop type to set
     */
    public void setGreenhouseCropType(int greenhouseIndex, CropGenerator.CropGeneratorType type) {
        // Handle null map for legacy data
        if (greenhouseCropTypes == null) {
            greenhouseCropTypes = new HashMap<>();
        }
        greenhouseCropTypes.put(greenhouseIndex, type);
    }

}
