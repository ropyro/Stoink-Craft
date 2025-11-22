package com.stoinkcraft.jobs.jobsites.sites.farmland;

import com.google.gson.annotations.Expose;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.CropGenerator;
import org.bukkit.util.Vector;

public class FarmlandData {

    @Expose
    private boolean isBuilt;

    @Expose
    private int farmerJoeNpcId;


    /**
     * Crop generator data
     */
    @Expose
    private CropGenerator.CropGeneratorType currentType;

    @Expose
    private int cropGrowthSpeedLevel;

    @Expose
    private boolean carrotUnlocked;

    @Expose
    private boolean potatoUnlocked;

    @Expose
    private boolean beetrootUnlocked;


    public FarmlandData(boolean isBuilt,
                        int farmerJoeNpcId,
                        CropGenerator.CropGeneratorType currentType,
                        int cropGrowthSpeedLevel,
                        boolean carrotUnlocked,
                        boolean potatoUnlocked,
                        boolean beetrootUnlocked) {
        this.isBuilt = isBuilt;
        this.farmerJoeNpcId = farmerJoeNpcId;
        this.currentType = currentType;
        this.cropGrowthSpeedLevel = cropGrowthSpeedLevel;
        this.carrotUnlocked = carrotUnlocked;
        this.potatoUnlocked = potatoUnlocked;
        this.beetrootUnlocked = beetrootUnlocked;
    }

    public boolean isBuilt() {
        return isBuilt;
    }

    public void setBuilt(boolean built) {
        isBuilt = built;
    }

    public int getFarmerJoeNpcId() {
        return farmerJoeNpcId;
    }

    public void setFarmerJoeNpcId(int farmerJoeNpcId) {
        this.farmerJoeNpcId = farmerJoeNpcId;
    }

    public CropGenerator.CropGeneratorType getCurrentType() {
        return currentType;
    }

    public void setCurrentType(CropGenerator.CropGeneratorType currentType) {
        this.currentType = currentType;
    }

    public int getCropGrowthSpeedLevel() {
        return cropGrowthSpeedLevel;
    }

    public void setCropGrowthSpeedLevel(int cropGrowthSpeedLevel) {
        this.cropGrowthSpeedLevel = cropGrowthSpeedLevel;
    }

    public boolean isCarrotUnlocked() {
        return carrotUnlocked;
    }

    public void setCarrotUnlocked(boolean carrotUnlocked) {
        this.carrotUnlocked = carrotUnlocked;
    }

    public boolean isPotatoUnlocked() {
        return potatoUnlocked;
    }

    public void setPotatoUnlocked(boolean potatoUnlocked) {
        this.potatoUnlocked = potatoUnlocked;
    }

    public boolean isBeetrootUnlocked() {
        return beetrootUnlocked;
    }

    public void setBeetrootUnlocked(boolean beetrootUnlocked) {
        this.beetrootUnlocked = beetrootUnlocked;
    }
}
