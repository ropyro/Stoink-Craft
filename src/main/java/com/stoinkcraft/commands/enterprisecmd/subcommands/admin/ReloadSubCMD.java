package com.stoinkcraft.commands.enterprisecmd.subcommands.admin;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.market.MarketManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ReloadSubCMD implements SubCommand {

    private StoinkCore plugin;

    public ReloadSubCMD(StoinkCore plugin){
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getUsage() {
        return "/enterprise reload - Reloads config files";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;

        player.sendMessage("Reloading StoinkCore...");

        File marketFile = new File(plugin.getDataFolder(), "market.yml");
        MarketManager.loadMarketPrices(marketFile);

        player.sendMessage("StoinkCore successfully reloaded!");

    }
}
