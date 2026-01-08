package com.stoinkcraft.serialization;

import com.google.gson.Gson;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.jobsites.sites.farmland.FarmlandData;
import com.stoinkcraft.earning.jobsites.sites.graveyard.GraveyardData;
import com.stoinkcraft.earning.jobsites.sites.quarry.QuarryData;
import com.stoinkcraft.earning.jobsites.sites.skyrise.SkyriseData;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;

public class JobSiteStorage {

    private static final File ENTERPRISES_DIR = new File(StoinkCore.getInstance().getDataFolder(), "Enterprises");
    private final Gson gson;

    public JobSiteStorage(Gson gson) {
        this.gson = gson;
    }

    // ======= SAVE =======

    /**
     * Save all job site data for an enterprise
     */
    public boolean saveJobSites(UUID enterpriseID, SkyriseData skyriseData, QuarryData quarryData, FarmlandData farmlandData, GraveyardData graveyardData) {
        File jobSitesDir = getJobSitesDirectory(enterpriseID);
        if (!jobSitesDir.exists()) {
            jobSitesDir.mkdirs();
        }

        boolean success = true;

        if (skyriseData != null) {
            success &= saveJobSite(jobSitesDir, "skyrise.json", skyriseData, SkyriseData.class);
        }

        if (quarryData != null) {
            success &= saveJobSite(jobSitesDir, "quarry.json", quarryData, QuarryData.class);
        }

        if(farmlandData != null){
            success &= saveJobSite(jobSitesDir, "farmland.json", farmlandData, FarmlandData.class);
        }

        if(graveyardData != null){
            success &= saveJobSite(jobSitesDir, "graveyard.json", graveyardData, GraveyardData.class);
        }

        return success;
    }

    private <T> boolean saveJobSite(File jobSitesDir, String fileName, T data, Class<T> clazz) {
        File jsonFile = new File(jobSitesDir, fileName);
        File backupFile = new File(jobSitesDir, fileName + ".backup");

        try {
            // Create backup
            if (jsonFile.exists()) {
                Files.copy(jsonFile.toPath(), backupFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // Write new data
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
                gson.toJson(data, clazz, writer);
            }

            return true;

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save job site: " + fileName, e);

            // Restore backup
            if (backupFile.exists() && (!jsonFile.exists() || jsonFile.length() == 0)) {
                try {
                    Files.copy(backupFile.toPath(), jsonFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    Bukkit.getLogger().warning("Restored backup for: " + fileName);
                } catch (IOException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to restore backup!", ex);
                }
            }
            return false;
        }
    }

    // ======= LOAD =======

    /**
     * Load Skyrise data
     */
    public SkyriseData loadSkyriseData(UUID enterpriseID) {
        File jobSitesDir = getJobSitesDirectory(enterpriseID);
        File jsonFile = new File(jobSitesDir, "skyrise.json");

        if (!jsonFile.exists()) {
            return null; // Will use defaults
        }

        try {
            return loadJobSite(jsonFile, SkyriseData.class);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to load skyrise data for: " + enterpriseID, e);

            // Try backup
            File backupFile = new File(jobSitesDir, "skyrise.json.backup");
            if (backupFile.exists()) {
                try {
                    Bukkit.getLogger().info("Attempting to load from backup...");
                    return loadJobSite(backupFile, SkyriseData.class);
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Backup also failed!", ex);
                }
            }
            return null;
        }
    }

    /**
     * Load Quarry data
     */
    public QuarryData loadQuarryData(UUID enterpriseID) {
        File jobSitesDir = getJobSitesDirectory(enterpriseID);
        File jsonFile = new File(jobSitesDir, "quarry.json");

        if (!jsonFile.exists()) {
            return null; // Will use defaults
        }

        try {
            return loadJobSite(jsonFile, QuarryData.class);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to load quarry data for: " + enterpriseID, e);

            // Try backup
            File backupFile = new File(jobSitesDir, "quarry.json.backup");
            if (backupFile.exists()) {
                try {
                    Bukkit.getLogger().info("Attempting to load from backup...");
                    return loadJobSite(backupFile, QuarryData.class);
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Backup also failed!", ex);
                }
            }
            return null;
        }
    }

    public FarmlandData loadFarmlandData(UUID enterpriseID) {
        File jobSitesDir = getJobSitesDirectory(enterpriseID);
        File jsonFile = new File(jobSitesDir, "farmland.json");

        if (!jsonFile.exists()) {
            return null; // Will use defaults
        }

        try {
            return loadJobSite(jsonFile, FarmlandData.class);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to load farmland data for: " + enterpriseID, e);

            // Try backup
            File backupFile = new File(jobSitesDir, "farmland.json.backup");
            if (backupFile.exists()) {
                try {
                    Bukkit.getLogger().info("Attempting to load from backup...");
                    return loadJobSite(backupFile, FarmlandData.class);
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Backup also failed!", ex);
                }
            }
            return null;
        }
    }

    public GraveyardData loadGraveyardData(UUID enterpriseID) {
        File jobSitesDir = getJobSitesDirectory(enterpriseID);
        File jsonFile = new File(jobSitesDir, "graveyard.json");

        if (!jsonFile.exists()) {
            return null; // Will use defaults
        }

        try {
            return loadJobSite(jsonFile, GraveyardData.class);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to load graveyard data for: " + enterpriseID, e);

            // Try backup
            File backupFile = new File(jobSitesDir, "graveyard.json.backup");
            if (backupFile.exists()) {
                try {
                    Bukkit.getLogger().info("Attempting to load from backup...");
                    return loadJobSite(backupFile, GraveyardData.class);
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Backup also failed!", ex);
                }
            }
            return null;
        }
    }

    private <T> T loadJobSite(File jsonFile, Class<T> clazz) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, clazz);
        }
    }

    // ======= DELETE =======

    /**
     * Delete all job site data for an enterprise
     */
    public void deleteJobSites(UUID enterpriseID) {
        File jobSitesDir = getJobSitesDirectory(enterpriseID);
        if (jobSitesDir.exists()) {
            deleteDirectory(jobSitesDir);
        }
    }

    private boolean deleteDirectory(File dir) {
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

    // ======= UTILITY =======

    private File getJobSitesDirectory(UUID enterpriseID) {
        return new File(ENTERPRISES_DIR, enterpriseID.toString() + "/jobsites");
    }
}