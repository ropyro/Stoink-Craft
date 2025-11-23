package com.stoinkcraft.jobs.jobsites.resourcegenerators.generators;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.ResourceGenerator;
import com.stoinkcraft.jobs.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.utils.RegionUtils;
import com.stoinkcraft.utils.TimeUtils;
import org.bukkit.*;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.*;
import org.bukkit.Location;
import org.bukkit.World;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;

import java.util.*;
import java.util.stream.Collectors;

public class PassiveMobGenerator extends ResourceGenerator {

    private final Location corner1;
    private final Location corner2;
    private final World bukkitWorld;
    private CuboidRegion cuboidRegion;
    private String regionName;

    // Tracking spawned mobs
    private final Set<UUID> spawnedMobs = new HashSet<>();

    // Spawn timing
    private int ticksSinceLastSpawn = 0;
    private static final int BASE_SPAWN_INTERVAL = 3; // Base 5 seconds (100 ticks)

    // Upgrade system
    private static final int MAX_SPAWN_SPEED_LEVEL = 10;
    private static final int MAX_MOB_CAPACITY_LEVEL = 10;
    private static final int BASE_MAX_MOBS = 500;
    private static final int MOBS_PER_CAPACITY_LEVEL = 3;

    public PassiveMobGenerator(Location corner1, Location corner2, JobSite parent, String regionName) {
        super(parent);
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.bukkitWorld = corner1.getWorld();
        this.cuboidRegion = getRegion(corner1, corner2);
        this.regionName = regionName;
    }

    @Override
    protected void onTick() {
        // Clean up dead/despawned mobs
        cleanupDeadMobs();

        // Only spawn during daytime
        if (!TimeUtils.isDay(getParent().getSpawnPoint().getWorld())) {
            return;
        }

        ticksSinceLastSpawn++;

        // Calculate spawn interval based on upgrade level
        int spawnInterval = calculateSpawnInterval();

        // Check if it's time to spawn and we haven't reached capacity
        if (ticksSinceLastSpawn >= spawnInterval && canSpawnMore()) {
            spawnMob();
            ticksSinceLastSpawn = 0;
        }
    }

