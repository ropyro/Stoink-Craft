package com.stoinkcraft.jobs.jobsites.components.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobs.jobsites.components.JobSiteStructure;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SchematicUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BeeHiveStructure extends JobSiteStructure {

    private static final File SCHEMATIC =
            new File(StoinkCore.getInstance().getDataFolder(), "/schematics/beehives.schem");

    private String constructionHologram;
    public static Vector constructionHologramOffset = new Vector(-15, 3, -34);

    private JobSiteHologram hologram;

    public BeeHiveStructure(JobSite jobSite) {
        super(
                "beehive",
                "Bee Hives",
                20,
                TimeUnit.SECONDS.toMillis(10),
                () -> 350_000,
                site -> true,
                jobSite
        );

        constructionHologram =  getJobSite().getEnterprise().getID() + "_" + JobSiteType.FARMLAND.name() + "_beehives";
        hologram = getHologram();
        getJobSite().addComponent(hologram);
    }

    @Override
    public void build() {
        super.build();
        if(getJobSite().getData().getStructure(getId()).getState().equals(StructureState.BUILT)){
            hologram.delete();
            JobSite site = getJobSite();
            Location pasteLoc = site.getSpawnPoint();
            SchematicUtils.pasteSchematic(SCHEMATIC, pasteLoc, false);
        }
    }

    @Override
    public void onConstructionStart() {
        List<String> constructionHologramLines = new ArrayList<>();
        constructionHologramLines.add(ChatColor.GOLD + "" + ChatColor.BOLD + "Bee Hives Under Construction");
        constructionHologramLines.add(ChatColor.WHITE + "The Bee hives unlocks bees and honey collection!");
        constructionHologramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
        constructionHologramLines.add(" ");
        constructionHologramLines.add(ChatColor.WHITE + "Time Remaining: " + ChatColor.GREEN + ChatUtils.formatDuration(getBuildTimeMillis()));
        hologram.setHologram(constructionHologramLines);
    }

    @Override
    public void onConstructionTick(long remainingMillis) {
        String timeRemainingLine = ChatColor.WHITE + "Time Remaining: " + ChatColor.GREEN + ChatUtils.formatDuration(remainingMillis);
        hologram.setLine(0, 4, timeRemainingLine);
    }

    @Override
    public void onConstructionComplete() {
        super.onConstructionComplete();
        JobSite site = getJobSite();
        Location pasteLoc = site.getSpawnPoint();
        SchematicUtils.pasteSchematic(SCHEMATIC, pasteLoc, false);

        //reward
        site.getData().incrementXp(1000);
        site.getEnterprise().sendEnterpriseMessage(                "§6§lBee Hive Construction Complete!",
                "",
                "§a+ 5000xp",
                "");

        hologram.delete();
    }

    @Override
    public void disband() {
    }

    @Override
    public void levelUp() {
        super.levelUp();
        List<String> constructionHologramLines = new ArrayList<>();
        if(canUnlock(getJobSite()) && !getJobSite().getData().getStructure(getId()).getState().equals(StructureState.BUILT)){
            constructionHologramLines.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Bee Hives Unlocked!");
            constructionHologramLines.add(ChatColor.WHITE + "Talk to Farmer Joe to start building the Bee hives.");
            constructionHologramLines.add(" ");
            constructionHologramLines.add(ChatColor.WHITE + "The Bee hives unlocks bees and honey collection!");
            constructionHologramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
            hologram.setHologram(constructionHologramLines);
        }
    }

    private JobSiteHologram getHologram(){
        List<String> constructionHologramLines = new ArrayList<>();
        if(canUnlock(getJobSite())){
            constructionHologramLines.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Bee Hives Unlocked!");
            constructionHologramLines.add(ChatColor.WHITE + "Talk to Farmer Joe to start building the Bee hives.");
            constructionHologramLines.add(" ");
            constructionHologramLines.add(ChatColor.WHITE + "The Bee hives unlocks bees and honey collection!");
            constructionHologramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
        }else{
            constructionHologramLines.add(ChatColor.RED + "" + ChatColor.BOLD + "Bee Hives Unlocked at Level: " + getRequiredJobsiteLevel());
            constructionHologramLines.add(ChatColor.WHITE + "The Bee hives unlocks bees and honey collection!");
            constructionHologramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
        }
        hologram = new JobSiteHologram(getJobSite(), constructionHologram, constructionHologramOffset, constructionHologramLines);
        return hologram;
    }
}
