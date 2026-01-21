package com.stoinkcraft;

import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.earning.jobsites.protection.ProtectionListeners;
import com.stoinkcraft.earning.jobsites.protection.ProtectionManager;
import com.stoinkcraft.enterprise.*;
import com.stoinkcraft.enterprise.listeners.*;
import com.stoinkcraft.earning.contracts.ContractFeedbackManager;
import com.stoinkcraft.earning.contracts.ContractPoolLoader;
import com.stoinkcraft.earning.contracts.ContractManager;
import com.stoinkcraft.earning.listeners.*;
import com.stoinkcraft.misc.CitizensLoadListener;
import com.stoinkcraft.misc.daily.DailyCMD;
import com.stoinkcraft.misc.daily.DailyManager;
import com.stoinkcraft.earning.boosters.BoostNoteInteractionListener;
import com.stoinkcraft.enterprise.commands.TopCeoCMD;
import com.stoinkcraft.enterprise.commands.enterprisecmd.EnterpriseCMD;
import com.stoinkcraft.enterprise.commands.enterprisecmd.EnterpriseTabCompleter;
import com.stoinkcraft.enterprise.commands.serverenterprisecmd.ServerEntCMD;
import com.stoinkcraft.enterprise.commands.serverenterprisecmd.ServerEntTabCompleter;
import com.stoinkcraft.misc.EnderChestListener;
import com.stoinkcraft.misc.playerupgrades.PMenuCommand;
import com.stoinkcraft.earning.jobsites.commands.FarmlandCommand;
import com.stoinkcraft.earning.jobsites.commands.QuarryCommand;
import com.stoinkcraft.earning.jobsites.commands.GraveyardCommand;
import com.stoinkcraft.serialization.EnterpriseStorageJson;
import com.stoinkcraft.enterprise.shares.SharesCMD;
import com.stoinkcraft.enterprise.shares.ShareManager;
import com.stoinkcraft.enterprise.shares.ShareStorage;
import com.stoinkcraft.misc.PhantomSpawnDisabler;
import com.stoinkcraft.misc.StoinkExpansion;
import com.stoinkcraft.earning.worlds.EnterprisePlotManager;
import com.stoinkcraft.earning.worlds.EnterpriseWorldManager;
import com.stoinkcraft.utils.ContractTimeUtil;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.xenondevs.invui.InvUI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StoinkCore extends JavaPlugin {

    private static Economy econ = null;
    private static StoinkCore INSTANCE;

    private boolean jobSitesLoaded = false;

    private EnterpriseManager em;
    public EnterpriseManager getEnterpriseManager(){
        return em;
    }

    private DailyManager dm;
    public DailyManager getDailyManager(){
        return dm;
    }

    private ShareManager sm;
    public ShareManager getShareManager(){
        return sm;
    }

    private EnterpriseWorldManager ewm;
    public EnterpriseWorldManager getEnterpriseWorldManager(){
        return ewm;
    }

    private EnterprisePlotManager epm;
    public EnterprisePlotManager getEnterprisePlotManager(){
        return epm;
    }

    private ContractManager cm;
    public ContractManager getContractManager(){
        return cm;
    }

    private ContractFeedbackManager cfm;
    public ContractFeedbackManager getContractFeedbackManager(){
        return cfm;
    }

    private ProtectionManager pm;
    public ProtectionManager getProtectionManager() {return pm;}


    @Override
    public void onDisable() {
        getLogger().info("Disabling StoinkCore...");

        cfm.clearAll();

        EnterpriseStorageJson.saveAllEnterprises();

        try {
            ShareStorage.saveShares();
        } catch (Exception ex) {
            getLogger().severe("Failed to save shares: " + ex.getMessage());
        }

        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        ConfigLoader.initialize(this);

        registerCommands();

        hookLibraries();
        initManagers();
        initFilesAndResources();
        registerListeners();
        ensureServerEnterprises();
        startTasks();

        // Handle hot reload scenario: if Citizens is already fully loaded,
        // CitizensEnableEvent won't fire again. Check on next tick if Citizens
        // has NPCs in registry (indicating it's already initialized)
        Bukkit.getScheduler().runTask(this, this::checkCitizensAlreadyLoaded);

        getLogger().info("StoinkCore loaded.");
    }

    /**
     * Checks if Citizens was already loaded before StoinkCore (hot reload scenario).
     * This handles the case where only StoinkCore is reloaded (not Citizens).
     * We delay the check to give CitizensEnableEvent a chance to fire first.
     */
    private void checkCitizensAlreadyLoaded() {
        // Delay check by 10 ticks to allow CitizensEnableEvent to fire if Citizens is also reloading
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (jobSitesLoaded) return; // Event already fired, we're good

            try {
                // If we get here, CitizensEnableEvent didn't fire - true hot reload of StoinkCore only
                // Check if Citizens has NPCs loaded (indicating it's truly ready)
                if (CitizensAPI.getNPCRegistry() != null && CitizensAPI.getNPCRegistry().iterator().hasNext()) {
                    getLogger().info("Hot reload detected (StoinkCore only), Citizens has NPCs. Initializing...");
                    onCitizensReady();
                }
            } catch (Exception e) {
                // Citizens not ready yet, event listener will handle it
            }
        }, 10L);
    }

    private void hookLibraries(){
        //GUI util hook
        InvUI.getInstance().setPlugin(this);

        //Vault hook
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //PAPI Expansion
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new StoinkExpansion(this).register();
        }
    }

    private void initManagers(){
        dm = new DailyManager();
        em = new EnterpriseManager(this, 6);
        sm = new ShareManager();
        ewm = new EnterpriseWorldManager();
        epm = new EnterprisePlotManager(ewm);
        cm = new ContractManager(ContractPoolLoader.load());
        cfm = new ContractFeedbackManager();
        pm = new ProtectionManager(this);
    }

    private void initFilesAndResources(){
        File marketFile = new File(getDataFolder(), "market.yml");
        if(!marketFile.exists()){
            saveResource("market.yml", false);
        }

        saveResourceFolder("schematics", false);

        // Load enterprises with job sites - NPCs are created as objects but not
        // looked up in Citizens registry yet (deferred to CitizensEnableEvent)
        EnterpriseStorageJson.loadAllEnterprises(true);
        getLogger().info("Enterprises and job sites loaded. Waiting for CitizensEnableEvent to initialize NPCs...");

        ShareStorage.loadShares();
    }

    private void indexProtectionRegions(){
        try{
            getLogger().info("Indexing protection regions...");
            pm.rebuildIndex();
        }catch (Exception e){
            getLogger().info(e.getMessage());
            return;
        }
        getLogger().info("Protection regions successfully indexed!");
    }

    /**
     * Called by CitizensLoadListener when CitizensEnableEvent fires.
     * This event fires AFTER Citizens has loaded all NPCs into the registry.
     */
    public void onCitizensReady() {
        if (jobSitesLoaded) return;
        jobSitesLoaded = true;

        getLogger().info("Citizens ready, initializing NPCs from registry...");

        // Initialize NPCs from Citizens registry for all enterprises
        for (Enterprise enterprise : em.getEnterpriseList()) {
            if (enterprise.getJobSiteManager() != null) {
                enterprise.getJobSiteManager().initializeNpcsFromRegistry();
            }
        }

        indexProtectionRegions();
        getLogger().info("NPC initialization complete.");
    }

    private void ensureServerEnterprises() {
        if (em.getEnterpriseList().stream().noneMatch(e -> e instanceof ServerEnterprise)) {
            StoinkCore.getInstance().getLogger().info("Adding server enterprises...");
            createDefaultServerEnterprise(em, "FarmerLLC");
            createDefaultServerEnterprise(em, "MinerCorp");
            createDefaultServerEnterprise(em, "HunterInc");
        }
    }

    private void createDefaultServerEnterprise(EnterpriseManager mgr, String name) {
        ServerEnterprise ent = new ServerEnterprise(name);
        mgr.createEnterprise(ent);
    }

    private void registerCommands(){
        EnterpriseCMD enterpriseCMD = new EnterpriseCMD(this);
        getCommand("enterprise").setExecutor(enterpriseCMD);
        getCommand("enterprise").setTabCompleter(new EnterpriseTabCompleter(enterpriseCMD.getSubcommands()));
        getCommand("serverenterprise").setExecutor(new ServerEntCMD());
        getCommand("serverenterprise").setTabCompleter(new ServerEntTabCompleter());
        getCommand("topceo").setExecutor(new TopCeoCMD());
        getCommand("shares").setExecutor(new SharesCMD());
        getCommand("daily").setExecutor(new DailyCMD());
        getCommand("pmenu").setExecutor(new PMenuCommand());
        getCommand("farmland").setExecutor(new FarmlandCommand());
        getCommand("quarry").setExecutor(new QuarryCommand());
        getCommand("graveyard").setExecutor(new GraveyardCommand());
    }

    public void registerListeners(){
        //Register listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        //Chat money listeners
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        //Misc listeners
        getServer().getPluginManager().registerEvents(new PhantomSpawnDisabler(), this);
        getServer().getPluginManager().registerEvents(new BoostNoteInteractionListener(), this);
        getServer().getPluginManager().registerEvents(new EnderChestListener(), this);
        getServer().getPluginManager().registerEvents(new CitizensLoadListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListeners(this), this);
        //Jobsite Listeners
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(ewm.getWorld()), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new NPCInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new HologramClickListener(this), this);
    }

    private void startTasks(){
        em.startJobSiteTicker(this);
        startPriceSnapshotRecording();
        startAutoTopCEOUpdate();
        startContractResetTask();
        cfm.startCleanupTask(this);

        // Periodic autosave (every 5 minutes):
        new BukkitRunnable() {
            @Override
            public void run() {
                EnterpriseStorageJson.saveAllEnterprisesAsync();
            }
        }.runTaskTimerAsynchronously(this, 20L * 60 * 20, 20L * 60 * 20);

        //em.startDailyTaxes(this);
    }

    public static StoinkCore getInstance() {
        return INSTANCE;
    }

    public void startContractResetTask() {
        // Immediately clean up any expired contracts from before plugin start
        getContractManager().handleDailyReset();
        getContractManager().handleWeeklyReset();

        // Calculate initial delay until next contract expiry (midnight)
        long now = System.currentTimeMillis();
        long initialDelay = ContractTimeUtil.nextDay() - now;
        long dailyPeriod = 24L * 60 * 60 * 1000; // 24 hours in ms

        // Convert to ticks (1 tick = 50ms)
        long initialDelayTicks = initialDelay / 50;
        long periodTicks = dailyPeriod / 50;

        // Repeating daily reset task - starts at next midnight, repeats every 24 hours
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            getContractManager().handleDailyReset();

            // Check if it's also Monday for weekly reset
            if (java.time.LocalDate.now().getDayOfWeek() == java.time.DayOfWeek.MONDAY) {
                getContractManager().handleWeeklyReset();
            }
        }, initialDelayTicks, periodTicks);
    }

    private void startPriceSnapshotRecording(){
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            EnterpriseManager.getEnterpriseManager().recordPriceSnapshots();
        }, 0L, 20L * 60 * 5);
    }

    public void updateTopCeoNpcs() {
        // Run async for data sorting and lookups
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            List<Enterprise> sorted = EnterpriseManager
                    .getEnterpriseManager()
                    .getEnterpriseList()
                    .stream()
                    .sorted(Comparator.comparingDouble(Enterprise::getNetWorth).reversed())
                    .collect(Collectors.toList());

            List<TopCeoData> top3 = new ArrayList<>();
            for (int i = 1; i <= 3 && i <= sorted.size(); i++) {
                Enterprise e = sorted.get(i - 1);
                OfflinePlayer ceo = Bukkit.getOfflinePlayer(e.getCeo());
                String ceoName = ceo.getName() != null ? ceo.getName() : "CEO";
                String displayName = "#" + i + " " + ChatColor.GREEN + ChatColor.BOLD + e.getName();
                top3.add(new TopCeoData(i, ceoName, displayName));
            }

            // Update NPCs safely on the main thread
            Bukkit.getScheduler().runTask(this, () -> {
                for (TopCeoData data : top3) {
                    NPC npc = getNpcByPosition(data.position());
                    if (npc == null) continue;

                    SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
                    skin.setSkinName(data.ceoName());
                    skin.run();

                    npc.setName(data.displayName());
                }
                Bukkit.getLogger().info("Top CEO NPCs updated.");
            });
        });
    }

    /**
     * Starts automatic updates every 10 minutes.
     */
    public void startAutoTopCEOUpdate() {
        Bukkit.getScheduler().runTaskTimer(this, this::updateTopCeoNpcs, 20L, 20L * 60 * 10);
    }

    private record TopCeoData(int position, String ceoName, String displayName) {

    }


    public static NPC getNpcByPosition(int position) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.data().has("top_ceo_position")) continue;
            if (npc.data().get("top_ceo_position").equals(position)) return npc;
        }
        return null;
    }


    public void saveResourceFolder(String path, boolean replace) {
        File outDir = new File(getDataFolder(), path);
        if (!outDir.exists()) outDir.mkdirs();

        try (InputStream stream = getResource(path)) {
            if (stream == null) {
                // It’s a directory, not a single file — iterate contents manually
                URL url = getClassLoader().getResource(path);
                if (url == null) return;

                URI uri = url.toURI();
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                Path jarPath = fileSystem.getPath(path);

                try (Stream<Path> walk = Files.walk(jarPath)) {
                    walk.filter(Files::isRegularFile).forEach(file -> {
                        try (InputStream is = Files.newInputStream(file)) {
                            File outFile = new File(outDir, file.getFileName().toString());
                            if (!outFile.exists() || replace) {
                                Files.copy(is, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } else {
                // Single file resource
                File outFile = new File(outDir, new File(path).getName());
                if (!outFile.exists() || replace) {
                    Files.copy(stream, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

}