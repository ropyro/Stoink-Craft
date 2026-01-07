package com.stoinkcraft.jobs.jobsites.components.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobs.jobsites.components.JobSiteStructure;
import com.stoinkcraft.jobs.jobsites.sites.graveyard.GraveyardData;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SchematicUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MausoleumStructure extends JobSiteStructure {

    private static final File SCHEMATIC =
            new File(StoinkCore.getInstance().getDataFolder(), "/schematics/mausoleum.schem");

    public static final int REQUIRED_LEVEL = 10;
    public static final long BUILD_TIME_MILLIS = TimeUnit.SECONDS.toMillis(10);

    public static Vector constructionHologramOffset = new Vector(-35, 4, 23.3); // Adjust based on schematic

    private JobSiteHologram hologram;
    private final String hologramId;

    // Horde spawning
    private Location hordeSpawnLocation;
    private int ticksSinceLastHorde = 0;
    private final Set<UUID> hordeSpiders = new HashSet<>();

    // Base values
    private static final int BASE_HORDE_INTERVAL_TICKS = 60*5; // 20 minutes
    private static final int MIN_HORDE_INTERVAL_TICKS = 60*1;   // 5 minutes
    private static final int BASE_HORDE_SIZE = 5;
    private static final int SPIDERS_PER_UPGRADE = 3;

    // Rewards per spider
    private static final int XP_PER_SPIDER = 15;
    private static final int MONEY_PER_SPIDER = 50;

    public MausoleumStructure(JobSite jobSite, Vector hordeSpawnOffset) {
        super(
                "mausoleum",
                "Mausoleum",
                REQUIRED_LEVEL,
                BUILD_TIME_MILLIS,
                () -> 500_000,
                site -> true,
                jobSite
        );

        this.hordeSpawnLocation = jobSite.getSpawnPoint().clone().add(hordeSpawnOffset);
        this.hologramId = jobSite.getEnterprise().getID() + "_" + JobSiteType.GRAVEYARD.name() + "_mausoleum";
        this.hologram = createHologram();
        jobSite.addComponent(hologram);
    }

    @Override
    public void tick() {
        super.tick(); // Handles unlock progress

        if (!isUnlocked()) return;

        // Clean up dead spiders
        cleanupDeadSpiders();

        // Spawn horde on timer
        ticksSinceLastHorde++;
        if (ticksSinceLastHorde >= getHordeIntervalTicks() && hordeSpiders.isEmpty()) {
            spawnHorde();
            ticksSinceLastHorde = 0;
        }

        updateHologram();
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
    public void disband() {
        super.disband();
        clearHorde();
        hologram.delete();
    }

    // ==================== Unlock Callbacks ====================

    @Override
    public void onUnlockStart() {
        hologram.setHologram(List.of(
                ChatColor.GOLD + "" + ChatColor.BOLD + "Mausoleum Under Construction",
                ChatColor.WHITE + "The Mausoleum spawns spider hordes for rewards!",
                " ",
                ChatColor.WHITE + "Time Remaining: " + ChatColor.GREEN + ChatUtils.formatDurationSeconds(getBuildTimeMillis())
        ));
    }

    @Override
    public void onUnlockTick(long remainingMillis) {
        String timeRemainingLine = ChatColor.WHITE + "Time Remaining: " + ChatColor.GREEN + ChatUtils.formatDuration(remainingMillis);
        hologram.setLine(0, 3, timeRemainingLine);
    }

    @Override
    public void onUnlockComplete() {
        pasteStructure();

        getJobSite().getData().incrementXp(2000);
        getJobSite().getEnterprise().sendEnterpriseMessage(
                "§5§lMausoleum Construction Complete!",
                "",
                "§a+ 2000 XP",
                "§7Spider hordes will now spawn periodically!",
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

    // ==================== Horde Spawning ====================

    private void spawnHorde() {
        int hordeSize = getHordeSize();

        getJobSite().getEnterprise().sendEnterpriseMessage(
                "§5§l🕷 Spider Horde Incoming!",
                "§7" + hordeSize + " spiders have emerged from the Mausoleum!"
        );

        Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () -> {
            for (int i = 0; i < hordeSize; i++) {
                Location spawnLoc = getRandomSpawnLocation();

                Spider spider = (Spider) hordeSpawnLocation.getWorld().spawnEntity(spawnLoc, EntityType.SPIDER);
                spider.setRemoveWhenFarAway(false);
                spider.setPersistent(true);
                spider.setCustomName(ChatColor.DARK_PURPLE + "Crypt Spider");
                spider.setCustomNameVisible(false);

                // Mark as mausoleum spider
                spider.getPersistentDataContainer().set(
                        new NamespacedKey(StoinkCore.getInstance(), "mausoleum_spider"),
                        PersistentDataType.STRING,
                        getJobSite().getEnterprise().getID().toString()
                );

                hordeSpiders.add(spider.getUniqueId());
            }
        });
    }

    public void spawnHorde(boolean override){
        if(override) spawnHorde();
    }

    private Location getRandomSpawnLocation() {
        double offsetX = (Math.random() - 0.5) * 6;
        double offsetZ = (Math.random() - 0.5) * 6;
        return hordeSpawnLocation.clone().add(offsetX, 0, offsetZ);
    }

    private void cleanupDeadSpiders() {
        hordeSpiders.removeIf(uuid -> {
            Entity e = Bukkit.getEntity(uuid);
            return e == null || e.isDead();
        });
    }

    public void clearHorde() {
        for (UUID uuid : new HashSet<>(hordeSpiders)) {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
        }
        hordeSpiders.clear();
    }

    /**
     * Called when a mausoleum spider is killed
     */
    public void onSpiderKilled(Player killer) {
        // Reward XP
        getJobSite().getData().incrementXp(XP_PER_SPIDER);

        // Reward money
        StoinkCore.getEconomy().depositPlayer(killer, MONEY_PER_SPIDER);
        ChatUtils.sendMessage(killer, "§a§l🕷 +$" + MONEY_PER_SPIDER + " +" + XP_PER_SPIDER + "xp");

        // Check if horde is cleared
        cleanupDeadSpiders();
        if (hordeSpiders.isEmpty()) {
            getJobSite().getEnterprise().sendEnterpriseMessage(
                    "§a§l🕷 Horde Cleared!",
                    "§7The spider horde has been defeated!"
            );
        }
    }

    // ==================== Timing & Sizing ====================

    private int getHordeIntervalTicks() {
        int speedLevel = getGraveyardData().getLevel("mausoleum_spawn_speed");
        int interval = BASE_HORDE_INTERVAL_TICKS - (speedLevel * 90); // 1.5 min reduction per level
        return Math.max(MIN_HORDE_INTERVAL_TICKS, interval);
    }

    private int getHordeSize() {
        int sizeLevel = getGraveyardData().getLevel("mausoleum_horde_size");
        return BASE_HORDE_SIZE + (sizeLevel * SPIDERS_PER_UPGRADE);
    }

    // ==================== Hologram ====================

    private void updateHologram() {
        if (!isUnlocked()) return;

        long remainingTicks = getHordeIntervalTicks() - ticksSinceLastHorde;
        long remainingSeconds = Math.max(0, remainingTicks);

        List<String> lines;
        if (!hordeSpiders.isEmpty()) {
            lines = List.of(
                    ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "🕷 Horde Active!",
                    ChatColor.WHITE + "Spiders Remaining: " + ChatColor.RED + hordeSpiders.size()
            );
        } else {
            lines = List.of(
                    ChatColor.GRAY + "🕷 Mausoleum",
                    ChatColor.WHITE + "Next Horde: " + ChatColor.YELLOW + formatTime(remainingSeconds)
            );
        }

        hologram.setLines(0, lines);
    }

    public long getRemainingSeconds(){
        long remainingTicks = getHordeIntervalTicks() - ticksSinceLastHorde;
        long remainingSeconds = Math.max(0, remainingTicks);
        return remainingSeconds;
    }

    private void updateHologramForLevel() {
        if (canUnlock()) {
            hologram.setHologram(List.of(
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Mausoleum Unlocked!",
                    ChatColor.WHITE + "Talk to the Grave Keeper to start building.",
                    " ",
                    ChatColor.WHITE + "The Mausoleum spawns spider hordes for rewards!"
            ));
        }
    }

    private JobSiteHologram createHologram() {
        List<String> lines;
        if (canUnlock()) {
            lines = List.of(
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Mausoleum Unlocked!",
                    ChatColor.WHITE + "Talk to the Grave Keeper to start building.",
                    " ",
                    ChatColor.WHITE + "The Mausoleum spawns spider hordes for rewards!"
            );
        } else {
            lines = List.of(
                    ChatColor.RED + "" + ChatColor.BOLD + "Mausoleum Unlocked at Level: " + REQUIRED_LEVEL,
                    ChatColor.WHITE + "The Mausoleum spawns spider hordes for rewards!"
            );
        }
        return new JobSiteHologram(getJobSite(), hologramId, constructionHologramOffset, lines);
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "Spawning...";
        long minutes = seconds / 60;
        long secs = seconds % 60;
        if (minutes > 0) return minutes + "m " + secs + "s";
        return secs + "s";
    }

    private void pasteStructure() {
        SchematicUtils.pasteSchematic(SCHEMATIC, getJobSite().getSpawnPoint(), false);
    }

    // ==================== Getters ====================

    private GraveyardData getGraveyardData() {
        return (GraveyardData) getJobSite().getData();
    }

    public int getActiveSpiderCount() {
        cleanupDeadSpiders();
        return hordeSpiders.size();
    }

    public boolean isHordeActive() {
        return !hordeSpiders.isEmpty();
    }
}
