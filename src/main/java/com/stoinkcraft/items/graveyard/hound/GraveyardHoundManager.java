package com.stoinkcraft.items.graveyard.hound;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.sites.graveyard.GraveyardSite;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages Graveyard Hounds - temporary wolf companions that hunt undead.
 */
public class GraveyardHoundManager {

    // PDC Keys
    private static final NamespacedKey HOUND_KEY = new NamespacedKey(StoinkCore.getInstance(), "graveyard_hound");
    private static final NamespacedKey OWNER_KEY = new NamespacedKey(StoinkCore.getInstance(), "hound_owner");
    private static final NamespacedKey JOBSITE_KEY = new NamespacedKey(StoinkCore.getInstance(), "hound_jobsite");
    private static final NamespacedKey REMAINING_TIME_KEY = new NamespacedKey(StoinkCore.getInstance(), "hound_remaining_time");

    // Configuration
    private static final long MAX_ACTIVE_TIME_SECONDS = 5 * 60; // 5 minutes
    private static final double TARGETING_RANGE = 20.0;
    private static final long TICK_INTERVAL = 20L; // 1 second

    // Active hounds tracking
    private static final Map<UUID, HoundData> activeHounds = new HashMap<>();
    private static BukkitTask tickTask;

    /**
     * Data class to track hound state.
     */
    private static class HoundData {
        final UUID wolfId;
        final UUID ownerId;
        final UUID jobSiteId;
        final Location entranceLocation;
        long remainingSeconds;

        HoundData(UUID wolfId, UUID ownerId, UUID jobSiteId, Location entranceLocation, long remainingSeconds) {
            this.wolfId = wolfId;
            this.ownerId = ownerId;
            this.jobSiteId = jobSiteId;
            this.entranceLocation = entranceLocation;
            this.remainingSeconds = remainingSeconds;
        }
    }

