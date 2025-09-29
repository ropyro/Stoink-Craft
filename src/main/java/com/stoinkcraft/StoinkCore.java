package com.stoinkcraft;

import com.stoinkcraft.commands.enterprisecmd.EnterpriseCMD;
import com.stoinkcraft.commands.enterprisecmd.EnterpriseTabCompleter;
import com.stoinkcraft.earnings.EarningListener;
import com.stoinkcraft.market.MarketManager;
import com.stoinkcraft.enterprise.EnterpriseManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class StoinkCore extends JavaPlugin {

    private static Economy econ = null;

    @Override
    public void onDisable() {
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

        File marketFile = new File(getDataFolder(), "market.yml");
        if(!marketFile.exists()){
            saveResource("market.yml", false);
        }
        MarketManager.loadMarketPrices(marketFile);

        //Register /enterprise command + tap completer
        EnterpriseCMD enterpriseCMD = new EnterpriseCMD(this);
        getCommand("enterprise").setExecutor(enterpriseCMD);
        getCommand("enterprise").setTabCompleter(new EnterpriseTabCompleter(enterpriseCMD.getSubcommands()));

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