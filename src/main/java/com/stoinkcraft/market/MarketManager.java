package com.stoinkcraft.market;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MarketManager {

    private static final Map<String, Double> values = new HashMap<>();

    public static void loadMarketPrices(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            double value = config.getDouble(key);
            values.put(key.toUpperCase(), value);
        }
    }

    public static double getValue(String materialName) {
        return values.getOrDefault(materialName.toUpperCase(), 0.0);
    }
}