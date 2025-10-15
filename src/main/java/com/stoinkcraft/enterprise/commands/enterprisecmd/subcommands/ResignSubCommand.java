package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands;

import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.ChatUtils;
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
            ChatUtils.sendMessage(player,"§cYou're not in an enterprise.");
            return;
        }

        // Prevent CEO from resigning for now (later: require transfer of ownership)
        if (e.getCeo().equals(player.getUniqueId())) {
            ChatUtils.sendMessage(player,"§cYou cannot resign as CEO. Transfer or disband the company first.");
            return;
        }

        e.resignMember(player.getUniqueId());
        ChatUtils.sendMessage(player,"§aYou have resigned from §e" + e.getName());
    }
}
