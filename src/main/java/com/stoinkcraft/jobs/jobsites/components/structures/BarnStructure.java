package com.stoinkcraft.jobs.jobsites.components.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
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

public class BarnStructure extends JobSiteStructure {

    private static final File SCHEMATIC =
            new File(StoinkCore.getInstance().getDataFolder(), "/schematics/barn.schem");

    public static Vector constructionHologramOffset = new Vector(-5, 3, -8);

    private JobSiteHologram hologram;
    private final String hologramId;

    public static final int REQUIRED_LEVEL = 10;
    public static final int COST = 50_000;
    public static final long BUILD_TIME = TimeUnit.MINUTES.toMillis(15); // 15 minutes
    public static final int COMPLETION_XP = 500;


    public BarnStructure(JobSite jobSite) {
        super(
                "barn",
                "Animal Barn",
                REQUIRED_LEVEL,
                BUILD_TIME,
                () -> COST,
                site -> true,
                jobSite
        );

        this.hologramId = getJobSite().getEnterprise().getID() + "_" + JobSiteType.FARMLAND.name() + "_barn";
        this.hologram = createHologram();
        getJobSite().addComponent(hologram);
    }

    @Override
    public void build() {
        super.build();
        if (isUnlocked()) {
            hologram.delete();
            pasteStructure();
        }
    }

    @Override
    public void onUnlockStart() {
        hologram.setHologram(List.of(
                ChatColor.GOLD + "" + ChatColor.BOLD + "Barn Under Construction",
                ChatColor.WHITE + "The barn unlocks new animals and more contracts!",
                ChatColor.WHITE + "It also gives a considerable amount of xp when built.",
                " ",
                ChatColor.WHITE + "Time Remaining: " + ChatColor.GREEN + ChatUtils.formatDuration(getBuildTimeMillis())
        ));
    }

    @Override
    public void onUnlockTick(long remainingMillis) {
        String timeRemainingLine = ChatColor.WHITE + "Time Remaining: " + ChatColor.GREEN + ChatUtils.formatDuration(remainingMillis);
        hologram.setLine(0, 4, timeRemainingLine);
    }

    @Override
    public void onUnlockComplete() {
        pasteStructure();
        getJobSite().getData().incrementXp(COMPLETION_XP);
        getJobSite().getEnterprise().sendEnterpriseMessage(
                "§6§lBarn Construction Complete!",
                "",
                "§a+ " + COMPLETION_XP + " XP",
                ""
        );
        hologram.delete();
    }

    @Override
    public void levelUp() {
        super.levelUp();
        if (!isUnlocked()) {
            updateHologramForLevel();
        }
    }

    private void pasteStructure() {
        SchematicUtils.pasteSchematic(SCHEMATIC, getJobSite().getSpawnPoint(), true);
    }

    private void updateHologramForLevel() {
        if (canUnlock()) {
            hologram.setHologram(List.of(
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Barn Unlocked!",
                    ChatColor.WHITE + "Talk to Farmer Joe to start building the barn.",
                    " ",
                    ChatColor.WHITE + "The barn unlocks new animals and more contracts!",
                    ChatColor.WHITE + "It also gives a considerable amount of xp when built."
            ));
        }
    }

    private JobSiteHologram createHologram() {
        List<String> lines;
        if (canUnlock()) {
            lines = List.of(
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Barn Unlocked!",
                    ChatColor.WHITE + "Talk to Farmer Joe to start building the barn.",
                    " ",
                    ChatColor.WHITE + "The barn unlocks new animals and more contracts!",
                    ChatColor.WHITE + "It also gives a considerable amount of xp when built."
            );
        } else {
            lines = List.of(
                    ChatColor.RED + "" + ChatColor.BOLD + "Barn Unlocked at Level: " + getRequiredJobsiteLevel(),
                    ChatColor.WHITE + "The barn unlocks new animals and more contracts!",
                    ChatColor.WHITE + "It also gives a considerable amount of xp when built."
            );
        }
        return new JobSiteHologram(getJobSite(), hologramId, constructionHologramOffset, lines);
    }
}