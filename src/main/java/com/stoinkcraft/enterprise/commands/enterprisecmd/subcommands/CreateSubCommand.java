package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
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
            ChatUtils.sendMessage(player,ChatColor.YELLOW + "Usage: " + getUsage());
            return;
        }

        if (EnterpriseManager.getEnterpriseManager().isInEnterprise(player.getUniqueId())) {
            ChatUtils.sendMessage(player,ChatColor.RED + "You're already in an enterprise. Resign first.");
            return;
        }

        if(StoinkCore.getEconomy().getBalance(player) < SCConstants.ENTERPRISE_FOUNDING_COST){
            ChatUtils.sendMessage(player,"Insufficient funds. Founding enterprise costs: " + ChatUtils.formatMoney(SCConstants.ENTERPRISE_FOUNDING_COST));
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

        ChatUtils.sendMessage(player,ChatColor.GREEN + "Enterprise '" + name + "' created.");
    }
}
