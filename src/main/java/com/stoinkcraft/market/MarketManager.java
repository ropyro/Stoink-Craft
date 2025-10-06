package com.stoinkcraft.market;

import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class MarketManager {

    private static final List<String> boostedPrices = new ArrayList<>();
    private static final Map<String, Double> resourcePrices = new HashMap<>();
    private static final Map<String, Double> huntingPrices = new HashMap<>();
    private static final Map<String, Double> fishingPrices = new HashMap<>();

    private static Instant lastRotationTime;
    private static final Duration ROTATION_INTERVAL = Duration.ofDays(1);
    public static void startRotatingBoosts(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                rotateBoostedItems();
                lastRotationTime = Instant.now();
            }
        }.runTaskTimer(plugin, 0L, 1728000L); // 24 hours
    }

    public static void rotateBoostedItems() {
        boostedPrices.clear();

        Set<String> allItems = new HashSet<>();
        allItems.addAll(resourcePrices.keySet());
        allItems.addAll(huntingPrices.keySet());
        allItems.addAll(fishingPrices.keySet());

        List<String> itemPool = new ArrayList<>(allItems);
        Collections.shuffle(itemPool);

        int boostCount = Math.min(4, itemPool.size());
        for (int i = 0; i < boostCount; i++) {
            boostedPrices.add(itemPool.get(i));
        }

        Bukkit.broadcastMessage("§6Today's boosted items have reset check them out in §e/market");
    }

    public static String getTimeUntilNextRotation() {
        if (lastRotationTime == null) {
            return "Unknown";
        }

        Instant nextRotation = lastRotationTime.plus(ROTATION_INTERVAL);
        Duration remaining = Duration.between(Instant.now(), nextRotation);

        if (remaining.isNegative()) {
            return "00h 00m 00s";
        }

        long hours = remaining.toHours();
        long minutes = remaining.toMinutesPart();
        long seconds = remaining.toSecondsPart();

        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }


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

    public static List<String> getBoostedPrices(){
        return boostedPrices;
    }

    public static double getPrice(String material) {
        String key = material.toUpperCase();
        double value = 0.0;
        if (resourcePrices.containsKey(key)) value = resourcePrices.getOrDefault(key, 0.0);
        if (huntingPrices.containsKey(key)) value = huntingPrices.getOrDefault(key, 0.0);
        if (fishingPrices.containsKey(key)) value = fishingPrices.getOrDefault(key, 0.0);
        if(boostedPrices.contains(material)) {
            value *= SCConstants.PRICE_BOOST;
        }
        return value;
    }


    public static double getPrice(String material, JobType jobType) {
        double value = 0.0;
         switch (jobType) {
             case RESOURCE_COLLECTION -> value = resourcePrices.getOrDefault(material.toUpperCase(), 0.0);
             case HUNTING -> value = huntingPrices.getOrDefault(material.toUpperCase(), 0.0);
             case FISHING -> value = fishingPrices.getOrDefault(material.toUpperCase(), 0.0);
         }
        if(boostedPrices.contains(material)){
            value *= SCConstants.PRICE_BOOST;
        }
        return value;
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
