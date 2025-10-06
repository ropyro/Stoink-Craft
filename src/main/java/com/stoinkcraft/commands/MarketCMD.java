package com.stoinkcraft.commands;

import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.guis.MarketGUI;
import com.stoinkcraft.market.MarketManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

public class MarketCMD implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(!(commandSender instanceof Player)) return true;
        Player player = (Player)commandSender;

        if(EnterpriseManager.getEnterpriseManager().isInEnterprise(player.getUniqueId())){
            new MarketGUI(player).openWindow();
        }else{
            player.sendMessage("You must join an enterprise to use this command! /enterprise");
        }
        return true;
    }
}
