package com.stoinkcraft.commands.enterprisecmd.subcommands;

import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.command.CommandSender;

public class ListSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getUsage() {
        return "/enterprise list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (EnterpriseManager.getEnterpriseManager().getEnterpriseList().isEmpty()) {
            sender.sendMessage("§7There are currently no enterprises.");
            return;
        }

        sender.sendMessage("§a== Enterprises ==");
        for (Enterprise e : EnterpriseManager.getEnterpriseManager().getEnterpriseList()) {
            sender.sendMessage("§e• " + e.getName() + " §7(Net: $" + e.getNetWorth() + ")");
        }
    }
}
