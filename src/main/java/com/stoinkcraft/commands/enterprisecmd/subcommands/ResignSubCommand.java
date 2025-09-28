package com.stoinkcraft.commands.enterprisecmd.subcommands;

import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResignSubCommand implements SubCommand {

    @Override
    public String getName() {
        return "resign";
    }

    @Override
    public String getUsage() {
        return "/enterprise resign";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        Enterprise e = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());

        if (e == null) {
            player.sendMessage("§cYou're not in an enterprise.");
            return;
        }

        // Prevent CEO from resigning for now (later: require transfer of ownership)
        if (e.getCeo().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot resign as CEO. Transfer or disband the company first.");
            return;
        }

        e.resignMember(player.getUniqueId());
        player.sendMessage("§aYou have resigned from §e" + e.getName());
    }
}
