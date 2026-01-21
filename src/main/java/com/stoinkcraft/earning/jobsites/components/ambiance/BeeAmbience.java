package com.stoinkcraft.earning.jobsites.components.ambiance;

import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.components.JobSiteComponent;
import com.stoinkcraft.earning.jobsites.components.generators.HoneyGenerator;
import com.stoinkcraft.earning.jobsites.sites.farmland.FarmlandSite;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

public class BeeAmbience implements JobSiteComponent {

    public static final String METADATA_KEY = "stoinkcraft_ambient_bee";

    private static final int TARGET_BEE_COUNT = 4;
    private static final int MAINTENANCE_INTERVAL_SECONDS = 5;
    private static final double WANDER_RADIUS = 5.0;

    private final Plugin plugin;
    private final JobSite parent;
    private final World world;
    private final Supplier<List<Location>> hiveLocationSupplier;
    private final List<UUID> managedBees = new ArrayList<>();
    private final Random random = new Random();
    private final String siteId;

    private int tickCounter = 0;
    private boolean active = false;

    /**
     * @param plugin              The main plugin instance
     * @param parent              The parent JobSite
     * @param hiveLocationSupplier Supplier that returns current hive locations (allows dynamic updates)
     */
    public BeeAmbience(Plugin plugin, JobSite parent, Supplier<List<Location>> hiveLocationSupplier) {
        this.plugin = plugin;
        this.parent = parent;
        this.world = parent.getSpawnPoint().getWorld();
        this.siteId = parent.getEnterprise().getID().toString();
        this.hiveLocationSupplier = hiveLocationSupplier;
    }

    /**
     * Convenience constructor for FarmlandSite with HoneyGenerators
     */
    public BeeAmbience(Plugin plugin, JobSite parent, List<HoneyGenerator> honeyGenerators) {
        this(plugin, parent, () -> honeyGenerators.stream()
                .map(HoneyGenerator::getHiveLocation)
                .toList());
    }

    @Override
    public void build() {
        active = true;
        if(parent instanceof FarmlandSite farmlandSite){
            active = farmlandSite.areBeeHivesBuilt();
        }
        if(active)
            spawnMissingBees();
    }

    @Override
    public void tick() {
        if (!active){
            if(parent instanceof FarmlandSite farmlandSite){
                active = farmlandSite.areBeeHivesBuilt();
            }
            return;
        }

        List<Location> hives = hiveLocationSupplier.get();
        if (hives.isEmpty()) return;

        // Check if players are nearby - if not, despawn bees to save resources
        if (!parent.containsActivePlayer()) {
            if (!managedBees.isEmpty()) {
                removeAllBees();
            }
            return;
        }

        tickCounter++;

        if (tickCounter >= MAINTENANCE_INTERVAL_SECONDS) {
            tickCounter = 0;
            performMaintenance();
        }
    }

    @Override
    public void disband() {
        active = false;
        removeAllBees();
    }

    @Override
    public void levelUp() {
        // Could increase bee count at higher levels if desired
    }

    // =========================================================================
    // BEE MANAGEMENT
    // =========================================================================

    private void performMaintenance() {
        cleanupDeadBees();
        spawnMissingBees();
        randomlyRedirectBees();
    }

    private void spawnMissingBees() {
        List<Location> hives = hiveLocationSupplier.get();
        if (hives.isEmpty()) return;

        int toSpawn = TARGET_BEE_COUNT - managedBees.size();

        for (int i = 0; i < toSpawn; i++) {
            Location hive = getRandomHive(hives);
            if (hive == null) continue;

            Location spawnLoc = hive.clone().add(
                    random.nextDouble() * 2 - 1,
                    1.5,
                    random.nextDouble() * 2 - 1
            );

            Bee bee = (Bee) world.spawnEntity(spawnLoc, EntityType.BEE);
            configureBee(bee, hive);
            managedBees.add(bee.getUniqueId());
        }
    }

    private void configureBee(Bee bee, Location hive) {
        // Mark as our managed bee with site ID
        bee.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, siteId));

        // Prevent natural despawning
        bee.setPersistent(true);
        bee.setRemoveWhenFarAway(false);

        // Make passive and content
        bee.setAnger(0);
        bee.setHasStung(false);
        bee.setCannotEnterHiveTicks(Integer.MAX_VALUE);

        // Set hive location
        bee.setHive(hive);

        // Set initial wander target
        Location wanderTarget = getWanderTarget(hive);
        bee.setFlower(wanderTarget);

        // Custom name for debugging (optional - remove in production)
        // bee.setCustomName(ChatColor.GOLD + "§6🐝");
        // bee.setCustomNameVisible(false);
    }

    private void cleanupDeadBees() {
        managedBees.removeIf(uuid -> {
            Entity entity = getEntityByUUID(uuid);
            return entity == null || entity.isDead() || !entity.isValid();
        });
    }

    private void randomlyRedirectBees() {
        List<Location> hives = hiveLocationSupplier.get();
        if (hives.isEmpty()) return;

        for (UUID uuid : managedBees) {
            Entity entity = getEntityByUUID(uuid);
            if (!(entity instanceof Bee bee)) continue;

            // 30% chance to pick a new target
            if (random.nextDouble() < 0.3) {
                Location newHive = getRandomHive(hives);
                if (newHive != null) {
                    bee.setHive(newHive);
                    bee.setFlower(getWanderTarget(newHive));
                }
            }

            // Ensure bee stays passive
            if (bee.getAnger() > 0) {
                bee.setAnger(0);
            }
        }
    }

    private void removeAllBees() {
        for (UUID uuid : managedBees) {
            Entity entity = getEntityByUUID(uuid);
            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }
        managedBees.clear();
    }

    // =========================================================================
    // UTILITY
    // =========================================================================

    private Location getRandomHive(List<Location> hives) {
        if (hives.isEmpty()) return null;
        return hives.get(random.nextInt(hives.size()));
    }

    private Location getWanderTarget(Location center) {
        return center.clone().add(
                (random.nextDouble() - 0.5) * WANDER_RADIUS * 2,
                random.nextDouble() * 2,
                (random.nextDouble() - 0.5) * WANDER_RADIUS * 2
        );
    }

    private Entity getEntityByUUID(UUID uuid){
        Entity entity = world.getEntities().stream().filter(e -> e.getUniqueId().equals(uuid)).findFirst().orElse(null);
        return entity;
    }

    // =========================================================================
    // STATIC HELPERS
    // =========================================================================

    /**
     * Check if an entity is a managed ambient bee
     */
    public static boolean isAmbientBee(Entity entity) {
        return entity.hasMetadata(METADATA_KEY);
    }

    /**
     * Get the site ID for an ambient bee
     */
    public static String getAmbientBeeSiteId(Entity entity) {
        if (!entity.hasMetadata(METADATA_KEY)) return null;
        var metadata = entity.getMetadata(METADATA_KEY);
        if (metadata.isEmpty()) return null;
        return metadata.get(0).asString();
    }

    /**
     * Get the current number of active bees
     */
    public int getActiveBeeCount() {
        return managedBees.size();
    }
}