    /**
     * Spawns a new Graveyard Hound for the player.
     */
    public static void spawnHound(Player player, JobSite jobSite) {
        Location spawnLoc = player.getLocation();
        World world = spawnLoc.getWorld();

        GraveyardSite graveyard = (GraveyardSite) jobSite;
        Location entranceLoc = graveyard.getSpawnPoint(); // Entrance/spawn point

        Wolf wolf = world.spawn(spawnLoc, Wolf.class, w -> {
            // Basic setup
            w.setTamed(true);
            w.setOwner(player);
            w.setCustomName(ChatColor.AQUA + player.getName() + "'s Hound");
            w.setCustomNameVisible(true);
            w.setSitting(false);
            w.setRemoveWhenFarAway(false);
            w.setPersistent(true);

            // Visual distinction - collar color
            w.setCollarColor(DyeColor.CYAN);

            // Tag as graveyard hound
            PersistentDataContainer pdc = w.getPersistentDataContainer();
            pdc.set(HOUND_KEY, PersistentDataType.BOOLEAN, true);
            pdc.set(OWNER_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
            pdc.set(JOBSITE_KEY, PersistentDataType.STRING, jobSite.getEnterprise().getID().toString());
            pdc.set(REMAINING_TIME_KEY, PersistentDataType.LONG, MAX_ACTIVE_TIME_SECONDS);
        });

        // Apply glowing effect for visual distinction
        wolf.setGlowing(true);

        // Spawn particles
        world.spawnParticle(Particle.SOUL, spawnLoc, 20, 0.5, 0.5, 0.5, 0.02);
        world.playSound(spawnLoc, Sound.ENTITY_WOLF_BIG_GROWL, 1.0f, 1.2f);

        // Track the hound
        activeHounds.put(wolf.getUniqueId(), new HoundData(
                wolf.getUniqueId(),
                player.getUniqueId(),
                jobSite.getEnterprise().getID(),
                entranceLoc,
                MAX_ACTIVE_TIME_SECONDS
        ));

        // Ensure tick task is running
        ensureTickTaskRunning();
    }

    /**
     * Ensures the tick task is running.
     */
    private static void ensureTickTaskRunning() {
        if (tickTask != null && !tickTask.isCancelled()) {
            return;
        }

        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                tickHounds();
            }
        }.runTaskTimer(StoinkCore.getInstance(), TICK_INTERVAL, TICK_INTERVAL);
    }

    /**
     * Ticks all active hounds - handles AI, timing, and cleanup.
     */
    private static void tickHounds() {
        if (activeHounds.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, HoundData>> iterator = activeHounds.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, HoundData> entry = iterator.next();
            HoundData data = entry.getValue();

            Entity entity = Bukkit.getEntity(data.wolfId);

            // Remove if wolf is dead or despawned
            if (entity == null || entity.isDead() || !(entity instanceof Wolf wolf)) {
                iterator.remove();
                continue;
            }

            // Check if owner is online and in the graveyard
            Player owner = Bukkit.getPlayer(data.ownerId);
            boolean ownerPresent = isOwnerInGraveyard(owner, data.jobSiteId);

            if (ownerPresent) {
                // Decrement active time
                data.remainingSeconds--;

                // Update PDC
                wolf.getPersistentDataContainer().set(
                        REMAINING_TIME_KEY,
                        PersistentDataType.LONG,
                        data.remainingSeconds
                );

                // Check if time expired
                if (data.remainingSeconds <= 0) {
                    despawnHound(wolf, owner, "Time's up!");
                    iterator.remove();
                    continue;
                }

                // Active behavior - hunt undead
                wolf.setSitting(false);
                targetNearestUndead(wolf);

                // Ambient particles occasionally
                if (data.remainingSeconds % 10 == 0) {
                    wolf.getWorld().spawnParticle(
                            Particle.SOUL,
                            wolf.getLocation().add(0, 0.5, 0),
                            3, 0.2, 0.2, 0.2, 0.01
                    );
                }

            } else {
                // Owner not present - sit at entrance
                wolf.setSitting(true);

                // Teleport to entrance if too far
                if (wolf.getLocation().distanceSquared(data.entranceLocation) > 100) { // 10 blocks
                    wolf.teleport(data.entranceLocation);
                }
            }
        }

        // Cancel task if no more hounds
        if (activeHounds.isEmpty() && tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    /**
     * Checks if the owner is online and inside the graveyard.
     */
    private static boolean isOwnerInGraveyard(Player owner, UUID enterpriseId) {
        if (owner == null || !owner.isOnline()) {
            return false;
        }

        // Check if player is in the graveyard jobsite
        JobSite jobSite = StoinkCore.getInstance().getProtectionManager()
                .getJobSiteAt(owner.getLocation(), JobSiteType.GRAVEYARD);

        if (jobSite == null) {
            return false;
        }

        return jobSite.getEnterprise().getID().equals(enterpriseId);
    }

    /**
     * Targets the nearest undead mob within range.
     */
    private static void targetNearestUndead(Wolf wolf) {
        // Don't retarget if already attacking something alive
        LivingEntity currentTarget = wolf.getTarget();
        if (currentTarget != null && !currentTarget.isDead() && isUndead(currentTarget)) {
            return;
        }

        // Find nearest undead
        LivingEntity nearestUndead = null;
        double nearestDistance = TARGETING_RANGE * TARGETING_RANGE;

        for (Entity entity : wolf.getNearbyEntities(TARGETING_RANGE, TARGETING_RANGE, TARGETING_RANGE)) {
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            if (!isUndead(living)) {
                continue;
            }

            if (living.isDead()) {
                continue;
            }

            double distance = wolf.getLocation().distanceSquared(entity.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestUndead = living;
            }
        }

        if (nearestUndead != null) {
            wolf.setTarget(nearestUndead);
        }
    }

    /**
     * Checks if an entity is an undead mob.
     */
    private static boolean isUndead(LivingEntity entity) {
        return entity instanceof Zombie ||
                entity instanceof Skeleton ||
                entity instanceof Stray ||
                entity instanceof Husk ||
                entity instanceof Drowned ||
                entity instanceof Phantom ||
                entity instanceof Wither ||
                entity instanceof WitherSkeleton ||
                entity instanceof Zoglin ||
                entity instanceof ZombieVillager ||
                entity instanceof PigZombie; // Zombified Piglin
    }

    /**
     * Despawns a hound with effects.
     */
    private static void despawnHound(Wolf wolf, Player owner, String reason) {
        Location loc = wolf.getLocation();
        World world = wolf.getWorld();

        // Effects
        world.spawnParticle(Particle.SOUL, loc, 30, 0.5, 0.5, 0.5, 0.05);
        world.playSound(loc, Sound.ENTITY_WOLF_WHINE, 1.0f, 0.8f);

        // Notify owner if online
        if (owner != null && owner.isOnline()) {
            ChatUtils.sendMessage(owner,
                    ChatColor.AQUA + "🐺 " + ChatColor.GRAY + "Your Graveyard Hound fades away... " +
                            ChatColor.DARK_GRAY + "(" + reason + ")");
        }

        // Remove the wolf
        wolf.remove();
    }

    /**
     * Checks if an entity is a Graveyard Hound.
     */
    public static boolean isGraveyardHound(Entity entity) {
        if (!(entity instanceof Wolf wolf)) {
            return false;
        }

        PersistentDataContainer pdc = wolf.getPersistentDataContainer();
        return pdc.has(HOUND_KEY, PersistentDataType.BOOLEAN);
    }

    /**
     * Gets remaining time for a hound in seconds.
     */
    public static long getRemainingTime(Wolf wolf) {
        HoundData data = activeHounds.get(wolf.getUniqueId());
        if (data != null) {
            return data.remainingSeconds;
        }
        return 0;
    }

    /**
     * Called on server startup to restore hounds from saved wolves.
     */
    public static void restoreHoundsOnStartup() {
        for (World world : Bukkit.getWorlds()) {
            for (Wolf wolf : world.getEntitiesByClass(Wolf.class)) {
                if (!isGraveyardHound(wolf)) {
                    continue;
                }

                PersistentDataContainer pdc = wolf.getPersistentDataContainer();

                String ownerIdStr = pdc.get(OWNER_KEY, PersistentDataType.STRING);
                String jobSiteIdStr = pdc.get(JOBSITE_KEY, PersistentDataType.STRING);
                Long remainingTime = pdc.get(REMAINING_TIME_KEY, PersistentDataType.LONG);

                if (ownerIdStr == null || jobSiteIdStr == null || remainingTime == null) {
                    // Invalid data, remove the wolf
                    wolf.remove();
                    continue;
                }

                // If time already expired, remove
                if (remainingTime <= 0) {
                    wolf.remove();
                    continue;
                }

                UUID ownerId = UUID.fromString(ownerIdStr);
                UUID jobSiteId = UUID.fromString(jobSiteIdStr);

                // Get entrance location from graveyard
                Location entranceLoc = wolf.getLocation();
                try {
                    Enterprise enterprise = StoinkCore.getInstance().getEnterpriseManager()
                            .getEnterpriseByID(jobSiteId);
                    if (enterprise != null) {
                        GraveyardSite graveyard = enterprise.getJobSiteManager().getGraveyardSite();
                        if (graveyard != null) {
                            entranceLoc = graveyard.getSpawnPoint();
                        }
                    }
                } catch (Exception ignored) {}

                // Restore tracking
                activeHounds.put(wolf.getUniqueId(), new HoundData(
                        wolf.getUniqueId(),
                        ownerId,
                        jobSiteId,
                        entranceLoc,
                        remainingTime
                ));
            }
        }

        // Start tick task if we found any hounds
        if (!activeHounds.isEmpty()) {
            ensureTickTaskRunning();
            StoinkCore.getInstance().getLogger().info("Restored " + activeHounds.size() + " Graveyard Hounds");
        }
    }

    /**
     * Called on plugin disable to clean up.
     */
    public static void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        // Don't remove wolves - they persist with their PDC data
        activeHounds.clear();
    }
}