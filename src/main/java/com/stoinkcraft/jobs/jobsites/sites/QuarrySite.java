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

public class QuarrySite extends JobSite {

    private String welcomeHologramName;

    public QuarrySite(Enterprise enterprise, Location spawnPoint, boolean isbuilt) {
        super(enterprise, JobSiteType.SKYRISE, spawnPoint, new File(StoinkCore.getInstance().getDataFolder(), "/schematics/quarry.schem"), isbuilt);
        welcomeHologramName = enterprise.getID() + "_" + JobSiteType.SKYRISE.name() + "_" + "welcome";
    }

    @Override
    public void initializeJobs() {}

    @Override
    public void initializeBuild() {
        //create entryway holo
        List<String> entryHoloGramLines = new ArrayList<>();
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to the Quarry");
        entryHoloGramLines.add(ChatColor.WHITE + "Here you will mine ores and stones");
        entryHoloGramLines.add(ChatColor.WHITE + "to complete resource collection contracts!");
        entryHoloGramLines.add(ChatColor.WHITE + "Chat with Miner Joe to upgrade your quarry's");
        entryHoloGramLines.add(ChatColor.WHITE + "regeneration speed and unlock new ores!");
        initializeHologram(welcomeHologramName, entryHoloGramLines, spawnPoint.clone().add(-3.5, 3d, 0.5));
    }

    @Override
    public void disband() {
        super.disband();

        //remove holograms
        try{
            if(DHAPI.getHologram(welcomeHologramName) != null)
                DHAPI.getHologram(welcomeHologramName).delete();
        }catch (IllegalArgumentException e){}
    }
}
