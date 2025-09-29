package com.stoinkcraft.commands.enterprisecmd.subcommands;

import com.stoinkcraft.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.Role;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DisbandSubCommand implements SubCommand {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getUsage() {
        return null;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player)) return;
        Player player = (Player)sender;
        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();

        if(em.getEnterpriseByMember(player.getUniqueId()) != null){
            Enterprise e = em.getEnterpriseByMember(player.getUniqueId());
            if(e.getMemberRole(player.getUniqueId()).equals(Role.CEO)){
                em.disband(e);
            }else{
                player.sendMessage("You must be the CEO to disband the enterprise!");
            }
        }
    }
}
