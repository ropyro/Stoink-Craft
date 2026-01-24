package com.stoinkcraft.items.admin;

import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminItemsCMD implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission("stoinkcraft.admin")) {
            ChatUtils.sendMessage(player, "§cYou don't have permission to use this command.");
            return true;
        }

        new AdminItemsGUI(player).openWindow();
        return true;
    }
}
