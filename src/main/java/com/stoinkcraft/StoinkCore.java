package com.stoinkcraft;

import com.stoinkcraft.daily.DailyCMD;
import com.stoinkcraft.daily.DailyManager;
import com.stoinkcraft.market.boosters.BoostNoteInteractionListener;
import com.stoinkcraft.market.MarketCMD;
import com.stoinkcraft.enterprise.commands.TopCeoCMD;
import com.stoinkcraft.enterprise.commands.enterprisecmd.EnterpriseCMD;
import com.stoinkcraft.enterprise.commands.enterprisecmd.EnterpriseTabCompleter;
import com.stoinkcraft.enterprise.commands.serverenterprisecmd.ServerEntCMD;
import com.stoinkcraft.enterprise.commands.serverenterprisecmd.ServerEntTabCompleter;
import com.stoinkcraft.market.listeners.BlockPlacedManager;
import com.stoinkcraft.market.listeners.EarningListener;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseStorage;
import com.stoinkcraft.enterprise.ServerEnterprise;
import com.stoinkcraft.enterprise.listeners.ChatDepositListener;
import com.stoinkcraft.enterprise.listeners.ChatInvestListener;
import com.stoinkcraft.enterprise.listeners.ChatWithdrawListener;
import com.stoinkcraft.enterprise.listeners.PlayerJoinListener;
import com.stoinkcraft.market.MarketManager;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.misc.EnderChestListener;
import com.stoinkcraft.misc.JoinMOTDListener;
import com.stoinkcraft.playerupgrades.PMenuCommand;
import com.stoinkcraft.shares.SharesCMD;
import com.stoinkcraft.shares.ShareManager;
import com.stoinkcraft.shares.ShareStorage;
import com.stoinkcraft.misc.PhantomSpawnDisabler;
import com.stoinkcraft.utils.StoinkExpansion;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.xenondevs.invui.InvUI;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StoinkCore extends JavaPlugin implements Listener {

    private static Economy econ = null;

    private static StoinkCore INSTANCE;

    @Override
    public void onDisable() {
        EnterpriseStorage.saveAllEnterprises();
        ShareStorage.saveShares();

        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        //GUI util hook
        InvUI.getInstance().setPlugin(this);

        //Vault hook
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DailyManager dm = new DailyManager();

        //Inititalize the enterprise manager class TODO: pull enterprise list from saved data
        EnterpriseManager em = new EnterpriseManager(this, econ, 2);
        ShareManager sm = new ShareManager();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new StoinkExpansion(this).register(); //
        }

        File marketFile = new File(getDataFolder(), "market.yml");
        if(!marketFile.exists()){
            saveResource("market.yml", false);
        }
        MarketManager.loadMarketPrices(marketFile);
        MarketManager.startRotatingBoosts(this);

        EnterpriseStorage.loadAllEnterprises();
        EnterpriseManager.startDailyTaxes(this);

        ShareStorage.loadShares();

        if(EnterpriseManager.getEnterpriseManager().getEnterpriseList()
                .stream()
                .noneMatch(e -> e instanceof ServerEnterprise)){
            getLogger().info("Adding server enterprises...");
            EnterpriseManager.getEnterpriseManager().createEnterprise(new ServerEnterprise("FarmerLLC"));
            EnterpriseManager.getEnterpriseManager().createEnterprise(new ServerEnterprise("MinerCorp"));
            EnterpriseManager.getEnterpriseManager().createEnterprise(new ServerEnterprise("HunterInc"));
            EnterpriseStorage.saveAllEnterprises();
        }

        //Register /enterprise command + tap completer
        Bukkit.getScheduler().runTask(this, () -> {
            EnterpriseCMD enterpriseCMD = new EnterpriseCMD(this);
            getCommand("enterprise").setExecutor(enterpriseCMD);
            getCommand("enterprise").setTabCompleter(new EnterpriseTabCompleter(enterpriseCMD.getSubcommands()));
            getCommand("market").setExecutor(new MarketCMD());
            getCommand("serverenterprise").setExecutor(new ServerEntCMD());
            getCommand("serverenterprise").setTabCompleter(new ServerEntTabCompleter());
            getCommand("topceo").setExecutor(new TopCeoCMD());
            getCommand("shares").setExecutor(new SharesCMD());
            getCommand("daily").setExecutor(new DailyCMD());
            getCommand("pmenu").setExecutor(new PMenuCommand());
        });

        //Register Earning listeners
        getServer().getPluginManager().registerEvents(new EarningListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatWithdrawListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatInvestListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatDepositListener(this), this);
        getServer().getPluginManager().registerEvents(new PhantomSpawnDisabler(), this);
        getServer().getPluginManager().registerEvents(new BoostNoteInteractionListener(), this);
        getServer().getPluginManager().registerEvents(new JoinMOTDListener(), this);
        getServer().getPluginManager().registerEvents(new EnderChestListener(), this);
        getServer().getPluginManager().registerEvents(this, this);

        new BlockPlacedManager(this);

        startAutoSaveTask();
        startPriceSnapshotRecording();
        startAutoTopCEOUpdate();

        getLogger().info("StoinkCore loaded.");
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() == Material.COBBLESTONE || block.getType() == Material.SUGAR_CANE) {
            Bukkit.getLogger().info("[DEBUG] Player placed: " + block.getType() + " at " + block.getLocation());
            BlockPlacedManager.getInstance().markPlaced(block);
        }
    }

    @EventHandler
    public void onDestroy(BlockBreakEvent event){
        Block block = event.getBlock();
        if (block.getType() == Material.COBBLESTONE || block.getType() == Material.SUGAR_CANE) {
            Bukkit.getLogger().info("[DEBUG] Player destroyed: " + block.getType() + " at " + block.getLocation());
            BlockPlacedManager.getInstance().unmarkPlaced(block);
        }
    }


    public static StoinkCore getInstance() {
        return INSTANCE;
    }

    private void startPriceSnapshotRecording(){
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            EnterpriseManager.getEnterpriseManager().recordPriceSnapshots();
        }, 0L, 20L * 60 * 5);
    }

    private void startAutoSaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    EnterpriseStorage.saveAllEnterprises();
                    ShareStorage.saveShares();
                    getLogger().info("[AutoSave] Enterprises and shares saved successfully.");
                } catch (Exception e) {
                    getLogger().severe("[AutoSave] Failed to save enterprises/shares: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(this, 20L * 60L * 5L, 20L * 60L * 5L); // every 5 minutes
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
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::updateTopCeoNpcs, 20L, 20L * 60 * 10);
    }

    private record TopCeoData(int position, String ceoName, String displayName) {}


    public static NPC getNpcByPosition(int position) {
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            if (!npc.data().has("top_ceo_position")) continue;
            if (npc.data().get("top_ceo_position").equals(position)) return npc;
        }
        return null;
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