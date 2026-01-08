package com.stoinkcraft.earning.jobsites.components.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.earning.jobsites.components.JobSiteHologram;
import com.stoinkcraft.earning.jobsites.components.JobSiteStructure;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SchematicUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PowerCellStructure extends JobSiteStructure {

    private static final File SCHEMATIC =
            new File(StoinkCore.getInstance().getDataFolder(), "/schematics/powercell.schem");

    public static final Vector HOLOGRAM_OFFSET = new Vector(-25.5, 3, -0.5);

    private JobSiteHologram hologram;
    private final String hologramId;

    // Haste effect settings
    private static final int EFFECT_CHECK_INTERVAL = 3; // ticks (1 second)
    private int tickCounter = 0;

    public static final int REQUIRED_LEVEL = 10;
    public static final int COST = 75_000;
    public static final long BUILD_TIME = TimeUnit.MINUTES.toMillis(20); // 20 minutes
    public static final int COMPLETION_XP = 750;

    public PowerCellStructure(JobSite jobSite) {
        super(
                "powercell",
                "Power Cell",
                REQUIRED_LEVEL,
                BUILD_TIME,
                () -> COST,
                site -> true,
                jobSite
        );

        this.hologramId = getJobSite().getEnterprise().getID() + "_" +
                JobSiteType.QUARRY.name() + "_powercell";
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
    public void tick() {
        super.tick();

        // Apply haste effect to players in the quarry
        if (isUnlocked()) {
            tickCounter++;
            if (tickCounter >= EFFECT_CHECK_INTERVAL) {
                tickCounter = 0;
                applyHasteToPlayers();
            }
        }
    }

    private void applyHasteToPlayers() {
        int powerLevel = getPowerLevel();
        if (powerLevel <= 0) return;

        int hasteAmplifier = powerLevel - 1; // Haste I, II, or III (0-indexed)
        int durationTicks = EFFECT_CHECK_INTERVAL*20 + 20; // Slightly longer than check interval

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getJobSite().contains(player.getLocation())) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.HASTE,
                        durationTicks,
                        hasteAmplifier,
                        true,  // ambient
                        false, // no particles
                        true   // show icon
                ));
            }
        }
    }

    /**
     * @return Power level 1-3 based on upgrade, or 0 if not upgraded
     */
    public int getPowerLevel() {
        return getJobSite().getData().getLevel("power_level");
    }

    @Override
    public void onUnlockStart() {
        hologram.setLines(0, List.of(
                ChatColor.GOLD + "" + ChatColor.BOLD + "Power Cell Under Construction",
                ChatColor.WHITE + "The Power Cell provides Haste",
                ChatColor.WHITE + "to all miners in the quarry!",
                " ",
                ChatColor.WHITE + "Time Remaining: " + ChatColor.GREEN +
                        ChatUtils.formatDuration(getBuildTimeMillis())
        ));
    }

    @Override
    public void onUnlockTick(long remainingMillis) {
        hologram.setLine(0, 4,
                ChatColor.WHITE + "Time Remaining: " + ChatColor.GREEN +
                        ChatUtils.formatDuration(remainingMillis));
    }

    @Override
    public void onUnlockComplete() {
        pasteStructure();

        getJobSite().getData().incrementXp(COMPLETION_XP);
        getJobSite().getEnterprise().sendEnterpriseMessage(
                "§6§lPower Cell Construction Complete!",
                "",
                "§a+ " + COMPLETION_XP + " XP",
                "§eMiners now receive Haste in the quarry!",
                "§7Upgrade the Power Cell for stronger effects."
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
            hologram.setLines(0, List.of(
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Power Cell Unlocked!",
                    ChatColor.WHITE + "Talk to Miner Bob to start building.",
                    " ",
                    ChatColor.WHITE + "The Power Cell provides Haste",
                    ChatColor.WHITE + "to all miners in the quarry!"
            ));
        }
    }

    private JobSiteHologram createHologram() {
        List<String> lines;
        if (canUnlock()) {
            lines = List.of(
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Power Cell Unlocked!",
                    ChatColor.WHITE + "Talk to Miner Bob to start building.",
                    " ",
                    ChatColor.WHITE + "The Power Cell provides Haste",
                    ChatColor.WHITE + "to all miners in the quarry!"
            );
        } else {
            lines = List.of(
                    ChatColor.RED + "" + ChatColor.BOLD + "Power Cell Unlocks at Level: " +
                            getRequiredJobsiteLevel(),
                    ChatColor.WHITE + "The Power Cell provides Haste",
                    ChatColor.WHITE + "to all miners in the quarry!"
            );
        }
        return new JobSiteHologram(getJobSite(), hologramId, HOLOGRAM_OFFSET, lines);
    }
}