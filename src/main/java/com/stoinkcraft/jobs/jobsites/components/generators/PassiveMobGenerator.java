package com.stoinkcraft.jobs.jobsites.components.generators;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.components.JobSiteGenerator;
import com.stoinkcraft.jobs.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.utils.RegionUtils;
import com.stoinkcraft.utils.TimeUtils;
import org.bukkit.*;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class PassiveMobGenerator extends JobSiteGenerator {

    private final Location corner1;
    private final Location corner2;
    private final World bukkitWorld;

    private final CuboidRegion cuboidRegion;
    private final String regionName;

    // Tracking spawned mobs
    private final Set<UUID> spawnedMobs = new HashSet<>();

    // Spawn timing
    private int ticksSinceLastSpawn = 0;

    // Base values (no longer upgrades)
    private static final int BASE_SPAWN_INTERVAL = 100; // ticks
    private static final int BASE_MAX_MOBS = 10;
    private static final int MOBS_PER_CAPACITY_LEVEL = 5;

    public PassiveMobGenerator(Location corner1, Location corner2, JobSite parent, String regionName) {
        super(parent);

        this.corner1 = corner1.clone();
        this.corner2 = corner2.clone();
        this.bukkitWorld = corner1.getWorld();
        this.regionName = regionName;

        this.cuboidRegion = getRegion(this.corner1, this.corner2);
    }

    @Override
    public void tick() {
        super.tick();

        cleanupDeadMobs();
        if (!TimeUtils.isDay(getParent().getSpawnPoint().getWorld())) return;

        ticksSinceLastSpawn++;

        if (ticksSinceLastSpawn >= calculateSpawnInterval() && canSpawnMore()) {
            spawnMob();
            ticksSinceLastSpawn = 0;
        }
    }

    @Override
    public void build() {
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
                10
        );
    }

    @Override
    public void disband(){
        clearAllMobs();
    }

    // --------------------------------------------------
    // SPAWNING
    // --------------------------------------------------

    private void spawnMob() {
        Location spawnLocation = getRandomSpawnLocation();
        if (spawnLocation == null) return;

        PassiveMobType mobType = getCurrentMobType();
        if (mobType == PassiveMobType.NONE) return;

        Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () -> {
            if (!spawnLocation.getChunk().isLoaded()) {
                spawnLocation.getChunk().load();
            }

            EntityType entityType = getEntityType(mobType);
            if (entityType == null) return;

            LivingEntity entity = (LivingEntity) bukkitWorld.spawnEntity(spawnLocation, entityType);

            entity.setRemoveWhenFarAway(false);
            entity.setPersistent(true);
            entity.setCustomName(ChatColor.YELLOW + mobType.getDisplayName());
            entity.setCustomNameVisible(false);
            entity.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);

            spawnedMobs.add(entity.getUniqueId());
        });
    }

    private EntityType getEntityType(PassiveMobType type) {
        return switch (type) {
            case COW -> EntityType.COW;
            case SHEEP -> EntityType.SHEEP;
            case PIG -> EntityType.PIG;
            case CHICKEN -> EntityType.CHICKEN;
            case HORSE -> EntityType.HORSE;
            default -> null;
        };
    }

    // --------------------------------------------------
    // RANDOM LOCATION
    // --------------------------------------------------

    private Location getRandomSpawnLocation() {
        Random random = new Random();

        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        int x = minX + random.nextInt(maxX - minX + 1);
        int z = minZ + random.nextInt(maxZ - minZ + 1);

        Location loc = new Location(bukkitWorld, x + 0.5, corner1.getY() + 1, z + 0.5);

        return isSafeSpawnLocation(loc) ? loc : null;
    }

    private boolean isSafeSpawnLocation(Location loc) {
        return loc.getBlock().getType().isAir()
                && loc.clone().add(0, 1, 0).getBlock().getType().isAir()
                && loc.clone().subtract(0, 1, 0).getBlock().getType().isSolid();
    }

    // --------------------------------------------------
    // CLEANUP
    // --------------------------------------------------

    private void cleanupDeadMobs() {
        spawnedMobs.removeIf(uuid -> {
            Entity e = Bukkit.getEntity(uuid);
            return e == null || e.isDead();
        });
    }

    public void clearAllMobs() {
        for (UUID uuid : new HashSet<>(spawnedMobs)) {
            Entity e = Bukkit.getEntity(uuid);
            if (e != null) e.remove();
        }
        spawnedMobs.clear();
    }

    // --------------------------------------------------
    // UPGRADE INTEGRATION
    // --------------------------------------------------

    private int getSpawnSpeedLevel() {
        return ((FarmlandSite) getParent()).getData().getLevel("mob_spawn_speed");
    }

    private int getMobCapacityLevel() {
        return ((FarmlandSite) getParent()).getData().getLevel("mob_capacity");
    }

    private int calculateSpawnInterval() {
        int lvl = getSpawnSpeedLevel();
        return Math.max(10, BASE_SPAWN_INTERVAL - (lvl * 9));
    }

    private int getMaxMobCapacity() {
        return BASE_MAX_MOBS + (getMobCapacityLevel() * MOBS_PER_CAPACITY_LEVEL);
    }

    private boolean canSpawnMore() {
        return spawnedMobs.size() < getMaxMobCapacity();
    }

    // --------------------------------------------------
    // MOB TYPE
    // --------------------------------------------------

    private PassiveMobType getCurrentMobType() {
        return ((FarmlandSite) getParent()).getData().getCurrentMobType();
    }

    public void setMobType(PassiveMobType type) {
        clearAllMobs();
        ((FarmlandSite) getParent()).getData().setCurrentMobType(type);
    }

    // --------------------------------------------------
    // GETTERS
    // --------------------------------------------------

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

    // --------------------------------------------------
    // ENUM
    // --------------------------------------------------

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