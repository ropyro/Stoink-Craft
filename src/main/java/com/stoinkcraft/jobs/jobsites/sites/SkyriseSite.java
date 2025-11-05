package com.stoinkcraft.jobs.jobsites.sites;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import eu.decentsoftware.holograms.api.DHAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SkyriseSite extends JobSite {

    private String entryHologramName;

    public SkyriseSite(Enterprise enterprise, Location spawnPoint, boolean isbuilt) {
        super(enterprise, JobSiteType.SKYRISE, spawnPoint, new File(StoinkCore.getInstance().getDataFolder(), "/schematics/building.schem"), isbuilt);
        entryHologramName = enterprise.getID() + "_" + JobSiteType.SKYRISE.name() + "_" + "entryway";
    }

    @Override
    public void initializeJobs() {}

    @Override
    public void initializeBuild() {
        //create entryway holo
        initializeEntryHologram();
    }

    @Override
    public void disband() {
        super.disband();

        //remove entry way holo
        try{
            if(DHAPI.getHologram(entryHologramName) != null)
                DHAPI.getHologram(entryHologramName).delete();
        }catch (IllegalArgumentException e){}
    }


    public void initializeEntryHologram(){
        List<String> entryHoloGramLines = new ArrayList<>();
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + enterprise.getName() + "'s");
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Skyrise Building");
        initializeHologram(entryHologramName, entryHoloGramLines, spawnPoint.clone().add(-5.5, 4, 0.5));
    }
}
