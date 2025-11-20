package com.stoinkcraft.jobs.jobsites.sites;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.data.FarmlandData;
import com.stoinkcraft.jobs.jobsites.data.QuarryData;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.CropGenerator;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.MobGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FarmlandSite extends JobSite {

    private String welcomeHologramName;
    public static Vector welcomeHologramOffset = new Vector(-3.5, 3, 0.5);

    private CropGenerator cropGenerator;

    public static Vector cropGenCorner1Offset = new Vector(-25, 0, -16);
    public static Vector cropGenCorner2Offset = new Vector(-54, 0, -45);

    private MobGenerator mobGenerator;

    private FarmlandData data;

    private String cropRegionID;

    public FarmlandSite(Enterprise enterprise, Location spawnPoint, FarmlandData data) {
        super(enterprise, JobSiteType.FARMLAND, spawnPoint,
                new File(StoinkCore.getInstance().getDataFolder(), "/schematics/farmland.schem"),
                data.isBuilt());
        this.data = data;

        welcomeHologramName = enterprise.getID() + "_" + JobSiteType.FARMLAND.name() + "_welcome";
        cropRegionID = enterprise.getID() + "_" + JobSiteType.FARMLAND.name() + "_crops";

        cropGenerator = new CropGenerator(spawnPoint.clone().add(cropGenCorner1Offset),
                spawnPoint.clone().add(cropGenCorner2Offset), this, CropGenerator.CropGeneratorType.CARROT, cropRegionID);
    }

    @Override
    public void tick() {
        super.tick();
        cropGenerator.tick();
    }

    @Override
    public void initializeJobs() {

    }

    @Override
    public void initializeBuild() {
        List<String> entryHoloGramLines = new ArrayList<>();
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to the Farmland");
        entryHoloGramLines.add(ChatColor.WHITE + "Here you will harvest crops and butcher meat");
        entryHoloGramLines.add(ChatColor.WHITE + "to complete resource collection contracts!");
        entryHoloGramLines.add(ChatColor.WHITE + "Chat with Farmer Joe to upgrade your farmland's");
        entryHoloGramLines.add(ChatColor.WHITE + "crop grow speed and unlock new crops and animals!");
        Location holoLoc = spawnPoint.clone().add(welcomeHologramOffset);
        initializeHologram(welcomeHologramName, entryHoloGramLines, holoLoc);

        cropGenerator.init();
        cropGenerator.regenerateCrops();
    }

    public CropGenerator getCropGenerator() {
        return cropGenerator;
    }

    public FarmlandData getData(){
        data.setBuilt(isBuilt);
        return data;
    }
}
