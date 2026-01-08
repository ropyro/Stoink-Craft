package com.stoinkcraft.earning.jobsites.components.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.earning.jobsites.components.JobSiteHologram;
import com.stoinkcraft.earning.jobsites.components.JobSiteStructure;
import com.stoinkcraft.earning.jobsites.sites.graveyard.GraveyardData;
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

    public static final int REQUIRED_LEVEL = 15;
    public static final int COST = 175_000;
    public static final long BUILD_TIME_MILLIS = TimeUnit.MINUTES.toMillis(30); // 30 minutes
    public static final int COMPLETION_XP = 1500;

    public static Vector constructionHologramOffset = new Vector(-35, 4, 23.3); // Adjust based on schematic

    private JobSiteHologram hologram;
    private final String hologramId;

    // Horde spawning
    private Location hordeSpawnLocation;
    private int ticksSinceLastHorde = 0;
    private final Set<UUID> hordeSpiders = new HashSet<>();
    private int cachedSpiderCount = -1;
    private boolean readyToSpawn = false;

    // Base values
    // Horde timing
    private static final int BASE_HORDE_INTERVAL_SECONDS = 60 * 10; // 10 minutes base
    private static final int MIN_HORDE_INTERVAL_SECONDS = 60 * 3;   // 3 minutes minimum
    private static final int BASE_HORDE_SIZE = 6;
    private static final int SPIDERS_PER_UPGRADE = 2;
    private static final int MAX_HORDE_SIZE = 30;

    // Rewards per spider
    private static final int XP_PER_SPIDER = 12;
    private static final int MONEY_PER_SPIDER = 40;

    public MausoleumStructure(JobSite jobSite, Vector hordeSpawnOffset) {
        super(
                "mausoleum",
                "Mausoleum",
                REQUIRED_LEVEL,
                BUILD_TIME_MILLIS,
                () -> COST,
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
            ticksSinceLastHorde = 0;
            if(!readyToSpawn)
                getJobSite().getEnterprise().sendEnterpriseMessage(
                        "§5§l🕷 Spider Horde Incoming!",
                        "§7" + getHordeSize() + " spiders have emerged from the Mausoleum!"
                );

            readyToSpawn = true;
        }

        if(readyToSpawn && getJobSite().containsActivePlayer()){
            spawnHorde();
            readyToSpawn = false;
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

        getJobSite().getData().incrementXp(COMPLETION_XP);
        getJobSite().getEnterprise().sendEnterpriseMessage(
                "§5§lMausoleum Construction Complete!",
                "",
                "§a+ " + COMPLETION_XP + " XP",
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
            cachedSpiderCount = -1;
        }
    }

    // ==================== Timing & Sizing ====================

    private int getHordeIntervalTicks() {
        int speedLevel = getGraveyardData().getLevel("mausoleum_spawn_speed");
        // Each level reduces by 42 seconds (0.7 minutes)
        int interval = BASE_HORDE_INTERVAL_SECONDS - (speedLevel * 42);
        return Math.max(MIN_HORDE_INTERVAL_SECONDS, interval);
    }


    private int getHordeSize() {
        int sizeLevel = getGraveyardData().getLevel("mausoleum_horde_size");
        int size = BASE_HORDE_SIZE + (sizeLevel * SPIDERS_PER_UPGRADE);
        return Math.min(MAX_HORDE_SIZE, size);
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
                    ChatColor.WHITE + "Spiders Remaining: " + ChatColor.RED + getActiveSpiderCount()
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
        cachedSpiderCount = hordeSpiders.size();
        return hordeSpiders.size();
    }

    public int getCachedSpiderCount(){
        if(cachedSpiderCount == -1){
            cachedSpiderCount = getHordeSize();
        }
        return cachedSpiderCount;
    }

    public boolean isHordeActive() {
        return !hordeSpiders.isEmpty();
    }
}
