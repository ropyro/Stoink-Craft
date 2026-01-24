package com.stoinkcraft.jobsites.components.generators;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.components.JobSiteGenerator;
import com.stoinkcraft.jobsites.components.JobSiteHologram;
import com.stoinkcraft.jobsites.sites.farmland.FarmlandData;
import com.stoinkcraft.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.utils.TimeUtils;
import org.bukkit.*;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class PassiveMobGenerator extends JobSiteGenerator {

    private final Location corner1;
    private final Location corner2;
    private final World bukkitWorld;

    private final CuboidRegion cuboidRegion;
    private final String regionName;

    // Tracking spawned mobs
    private final Set<UUID> spawnedMobs = new HashSet<>();

    // Spawn timing - tracks "pending" spawns that accumulate while players are away
    private int ticksSinceLastSpawn = 0;
    private int pendingSpawns = 0;

    // Notification tracking
    private boolean fullNotificationSent = false;

    // Hologram
    private JobSiteHologram hologram;
    private final String hologramId;
    private final Vector hologramOffset;

    public PassiveMobGenerator(Location corner1, Location corner2, JobSite parent, String regionName, Vector hologramOffset) {
        super(parent);

        this.corner1 = corner1.clone();
        this.corner2 = corner2.clone();
        this.bukkitWorld = corner1.getWorld();
        this.regionName = regionName;
        this.hologramOffset = hologramOffset;

        this.cuboidRegion = getRegion(this.corner1, this.corner2);

        // Initialize hologram
        this.hologramId = parent.getEnterprise().getID() + "_passive_mob_" + regionName;
        this.hologram = new JobSiteHologram(
                parent,
                hologramId,
                hologramOffset,
                List.of()
        );
        parent.addComponent(hologram);
    }

    @Override
    public void tick() {
        super.tick();

        // Day-only spawning logic
        if (!TimeUtils.isDay(getParent().getSpawnPoint().getWorld())) {
            updateHologram();
            return;
        }

        PassiveMobType mobType = getCurrentMobType();
        if (mobType == PassiveMobType.NONE) {
            updateHologram();
            return;
        }

        // Always tick the spawn timer, even without players
        tickSpawnTimer();

        // Only cleanup and spawn when chunks are loaded and players present
        if (isChunkLoaded() && getParent().containsActivePlayer()) {
            cleanupDeadMobs();
            spawnPendingMobs();
        }

        // Check if full and send notification
        checkAndNotifyFull();

        updateHologram();
    }

    @Override
    public void build() {
        // Could add fence posts or markers here in the future
    }

    @Override
    public void disband() {
        clearAllMobs();
        if (hologram != null) {
            hologram.delete();
        }
    }

    // --------------------------------------------------
    // SPAWN TIMER (runs even without players)
    // --------------------------------------------------

    private void tickSpawnTimer() {
        // Don't accumulate if already at or beyond capacity
        int totalPending = spawnedMobs.size() + pendingSpawns;
        if (totalPending >= getMaxMobCapacity()) {
            ticksSinceLastSpawn = 0;
            return;
        }

        ticksSinceLastSpawn++;

        if (ticksSinceLastSpawn >= calculateSpawnInterval()) {
            pendingSpawns++;
            ticksSinceLastSpawn = 0;

            // Cap pending spawns to not exceed capacity
            int maxPending = getMaxMobCapacity() - spawnedMobs.size();
            pendingSpawns = Math.min(pendingSpawns, maxPending);
        }
    }

    // --------------------------------------------------
    // SPAWNING
    // --------------------------------------------------

    private void spawnPendingMobs() {
        if (pendingSpawns <= 0) return;

        int spawned = 0;
        int toSpawn = Math.min(pendingSpawns, getMaxMobCapacity() - spawnedMobs.size());

        for (int i = 0; i < toSpawn; i++) {
            if (spawnMob()) {
                spawned++;
            }
        }

        pendingSpawns -= spawned;
        pendingSpawns = Math.max(0, pendingSpawns); // Safety clamp
    }

    private boolean spawnMob() {
        if (spawnedMobs.size() >= getMaxMobCapacity()) {
            return false;
        }

        Location spawnLocation = getRandomSpawnLocation();
        if (spawnLocation == null) {
            return false;
        }

        PassiveMobType mobType = getCurrentMobType();
        if (mobType == PassiveMobType.NONE) {
            return false;
        }

        EntityType entityType = mobType.getEntityType();
        if (entityType == null) {
            return false;
        }

        if (!spawnLocation.getChunk().isLoaded()) {
            return false;
        }

        LivingEntity entity = (LivingEntity) bukkitWorld.spawnEntity(spawnLocation, entityType);

        entity.setRemoveWhenFarAway(false);
        entity.setPersistent(true);
        entity.setCustomName(mobType.getColoredDisplayName());
        entity.setCustomNameVisible(false);

        if (entity.getAttribute(Attribute.JUMP_STRENGTH) != null) {
            entity.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
        }

        // Tag for enterprise drop tracking
        entity.getPersistentDataContainer().set(
                new NamespacedKey(StoinkCore.getInstance(), "farmland_mob"),
                PersistentDataType.STRING,
                getParent().getEnterprise().getID().toString()
        );

        spawnedMobs.add(entity.getUniqueId());

        // Reset full notification when a mob is added (pen was not full before)
        if (spawnedMobs.size() < getMaxMobCapacity()) {
            fullNotificationSent = false;
        }

        return true;
    }

    // --------------------------------------------------
    // FULL NOTIFICATION
    // --------------------------------------------------

    private void checkAndNotifyFull() {
        if (fullNotificationSent) return;

        int total = spawnedMobs.size() + pendingSpawns;
        if (total >= getMaxMobCapacity()) {
            PassiveMobType mobType = getCurrentMobType();
            String mobName = mobType.getDisplayName();
            String plural = getPluralName(mobType);

            getParent().getEnterprise().sendEnterpriseMessage(
                    "",
                    ChatColor.GOLD + "  ✦ " + ChatColor.YELLOW + "" + ChatColor.BOLD + "Pasture Full!" + ChatColor.GOLD + " ✦",
                    "",
                    ChatColor.GRAY + "  Your " + ChatColor.YELLOW + plural + ChatColor.GRAY + " pasture is at capacity!",
                    ChatColor.GRAY + "  " + ChatColor.WHITE + getMaxMobCapacity() + "/" + getMaxMobCapacity() + ChatColor.GRAY + " " + plural + " ready for harvest.",
                    ""
            );

            fullNotificationSent = true;
        }
    }

    private String getPluralName(PassiveMobType type) {
        return switch (type) {
            case SHEEP -> "Sheep";
            case CHICKEN -> "Chickens";
            case COW -> "Cows";
            case PIG -> "Pigs";
            default -> type.getDisplayName() + "s";
        };
    }

    // --------------------------------------------------
    // CHUNK SAFETY
    // --------------------------------------------------

    private boolean isChunkLoaded() {
        return corner1.getChunk().isLoaded() && corner2.getChunk().isLoaded();
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

        for (int attempt = 0; attempt < 10; attempt++) {
            int x = minX + random.nextInt(maxX - minX + 1);
            int z = minZ + random.nextInt(maxZ - minZ + 1);

            Location loc = new Location(bukkitWorld, x + 0.5, corner1.getY() + 1, z + 0.5);

            if (isSafeSpawnLocation(loc)) {
                return loc;
            }
        }

        return null;
    }

    private boolean isSafeSpawnLocation(Location loc) {
        if (!loc.getChunk().isLoaded()) {
            return false;
        }

        return loc.getBlock().getType().isAir()
                && loc.clone().add(0, 1, 0).getBlock().getType().isAir()
                && loc.clone().subtract(0, 1, 0).getBlock().getType().isSolid();
    }

    // --------------------------------------------------
    // CLEANUP
    // --------------------------------------------------

    private void cleanupDeadMobs() {
        int beforeSize = spawnedMobs.size();

        spawnedMobs.removeIf(uuid -> {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity == null) {
                return true;
            }
            return entity.isDead();
        });

        // If mobs were removed (killed), reset the full notification
        if (spawnedMobs.size() < beforeSize) {
            fullNotificationSent = false;
        }
    }

    public void clearAllMobs() {
        for (UUID uuid : new HashSet<>(spawnedMobs)) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
        spawnedMobs.clear();
        pendingSpawns = 0;
        fullNotificationSent = false;
    }

    // --------------------------------------------------
    // HOLOGRAM
    // --------------------------------------------------

    private void updateHologram() {
        if (!isEnabled()) {
            hologram.delete();
            return;
        }

        FarmlandSite farmland = (FarmlandSite) getParent();
        int spawnSpeedLevel = getSpawnSpeedLevel();
        int capacityLevel = getMobCapacityLevel();
        PassiveMobType mobType = getCurrentMobType();
        boolean isNight = !TimeUtils.isDay(farmland.getSpawnPoint().getWorld());
        boolean hasPlayer = getParent().containsActivePlayer();

        List<String> lines = new ArrayList<>();

        // Header
        lines.add(ChatColor.GOLD + "✦ " + ChatColor.YELLOW + "" + ChatColor.BOLD + "Pasture" + ChatColor.GOLD + " ✦");

        // Entity display
        if (mobType != PassiveMobType.NONE) {
            lines.add("#ENTITY: " + mobType.getEntityType().name());
            lines.add("");
        } else {
            lines.add(ChatColor.GRAY + "No animal selected");
        }

        // Upgrade levels
        lines.add(ChatColor.AQUA + "❖ " + ChatColor.WHITE + "Speed " + ChatColor.GRAY + "Lv." + ChatColor.AQUA + spawnSpeedLevel +
                ChatColor.DARK_GRAY + " | " +
                ChatColor.GREEN + "❖ " + ChatColor.WHITE + "Capacity " + ChatColor.GRAY + "Lv." + ChatColor.GREEN + capacityLevel);

        // Mob count (including pending)
        if (mobType != PassiveMobType.NONE) {
            int currentCount = spawnedMobs.size();
            int totalReady = currentCount + pendingSpawns;
            int maxCapacity = getMaxMobCapacity();

            ChatColor countColor = totalReady >= maxCapacity ? ChatColor.GREEN : ChatColor.YELLOW;
            String plural = getPluralName(mobType);

            if (pendingSpawns > 0 && !hasPlayer) {
                // Show pending when player is away
                lines.add(countColor + "🐾 " + ChatColor.WHITE + currentCount + ChatColor.GRAY + " (+" + pendingSpawns + " pending)" + ChatColor.WHITE + "/" + maxCapacity + " " + plural);
            } else {
                lines.add(countColor + "🐾 " + ChatColor.WHITE + currentCount + "/" + maxCapacity + " " + plural);
            }
        }

        // Status line
        if (mobType == PassiveMobType.NONE) {
            lines.add(ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + "Select an animal type" + ChatColor.DARK_GRAY + " «");
        } else if (isNight) {
            lines.add(ChatColor.DARK_PURPLE + "☽ " + ChatColor.LIGHT_PURPLE + "The animals rest...");
        } else if (isFull()) {
            lines.add(ChatColor.GREEN + "✔ " + ChatColor.DARK_GREEN + "Pen is full!");
        } else if (!hasPlayer && pendingSpawns > 0) {
            lines.add(ChatColor.YELLOW + "⏸ " + ChatColor.GOLD + pendingSpawns + " ready to spawn on arrival");
        } else if (!hasPlayer) {
            int remainingTicks = calculateSpawnInterval() - ticksSinceLastSpawn;
            lines.add(ChatColor.GRAY + "⏱ " + ChatColor.DARK_GRAY + "Next spawn: " + ChatColor.GRAY + formatTime(remainingTicks));
        } else {
            int remainingTicks = calculateSpawnInterval() - ticksSinceLastSpawn;
            lines.add(ChatColor.WHITE + "⏱ " + ChatColor.GRAY + "Next spawn: " + ChatColor.WHITE + formatTime(remainingTicks));
        }

        hologram.setLines(0, lines);
    }

    private boolean isFull() {
        return (spawnedMobs.size() + pendingSpawns) >= getMaxMobCapacity();
    }

    private String formatTime(int ticks) {
        int seconds = ticks;
        if (seconds <= 0) return "Soon...";
        if (seconds < 60) return seconds + "s";
        return (seconds / 60) + "m " + (seconds % 60) + "s";
    }

    // --------------------------------------------------
    // UPGRADE INTEGRATION
    // --------------------------------------------------

    private FarmlandData getData() {
        return ((FarmlandSite) getParent()).getData();
    }

    private int getSpawnSpeedLevel() {
        return getData().getLevel("mob_spawn_speed");
    }

    private int getMobCapacityLevel() {
        return getData().getLevel("mob_capacity");
    }

    private int calculateSpawnInterval() {
        int lvl = getSpawnSpeedLevel();
        int baseInterval = ConfigLoader.getGenerators().getPassiveMobBaseSpawnInterval();
        int minInterval = ConfigLoader.getGenerators().getPassiveMobMinSpawnInterval();
        double reductionPerLevel = ConfigLoader.getGenerators().getPassiveMobSpawnSpeedReductionPerLevel();
        return Math.max(minInterval, baseInterval - (int) (lvl * reductionPerLevel));
    }

    private int getMaxMobCapacity() {
        int baseMaxMobs = ConfigLoader.getGenerators().getPassiveMobBaseMaxMobs();
        int mobsPerLevel = ConfigLoader.getGenerators().getPassiveMobMobsPerCapacityLevel();
        return baseMaxMobs + (getMobCapacityLevel() * mobsPerLevel);
    }

    // --------------------------------------------------
    // MOB TYPE
    // --------------------------------------------------

    private PassiveMobType getCurrentMobType() {
        return getData().getCurrentMobType();
    }

    public void setMobType(PassiveMobType type) {
        clearAllMobs();
        getData().setCurrentMobType(type);
        ticksSinceLastSpawn = 0;
        pendingSpawns = 0;
        fullNotificationSent = false;
    }

    // --------------------------------------------------
    // GETTERS
    // --------------------------------------------------

    public int getCurrentMobCount() {
        return spawnedMobs.size();
    }

    public int getPendingSpawns() {
        return pendingSpawns;
    }

    public int getTotalCount() {
        return spawnedMobs.size() + pendingSpawns;
    }

    public String getRegionName() {
        return regionName;
    }

    public CuboidRegion getCuboidRegion() {
        return cuboidRegion;
    }

    public JobSiteHologram getHologram() {
        return hologram;
    }

    public Set<UUID> getSpawnedMobs() {
        return new HashSet<>(spawnedMobs);
    }

    // --------------------------------------------------
    // ENUM
    // --------------------------------------------------

    public enum PassiveMobType {
        COW("Cow", EntityType.COW, ChatColor.WHITE + "🐄", ChatColor.WHITE),
        SHEEP("Sheep", EntityType.SHEEP, ChatColor.WHITE + "🐑", ChatColor.WHITE),
        PIG("Pig", EntityType.PIG, ChatColor.LIGHT_PURPLE + "🐷", ChatColor.LIGHT_PURPLE),
        CHICKEN("Chicken", EntityType.CHICKEN, ChatColor.YELLOW + "🐔", ChatColor.YELLOW),
        NONE("None", null, ChatColor.GRAY + "🚫", ChatColor.GRAY);

        private final String displayName;
        private final EntityType entityType;
        private final String icon;
        private final ChatColor color;

        PassiveMobType(String displayName, EntityType entityType, String icon, ChatColor color) {
            this.displayName = displayName;
            this.entityType = entityType;
            this.icon = icon;
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public EntityType getEntityType() {
            return entityType;
        }

        public String getIcon() {
            return icon;
        }

        public ChatColor getColor() {
            return color;
        }

        public String getColoredDisplayName() {
            return color + displayName;
        }
    }
}