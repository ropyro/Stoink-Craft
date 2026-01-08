package com.stoinkcraft.jobs.jobsites.components.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobs.jobsites.components.JobSiteStructure;
import com.stoinkcraft.jobs.jobsites.components.unlockable.UnlockableState;
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

    public static Vector constructionHologramOffset = new Vector(-15, 3, -34);

    private JobSiteHologram hologram;
    private final String hologramId;

    public static final int REQUIRED_LEVEL = 20;
    public static final int COST = 150_000;
    public static final long BUILD_TIME = TimeUnit.MINUTES.toMillis(45); // 45 minutes
    public static final int COMPLETION_XP = 2000;
    public BeeHiveStructure(JobSite jobSite) {
        super(
                "beehive",
                "Bee Hives",
                REQUIRED_LEVEL,
                BUILD_TIME,
                () -> COST,
                site -> site.getData().getUnlockableState("barn") == UnlockableState.UNLOCKED,
                jobSite
        );

        this.hologramId = getJobSite().getEnterprise().getID() + "_" + JobSiteType.FARMLAND.name() + "_beehives";
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
                ChatColor.GOLD + "" + ChatColor.BOLD + "Bee Hives Under Construction",
                ChatColor.WHITE + "The Bee hives unlocks bees and honey collection!",
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
                "§6§lBee Hive Construction Complete!",
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
        SchematicUtils.pasteSchematic(SCHEMATIC, getJobSite().getSpawnPoint(), false);
    }

    private void updateHologramForLevel() {
        if (canUnlock()) {
            hologram.setHologram(List.of(
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Bee Hives Unlocked!",
                    ChatColor.WHITE + "Talk to Farmer Joe to start building the Bee hives.",
                    " ",
                    ChatColor.WHITE + "The Bee hives unlocks bees and honey collection!",
                    ChatColor.WHITE + "It also gives a considerable amount of xp when built."
            ));
        }
    }

    private JobSiteHologram createHologram() {
        List<String> lines;
        if (canUnlock()) {
            lines = List.of(
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Bee Hives Unlocked!",
                    ChatColor.WHITE + "Talk to Farmer Joe to start building the Bee hives.",
                    " ",
                    ChatColor.WHITE + "The Bee hives unlocks bees and honey collection!",
                    ChatColor.WHITE + "It also gives a considerable amount of xp when built."
            );
        } else {
            lines = List.of(
                    ChatColor.RED + "" + ChatColor.BOLD + "Bee Hives Unlocked at Level: " + getRequiredJobsiteLevel(),
                    ChatColor.WHITE + "The Bee hives unlocks bees and honey collection!",
                    ChatColor.WHITE + "It also gives a considerable amount of xp when built."
            );
        }
        return new JobSiteHologram(getJobSite(), hologramId, constructionHologramOffset, lines);
    }
}