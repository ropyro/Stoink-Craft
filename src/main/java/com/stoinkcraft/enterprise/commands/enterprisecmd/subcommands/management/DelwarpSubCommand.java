package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands.management;

import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DelwarpSubCommand implements SubCommand {
    @Override
    public String getName() {
        return "delwarp";
    }

    @Override
    public String getUsage() {
        return "/enterprise delwarp";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player)) return;
        Player player = (Player)sender;

        EnterpriseManager.getEnterpriseManager().deleteEnterpriseWarp(player);
    }
}
