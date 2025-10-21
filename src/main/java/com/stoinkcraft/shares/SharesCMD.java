package com.stoinkcraft.shares;

import com.stoinkcraft.shares.guis.SharePortfolioGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SharesCMD implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!(sender instanceof Player)) return true;
        Player player = (Player)sender;

        new SharePortfolioGUI(player).openWindow();

        return true;
    }
}
