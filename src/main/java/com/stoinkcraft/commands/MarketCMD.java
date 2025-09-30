package com.stoinkcraft.commands;

import com.stoinkcraft.market.MarketManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MarketCMD implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(!(commandSender instanceof Player)) return true;
        Player player = (Player)commandSender;

        player.sendMessage("Market Values:");
        for (String product : MarketManager.values.keySet()){
            player.sendMessage(product + ": " + MarketManager.getValue(product));
        }

        return true;
    }
}
