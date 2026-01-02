package com.stoinkcraft.jobs.jobsites.sites.farmland;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.jobs.jobsites.JobSiteData;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.CropGenerator;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.PassiveMobGenerator;

public class FarmlandData extends JobSiteData {

    @Expose
    private int farmerJoeNpcId;
    @Expose
    private CropGenerator.CropGeneratorType currentType = CropGenerator.CropGeneratorType.WHEAT;
    @Expose
    private PassiveMobGenerator.PassiveMobType currentMobType = PassiveMobGenerator.PassiveMobType.COW;

    public FarmlandData(boolean isBuilt) {
        super(isBuilt);
    }

    public PassiveMobGenerator.PassiveMobType getCurrentMobType() {
        return currentMobType;
    }

    public void setCurrentMobType(PassiveMobGenerator.PassiveMobType currentMobType) {
        this.currentMobType = currentMobType;
    }

    public int getFarmerJoeNpcId() {
        return farmerJoeNpcId;
    }

    public void setFarmerJoeNpcId(int farmerJoeNpcId) {
        this.farmerJoeNpcId = farmerJoeNpcId;
    }

    public CropGenerator.CropGeneratorType getCurrentCropType() {
        return currentType;
    }

    public void setCurrentCropType(CropGenerator.CropGeneratorType currentType) {
        this.currentType = currentType;
    }

}
