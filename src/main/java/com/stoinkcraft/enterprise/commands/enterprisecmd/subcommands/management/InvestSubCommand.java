package com.stoinkcraft.enterprise.commands.enterprisecmd.subcommands.management;

import com.stoinkcraft.enterprise.commands.SubCommand;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.Role;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvestSubCommand implements SubCommand {
    @Override
    public String getName() {
        return "invest";
    }

    @Override
    public String getUsage() {
        return "/enterprise invest <amount>";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof Player)) return;
        Player player = (Player)sender;

        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();

        if(em.getEnterpriseByMember(player.getUniqueId()) != null){
            Enterprise e = em.getEnterpriseByMember(player.getUniqueId());
            if(e.getMemberRole(player.getUniqueId()).equals(Role.CEO)){
                if(args.length == 2 && !Double.valueOf(args[1]).isNaN()){
                        double value = Double.valueOf(args[1]);
                        if(e.getBankBalance() >= value){
                            e.increaseNetworth(Double.valueOf(args[1]));
                            e.decreaseBankBalance(value);
                            ChatUtils.sendMessage(player,"Invested " + args[1] + " from bank funds into networth!");
                        }else{
                            ChatUtils.sendMessage(player,"Enterprise has insufficient funds.");
                        }
                }else{
                    ChatUtils.sendMessage(player,"Invalid usage: " + getUsage());
                }
            }else{
                ChatUtils.sendMessage(player,"Only the CEO can invest bank funds!");
            }
        }
    }
}
