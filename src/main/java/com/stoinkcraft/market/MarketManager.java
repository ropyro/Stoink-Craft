package com.stoinkcraft.market;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MarketManager {

    private static final Map<String, Double> resourcePrices = new HashMap<>();
    private static final Map<String, Double> huntingPrices = new HashMap<>();
    private static final Map<String, Double> fishingPrices = new HashMap<>();

    public static void loadMarketPrices(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        resourcePrices.clear();
        huntingPrices.clear();
        fishingPrices.clear();

        for (String key : config.getConfigurationSection("resource-collection").getKeys(false)) {
            resourcePrices.put(key.toUpperCase(), config.getDouble("resource-collection." + key));
        }

        for (String key : config.getConfigurationSection("monster-hunting").getKeys(false)) {
            huntingPrices.put(key.toUpperCase(), config.getDouble("monster-hunting." + key));
        }

        for (String key : config.getConfigurationSection("fishing").getKeys(false)) {
            fishingPrices.put(key.toUpperCase(), config.getDouble("fishing." + key));
        }
    }

    public static double getPrice(String material, JobType jobType) {
        return switch (jobType) {
            case RESOURCE_COLLECTION -> resourcePrices.getOrDefault(material.toUpperCase(), 0.0);
            case HUNTING -> huntingPrices.getOrDefault(material.toUpperCase(), 0.0);
            case FISHING -> fishingPrices.getOrDefault(material.toUpperCase(), 0.0);
        };
    }

    public static Map<String, Double> getPrices(JobType jobType){
        return switch (jobType) {
            case RESOURCE_COLLECTION -> resourcePrices;
            case HUNTING -> huntingPrices;
            case FISHING -> fishingPrices;
        };
    }

    public enum JobType {
        RESOURCE_COLLECTION, HUNTING, FISHING
    }
}
