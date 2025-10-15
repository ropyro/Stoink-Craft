package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands.management;

import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpSubCommand implements SubCommand {
    @Override
    public String getName() {
        return "warp";
    }

    @Override
    public String getUsage() {
        return "/enterprise warp [enterprise]";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player)) return;
        Player player = (Player)sender;

        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();

        if(args.length < 2){
            if(em.isInEnterprise(player.getUniqueId())){
                Enterprise e = em.getEnterpriseByMember(player.getUniqueId());
                if(e.getWarp() == null){
                    ChatUtils.sendMessage(player,"Warp not set for this enterprise.");
                }else{
                    player.teleport(e.getWarp());
                    ChatUtils.sendMessage(player,"Teleported to " + e.getName() + "'s warp.");
                }
            }else{
                ChatUtils.sendMessage(player,"You are not in an enterprise. Use /enterprise warp <enterprise>");
            }
        }else{
            if(em.getEnterpriseByName(args[1]) != null){
                Enterprise e = em.getEnterpriseByName(args[1]);
                if(e.getWarp() == null){
                    ChatUtils.sendMessage(player,"Warp not set for this enterprise.");
                }else{
                    player.teleport(e.getWarp());
                    ChatUtils.sendMessage(player,"Teleported to " + e.getName() + "'s warp.");
                }
            }else{
                ChatUtils.sendMessage(player,"Enterprise not found try again.");
            }
        }
    }
}
