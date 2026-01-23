package com.stoinkcraft.jobsites.sites.components.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.config.StructureConfig;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.components.JobSiteHologram;
import com.stoinkcraft.jobsites.sites.components.JobSiteStructure;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SchematicUtils;
import org.bukkit.ChatColor;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.List;

public class BeeHiveStructure extends JobSiteStructure {

    public static Vector constructionHologramOffset = new Vector(-15, 3, -34);

    private JobSiteHologram hologram;
    private final String hologramId;

    private static StructureConfig config() {
        return ConfigLoader.getStructures();
    }

    public BeeHiveStructure(JobSite jobSite) {
        super(
                "beehive",
                "Bee Hives",
                () -> config().getBeehiveRequiredLevel(),
                () -> config().getBeehiveBuildTimeMillis(),
                () -> config().getBeehiveCost(),
                site -> true,
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
        int completionXp = config().getBeehiveCompletionXp();
        getJobSite().getData().incrementXp(completionXp);
        getJobSite().getEnterprise().sendEnterpriseMessage(
                "§6§lBee Hive Construction Complete!",
                "",
                "§a+ " + completionXp + " XP",
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
        File schematic = new File(StoinkCore.getInstance().getDataFolder(), "/schematics/" + config().getBeehiveSchematic());
        SchematicUtils.pasteSchematic(schematic, getJobSite().getSpawnPoint(), false);
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