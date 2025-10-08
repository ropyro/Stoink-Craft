package com.stoinkcraft.commands.enterprisecmd.subcommands;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

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

        if(StoinkCore.getEconomy().getBalance(player) < SCConstants.ENTERPRISE_FOUNDING_COST){
            player.sendMessage("Insufficient funds. Founding enterprise costs: " + ChatUtils.formatMoney(SCConstants.ENTERPRISE_FOUNDING_COST));
            return;
        }

//        StringBuilder nameBuilder = new StringBuilder(args[1]);
//        List<String> argsList = Arrays.stream(args).toList();
//        argsList.remove(0);
//        argsList.forEach(a -> nameBuilder.append(" " + a));

        String name = args[1];
        Enterprise e = new Enterprise(name, player.getUniqueId());
        EnterpriseManager.getEnterpriseManager().createEnterprise(e);

        StoinkCore.getEconomy().withdrawPlayer(player, SCConstants.ENTERPRISE_FOUNDING_COST);

        player.sendMessage(ChatColor.GREEN + "Enterprise '" + name + "' created.");
    }
}
