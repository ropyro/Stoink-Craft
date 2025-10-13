package com.stoinkcraft;

import com.stoinkcraft.market.boosters.BoostNoteInteractionListener;
import com.stoinkcraft.market.MarketCMD;
import com.stoinkcraft.enterprise.commands.TopCeoCMD;
import com.stoinkcraft.enterprise.commands.enterprisecmd.EnterpriseCMD;
import com.stoinkcraft.enterprise.commands.enterprisecmd.EnterpriseTabCompleter;
import com.stoinkcraft.enterprise.commands.serverenterprisecmd.ServerEntCMD;
import com.stoinkcraft.enterprise.commands.serverenterprisecmd.ServerEntTabCompleter;
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
import com.stoinkcraft.utils.PhantomSpawnDisabler;
import com.stoinkcraft.utils.StoinkExpansion;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.InvUI;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StoinkCore extends JavaPlugin {

    private static Economy econ = null;

    private static StoinkCore INSTANCE;

    @Override
    public void onDisable() {
        File enterpriseFile = new File(getDataFolder(), "enterprises.yml");
        EnterpriseStorage.saveAllEnterprises(enterpriseFile);

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

        //Inititalize the enterprise manager class TODO: pull enterprise list from saved data
        EnterpriseManager em = new EnterpriseManager(this, econ, 2);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) { //
            new StoinkExpansion(this).register(); //
        }

        File marketFile = new File(getDataFolder(), "market.yml");
        if(!marketFile.exists()){
            saveResource("market.yml", false);
        }
        MarketManager.loadMarketPrices(marketFile);
        MarketManager.startRotatingBoosts(this);

        File enterpriseFile = new File(getDataFolder(), "enterprises.yml");
        if (!enterpriseFile.exists()) {
            try {
                enterpriseFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        EnterpriseStorage.loadAllEnterprises(enterpriseFile);
        EnterpriseManager.startDailyTaxes(this);

        if(EnterpriseManager.getEnterpriseManager().getEnterpriseList()
                .stream()
                .filter(e -> e instanceof ServerEnterprise)
                .toList().isEmpty()){
            EnterpriseManager.getEnterpriseManager().createEnterprise(new ServerEnterprise("FarmerLLC"));
            EnterpriseManager.getEnterpriseManager().createEnterprise(new ServerEnterprise("MinerCorp"));
            EnterpriseManager.getEnterpriseManager().createEnterprise(new ServerEnterprise("HunterInc"));
            EnterpriseStorage.saveAllEnterprises(enterpriseFile);
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
        });

        //Register Earning listeners
        getServer().getPluginManager().registerEvents(new EarningListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatWithdrawListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatInvestListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatDepositListener(this), this);
        getServer().getPluginManager().registerEvents(new PhantomSpawnDisabler(), this);
        getServer().getPluginManager().registerEvents(new BoostNoteInteractionListener(), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> StoinkCore.updateTopCeoNpcs(), 20L, 20L * 60 * 10); // every 10 min

        getLogger().info("StoinkCore loaded.");
    }

    public static StoinkCore getInstance() {
        return INSTANCE;
    }

    public static void updateTopCeoNpcs() {
        List<Enterprise> sortedEnterprises = EnterpriseManager
                .getEnterpriseManager()
                .getEnterpriseList()
                .stream()
                .sorted(Comparator.comparingDouble(Enterprise::getNetWorth).reversed())
                .collect(Collectors.toList());

        for (int i = 1; i <= 3; i++) {
            NPC npc = getNpcByPosition(i);
            if (npc == null) continue;
            if (sortedEnterprises.size() < i) continue;

            Enterprise ent = sortedEnterprises.get(i - 1);
            OfflinePlayer ceo = Bukkit.getOfflinePlayer(ent.getCeo());
            String ceoName = ceo.getName() != null ? ceo.getName() : "CEO";
            String displayName = "#" + i + " " + ChatColor.GREEN + ChatColor.BOLD + ent.getName();

            // Set skin
            SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
            skin.setSkinName(ceoName);
            skin.run();

            // Set name
            npc.setName(displayName);
        }
    }

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