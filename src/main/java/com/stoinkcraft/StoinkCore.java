package com.stoinkcraft;

import com.stoinkcraft.commands.enterprisecmd.EnterpriseCMD;
import com.stoinkcraft.commands.enterprisecmd.EnterpriseTabCompleter;
import com.stoinkcraft.earnings.EarningListener;
import com.stoinkcraft.enterprise.EnterpriseStorage;
import com.stoinkcraft.market.MarketManager;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.StoinkExpansion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class StoinkCore extends JavaPlugin {

    private static Economy econ = null;

    @Override
    public void onDisable() {
        File enterpriseFile = new File(getDataFolder(), "enterprises.yml");
        EnterpriseStorage.saveAllEnterprises(enterpriseFile);

        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        //Vault hook
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //Inititalize the enterprise manager class TODO: pull enterprise list from saved data
        EnterpriseManager em = new EnterpriseManager(this, econ, 5);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new StoinkExpansion(this).register(); //
        }

        File marketFile = new File(getDataFolder(), "market.yml");
        if(!marketFile.exists()){
            saveResource("market.yml", false);
        }
        MarketManager.loadMarketPrices(marketFile);

        File enterpriseFile = new File(getDataFolder(), "enterprises.yml");
        if (!enterpriseFile.exists()) {
            try {
                enterpriseFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        EnterpriseStorage.loadAllEnterprises(enterpriseFile);

        //Register /enterprise command + tap completer
        Bukkit.getScheduler().runTask(this, () -> {
            EnterpriseCMD enterpriseCMD = new EnterpriseCMD(this);
            getCommand("enterprise").setExecutor(enterpriseCMD);
            getCommand("enterprise").setTabCompleter(new EnterpriseTabCompleter(enterpriseCMD.getSubcommands()));
        });

        //Register Earning listeners
        getServer().getPluginManager().registerEvents(new EarningListener(this), this);

        getLogger().info("StoinkCore loaded.");
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