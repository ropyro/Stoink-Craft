package com.stoinkcraft.serialization;

import com.google.gson.Gson;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobsites.sites.JobSiteData;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.farmland.FarmlandData;
import com.stoinkcraft.jobsites.sites.graveyard.GraveyardData;
import com.stoinkcraft.jobsites.sites.quarry.QuarryData;
import com.stoinkcraft.jobsites.sites.skyrise.SkyriseData;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class JobSiteStorage {

    private static final File ENTERPRISES_DIR = new File(StoinkCore.getInstance().getDataFolder(), StorageConstants.ENTERPRISES_DIR);

    /**
     * Type registry mapping JobSiteType to its data class.
     */
    private static final Map<JobSiteType, Class<? extends JobSiteData>> DATA_CLASSES = Map.of(
            JobSiteType.SKYRISE, SkyriseData.class,
            JobSiteType.QUARRY, QuarryData.class,
            JobSiteType.FARMLAND, FarmlandData.class,
            JobSiteType.GRAVEYARD, GraveyardData.class
    );

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
            success &= saveJobSite(jobSitesDir, StorageConstants.SKYRISE_FILE, skyriseData, SkyriseData.class);
        }

        if (quarryData != null) {
            success &= saveJobSite(jobSitesDir, StorageConstants.QUARRY_FILE, quarryData, QuarryData.class);
        }

        if (farmlandData != null) {
            success &= saveJobSite(jobSitesDir, StorageConstants.FARMLAND_FILE, farmlandData, FarmlandData.class);
        }

        if (graveyardData != null) {
            success &= saveJobSite(jobSitesDir, StorageConstants.GRAVEYARD_FILE, graveyardData, GraveyardData.class);
        }

        return success;
    }

    private <T> boolean saveJobSite(File jobSitesDir, String fileName, T data, Class<T> clazz) {
        File jsonFile = new File(jobSitesDir, fileName);

        try {
            // Create rotating backup
            BackupManager.createRotatingBackup(jsonFile);

            // Write new data
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
                gson.toJson(data, clazz, writer);
            }

            return true;

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save job site: " + fileName, e);

            // Restore from most recent backup
            File backupFile = BackupManager.findMostRecentBackup(jsonFile);
            if (backupFile != null && (!jsonFile.exists() || jsonFile.length() == 0)) {
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
     * Generic method to load job site data by type.
     *
     * @param enterpriseID The enterprise UUID
     * @param type The job site type
     * @param <T> The data class type (must extend JobSiteData)
     * @return The loaded data, or null if not found or failed to load
     */
    @SuppressWarnings("unchecked")
    public <T extends JobSiteData> T loadJobSiteData(UUID enterpriseID, JobSiteType type) {
        Class<? extends JobSiteData> dataClass = DATA_CLASSES.get(type);
        if (dataClass == null) {
            Bukkit.getLogger().warning("Unknown job site type: " + type);
            return null;
        }

        String fileName = StorageConstants.getJobSiteFileName(type);
        File jobSitesDir = getJobSitesDirectory(enterpriseID);
        File jsonFile = new File(jobSitesDir, fileName);

        if (!jsonFile.exists()) {
            return null; // Will use defaults
        }

        try {
            return (T) loadJobSiteFromFile(jsonFile, dataClass);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE,
                    "Failed to load " + type.getDisplayName() + " data for: " + enterpriseID, e);

            // Try backup
            File backupFile = BackupManager.findMostRecentBackup(jsonFile);
            if (backupFile != null) {
                try {
                    Bukkit.getLogger().info("Attempting to load " + type.getDisplayName() + " from backup...");
                    return (T) loadJobSiteFromFile(backupFile, dataClass);
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Backup also failed!", ex);
                }
            }
            return null;
        }
    }

    /**
     * Load Skyrise data (convenience method for backward compatibility)
     */
    public SkyriseData loadSkyriseData(UUID enterpriseID) {
        return loadJobSiteData(enterpriseID, JobSiteType.SKYRISE);
    }

    /**
     * Load Quarry data (convenience method for backward compatibility)
     */
    public QuarryData loadQuarryData(UUID enterpriseID) {
        return loadJobSiteData(enterpriseID, JobSiteType.QUARRY);
    }

    /**
     * Load Farmland data (convenience method for backward compatibility)
     */
    public FarmlandData loadFarmlandData(UUID enterpriseID) {
        return loadJobSiteData(enterpriseID, JobSiteType.FARMLAND);
    }

    /**
     * Load Graveyard data (convenience method for backward compatibility)
     */
    public GraveyardData loadGraveyardData(UUID enterpriseID) {
        return loadJobSiteData(enterpriseID, JobSiteType.GRAVEYARD);
    }

    private <T> T loadJobSiteFromFile(File jsonFile, Class<T> clazz) throws IOException {
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
        return new File(ENTERPRISES_DIR, enterpriseID.toString() + "/" + StorageConstants.JOBSITES_SUBDIR);
    }
}
