package com.stoinkcraft.earning.jobsites.commands;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.jobsites.sites.graveyard.GraveyardGui;
import com.stoinkcraft.earning.jobsites.sites.graveyard.GraveyardSite;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GraveyardCommand implements CommandExecutor {

    private static final String PERMISSION = "stoinkcore.jobsite.graveyard";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            ChatUtils.sendMessage(player, ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        Enterprise enterprise = StoinkCore.getInstance()
                .getEnterpriseManager()
                .getEnterpriseByMember(player.getUniqueId());

        if (enterprise == null) {
            ChatUtils.sendMessage(player, ChatColor.RED + "You must be in an enterprise to use this command.");
            return true;
        }

        GraveyardSite graveyardSite = enterprise.getJobSiteManager().getGraveyardSite();

        if (graveyardSite == null || !graveyardSite.isBuilt()) {
            ChatUtils.sendMessage(player, ChatColor.RED + "Your enterprise hasn't built the Graveyard yet!");
            return true;
        }

        new GraveyardGui(graveyardSite, player).openWindow();
        return true;
    }
}
