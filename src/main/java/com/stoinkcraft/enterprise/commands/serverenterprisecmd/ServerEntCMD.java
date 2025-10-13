package com.stoinkcraft.enterprise.commands.serverenterprisecmd;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.market.boosters.BoosterItemHelper;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.ServerEnterprise;
import com.stoinkcraft.shares.ShareManager;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ServerEntCMD implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(sender instanceof Player && !sender.hasPermission(SCConstants.SERVER_ENT_COMMAND)){
            sender.sendMessage("Error you do not have permission for this command.");
        }

        //Allows console use of the command
        if(args.length >= 1 && args[0].equalsIgnoreCase("givebooster")){
               if(args.length >= 4){
                   double multiplier = Double.parseDouble(args[2]);
                   long duration = Long.parseLong(args[3]);
                   Player target = Bukkit.getPlayer(args[1]);
                   target.getInventory().addItem(BoosterItemHelper.getBoosterItemStack(multiplier, duration));
                   target.sendMessage("You have received an enterprise booster!");
               }else{
                   sender.sendMessage("Invalid usage: /serverent givebooster <player> <multiplier> <duration>");
               }
               return true;
       }

        if(!(sender instanceof Player)) return false;
       Player player = (Player)sender;

       if(!player.hasPermission(SCConstants.SERVER_ENT_COMMAND)){
           player.sendMessage("Error you do not have permission for this command.");
           return true;
       }

       if(args.length == 0){
           player.sendMessage("== Server Admin Help ==");
           player.sendMessage( " - /serverenterprise setwarp <enterprise> - sets the warp for the enterprise");
       }else
       if(args.length >= 1){
           if(args[0].equalsIgnoreCase("setwarp")){
               if(args.length == 2){
                   Optional<Enterprise> opt = EnterpriseManager.getEnterpriseManager()
                           .getEnterpriseList().stream()
                           .filter(e -> (e instanceof ServerEnterprise))
                           .filter(e1 -> e1.getName().equalsIgnoreCase(args[1]))
                           .findFirst();
                   if (opt.isPresent()) {
                        opt.get().setWarp(player.getLocation());
                       player.sendMessage("§aWarp set for §e" + opt.get().getName());
                   } else {
                       player.sendMessage("§cNo server-owned enterprise found by that name.");
                   }
               }else{
                   player.sendMessage("Invalid usage: /serverenterprise setwarp <enterprise>");
               }
               return true;
           }
           if(args[0].equalsIgnoreCase("updateeco")){
                EnterpriseManager.getEnterpriseManager().updateBankBalances();

               return true;
           }
           if(args[0].equalsIgnoreCase("pricesnapshot")){
               EnterpriseManager.getEnterpriseManager().recordPriceSnapshots();
               return true;
           }
       }
        return true;
    }
}
