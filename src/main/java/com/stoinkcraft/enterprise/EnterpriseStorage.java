package com.stoinkcraft.enterprise;

import com.stoinkcraft.StoinkCore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.stoinkcraft.StoinkCore;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EnterpriseStorage {

    private static final File ENTERPRISES_DIR = new File(StoinkCore.getInstance().getDataFolder(), "Enterprises");

    // ======= SAVE =======
    public static void saveAllEnterprises() {
        if (!ENTERPRISES_DIR.exists()) ENTERPRISES_DIR.mkdirs();

        for (Enterprise e : EnterpriseManager.getEnterpriseManager().getEnterpriseList()) {
            File entDir = new File(ENTERPRISES_DIR, e.getID().toString());
            if (!entDir.exists()) entDir.mkdirs();

            saveEnterpriseData(e, new File(entDir, "enterprise.yml"));
            savePriceHistory(e, new File(entDir, "pricehistory.yml"));
        }
    }

    public static void disband(Enterprise e){
        File entDir = new File(ENTERPRISES_DIR, e.getID().toString());
        if (deleteDirectory(entDir)) {
            Bukkit.getLogger().info("Deleted enterprise folder: " + e.getName());
        } else {
            Bukkit.getLogger().warning("Failed to delete enterprise folder: " + e.getName());
        }
    }


    public static boolean deleteDirectory(File dir) {
        if (!dir.exists()) return true;

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return dir.delete();
    }


    private static void saveEnterpriseData(Enterprise e, File file) {
        YamlConfiguration config = new YamlConfiguration();

        config.set("name", e.getName());
        config.set("ceo", e.getCeo().toString());
        config.set("bankBalance", e.getBankBalance());
        config.set("netWorth", e.getNetWorth());
        config.set("outstandingShares", e.getOutstandingShares());
        config.set("serverOwned", e instanceof ServerEnterprise);

        // Members
        Map<String, String> memberMap = new HashMap<>();
        e.getMembers().forEach((uuid, role) -> memberMap.put(uuid.toString(), role.name()));
        config.set("members", memberMap);
        
//        long timeRemaining = e.getActiveBooster().getTimeRemaining();
//        double multiplier = e.getActiveBooster().getMultiplier();
//
//        config.set("activebooster.timeRemaining", timeRemaining);
//        config.set("activebooster.multiplier", multiplier);

        // Warp
        if (e.getWarp() != null) {
            Location loc = e.getWarp();
            config.set("warp.world", loc.getWorld().getName());
            config.set("warp.x", loc.getX());
            config.set("warp.y", loc.getY());
            config.set("warp.z", loc.getZ());
            config.set("warp.yaw", loc.getYaw());
            config.set("warp.pitch", loc.getPitch());
        }

        config.set("jobsites.plotIndex", e.getPlotIndex());
        config.set("jobsites.skyrise.isBuilt", e.getJSM().getSkyriseSite().isBuilt());
        config.set("jobsites.quarry.isBuilt", e.getJSM().getQuarrySite().isBuilt());

        try {
            config.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void savePriceHistory(Enterprise e, File file) {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList<>();

        for (PriceSnapshot snapshot : e.getPriceHistory()) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("time", snapshot.getTimestamp());
            entry.put("value", snapshot.getSharePrice());
            list.add(entry);
        }

        config.set("priceHistory", list);

        try {
            config.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // ======= LOAD =======
    public static void loadAllEnterprises() {
        if (!ENTERPRISES_DIR.exists()) return;

        EnterpriseManager manager = EnterpriseManager.getEnterpriseManager();

        for (File folder : Objects.requireNonNull(ENTERPRISES_DIR.listFiles(File::isDirectory))) {
            File mainFile = new File(folder, "enterprise.yml");
            File historyFile = new File(folder, "pricehistory.yml");
            if (!mainFile.exists()) continue;

            Enterprise e = loadEnterprise(mainFile);
            loadPriceHistory(e, historyFile);
            manager.loadEnterprise(e); // Add to list
        }

        // After all are loaded, sync plot indexes
        StoinkCore.getEnterprisePlotManager().resetNextIndex(manager.getEnterpriseList());
    }


    private static Enterprise loadEnterprise(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String name = config.getString("name");
        UUID ceo = UUID.fromString(config.getString("ceo"));
        boolean serverOwned = config.getBoolean("serverOwned");
        UUID id = UUID.fromString(file.getParentFile().getName());

        Enterprise e = serverOwned ? new ServerEnterprise(name) : new Enterprise(name, ceo);
        e.setNetWorth(config.getDouble("netWorth"));
        e.setBankBalance(config.getDouble("bankBalance"));
        e.addOutstandingShares(config.getInt("outstandingShares"));
        e.setEnterpriseID(id);

        e.setPlotIndex(config.getInt("jobsites.plotIndex", -1));
        e.initializeJobSiteManager(config.getBoolean("jobsites.skyrise.isBuilt", false),
                config.getBoolean("jobsites.quarry.isBuilt", false));

        // Members
        if (config.isConfigurationSection("members")) {
            for (String uuidStr : config.getConfigurationSection("members").getKeys(false)) {
                Role role = Role.valueOf(config.getString("members." + uuidStr));
                e.getMembers().put(UUID.fromString(uuidStr), role);
            }
        }

        // Warp
        if (config.isConfigurationSection("warp")) {
            String worldName = config.getString("warp.world");
            World world = Bukkit.getWorld(worldName);
            double x = config.getDouble("warp.x");
            double y = config.getDouble("warp.y");
            double z = config.getDouble("warp.z");
            float yaw = (float) config.getDouble("warp.yaw");
            float pitch = (float) config.getDouble("warp.pitch");

            if (world != null) {
                e.setWarp(new Location(world, x, y, z, yaw, pitch));
            }
        }

        return e;
    }

    private static void loadPriceHistory(Enterprise e, File file) {
        if (!file.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        if (config.isList("priceHistory")) {
            for (Map<?, ?> entry : config.getMapList("priceHistory")) {
                long time = ((Number) entry.get("time")).longValue();
                double value = ((Number) entry.get("value")).doubleValue();
                e.getPriceHistory().add(new PriceSnapshot(time, value));
            }
        }
    }
}
