package com.stoinkcraft.commands.enterprisecmd.subcommands;

import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getUsage() {
        return "/enterprise create <name>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: " + getUsage());
            return;
        }

        if (EnterpriseManager.getEnterpriseManager().isInEnterprise(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You're already in an enterprise. Resign first.");
            return;
        }

        String name = args[1];
        Enterprise e = new Enterprise(name, player.getUniqueId());
        EnterpriseManager.getEnterpriseManager().createEnterprise(e);
        player.sendMessage(ChatColor.GREEN + "Enterprise '" + name + "' created.");
    }
}
