package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatSubCommand implements SubCommand {
    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public String getUsage() {
        return "/ep chat - enables enterprise chat";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;

        if(EnterpriseManager.getEnterpriseManager().isInEnterprise(player.getUniqueId())){
            Enterprise e = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
            if(!e.getActiveEnterpriseChat().contains(player.getUniqueId())){
                e.addEnterpriseChatter(player.getUniqueId());
                ChatUtils.sendMessage(player, "Enabled enterprise chat!");
            }else{
                e.removeEnterpriseChatter(player.getUniqueId());
                ChatUtils.sendMessage(player, "Disabled enterprise chat!");
            }
        }else{
            ChatUtils.sendMessage(player, "You must be in an enterprise to enable enterprise chat.");
        }
    }
}
