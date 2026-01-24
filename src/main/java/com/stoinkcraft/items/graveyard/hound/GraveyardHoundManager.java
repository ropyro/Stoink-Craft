package com.stoinkcraft.items.graveyard.hound;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.graveyard.GraveyardSite;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
    private static final NamespacedKey EXPIRE_TIMESTAMP_KEY = new NamespacedKey(StoinkCore.getInstance(), "hound_expire_timestamp");

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
        long expireTimestamp; // Absolute time when hound expires

        HoundData(UUID wolfId, UUID ownerId, UUID jobSiteId, Location entranceLocation, long expireTimestamp) {
            this.wolfId = wolfId;
            this.ownerId = ownerId;
            this.jobSiteId = jobSiteId;
            this.entranceLocation = entranceLocation;
            this.expireTimestamp = expireTimestamp;
        }

        long getRemainingSeconds() {
            return Math.max(0, (expireTimestamp - System.currentTimeMillis()) / 1000);
        }

        boolean isExpired() {
            return System.currentTimeMillis() >= expireTimestamp;
        }
    }

    /**
     * Spawns a new Graveyard Hound for the player.
     */
    public static void spawnHound(Player player, JobSite jobSite) {
        Location spawnLoc = player.getLocation();
        World world = spawnLoc.getWorld();

        GraveyardSite graveyard = (GraveyardSite) jobSite;
        Location entranceLoc = graveyard.getSpawnPoint();

        long expireTimestamp = System.currentTimeMillis() + (MAX_ACTIVE_TIME_SECONDS * 1000);

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
            pdc.set(EXPIRE_TIMESTAMP_KEY, PersistentDataType.LONG, expireTimestamp);
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
                expireTimestamp
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

            // FIRST: Check if expired (regardless of entity state)
            if (data.isExpired()) {
                handleExpiredHound(data, iterator);
                continue;
            }

            Entity entity = Bukkit.getEntity(data.wolfId);

            // Wolf doesn't exist in loaded chunks - could be unloaded or dead
            if (entity == null) {
                // Check if chunk is loaded
                if (!isChunkLoaded(data.entranceLocation)) {
                    // Chunk unloaded - keep tracking, will handle when loaded or expired
                    continue;
                }
                // Chunk is loaded but entity is null - wolf is dead/removed
                iterator.remove();
                continue;
            }

            if (entity.isDead() || !(entity instanceof Wolf wolf)) {
                iterator.remove();
                continue;
            }

            // Check if owner is online and in the graveyard
            Player owner = Bukkit.getPlayer(data.ownerId);
            boolean ownerPresent = isOwnerInGraveyard(owner, data.jobSiteId);

            if (ownerPresent) {
                // Active behavior - hunt undead
                wolf.setSitting(false);
                targetNearestUndead(wolf);

                // Ambient particles occasionally
                long remaining = data.getRemainingSeconds();
                if (remaining % 10 == 0) {
                    wolf.getWorld().spawnParticle(
                            Particle.SOUL,
                            wolf.getLocation().add(0, 0.5, 0),
                            3, 0.2, 0.2, 0.2, 0.01
                    );
                }

                // Warning when low on time
                if (remaining == 60 || remaining == 30 || remaining == 10) {
                    ChatUtils.sendMessage(owner,
                            ChatColor.AQUA + "🐺 " + ChatColor.YELLOW + "Your hound has " +
                                    ChatColor.WHITE + remaining + "s" + ChatColor.YELLOW + " remaining!");
                }

            } else {
                // Owner not present - sit at entrance
                wolf.setSitting(true);

                // Teleport to entrance if too far
                if (wolf.getLocation().distanceSquared(data.entranceLocation) > 100) {
                    wolf.teleport(data.entranceLocation);
                }
            }

            updateHoundName(wolf, data);
        }

        if (activeHounds.isEmpty() && tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    /**
     * Handles an expired hound - removes it regardless of owner presence.
     */
    private static void handleExpiredHound(HoundData data, Iterator<Map.Entry<UUID, HoundData>> iterator) {
        Player owner = Bukkit.getPlayer(data.ownerId);
        Entity entity = Bukkit.getEntity(data.wolfId);

        if (entity != null && entity instanceof Wolf wolf) {
            // Wolf exists - despawn with effects
            despawnHound(wolf, owner, "Time's up!");
        } else if (isChunkLoaded(data.entranceLocation)) {
            // Chunk is loaded but wolf is gone - already removed somehow
            // Just notify owner if online
            if (owner != null && owner.isOnline()) {
                ChatUtils.sendMessage(owner,
                        ChatColor.AQUA + "🐺 " + ChatColor.GRAY + "Your Graveyard Hound has faded away...");
            }
        } else {
            // Chunk not loaded - schedule removal for when it loads
            scheduleRemovalOnChunkLoad(data);
        }

        iterator.remove();
    }

    /**
     * Schedules wolf removal when its chunk loads.
     */
    private static void scheduleRemovalOnChunkLoad(HoundData data) {
        // Store in a pending removal set that gets checked on chunk load
        // For simplicity, we'll just let restoreHoundsOnStartup handle it
        // or rely on the PDC timestamp check

        // Alternative: Force load chunk briefly to remove
        Location loc = data.entranceLocation;
        if (loc != null && loc.getWorld() != null) {
            Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () -> {
                Chunk chunk = loc.getChunk();
                boolean wasLoaded = chunk.isLoaded();

                if (!wasLoaded) {
                    chunk.load();
                }

                // Find and remove the wolf
                for (Entity entity : chunk.getEntities()) {
                    if (entity.getUniqueId().equals(data.wolfId)) {
                        Player owner = Bukkit.getPlayer(data.ownerId);
                        if (entity instanceof Wolf wolf) {
                            despawnHound(wolf, owner, "Time's up!");
                        } else {
                            entity.remove();
                        }
                        break;
                    }
                }

                // Unload if we loaded it
                if (!wasLoaded) {
                    chunk.unload();
                }
            });
        }
    }

    /**
     * Checks if a chunk at a location is loaded.
     */
    private static boolean isChunkLoaded(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        return location.getWorld().isChunkLoaded(
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4
        );
    }

    /**
     * Updates the hound's display name with remaining time.
     */
    private static void updateHoundName(Wolf wolf, HoundData data) {
        long remaining = data.getRemainingSeconds();
        String timeStr = formatTime(remaining);

        Player owner = Bukkit.getPlayer(data.ownerId);
        String ownerName = owner != null ? owner.getName() : "Unknown";

        ChatColor timeColor;
        if (remaining <= 30) {
            timeColor = ChatColor.RED;
        } else if (remaining <= 60) {
            timeColor = ChatColor.YELLOW;
        } else {
            timeColor = ChatColor.GREEN;
        }

        wolf.setCustomName(ChatColor.AQUA + ownerName + "'s Hound");
    }

    /**
     * Formats seconds into a readable time string.
     */
    private static String formatTime(long seconds) {
        if (seconds <= 0) return "0s";
        if (seconds < 60) return seconds + "s";
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    /**
     * Checks if the owner is online and inside the graveyard.
     */
    private static boolean isOwnerInGraveyard(Player owner, UUID enterpriseId) {
        if (owner == null || !owner.isOnline()) {
            return false;
        }

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
        LivingEntity currentTarget = wolf.getTarget();
        if (currentTarget != null && !currentTarget.isDead() && isUndead(currentTarget)) {
            return;
        }

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
                entity instanceof PigZombie;
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
            return data.getRemainingSeconds();
        }

        // Fallback to PDC if not in active map
        PersistentDataContainer pdc = wolf.getPersistentDataContainer();
        Long expireTimestamp = pdc.get(EXPIRE_TIMESTAMP_KEY, PersistentDataType.LONG);
        if (expireTimestamp != null) {
            return Math.max(0, (expireTimestamp - System.currentTimeMillis()) / 1000);
        }

        return 0;
    }

    /**
     * Called on server startup to restore hounds from saved wolves.
     */
    public static void restoreHoundsOnStartup() {
        int restored = 0;
        int removed = 0;

        for (World world : Bukkit.getWorlds()) {
            for (Wolf wolf : world.getEntitiesByClass(Wolf.class)) {
                if (!isGraveyardHound(wolf)) {
                    continue;
                }

                PersistentDataContainer pdc = wolf.getPersistentDataContainer();

                String ownerIdStr = pdc.get(OWNER_KEY, PersistentDataType.STRING);
                String jobSiteIdStr = pdc.get(JOBSITE_KEY, PersistentDataType.STRING);
                Long expireTimestamp = pdc.get(EXPIRE_TIMESTAMP_KEY, PersistentDataType.LONG);

                // Handle legacy data (remaining time instead of timestamp)
                if (expireTimestamp == null) {
                    Long remainingTime = pdc.get(REMAINING_TIME_KEY, PersistentDataType.LONG);
                    if (remainingTime != null && remainingTime > 0) {
                        // Convert to timestamp (assume time continued while offline)
                        expireTimestamp = System.currentTimeMillis() + (remainingTime * 1000);
                        pdc.set(EXPIRE_TIMESTAMP_KEY, PersistentDataType.LONG, expireTimestamp);
                    }
                }

                if (ownerIdStr == null || jobSiteIdStr == null || expireTimestamp == null) {
                    wolf.remove();
                    removed++;
                    continue;
                }

                // Check if already expired
                if (System.currentTimeMillis() >= expireTimestamp) {
                    Player owner = Bukkit.getPlayer(UUID.fromString(ownerIdStr));
                    despawnHound(wolf, owner, "Time expired while offline");
                    removed++;
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
                        expireTimestamp
                ));
                restored++;
            }
        }

        // Start tick task if we found any hounds
        if (!activeHounds.isEmpty()) {
            ensureTickTaskRunning();
        }

        if (restored > 0 || removed > 0) {
            StoinkCore.getInstance().getLogger().info(
                    "Graveyard Hounds: Restored " + restored + ", Removed " + removed + " (expired)");
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
        activeHounds.clear();
    }

    /**
     * Gets count of active hounds for a player.
     */
    public static int getActiveHoundCount(UUID playerId) {
        return (int) activeHounds.values().stream()
                .filter(data -> data.ownerId.equals(playerId))
                .count();
    }

    /**
     * Gets all active hound data (for debugging/admin).
     */
    public static Collection<UUID> getActiveHoundIds() {
        return new HashSet<>(activeHounds.keySet());
    }
}