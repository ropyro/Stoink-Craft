package com.stoinkcraft.jobs.jobsites.components.generators;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.components.JobSiteGenerator;
import com.stoinkcraft.jobs.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobs.jobsites.sites.graveyard.GraveyardData;
import com.stoinkcraft.jobs.jobsites.sites.graveyard.UndeadMobType;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.TimeUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class TombstoneGenerator extends JobSiteGenerator {

    private final int index;
    private final Location tombstoneLocation;
    private final World world;

    // Spawn timing
    private int ticksSinceLastSpawn = 0;
    private long lastSpawnTime = 0;
    private boolean readyToSpawn = false;

    // Currently spawned mob (only one per tombstone at a time)
    private UUID spawnedMobId = null;

    // Hologram
    private JobSiteHologram hologram;
    private final String hologramId;
    private static final Vector HOLOGRAM_OFFSET = new Vector(-0.2, 2, 0.5);

    // Base spawn interval (1 tick = 1 second in your system)
    private static final int BASE_SPAWN_INTERVAL_SECONDS = 30; // 30 seconds
    private static final int MIN_SPAWN_INTERVAL_SECONDS = 8;   // 8 seconds minimum

    public TombstoneGenerator(Location tombstoneLocation, JobSite parent, int index) {
        super(parent, false); // Disabled by default until purchased
        this.index = index;
        this.tombstoneLocation = tombstoneLocation.clone();
        this.world = tombstoneLocation.getWorld();

        this.hologramId = parent.getEnterprise().getID() + "_tombstone_" + index;

        this.hologram = new JobSiteHologram(
                parent,
                hologramId,
                tombstoneLocation.clone().subtract(parent.getSpawnPoint()).add(HOLOGRAM_OFFSET).toVector(),
                List.of()
        );

        parent.addComponent(hologram);
    }

    @Override
    public void tick() {
        if (!isEnabled() || TimeUtils.isDay(getParent().getSpawnPoint().getWorld())) {
            updateHologram();
            return;
        }

        // Check if mob is still alive
        if (spawnedMobId != null) {
            Entity entity = Bukkit.getEntity(spawnedMobId);
            if (entity == null || entity.isDead()) {
                spawnedMobId = null;
            }
        }

        // Only spawn if no mob currently exists
        if (spawnedMobId == null) {
            ticksSinceLastSpawn++;

            if (ticksSinceLastSpawn >= getSpawnIntervalTicks()) {
                readyToSpawn = true;
                ticksSinceLastSpawn = 0;
                lastSpawnTime = System.currentTimeMillis();
            }
        }

        if(readyToSpawn && getParent().containsActivePlayer()){
            spawnMob();
        }

        updateHologram();
    }

    @Override
    public void build() {
        super.build();
    }

    @Override
    public void disband() {
        super.disband();
        clearMob();
        hologram.delete();
    }

    // ==================== Spawning ====================

    private void spawnMob() {
        Location spawnLoc = tombstoneLocation.clone().add(0.5, 1, 0.5);

        if (!spawnLoc.getChunk().isLoaded()) {
            spawnLoc.getChunk().load();
        }

        UndeadMobType mobType = getMobTypeToSpawn();
        EntityType entityType = mobType.getEntityType();

        if (entityType == null) return;

        Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () -> {
            LivingEntity entity = (LivingEntity) world.spawnEntity(spawnLoc, entityType);

            entity.setRemoveWhenFarAway(false);
            entity.setPersistent(true);
            entity.setCustomName(ChatColor.RED + mobType.getDisplayName());
            entity.setCustomNameVisible(false);
            entity.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);

            // Mark as graveyard mob for soul drops
            entity.getPersistentDataContainer().set(
                    new NamespacedKey(StoinkCore.getInstance(), "graveyard_mob"),
                    PersistentDataType.STRING,
                    getParent().getEnterprise().getID().toString()
            );

            spawnedMobId = entity.getUniqueId();
            readyToSpawn = false;
        });
    }

    private UndeadMobType getMobTypeToSpawn() {
        GraveyardData data = getGraveyardData();
        UndeadMobType attunement = data.getAttunement(index);

        if (attunement == UndeadMobType.RANDOM) {
            return UndeadMobType.getRandomDefault();
        }

        return attunement;
    }

    public void clearMob() {
        if (spawnedMobId != null) {
            Entity entity = Bukkit.getEntity(spawnedMobId);
            if (entity != null) {
                entity.remove();
            }
            spawnedMobId = null;
        }
    }

    // ==================== Hologram ====================

    private void updateHologram() {
        List<String> lines;

        if (!isEnabled()) {
//            lines = List.of(
//                    ChatColor.GRAY + "💀",
//                    ChatColor.GRAY + "Inactive"
//            );
            lines = List.of();
        }else if(TimeUtils.isDay(getParent().getSpawnPoint().getWorld())){
            lines = List.of(
                    ChatColor.GRAY + "💀",
                    ChatColor.DARK_RED + "Inactive During Daytime"
            );
        }else if (spawnedMobId != null) {
            UndeadMobType type = UndeadMobType.getFromEntityType(Bukkit.getEntity(spawnedMobId).getType());
            lines = List.of(
                    ChatColor.RED + "💀",
                    ChatColor.GRAY + type.getDisplayName()
            );
        } else {
            UndeadMobType type = getAttunement();
            long remainingTicks = getSpawnIntervalTicks() - ticksSinceLastSpawn;
            long remainingSeconds = Math.max(0, remainingTicks);

            lines = List.of(
                    ChatColor.YELLOW + "💀",
                    ChatColor.GRAY + "Attuned to: " + ChatColor.RED + type.getDisplayName(),
                    ChatColor.WHITE + formatTime(remainingSeconds)
            );
            if(readyToSpawn){
                lines = List.of(
                        ChatColor.YELLOW + "💀",
                        ChatColor.GRAY + "Attuned to: " + ChatColor.RED + type.getDisplayName(),
                        ChatColor.WHITE + "Summoning..."
                );
            }
        }

        hologram.setLines(0, lines);
    }

    private String formatTime(long seconds) {
        if (seconds <= 0) return "Spawning...";
        if (seconds < 60) return seconds + "s";
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    // ==================== Timing ====================

    private int getSpawnIntervalTicks() {
        int speedLevel = getGraveyardData().getLevel("spawn_speed");
        // Each level reduces by 2.2 seconds
        int interval = BASE_SPAWN_INTERVAL_SECONDS - (int)(speedLevel * 2.2);
        return Math.max(MIN_SPAWN_INTERVAL_SECONDS, interval);
    }

    // ==================== Attunement ====================

    public UndeadMobType getAttunement() {
        return getGraveyardData().getAttunement(index);
    }

    public boolean setAttunement(UndeadMobType type) {
        GraveyardData data = getGraveyardData();

        // Check level requirement
        if (getParent().getLevel() < type.getRequiredLevel()) {
            return false;
        }

        // Check and spend souls
        if (type.requiresAttunement()) {
            if (!data.spendSouls(type.getSoulCost())) {
                return false;
            }
        }

        data.setAttunement(index, type);
        clearMob(); // Clear current mob so new type spawns
        return true;
    }

    // ==================== Getters ====================

    private GraveyardData getGraveyardData() {
        return (GraveyardData) getParent().getData();
    }

    public int getIndex() {
        return index;
    }

    public Location getTombstoneLocation() {
        return tombstoneLocation.clone();
    }

    public boolean hasMobSpawned() {
        return spawnedMobId != null;
    }

    public JobSiteHologram getHologram() {
        return hologram;
    }
}