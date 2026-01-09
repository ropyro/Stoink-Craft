package com.stoinkcraft.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Central configuration loader and manager for all plugin configs.
 * Follows singleton pattern for global access throughout the plugin.
 */
public class ConfigLoader {

    private static ConfigLoader instance;

    private final JavaPlugin plugin;
    private final Logger logger;

    private EconomyConfig economy;
    private CollectionConfig collections;
    private GeneratorConfig generators;
    // Future config classes will be added here:
    // private StructureConfig structures;
    // private JobSiteUpgradeConfig jobSiteUpgrades;
    // private ContractConfigLoader contracts;

    private ConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadAll();
    }

    /**
     * Initialize the ConfigLoader singleton. Call this once during plugin startup.
     */
    public static void initialize(JavaPlugin plugin) {
        if (instance != null) {
            plugin.getLogger().warning("ConfigLoader already initialized!");
            return;
        }
        instance = new ConfigLoader(plugin);
    }

    /**
     * Get the ConfigLoader singleton instance.
     */
    public static ConfigLoader getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConfigLoader not initialized! Call initialize() first.");
        }
        return instance;
    }

    /**
     * Check if ConfigLoader has been initialized.
     */
    public static boolean isInitialized() {
        return instance != null;
    }

    /**
     * Get economy configuration.
     */
    public static EconomyConfig getEconomy() {
        return getInstance().economy;
    }

    /**
     * Get collection configuration.
     */
    public static CollectionConfig getCollections() {
        return getInstance().collections;
    }

    /**
     * Get generator configuration.
     */
    public static GeneratorConfig getGenerators() {
        return getInstance().generators;
    }

    /**
     * Load all configuration files.
     */
    private void loadAll() {
        logger.info("Loading StoinkCore configurations...");

        try {
            loadEconomyConfig();
            loadCollectionsConfig();
            loadGeneratorsConfig();
            // Future configs will be loaded here
            logger.info("All configurations loaded successfully!");
        } catch (Exception e) {
            logger.severe("Failed to load configurations: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    /**
     * Reload all configuration files without restarting the plugin.
     */
    public void reload() {
        logger.info("Reloading StoinkCore configurations...");
        loadAll();
    }

    private void loadEconomyConfig() {
        File economyFile = new File(plugin.getDataFolder(), "economy.yml");

        // Create default economy.yml if it doesn't exist
        if (!economyFile.exists()) {
            plugin.saveResource("economy.yml", false);
            logger.info("Created default economy.yml");
        }

        economy = new EconomyConfig(plugin, economyFile);
    }

    private void loadCollectionsConfig() {
        File collectionsFile = new File(plugin.getDataFolder(), "collections.yml");

        // Create default collections.yml if it doesn't exist
        if (!collectionsFile.exists()) {
            plugin.saveResource("collections.yml", false);
            logger.info("Created default collections.yml");
        }

        collections = new CollectionConfig(plugin, collectionsFile);
    }

    private void loadGeneratorsConfig() {
        File generatorsFile = new File(plugin.getDataFolder(), "generators.yml");

        // Create default generators.yml if it doesn't exist
        if (!generatorsFile.exists()) {
            plugin.saveResource("generators.yml", false);
            logger.info("Created default generators.yml");
        }

        generators = new GeneratorConfig(plugin, generatorsFile);
    }
}
