package com.stoinkcraft.enterprise.commands.serverenterprisecmd;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.items.graveyard.SoulVoucherItem;
import com.stoinkcraft.jobsites.sites.JobSiteManager;
import com.stoinkcraft.jobsites.sites.JobsiteLevelHelper;
import com.stoinkcraft.jobsites.sites.sites.farmland.FarmlandData;
import com.stoinkcraft.jobsites.sites.sites.farmland.FarmlandGui;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.enterprise.ServerEnterprise;
import com.stoinkcraft.jobsites.sites.sites.graveyard.GraveyardData;
import com.stoinkcraft.jobsites.sites.sites.quarry.QuarryData;
import com.stoinkcraft.items.StoinkItem;
import com.stoinkcraft.items.StoinkItemRegistry;
import com.stoinkcraft.items.booster.BoosterItem;
import com.stoinkcraft.items.booster.BoosterTier;
import com.stoinkcraft.items.quarry.MineBomb;
import com.stoinkcraft.items.quarry.MineBombTier;
import com.stoinkcraft.serialization.EnterpriseMigration;
import com.stoinkcraft.serialization.EnterpriseStorageJson;
import com.stoinkcraft.enterprise.shares.ShareStorage;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ServerEntCMD implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(sender instanceof Player && !sender.hasPermission(SCConstants.SERVER_ENT_COMMAND)){
            sender.sendMessage("Error you do not have permission for this command.");
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("givehound")) {
            if (args.length >= 2) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found: " + args[1]);
                    return true;
                }

                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid amount: " + args[2]);
                        return true;
                    }
                }

                StoinkItem hound = StoinkItemRegistry.getById("graveyard_hound");
                if (hound != null) {
                    target.getInventory().addItem(hound.createItemStack(amount));
                    ChatUtils.sendMessage(target, "§bYou received a Graveyard Hound summon!");
                    sender.sendMessage("§aGave " + amount + "x Graveyard Hound to " + target.getName());
                }
            } else {
                sender.sendMessage("§cUsage: /serverent givehound <player> [amount]");
            }
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("givesoulvoucher")) {
            if (args.length >= 3) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found: " + args[1]);
                    return true;
                }

                int soulAmount;
                try {
                    soulAmount = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid soul amount: " + args[2]);
                    return true;
                }

                int itemCount = 1;
                if (args.length >= 4) {
                    try {
                        itemCount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid item count: " + args[3]);
                        return true;
                    }
                }

                SoulVoucherItem voucher = (SoulVoucherItem) StoinkItemRegistry.getById("soul_voucher");
                if (voucher != null) {
                    target.getInventory().addItem(voucher.createItemStack(itemCount, soulAmount));
                    ChatUtils.sendMessage(target, "§dYou received a Soul Voucher worth §5" + soulAmount + " souls§d!");
                    sender.sendMessage("§aGave " + itemCount + "x Soul Voucher (" + soulAmount + " souls) to " + target.getName());
                }
            } else {
                sender.sendMessage("§cUsage: /serverent givesoulvoucher <player> <souls> [amount]");
            }
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("giveminebomb")) {
            if (args.length >= 3) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found: " + args[1]);
                    return true;
                }

                String tierName = args[2].toUpperCase();
                MineBombTier tier;

                try {
                    tier = MineBombTier.valueOf(tierName);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid tier: " + args[2]);
                    sender.sendMessage("§cValid tiers: SMALL, MEDIUM, LARGE");
                    return true;
                }

                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid amount: " + args[3]);
                        return true;
                    }
                }

                MineBomb bomb = new MineBomb(tier);
                target.getInventory().addItem(bomb.createItemStack(amount));

                ChatUtils.sendMessage(target, "§aYou received a " + tier.getDisplayName() + "!");
                sender.sendMessage("§aGave " + amount + "x " + tier.getDisplayName() + " to " + target.getName());

            } else {
                sender.sendMessage("§cUsage: /serverent giveminebomb <player> <tier> [amount]");
                sender.sendMessage("§cTiers: SMALL, MEDIUM, LARGE");
            }
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("givefertilizer")) {
            if (args.length >= 2) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found: " + args[1]);
                    return true;
                }

                int amount = 1;
                if (args.length >= 3) {
                    try {
                        amount = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid amount: " + args[2]);
                        return true;
                    }
                }

                StoinkItem fertilizer = StoinkItemRegistry.getById("fertilizer_bomb");
                if (fertilizer != null) {
                    target.getInventory().addItem(fertilizer.createItemStack(amount));
                    ChatUtils.sendMessage(target, "§aYou received a Fertilizer Bomb!");
                    sender.sendMessage("§aGave " + amount + "x Fertilizer Bomb to " + target.getName());
                }
            } else {
                sender.sendMessage("§cUsage: /serverent givefertilizer <player> [amount]");
            }
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("givebooster")) {
            if (args.length >= 3) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found: " + args[1]);
                    return true;
                }

                String tierName = args[2].toUpperCase();
                BoosterTier tier;

                try {
                    tier = BoosterTier.valueOf(tierName);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid tier: " + args[2]);
                    sender.sendMessage("§cValid tiers: SMALL, MEDIUM, LARGE");
                    return true;
                }

                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("§cInvalid amount: " + args[3]);
                        return true;
                    }
                }

                BoosterItem boosterItem = new BoosterItem(tier);
                target.getInventory().addItem(boosterItem.createItemStack(amount));

                ChatUtils.sendMessage(target, "§aYou have received a §e" + tier.getDisplayName() + "§a!");
                sender.sendMessage("§aGave " + amount + "x " + tier.getDisplayName() + " to " + target.getName());

            } else {
                sender.sendMessage("§cInvalid usage: /serverent givebooster <player> <tier> [amount]");
                sender.sendMessage("§cTiers: SMALL, MEDIUM, LARGE");
            }
            return true;
        }

        if(!(sender instanceof Player)) return false;
       Player player = (Player)sender;

       if(!player.hasPermission(SCConstants.SERVER_ENT_COMMAND)){
           ChatUtils.sendMessage(player, "§cError you do not have permission for this command.");
           return true;
       }

       if(args.length == 0){
           player.sendMessage("== Server Admin Help ==");
           player.sendMessage( " - /se setwarp <enterprise> - sets the warp for the enterprise");
           player.sendMessage(" - /se updateceo");
           player.sendMessage(" - /se pricesnapshot");
           player.sendMessage(" - /se updateceo");
           player.sendMessage( " - /se save - saves enterprise data");
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
           if(args[0].equalsIgnoreCase("getoffset")){
               Location playerLoc = player.getLocation();
               Vector offset = new Vector(0, 0 ,0);
               JobSiteManager jsm = StoinkCore.getInstance().getEnterpriseManager().getEnterpriseByMember(player.getUniqueId()).getJobSiteManager();
               if(jsm.getGraveyardSite().contains(playerLoc)){
                   offset = playerLoc.subtract(jsm.getGraveyardSite().getSpawnPoint()).toVector();
               }else if(jsm.getFarmlandSite().contains(playerLoc)){
                   offset = playerLoc.subtract(jsm.getFarmlandSite().getSpawnPoint()).toVector();
               } else if (jsm.getQuarrySite().contains(playerLoc)) {
                   offset = playerLoc.subtract(jsm.getQuarrySite().getSpawnPoint()).toVector();
               }
               player.sendMessage("x: " + offset.getX() + " y: " + offset.getY() + " z: " + offset.getZ());
           }
           if(args[0].equalsIgnoreCase("save")){
               Bukkit.getScheduler().runTaskAsynchronously(StoinkCore.getInstance(), () -> {
                   try {

                       EnterpriseStorageJson.saveAllEnterprises();
                       ShareStorage.saveShares();
                       Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () -> {
                           Bukkit.getLogger().info("[AutoSave] Enterprises and shares saved successfully.");
                           ChatUtils.sendMessage(player, "Enterprises & shares saved successfully!");
                       });
                   } catch (Exception e) {
                       Bukkit.getLogger().severe("[AutoSave] Failed to save enterprises/shares: " + e.getMessage());
                       e.printStackTrace();
                       Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () ->
                               ChatUtils.sendMessage(player, "Error occurred while saving enterprises & shares!")
                       );
                   }
               });
               return true;           }
           if(args[0].equalsIgnoreCase("rebuild")){
               if(args.length >= 2){
                   String enterpriseName = args[1];
                   Enterprise enterprise = StoinkCore.getInstance().getEnterpriseManager().getEnterpriseByName(enterpriseName);
                   if(enterprise != null){
                       enterprise.getJobSiteManager().getSkyriseSite().rebuild();
                       enterprise.getJobSiteManager().getQuarrySite().rebuild();
                       enterprise.getJobSiteManager().getFarmlandSite().rebuild();
                       enterprise.getJobSiteManager().getGraveyardSite().rebuild();
                       ChatUtils.sendMessage(player, "Rebuilt jobsites for " + enterprise.getName());
                   }
               }
           }
           if(args[0].equalsIgnoreCase("regencontracts")){
               if(args.length >= 2){
                   String enterpriseName = args[1];
                   Enterprise enterprise = StoinkCore.getInstance().getEnterpriseManager().getEnterpriseByName(enterpriseName);
                   if(enterprise != null){
                       StoinkCore.getInstance().getContractManager().regenerateContracts(enterprise);
                   }
               }
           }
           if(args[0].equalsIgnoreCase("levelup")) {
               if (args.length >= 2) {
                   String enterpriseName = args[1];
                   Enterprise enterprise = StoinkCore.getInstance().getEnterpriseManager().getEnterpriseByName(enterpriseName);
                   if (enterprise != null) {
                       FarmlandData data = enterprise.getJobSiteManager().getFarmlandData();
                       data.incrementXp(JobsiteLevelHelper.getXpToNextLevel(data.getXp()));
                       GraveyardData data2 = enterprise.getJobSiteManager().getGraveyardData();
                       data2.incrementXp(JobsiteLevelHelper.getXpToNextLevel(data2.getXp()));
                       QuarryData data3 = enterprise.getJobSiteManager().getQuarryData();
                       data3.incrementXp(JobsiteLevelHelper.getXpToNextLevel(data3.getXp()));
                   }
               }
           }
           if(args[0].equalsIgnoreCase("listcontracts")){
               if(args.length >= 2){
                   String enterpriseName = args[1];
                   Enterprise enterprise = StoinkCore.getInstance().getEnterpriseManager().getEnterpriseByName(enterpriseName);
                   if(enterprise != null){
                       ChatUtils.sendMessage(player, "Contracts for: " + enterprise.getName());
                       StoinkCore.getInstance().getContractManager().getContracts(enterprise).forEach(c -> player.sendMessage(c.getContractId() + " " + c.getTarget() + " " + c.getProgress()));
                   }
               }
           }
           if(args[0].equalsIgnoreCase("migrate")){

               int count = EnterpriseMigration.getYamlFilesCount();

               if (count == 0) {
                   ChatUtils.sendMessage(player, ChatColor.GREEN + "No YAML files to migrate!");
                   return true;
               }

               ChatUtils.sendMessage(player, ChatColor.YELLOW + "Starting migration of " + count + " enterprises...");
               ChatUtils.sendMessage(player, ChatColor.YELLOW + "This may take a moment...");

               int migrated = EnterpriseMigration.migrateAllYamlToJson(false);

               ChatUtils.sendMessage(player, ChatColor.GREEN + "Migration complete!");
               ChatUtils.sendMessage(player, ChatColor.GREEN + "Migrated: " + migrated + " enterprises");

               sender.sendMessage(ChatColor.YELLOW + "Old YAML files kept as backup.");
           }
           if(args[0].equalsIgnoreCase("farmland")){
               new FarmlandGui(EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId()).getJobSiteManager().getFarmlandSite(), player).openWindow();
           }
       }
        return true;
    }
}
