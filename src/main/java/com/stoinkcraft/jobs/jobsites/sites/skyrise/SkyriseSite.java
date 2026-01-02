package com.stoinkcraft.jobs.jobsites.sites.skyrise;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import eu.decentsoftware.holograms.api.DHAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SkyriseSite extends JobSite {

    private String entryHologramName;
    private SkyriseData data;

    public SkyriseSite(Enterprise enterprise, Location spawnPoint, SkyriseData data) {
        super(enterprise, JobSiteType.SKYRISE, spawnPoint,
                new File(StoinkCore.getInstance().getDataFolder(), "/schematics/building.schem"),
                data, data.isBuilt());
        this.data = data;
        entryHologramName = enterprise.getID() + "_" + JobSiteType.SKYRISE.name() + "_entryway";
    }

    @Override
    public void initializeBuild() {
        initializeEntryHologram();
    }

    @Override
    public void disband() {
        super.disband();
        try {
            if (DHAPI.getHologram(entryHologramName) != null)
                DHAPI.getHologram(entryHologramName).delete();
        } catch (IllegalArgumentException e) {}
    }

    @Override
    public void tick() {

    }

    public void initializeEntryHologram() {
        List<String> entryHoloGramLines = new ArrayList<>();
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + enterprise.getName() + "'s");
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Skyrise Building");
        Location holoLoc = spawnPoint.clone().add(data.getEntryHologramOffset());
        initializeHologram(entryHologramName, entryHoloGramLines, holoLoc);
    }

    // For serialization
    public SkyriseData getData() {
        data.setBuilt(isBuilt);
        return data;
    }
}