package com.stoinkcraft.serialization;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class EnterpriseMigration {

    private static final File ENTERPRISES_DIR = new File(StoinkCore.getInstance().getDataFolder(), StorageConstants.ENTERPRISES_DIR);

    /**
     * Migrate all YAML enterprises to JSON format
     * @param deleteYamlAfter Whether to delete YAML files after successful migration
     * @return Number of enterprises migrated
     */
    public static int migrateAllYamlToJson(boolean deleteYamlAfter) {
        if (!ENTERPRISES_DIR.exists()) {
            Bukkit.getLogger().info("No enterprises directory found.");
            return 0;
        }

        int migrated = 0;
        int failed = 0;
        int skipped = 0;

        File[] folders = ENTERPRISES_DIR.listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) {
            Bukkit.getLogger().info("No enterprise folders found.");
            return 0;
        }

        for (File folder : folders) {
            File yamlFile = new File(folder, StorageConstants.ENTERPRISE_YAML_FILE);
            File jsonFile = new File(folder, StorageConstants.ENTERPRISE_FILE);

            // Skip if YAML doesn't exist
            if (!yamlFile.exists()) {
                continue;
            }

            // Skip if JSON already exists (already migrated)
            if (jsonFile.exists()) {
                Bukkit.getLogger().info("Skipping " + folder.getName() + " - already has JSON file");
                skipped++;
                continue;
            }

            try {
                Bukkit.getLogger().info("Migrating: " + folder.getName());

                // Load from YAML
                Enterprise enterprise = loadEnterpriseFromYaml(yamlFile, folder);

                // Load price history if exists
                File historyFile = new File(folder, StorageConstants.PRICE_HISTORY_YAML_FILE);
                if (historyFile.exists()) {
                    loadPriceHistoryFromYaml(enterprise, historyFile);
                }

                // Save as JSON
                if (EnterpriseStorageJson.saveEnterprise(enterprise)) {
                    migrated++;
                    Bukkit.getLogger().info("Successfully migrated: " + enterprise.getName());

                    // Delete YAML files if requested
                    if (deleteYamlAfter) {
                        yamlFile.delete();
                        if (historyFile.exists()) {
                            historyFile.delete();
                        }
                        Bukkit.getLogger().info("Deleted old YAML files for: " + enterprise.getName());
                    }
                } else {
                    failed++;
                }

            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to migrate: " + folder.getName(), e);
                failed++;
            }
        }

        EnterpriseStorageJson.loadAllEnterprises(true);

        Bukkit.getLogger().info("=== Migration Complete ===");
        Bukkit.getLogger().info("Migrated: " + migrated);
        Bukkit.getLogger().info("Failed: " + failed);
        Bukkit.getLogger().info("Skipped: " + skipped);

        return migrated;
    }

    /**
     * Load a single enterprise from YAML format
     */
    private static Enterprise loadEnterpriseFromYaml(File yamlFile, File folder) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(yamlFile);

        String name = config.getString("name", "Unknown");
        UUID ceo = UUID.fromString(config.getString("ceo"));
        boolean serverOwned = config.getBoolean("serverOwned", false);
        UUID id = UUID.fromString(folder.getName());

        // Create appropriate enterprise type
        Enterprise enterprise;
        if (serverOwned) {
            enterprise = new ServerEnterprise(name);
            enterprise.setEnterpriseID(id);
            enterprise.setBankBalance(config.getDouble("bankBalance", 0));
        } else {
            // Constructor: name, ceo, bankBalance, reputation, outstandingShares, activeBooster, enterpriseID
            // Old YAML has netWorth but not reputation - reputation defaults to 0 for migrated enterprises
            enterprise = new Enterprise(name, ceo,
                    config.getDouble("bankBalance", 0) + config.getDouble("netWorth", 0),
                    config.getDouble("reputation", 0), // reputation (new field, default 0)
                    config.getInt("outstandingShares", 0),
                    null, // activeBooster - handle separately if needed
                    id
            );
        }

        // Load members
        if (config.isConfigurationSection("members")) {
            for (String uuidStr : config.getConfigurationSection("members").getKeys(false)) {
                try {
                    UUID memberUuid = UUID.fromString(uuidStr);
                    Role role = Role.valueOf(config.getString("members." + uuidStr));

                    // Don't re-add CEO (already added in constructor)
                    if (role != Role.CEO) {
                        enterprise.getMembers().put(memberUuid, role);
                    }
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Invalid member data for " + name + ": " + uuidStr);
                }
            }
        }

        // Load warp location
        if (config.isConfigurationSection("warp")) {
            try {
                String worldName = config.getString("warp.world");
                World world = Bukkit.getWorld(worldName);

                if (world != null) {
                    double x = config.getDouble("warp.x");
                    double y = config.getDouble("warp.y");
                    double z = config.getDouble("warp.z");
                    float yaw = (float) config.getDouble("warp.yaw");
                    float pitch = (float) config.getDouble("warp.pitch");

                    enterprise.setWarp(new Location(world, x, y, z, yaw, pitch));
                } else {
                    Bukkit.getLogger().warning("World not found for warp: " + worldName);
                }
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to load warp for " + name + ": " + e.getMessage());
            }
        }

        return enterprise;
    }

    /**
     * Load price history from YAML
     */
    private static void loadPriceHistoryFromYaml(Enterprise enterprise, File historyFile) {
        if (!historyFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(historyFile);

        if (config.isList("priceHistory")) {
            for (Map<?, ?> entry : config.getMapList("priceHistory")) {
                try {
                    long time = ((Number) entry.get("time")).longValue();
                    double value = ((Number) entry.get("value")).doubleValue();
                    enterprise.getPriceHistory().add(new PriceSnapshot(time, value));
                } catch (Exception e) {
                    Bukkit.getLogger().warning("Failed to load price snapshot: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Check if any YAML files need migration
     */
    public static boolean hasYamlFilesToMigrate() {
        if (!ENTERPRISES_DIR.exists()) return false;

        File[] folders = ENTERPRISES_DIR.listFiles(File::isDirectory);
        if (folders == null) return false;

        for (File folder : folders) {
            File yamlFile = new File(folder, StorageConstants.ENTERPRISE_YAML_FILE);
            File jsonFile = new File(folder, StorageConstants.ENTERPRISE_FILE);

            // Has YAML but no JSON
            if (yamlFile.exists() && !jsonFile.exists()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get count of YAML files that need migration
     */
    public static int getYamlFilesCount() {
        if (!ENTERPRISES_DIR.exists()) return 0;

        int count = 0;
        File[] folders = ENTERPRISES_DIR.listFiles(File::isDirectory);
        if (folders == null) return 0;

        for (File folder : folders) {
            File yamlFile = new File(folder, StorageConstants.ENTERPRISE_YAML_FILE);
            File jsonFile = new File(folder, StorageConstants.ENTERPRISE_FILE);

            if (yamlFile.exists() && !jsonFile.exists()) {
                count++;
            }
        }

        return count;
    }
}