    @Override
    public void init() {
        Map<StateFlag, StateFlag.State> flags = new HashMap<>();
        flags.put(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        flags.put(Flags.INTERACT, StateFlag.State.ALLOW);
        flags.put(Flags.USE, StateFlag.State.ALLOW);
        flags.put(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        flags.put(Flags.MOB_SPAWNING, StateFlag.State.ALLOW);
        flags.put(Flags.MOB_DAMAGE, StateFlag.State.ALLOW);
        flags.put(Flags.PVP, StateFlag.State.DENY);

        RegionUtils.createProtectedRegion(
                getParent().getSpawnPoint().getWorld(),
                cuboidRegion,
                regionName,
                flags,
                10);
    }

    /**
     * Spawn a mob of the current type at a random location in the region
     */
    private void spawnMob() {
        Location spawnLocation = getRandomSpawnLocation();
        if (spawnLocation == null) {
            return;
        }

        PassiveMobType mobType = PassiveMobType.COW;
        if (mobType == PassiveMobType.NONE) {
            return;
        }

        Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () -> {
            Bukkit.getLogger().info("[PassiveMobGenerator] === SPAWN ATTEMPT ===");
            Bukkit.getLogger().info("Location: " + spawnLocation);
            Bukkit.getLogger().info("World: " + bukkitWorld.getName());
            Bukkit.getLogger().info("Chunk loaded: " + spawnLocation.getChunk().isLoaded());

            // Ensure chunk is loaded
            if (!spawnLocation.getChunk().isLoaded()) {
                spawnLocation.getChunk().load();
                Bukkit.getLogger().info("Loaded chunk!");
            }

            // Check what's at the location
            Bukkit.getLogger().info("Block at location: " + spawnLocation.getBlock().getType());
            Bukkit.getLogger().info("Block below: " + spawnLocation.clone().subtract(0, 1, 0).getBlock().getType());

            Entity entity = bukkitWorld.spawnEntity(spawnLocation, EntityType.COW);

            if (entity != null) {
                Bukkit.getLogger().info("SUCCESS! Entity spawned: " + entity.getType() + " UUID: " + entity.getUniqueId());
                Bukkit.getLogger().info("Entity location after spawn: " + entity.getLocation());
                Bukkit.getLogger().info("Entity is valid: " + entity.isValid());
                Bukkit.getLogger().info("Entity is dead: " + entity.isDead());

                spawnedMobs.add(entity.getUniqueId());

                if (entity instanceof LivingEntity) {
                    ((LivingEntity) entity).setRemoveWhenFarAway(false);
                    entity.setPersistent(true);
                    Bukkit.getLogger().info("Set persistence flags");
                }

                entity.setCustomName(ChatColor.YELLOW + mobType.getDisplayName());
                entity.setCustomNameVisible(false);
            } else {
                Bukkit.getLogger().severe("FAILED! spawnEntity returned NULL");
            }

            Bukkit.getLogger().info("Current mob count: " + spawnedMobs.size());
        });
    }

    /**
     * Spawn the actual entity based on mob type
     */
    private Entity spawnMobEntity(Location location, PassiveMobType mobType) {
        EntityType entityType = switch (mobType) {
            case COW -> EntityType.COW;
            case SHEEP -> EntityType.SHEEP;
            case PIG -> EntityType.PIG;
            case CHICKEN -> EntityType.CHICKEN;
            case HORSE -> EntityType.HORSE;
            default -> null;
        };

        if (entityType == null) return null;

        // Use SPAWNER_EGG reason to bypass mob-spawning flag
        return bukkitWorld.spawnEntity(location, entityType);
    }

    /**
     * Get a random safe spawn location within the region
     */
    private Location getRandomSpawnLocation() {
        Random random = new Random();

        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        int randomX = minX + random.nextInt(maxX - minX + 1);
        int randomZ = minZ + random.nextInt(maxZ - minZ + 1);

        // Find highest solid block
        Location spawnLoc = new Location(bukkitWorld, randomX, corner1.getY() + 1, randomZ);
        return spawnLoc;
    }

    /**
     * Check if a location is safe for spawning
     */
    private boolean isSafeSpawnLocation(Location location) {
        // Check block below is solid
        if (!location.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
            return false;
        }

        // Check spawn location and above are air
        if (!location.getBlock().getType().isAir() ||
                !location.clone().add(0, 1, 0).getBlock().getType().isAir()) {
            return false;
        }

        return true;
    }

    /**
     * Remove dead/despawned mobs from tracking
     */
    private void cleanupDeadMobs() {
        spawnedMobs.removeIf(uuid -> {
            Entity entity = Bukkit.getEntity(uuid);
            return entity == null || entity.isDead();
        });
    }

    /**
     * Check if more mobs can be spawned
     */
    private boolean canSpawnMore() {
        return spawnedMobs.size() < getMaxMobCapacity();
    }

    /**
     * Calculate spawn interval based on upgrade level
     * Higher level = faster spawning
     */
    private int calculateSpawnInterval() {
        int spawnSpeedLevel = getSpawnSpeedLevel();
        // Level 1 = 100 ticks, Level 10 = 10 ticks (exponential decrease)
        return Math.max(10, BASE_SPAWN_INTERVAL - (spawnSpeedLevel));
    }

    /**
     * Calculate max mob capacity based on upgrade level
     */
    private int getMaxMobCapacity() {
        int capacityLevel = getMobCapacityLevel();
        return BASE_MAX_MOBS + (capacityLevel * MOBS_PER_CAPACITY_LEVEL);
    }

    /**
     * Clear all spawned mobs (useful when changing mob type or disbanding)
     */
    public void clearAllMobs() {
        for (UUID uuid : new HashSet<>(spawnedMobs)) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
        spawnedMobs.clear();
    }

    /**
     * Change the mob type (clears existing mobs)
     */
    public void setMobType(PassiveMobType mobType) {
        clearAllMobs();
        ((FarmlandSite) getParent()).getData().setCurrentMobType(mobType);
    }

    // Upgrade methods for spawn speed
    public boolean upgradeSpawnSpeed() {
        if (getSpawnSpeedLevel() < MAX_SPAWN_SPEED_LEVEL) {
            setSpawnSpeedLevel(getSpawnSpeedLevel() + 1);
            return true;
        }
        return false;
    }

    public int getSpawnSpeedLevel() {
        return ((FarmlandSite) getParent()).getData().getMobSpawnSpeedLevel();
    }

    public void setSpawnSpeedLevel(int level) {
        ((FarmlandSite) getParent()).getData().setMobSpawnSpeedLevel(
                Math.min(MAX_SPAWN_SPEED_LEVEL, Math.max(1, level))
        );
    }

    public int getMaxSpawnSpeedLevel() {
        return MAX_SPAWN_SPEED_LEVEL;
    }

    // Upgrade methods for mob capacity
    public boolean upgradeMobCapacity() {
        if (getMobCapacityLevel() < MAX_MOB_CAPACITY_LEVEL) {
            setMobCapacityLevel(getMobCapacityLevel() + 1);
            return true;
        }
        return false;
    }

    public int getMobCapacityLevel() {
        return ((FarmlandSite) getParent()).getData().getMobCapacityLevel();
    }

    public void setMobCapacityLevel(int level) {
        ((FarmlandSite) getParent()).getData().setMobCapacityLevel(
                Math.min(MAX_MOB_CAPACITY_LEVEL, Math.max(1, level))
        );
    }

    public int getMaxMobCapacityLevel() {
        return MAX_MOB_CAPACITY_LEVEL;
    }

    // Getters
    public int getCurrentMobCount() {
        cleanupDeadMobs();
        return spawnedMobs.size();
    }

    public String getRegionName() {
        return regionName;
    }

    public CuboidRegion getCuboidRegion() {
        return cuboidRegion;
    }

    /**
     * Enum for passive mob types
     */
    public enum PassiveMobType {
        COW("Cow"),
        SHEEP("Sheep"),
        PIG("Pig"),
        CHICKEN("Chicken"),
        HORSE("Horse"),
        NONE("None");

        private final String displayName;

        PassiveMobType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
