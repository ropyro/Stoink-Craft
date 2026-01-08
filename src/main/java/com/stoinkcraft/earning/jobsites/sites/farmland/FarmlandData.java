package com.stoinkcraft.earning.jobsites.sites.farmland;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteData;
import com.stoinkcraft.earning.jobsites.components.generators.CropGenerator;
import com.stoinkcraft.earning.jobsites.components.generators.PassiveMobGenerator;

public class FarmlandData extends JobSiteData {

    @Expose
    private CropGenerator.CropGeneratorType currentType = CropGenerator.CropGeneratorType.WHEAT;
    @Expose
    private PassiveMobGenerator.PassiveMobType currentMobType = PassiveMobGenerator.PassiveMobType.COW;

    public FarmlandData(boolean isBuilt, JobSite parent) {
        super(isBuilt, parent);
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

}
