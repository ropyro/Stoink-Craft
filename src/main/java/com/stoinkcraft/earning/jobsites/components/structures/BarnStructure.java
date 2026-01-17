package com.stoinkcraft.earning.jobsites.components.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.config.StructureConfig;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.components.JobSiteHologram;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.earning.jobsites.components.JobSiteStructure;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SchematicUtils;
import org.bukkit.ChatColor;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.List;

public class BarnStructure extends JobSiteStructure {

    public static Vector constructionHologramOffset = new Vector(-5, 3, -8);

    private JobSiteHologram hologram;
    private final String hologramId;

    private static StructureConfig config() {
        return ConfigLoader.getStructures();
    }

    public BarnStructure(JobSite jobSite) {
        super(
                "barn",
                "Animal Barn",
                () -> config().getBarnRequiredLevel(),
                () -> config().getBarnBuildTimeMillis(),
                () -> config().getBarnCost(),
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
        int completionXp = config().getBarnCompletionXp();
        getJobSite().getData().incrementXp(completionXp);
        getJobSite().getEnterprise().sendEnterpriseMessage(
                "§6§lBarn Construction Complete!",
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
        File schematic = new File(StoinkCore.getInstance().getDataFolder(), "/schematics/" + config().getBarnSchematic());
        SchematicUtils.pasteSchematic(schematic, getJobSite().getSpawnPoint(), true);
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