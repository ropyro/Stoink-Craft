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

public class PowerCellStructure extends JobSiteStructure {

    private static final File SCHEMATIC =
            new File(StoinkCore.getInstance().getDataFolder(), "/schematics/powercell.schem");

    private String constructionHologram;
    public static Vector constructionHologramOffset = new Vector(-27.5, 3, -0.5);
    private JobSiteHologram hologram;

    public PowerCellStructure(JobSite jobSite){
        super(
                "powercell",
                "Power Cell",
                10,
                TimeUnit.SECONDS.toMillis(10),
                () -> 125000,
                site -> true,
                jobSite
        );

        constructionHologram =  getJobSite().getEnterprise().getID() + "_" + JobSiteType.QUARRY.name() + "_" + getId();
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
        constructionHologramLines.add(ChatColor.GOLD + "" + ChatColor.BOLD + "Power Cell Under Construction");
        constructionHologramLines.add(ChatColor.WHITE + "The power cell unlocks potion effect buffs!");
        constructionHologramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
        constructionHologramLines.add(" ");
        constructionHologramLines.add(ChatColor.WHITE + "Time Remaining: " + ChatColor.GREEN + ChatUtils.formatDuration(getBuildTimeMillis()));
        hologram.setLines(0, constructionHologramLines);
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
        site.getEnterprise().sendEnterpriseMessage(                "§6§lPower Cell Construction Complete!",
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
        List<String> constructionHologramLines = new ArrayList<>();
        if(canUnlock(getJobSite()) && !getJobSite().getData().getStructure(getId()).getState().equals(StructureState.BUILT)){
            constructionHologramLines.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Power Cell Unlocked!");
            constructionHologramLines.add(ChatColor.WHITE + "Talk to Miner Bob to start building the Power Cell.");
            constructionHologramLines.add(" ");
            constructionHologramLines.add(ChatColor.WHITE + "The power cell unlocks potion effect buffs!");
            constructionHologramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
            hologram.setLines(0, constructionHologramLines);
        }
    }

    private JobSiteHologram getHologram(){
        List<String> constructionHologramLines = new ArrayList<>();
        if(canUnlock(getJobSite())){
            constructionHologramLines.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Power Cell Unlocked!");
            constructionHologramLines.add(ChatColor.WHITE + "Talk to Miner Bob to start building the Power Cell.");
            constructionHologramLines.add(" ");
            constructionHologramLines.add(ChatColor.WHITE + "The power cell unlocks potion effect buffs!");
            constructionHologramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
        }else{
            constructionHologramLines.add(ChatColor.RED + "" + ChatColor.BOLD + "Power Cell Unlocked at Level: " + getRequiredJobsiteLevel());
            constructionHologramLines.add(ChatColor.WHITE + "The power cell unlocks potion effect buffs!");
            constructionHologramLines.add(ChatColor.WHITE + "It also gives a considerable amount of xp when built.");
        }
        hologram = new JobSiteHologram(getJobSite(), constructionHologram, constructionHologramOffset, constructionHologramLines);
        return hologram;
    }
}
