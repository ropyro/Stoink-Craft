package com.stoinkcraft.market;

import com.stoinkcraft.market.values.EntityValue;
import com.stoinkcraft.market.values.ItemValue;
import com.stoinkcraft.market.values.TaskValue;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class MarketManager {

    private static final Map<TaskValue, JobType> boostedPrices = new HashMap<>();
    private static final List<ItemValue> resourcePrices = new ArrayList<>();
    private static final List<EntityValue> huntingPrices = new ArrayList<>();
    private static final List<ItemValue> fishingPrices = new ArrayList<>();
    private static Instant lastRotationTime;
    private static final Duration ROTATION_INTERVAL = Duration.ofDays(1);

    public static void startRotatingBoosts(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                rotateBoostedItemsAsync(plugin);
                lastRotationTime = Instant.now();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 1728000L); // 24 hours async
    }

    public static void rotateBoostedItemsAsync(JavaPlugin plugin) {
        // Run the heavy logic async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boostedPrices.clear();

            List<TaskValue> allItems = new ArrayList<>();
            allItems.addAll(resourcePrices);
            allItems.addAll(huntingPrices);
            allItems.addAll(fishingPrices);

            Collections.shuffle(allItems);

            int boostCount = Math.min(4, allItems.size());
            for (int i = 0; i < boostCount; i++) {
                if(resourcePrices.contains(allItems.get(i))){
                    boostedPrices.put(allItems.get(i), JobType.RESOURCE_COLLECTION);
                }else if(huntingPrices.contains(allItems.get(i))){
                    boostedPrices.put(allItems.get(i), JobType.HUNTING);
                }else if(fishingPrices.contains(allItems.get(i))){
                    boostedPrices.put(allItems.get(i), JobType.FISHING);
                }
            }

            // Schedule the broadcast back on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.broadcastMessage("§6Today's boosted items have reset! Check them out in §e/market");
            });
        });
    }

    public static String getTimeUntilNextRotation() {
        if (lastRotationTime == null) return "Unknown";

        Instant nextRotation = lastRotationTime.plus(ROTATION_INTERVAL);
        Duration remaining = Duration.between(Instant.now(), nextRotation);

        if (remaining.isNegative()) return "00h 00m 00s";

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
            try {
                Material material = Material.valueOf(key.toUpperCase());
                double price = config.getDouble("resource-collection." + key);
                resourcePrices.add(new ItemValue(material, price));
            } catch (IllegalArgumentException ignored) {
                Bukkit.getLogger().warning("Invalid material in resource-collection: " + key);
            }
        }

        for (String key : config.getConfigurationSection("monster-hunting").getKeys(false)) {
            try {
                EntityType entityType = EntityType.valueOf(key.toUpperCase());
                double price = config.getDouble("monster-hunting." + key);
                huntingPrices.add(new EntityValue(entityType, price));
            } catch (IllegalArgumentException ignored) {
                Bukkit.getLogger().warning("Invalid entity type in monster-hunting: " + key);
            }
        }

        for (String key : config.getConfigurationSection("fishing").getKeys(false)) {
            try {
                Material material = Material.valueOf(key.toUpperCase());
                double price = config.getDouble("fishing." + key);
                fishingPrices.add(new ItemValue(material, price));
            } catch (IllegalArgumentException ignored) {
                Bukkit.getLogger().warning("Invalid material in fishing: " + key);
            }
        }
    }


    public static Map<TaskValue, JobType> getBoostedPrices(){
        return boostedPrices;
    }


    public static double getItemPrice(Material material) {
        double value = 0.0;

        value += resourcePrices.stream()
                .filter(e -> e.getMaterial().equals(material))
                .mapToDouble(v -> v.getValue())
                .findFirst()
                .orElse(0.0);

        value += fishingPrices.stream()
                .filter(e -> e.getMaterial().equals(material))
                .mapToDouble(v -> v.getValue())
                .findFirst()
                .orElse(0.0);

        boolean isBoosted = boostedPrices.keySet().stream()
                .anyMatch(e -> e.getMaterialValue().equals(material));

        if (isBoosted) {
            value *= SCConstants.PRICE_BOOST;
        }

        return value;
    }


    public static double getPrice(TaskValue taskValue){
        List<TaskValue> allItems = new ArrayList<>();
        allItems.addAll(resourcePrices);
        allItems.addAll(huntingPrices);
        allItems.addAll(fishingPrices);

        return allItems.get(allItems.indexOf(taskValue)).getValue();
    }

    public static double getEntityPrice(EntityType entityType){
        double value = huntingPrices.stream().filter(e -> e.getEntityType().equals(entityType)).findFirst().get().getValue();
        if(boostedPrices.keySet().stream().filter(e -> e instanceof EntityValue).map(e -> (EntityValue)e).toList().stream().filter(entityValue -> entityValue.getEntityType().equals(entityType)).toList().size() > 0){
            value *= SCConstants.PRICE_BOOST;
        }
        return value;
    }

    public static List<ItemValue> getResourcePrices(){
        return resourcePrices;
    }
    public static List<ItemValue> getFishingPrices(){
        return fishingPrices;
    }
    public static List<EntityValue> getHuntingPrices(){
        return huntingPrices;
    }

        public enum JobType {
        RESOURCE_COLLECTION, HUNTING, FISHING
    }
}
