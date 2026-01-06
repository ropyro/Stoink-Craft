package com.stoinkcraft.jobs.jobsites.sites.graveyard;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteData;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobs.jobsites.sites.skyrise.SkyriseData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GraveyardSite extends JobSite {

    private JobSiteHologram entryHologram;

    public GraveyardSite(Enterprise enterprise, Location spawnPoint, GraveyardData data) {
        super(enterprise, JobSiteType.GRAVEYARD, spawnPoint,
                new File(StoinkCore.getInstance().getDataFolder(), "/schematics/graveyard.schem"),
                data, data.isBuilt());

        String entryHologramName = enterprise.getID() + "_" + JobSiteType.GRAVEYARD.name() + "_entryway";
        entryHologram = new JobSiteHologram(this, entryHologramName, new Vector(-3.5, 3, 0.5), getEntryHoloLines());
    }

    @Override
    public void build() {
        super.build();
    }

    @Override
    public void tick() {
        super.tick();
    }

    private List<String> getEntryHoloLines(){
        List<String> entryHoloGramLines = new ArrayList<>();
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + enterprise.getName() + "'s");
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Graveyard");
        return entryHoloGramLines;
    }

    @Override
    public GraveyardData getData() {
        return (GraveyardData) super.getData();
    }
}
