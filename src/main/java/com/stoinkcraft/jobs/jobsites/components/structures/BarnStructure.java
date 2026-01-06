package com.stoinkcraft.jobs.jobsites.components.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobs.jobsites.components.JobSiteStructure;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SchematicUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BarnStructure extends JobSiteStructure {

    private static final File SCHEMATIC =
            new File(StoinkCore.getInstance().getDataFolder(), "/schematics/barn.schem");

    private String constructionHologram;
    public static Vector constructionHologramOffset = new Vector(-5, 3, -8);

    private JobSiteHologram hologram;

    public BarnStructure(JobSite jobSite) {
        super(
                "barn",
                "Animal Barn",
                10,
                TimeUnit.SECONDS.toMillis(10),
                () -> 150_000,
                site -> true,
                jobSite
        );

        constructionHologram =  getJobSite().getEnterprise().getID() + "_" + JobSiteType.FARMLAND.name() + "_barn";
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
        List<String> entryHoloGramLines = new ArrayList<>();
        entryHoloGramLines.add(ChatColor.GOLD + "" + ChatColor.BOLD + "Barn Under Construction");
        entryHoloGramLines.add(ChatColor.WHITE + "The barn unlocks new animals and more contracts!");
        entryHoloGramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
        entryHoloGramLines.add(" ");
        entryHoloGramLines.add(ChatColor.WHITE + "Time Remaining: " + ChatColor.GREEN + ChatUtils.formatDuration(getBuildTimeMillis()));
        hologram.setHologram(entryHoloGramLines);
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
        SchematicUtils.pasteSchematic(SCHEMATIC, pasteLoc, true);

        //reward
        site.getData().incrementXp(1000);
        site.getEnterprise().sendEnterpriseMessage(                "§6§lBarn Construction Complete!",
                "",
                "§a+ 1000xp",
        "");

        hologram.delete();
    }

    @Override
    public void disband() {
    }

    @Override
    public void levelUp() {
        super.levelUp();
        List<String> entryHoloGramLines = new ArrayList<>();
        if(canUnlock(getJobSite()) && !getJobSite().getData().getStructure(getId()).getState().equals(StructureState.BUILT)){
            entryHoloGramLines.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Barn Unlocked!");
            entryHoloGramLines.add(ChatColor.WHITE + "Talk to Farmer Joe to start building the barn.");
            entryHoloGramLines.add(" ");
            entryHoloGramLines.add(ChatColor.WHITE + "The barn unlocks new animals and more contracts!");
            entryHoloGramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
            hologram.setHologram(entryHoloGramLines);
        }
    }

    private JobSiteHologram getHologram(){
        List<String> entryHoloGramLines = new ArrayList<>();
        if(canUnlock(getJobSite())){
            entryHoloGramLines.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Barn Unlocked!");
            entryHoloGramLines.add(ChatColor.WHITE + "Talk to Farmer Joe to start building the barn.");
            entryHoloGramLines.add(" ");
            entryHoloGramLines.add(ChatColor.WHITE + "The barn unlocks new animals and more contracts!");
            entryHoloGramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
        }else{
            entryHoloGramLines.add(ChatColor.RED + "" + ChatColor.BOLD + "Barn Unlocked at Level: " + getRequiredJobsiteLevel());
            entryHoloGramLines.add(ChatColor.WHITE + "The barn unlocks new animals and more contracts!");
            entryHoloGramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
        }
        hologram = new JobSiteHologram(getJobSite(), constructionHologram, constructionHologramOffset, entryHoloGramLines);
        return hologram;
    }
}