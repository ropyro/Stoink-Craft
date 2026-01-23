package com.stoinkcraft.jobsites.sites.components.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.config.StructureConfig;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.components.JobSiteHologram;
import com.stoinkcraft.jobsites.sites.components.JobSiteStructure;
import com.stoinkcraft.jobsites.sites.sites.graveyard.GraveyardData;
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

public class MausoleumStructure extends JobSiteStructure {

    public static Vector constructionHologramOffset = new Vector(-35, 4, 23.3); // Adjust based on schematic

    private JobSiteHologram hologram;
    private final String hologramId;

    // Horde spawning
    private Location hordeSpawnLocation;
    private int ticksSinceLastHorde = 0;
    private final Set<UUID> hordeSpiders = new HashSet<>();
    private int cachedSpiderCount = -1;
    private boolean readyToSpawn = false;

    private static StructureConfig config() {
        return ConfigLoader.getStructures();
    }

    public MausoleumStructure(JobSite jobSite, Vector hordeSpawnOffset) {
        super(
                "mausoleum",
                "Mausoleum",
                () -> config().getMausoleumRequiredLevel(),
                () -> config().getMausoleumBuildTimeMillis(),
                () -> config().getMausoleumCost(),
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

        if (!isUnlocked() || !hordeSpiders.isEmpty()){
            updateHologram();
            return;
        }

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

        int completionXp = config().getMausoleumCompletionXp();
        getJobSite().getData().incrementXp(completionXp);
        getJobSite().getEnterprise().sendEnterpriseMessage(
                "§5§lMausoleum Construction Complete!",
                "",
                "§a+ " + completionXp + " XP",
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
        int xpPerSpider = config().getMausoleumXpPerSpider();
        int moneyPerSpider = config().getMausoleumMoneyPerSpider();

        // Reward XP
        getJobSite().getData().incrementXp(xpPerSpider);

        // Reward money
        StoinkCore.getEconomy().depositPlayer(killer, moneyPerSpider);
        killer.sendMessage("§a+$" + moneyPerSpider + " +" + xpPerSpider + " Graveyard XP" + " §2🕷 Horde Spider");

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
        int baseInterval = config().getMausoleumBaseHordeIntervalSeconds();
        int reductionPerLevel = config().getMausoleumHordeIntervalReductionPerLevel();
        int minInterval = config().getMausoleumMinHordeIntervalSeconds();
        int interval = baseInterval - (speedLevel * reductionPerLevel);
        return Math.max(minInterval, interval);
    }


    private int getHordeSize() {
        int sizeLevel = getGraveyardData().getLevel("mausoleum_horde_size");
        int baseSize = config().getMausoleumBaseHordeSize();
        int spidersPerUpgrade = config().getMausoleumSpidersPerUpgrade();
        int maxSize = config().getMausoleumMaxHordeSize();
        int size = baseSize + (sizeLevel * spidersPerUpgrade);
        return Math.min(maxSize, size);
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
                    ChatColor.RED + "" + ChatColor.BOLD + "Mausoleum Unlocked at Level: " + config().getMausoleumRequiredLevel(),
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
        File schematic = new File(StoinkCore.getInstance().getDataFolder(), "/schematics/" + config().getMausoleumSchematic());
        SchematicUtils.pasteSchematic(schematic, getJobSite().getSpawnPoint(), false);
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
