package com.stoinkcraft;

import com.stoinkcraft.config.ConfigLoader;
import com.stoinkcraft.items.graveyard.hound.GraveyardHoundListener;
import com.stoinkcraft.items.graveyard.hound.GraveyardHoundManager;
import com.stoinkcraft.jobsites.protection.ProtectionListeners;
import com.stoinkcraft.jobsites.protection.ProtectionManager;
import com.stoinkcraft.enterprise.*;
import com.stoinkcraft.enterprise.listeners.*;
import com.stoinkcraft.jobsites.contracts.ContractFeedbackManager;
import com.stoinkcraft.jobsites.contracts.ContractPoolLoader;
import com.stoinkcraft.jobsites.contracts.ContractManager;
import com.stoinkcraft.jobsites.listeners.*;
import com.stoinkcraft.items.StoinkItemListener;
import com.stoinkcraft.items.StoinkItemRegistry;
import com.stoinkcraft.items.booster.BoosterManager;
import com.stoinkcraft.misc.CitizensLoadListener;
import com.stoinkcraft.misc.daily.DailyCMD;
import com.stoinkcraft.misc.daily.DailyManager;
import com.stoinkcraft.enterprise.commands.TopCeoCMD;
import com.stoinkcraft.enterprise.commands.enterprisecmd.EnterpriseCMD;
import com.stoinkcraft.enterprise.commands.enterprisecmd.EnterpriseTabCompleter;
import com.stoinkcraft.enterprise.commands.serverenterprisecmd.ServerEntCMD;
import com.stoinkcraft.enterprise.commands.serverenterprisecmd.ServerEntTabCompleter;
import com.stoinkcraft.misc.EnderChestListener;
import com.stoinkcraft.misc.playerupgrades.PMenuCommand;
import com.stoinkcraft.jobsites.commands.FarmlandCommand;
import com.stoinkcraft.jobsites.commands.QuarryCommand;
import com.stoinkcraft.jobsites.commands.GraveyardCommand;
import com.stoinkcraft.items.admin.AdminItemsCMD;
import com.stoinkcraft.serialization.EnterpriseStorageJson;
import com.stoinkcraft.enterprise.shares.SharesCMD;
import com.stoinkcraft.enterprise.shares.ShareManager;
import com.stoinkcraft.enterprise.shares.ShareStorage;
import com.stoinkcraft.misc.PhantomSpawnDisabler;
import com.stoinkcraft.misc.StoinkExpansion;
import com.stoinkcraft.jobsites.worlds.EnterprisePlotManager;
import com.stoinkcraft.jobsites.worlds.EnterpriseWorldManager;
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
    private DailyManager dm;
    private ShareManager sm;
    private EnterpriseWorldManager ewm;
    private EnterprisePlotManager epm;
    private ContractManager cm;
    private ContractFeedbackManager cfm;
    private ProtectionManager pm;
    private BoosterManager boosterManager;
    private GraveyardHoundManager graveyardHoundManager;

    @Override
    public void onDisable() {
        getLogger().info("Disabling StoinkCore...");

        cfm.clearAll();
        StoinkItemRegistry.clear();
        boosterManager.shutdown();
        graveyardHoundManager.shutdown();

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

        StoinkItemRegistry.registerItems();
        boosterManager.restoreBoostersOnStartup();
        graveyardHoundManager.restoreHoundsOnStartup();


        Bukkit.getScheduler().runTask(this, this::checkCitizensAlreadyLoaded);

        getLogger().info("StoinkCore loaded.");
    }

    private void checkCitizensAlreadyLoaded() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (jobSitesLoaded) return;

            try {
                if (CitizensAPI.getNPCRegistry() != null && CitizensAPI.getNPCRegistry().iterator().hasNext()) {
                    getLogger().info("Hot reload detected (StoinkCore only), Citizens has NPCs. Initializing...");
                    onCitizensReady();
                }
            } catch (Exception e) {
            }
        }, 10L);
    }

    private void hookLibraries(){
        InvUI.getInstance().setPlugin(this);

        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

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
        boosterManager = new BoosterManager(this);
    }

    private void initFilesAndResources(){
        File marketFile = new File(getDataFolder(), "market.yml");
        if(!marketFile.exists()){
            saveResource("market.yml", false);
        }

        saveResourceFolder("schematics", false);

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

    public void onCitizensReady() {
        if (jobSitesLoaded) return;
        jobSitesLoaded = true;

        getLogger().info("Citizens ready, initializing NPCs from registry...");

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
        getCommand("stoinkitems").setExecutor(new AdminItemsCMD());
    }

    public void registerListeners(){
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PhantomSpawnDisabler(), this);
        getServer().getPluginManager().registerEvents(new EnderChestListener(), this);
        getServer().getPluginManager().registerEvents(new CitizensLoadListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListeners(this), this);
        getServer().getPluginManager().registerEvents(new StoinkItemListener(), this);
        getServer().getPluginManager().registerEvents(new GraveyardHoundListener(), this);
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

        new BukkitRunnable() {
            @Override
            public void run() {
                EnterpriseStorageJson.saveAllEnterprisesAsync();
            }
        }.runTaskTimerAsynchronously(this, 20L * 60 * 20, 20L * 60 * 20);
    }

    public static StoinkCore getInstance() {
        return INSTANCE;
    }

    public void startContractResetTask() {
        getContractManager().handleDailyReset();
        getContractManager().handleWeeklyReset();

        long now = System.currentTimeMillis();
        long initialDelay = ContractTimeUtil.nextDay() - now;
        long dailyPeriod = 24L * 60 * 60 * 1000;

        long initialDelayTicks = initialDelay / 50;
        long periodTicks = dailyPeriod / 50;

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            getContractManager().handleDailyReset();

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

    public EnterpriseManager getEnterpriseManager(){
        return em;
    }

    public DailyManager getDailyManager(){
        return dm;
    }

    public ShareManager getShareManager(){
        return sm;
    }

    public EnterpriseWorldManager getEnterpriseWorldManager(){
        return ewm;
    }

    public EnterprisePlotManager getEnterprisePlotManager(){
        return epm;
    }

    public ContractManager getContractManager(){
        return cm;
    }

    public ContractFeedbackManager getContractFeedbackManager(){
        return cfm;
    }

    public ProtectionManager getProtectionManager() {return pm;}

    public BoosterManager getBoosterManager(){
        return boosterManager;
    }

    public GraveyardHoundManager getGraveyardHoundManager() {
        return graveyardHoundManager;
    }
}