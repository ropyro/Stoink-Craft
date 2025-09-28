package com.stoinkcraft;

import com.stoinkcraft.commands.enterprisecmd.EnterpriseCMD;
import com.stoinkcraft.commands.enterprisecmd.EnterpriseTabCompleter;
import com.stoinkcraft.enterprise.EnterpriseManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class StoinkCore extends JavaPlugin {

    private static Economy econ = null;

    @Override
    public void onDisable() {
        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        EnterpriseCMD enterpriseCMD = new EnterpriseCMD(this);
        getCommand("enterprise").setExecutor(enterpriseCMD);
        getCommand("enterprise").setTabCompleter(new EnterpriseTabCompleter(enterpriseCMD.getSubcommands()));

        EnterpriseManager em = new EnterpriseManager(this, econ, 5);

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