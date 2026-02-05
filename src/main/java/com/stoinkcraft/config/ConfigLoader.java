package com.stoinkcraft.config;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ConfigLoader {

    private static ConfigLoader instance;

    private final JavaPlugin plugin;
    private final Logger logger;

    private EconomyConfig economy;
    private CollectionConfig collections;
    private GeneratorConfig generators;
    private StructureConfig structures;

    private ConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadAll();
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance != null) {
            plugin.getLogger().warning("ConfigLoader already initialized!");
            return;
        }
        instance = new ConfigLoader(plugin);
    }

    public static ConfigLoader getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ConfigLoader not initialized! Call initialize() first.");
        }
        return instance;
    }

    public static boolean isInitialized() {
        return instance != null;
    }

    public static EconomyConfig getEconomy() {
        return getInstance().economy;
    }

    public static CollectionConfig getCollections() {
        return getInstance().collections;
    }

    public static GeneratorConfig getGenerators() {
        return getInstance().generators;
    }

    public static StructureConfig getStructures() {
        return getInstance().structures;
    }

    private void loadAll() {
        logger.info("Loading StoinkCore configurations...");

        try {
            loadEconomyConfig();
            loadCollectionsConfig();
            loadGeneratorsConfig();
            loadStructuresConfig();
            logger.info("All configurations loaded successfully!");
        } catch (Exception e) {
            logger.severe("Failed to load configurations: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Configuration loading failed", e);
        }
    }

    public void reload() {
        logger.info("Reloading StoinkCore configurations...");
        loadAll();
    }

    private void loadEconomyConfig() {
        File economyFile = new File(plugin.getDataFolder(), "economy.yml");

        if (!economyFile.exists()) {
            plugin.saveResource("economy.yml", false);
            logger.info("Created default economy.yml");
        }

        economy = new EconomyConfig(plugin, economyFile);
    }

    private void loadCollectionsConfig() {
        File collectionsFile = new File(plugin.getDataFolder(), "collections.yml");

        if (!collectionsFile.exists()) {
            plugin.saveResource("collections.yml", false);
            logger.info("Created default collections.yml");
        }

        collections = new CollectionConfig(plugin, collectionsFile);
    }

    private void loadGeneratorsConfig() {
        File generatorsFile = new File(plugin.getDataFolder(), "generators.yml");

        if (!generatorsFile.exists()) {
            plugin.saveResource("generators.yml", false);
            logger.info("Created default generators.yml");
        }

        generators = new GeneratorConfig(plugin, generatorsFile);
    }

    private void loadStructuresConfig() {
        File structuresFile = new File(plugin.getDataFolder(), "structures.yml");

        if (!structuresFile.exists()) {
            plugin.saveResource("structures.yml", false);
            logger.info("Created default structures.yml");
        }

        structures = new StructureConfig(plugin, structuresFile);
    }
}
