package com.stoinkcraft.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.jobsites.contracts.ActiveContract;
import com.stoinkcraft.jobsites.contracts.ContractManager;
import com.stoinkcraft.jobsites.sites.farmland.FarmlandData;
import com.stoinkcraft.jobsites.sites.graveyard.GraveyardData;
import com.stoinkcraft.jobsites.sites.quarry.QuarryData;
import com.stoinkcraft.jobsites.sites.skyrise.SkyriseData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class EnterpriseStorageJson {

    private static final File ENTERPRISES_DIR = new File(StoinkCore.getInstance().getDataFolder(), "Enterprises");
    private static Gson gson;
    private static JobSiteStorage jobSiteStorage;
    private static ContractStorage contractStorage;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new GsonAdapters.LocationAdapter())
                .registerTypeAdapter(UUID.class, new GsonAdapters.UUIDAdapter())
                .registerTypeAdapter(Vector.class, new VectorAdapter())
                .registerTypeAdapter(Enterprise.class, new EnterpriseTypeAdapter())
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        jobSiteStorage = new JobSiteStorage(gson);
        contractStorage = new ContractStorage(gson);
    }

    // ======= SAVE =======

    public static void saveAllEnterprisesAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllEnterprises();
            }
        }.runTaskAsynchronously(StoinkCore.getInstance());
    }

    public static void saveAllEnterprises() {
        if (!ENTERPRISES_DIR.exists()) {
            ENTERPRISES_DIR.mkdirs();
        }

        int saved = 0;
        for (Enterprise enterprise : EnterpriseManager.getEnterpriseManager().getEnterpriseList()) {
            if (saveEnterprise(enterprise)) {
                saved++;
            }
        }

        Bukkit.getLogger().info("Saved " + saved + " enterprises with job sites.");
    }

    public static boolean saveEnterprise(Enterprise enterprise) {
        File entDir = new File(ENTERPRISES_DIR, enterprise.getID().toString());
        if (!entDir.exists()) {
            entDir.mkdirs();
        }

        File jsonFile = new File(entDir, "enterprise.json");
        File backupFile = new File(entDir, "enterprise.json.backup");

        try {
            // Create backup
            if (jsonFile.exists()) {
                Files.copy(jsonFile.toPath(), backupFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // Write enterprise data
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
                gson.toJson(enterprise, Enterprise.class, writer);
            }

            // Save job site data separately
            saveJobSites(enterprise);

            //save contracts
            saveContracts(enterprise);

            return true;

        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to save enterprise: " + enterprise.getName(), e);

            // Restore backup
            if (backupFile.exists() && (!jsonFile.exists() || jsonFile.length() == 0)) {
                try {
                    Files.copy(backupFile.toPath(), jsonFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    Bukkit.getLogger().warning("Restored backup for enterprise: " + enterprise.getName());
                } catch (IOException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Failed to restore backup!", ex);
                }
            }
            return false;
        }
    }

    private static void saveJobSites(Enterprise enterprise) {
        if (enterprise.getJobSiteManager() == null) return;

        SkyriseData skyriseData = enterprise.getJobSiteManager().getSkyriseData();
        QuarryData quarryData = enterprise.getJobSiteManager().getQuarryData();
        FarmlandData farmlandData = enterprise.getJobSiteManager().getFarmlandData();
        GraveyardData graveyardData = enterprise.getJobSiteManager().getGraveyardData();

        jobSiteStorage.saveJobSites(enterprise.getID(), skyriseData, quarryData, farmlandData, graveyardData);
    }

    private static void saveContracts(Enterprise enterprise){
        contractStorage.saveContracts(enterprise.getID(), StoinkCore.getInstance().getContractManager().getContracts(enterprise));
    }

    // ======= LOAD =======

    public static void loadAllEnterprises(boolean loadJobSites) {
        if (!ENTERPRISES_DIR.exists()) {
            Bukkit.getLogger().info("No enterprises directory found. Creating...");
            ENTERPRISES_DIR.mkdirs();
            return;
        }

        // Check for YAML migration
        if (EnterpriseMigration.hasYamlFilesToMigrate()) {
            int yamlCount = EnterpriseMigration.getYamlFilesCount();
            Bukkit.getLogger().warning("==============================================");
            Bukkit.getLogger().warning("Found " + yamlCount + " YAML enterprises to migrate!");
            Bukkit.getLogger().warning("Run '/se migrate' to convert to JSON");
            Bukkit.getLogger().warning("==============================================");
        }

        EnterpriseManager manager = EnterpriseManager.getEnterpriseManager();
        int loaded = 0;
        int failed = 0;

        File[] folders = ENTERPRISES_DIR.listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) {
            Bukkit.getLogger().info("No enterprises to load.");
            return;
        }

        for (File folder : folders) {
            File jsonFile = new File(folder, "enterprise.json");

            if (!jsonFile.exists()) {
                continue;
            }

            try {
                Enterprise enterprise = loadEnterprise(jsonFile);
                if (enterprise != null) {
                    // Only load job sites if Citizens is ready
                    if (loadJobSites) {
                        loadJobSites(enterprise);
                    }
                    // Load contracts (these don't depend on Citizens)
                    loadContracts(enterprise);

                    manager.loadEnterprise(enterprise);
                    loaded++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to load enterprise from: " + jsonFile.getPath(), e);
                failed++;

                // Try backup
                File backupFile = new File(folder, "enterprise.json.backup");
                if (backupFile.exists()) {
                    try {
                        Bukkit.getLogger().info("Attempting to load from backup...");
                        Enterprise enterprise = loadEnterprise(backupFile);
                        if (enterprise != null) {
                            if (loadJobSites) {
                                loadJobSites(enterprise);
                            }
                            loadContracts(enterprise);
                            manager.loadEnterprise(enterprise);
                            loaded++;
                            failed--;
                            Bukkit.getLogger().info("Successfully loaded from backup!");
                        }
                    } catch (Exception ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "Backup also failed!", ex);
                    }
                }
            }
        }

        Bukkit.getLogger().info("Loaded " + loaded + " enterprises" + (loadJobSites ? " with job sites" : " (job sites pending)") + ". Failed: " + failed);

        // Sync plot indexes after loading
        StoinkCore.getInstance().getEnterprisePlotManager().resetNextIndex(manager.getEnterpriseList());
    }

    public static void loadAllJobSitesDeferred() {
        EnterpriseManager manager = EnterpriseManager.getEnterpriseManager();
        int count = 0;

        for (Enterprise enterprise : manager.getEnterpriseList()) {
            try {
                loadJobSites(enterprise);
                count++;
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to load job sites for enterprise: " + enterprise.getID(), e);
            }
        }

        Bukkit.getLogger().info("Loaded job sites for " + count + " enterprises.");
    }

    private static Enterprise loadEnterprise(File jsonFile) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8)) {
            Enterprise enterprise = gson.fromJson(reader, Enterprise.class);

            if (enterprise == null) {
                Bukkit.getLogger().warning("Null enterprise loaded from: " + jsonFile.getPath());
                return null;
            }

            return enterprise;
        }
    }

    private static void loadJobSites(Enterprise enterprise) {
        // Initialize the manager
        enterprise.initializeJobSiteManager();

        // Load job site data from separate files
        SkyriseData skyriseData = jobSiteStorage.loadSkyriseData(enterprise.getID());
        QuarryData quarryData = jobSiteStorage.loadQuarryData(enterprise.getID());
        FarmlandData farmlandData = jobSiteStorage.loadFarmlandData(enterprise.getID());
        GraveyardData graveyardData = jobSiteStorage.loadGraveyardData(enterprise.getID());

        // Initialize job sites with loaded data (or defaults if null)
        enterprise.getJobSiteManager().initializeJobSites(skyriseData, quarryData, farmlandData, graveyardData);
    }

    private static void loadContracts(Enterprise enterprise){
        ContractManager contractManager =
                StoinkCore.getInstance().getContractManager();

        List<ActiveContract> loaded =
                contractStorage.loadContracts(
                        enterprise.getID(),
                        contractManager.getContractPool()
                );

        contractManager.setContracts(enterprise, loaded);
    }

    // ======= DELETE =======

    public static void disband(Enterprise enterprise) {
        File entDir = new File(ENTERPRISES_DIR, enterprise.getID().toString());

        // Delete job sites data
        jobSiteStorage.deleteJobSites(enterprise.getID());

        // Delete enterprise directory
        if (deleteDirectory(entDir)) {
            Bukkit.getLogger().info("Deleted enterprise folder: " + enterprise.getName());
        } else {
            Bukkit.getLogger().warning("Failed to delete enterprise folder: " + enterprise.getName());
        }
    }

    private static boolean deleteDirectory(File dir) {
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

    public static Gson getGson() {
        return gson;
    }

    public static JobSiteStorage getJobSiteStorage() {
        return jobSiteStorage;
    }

    public static ContractStorage getContractStorage(){
        return contractStorage;
    }
}