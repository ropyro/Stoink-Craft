package com.stoinkcraft.earning.boosters;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BoostNoteInteractionListener implements Listener {

    @EventHandler
    public void onBoosterRedeem(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null || !BoosterItemHelper.isBoosterItem(item)) return;

        e.setCancelled(true);

        Enterprise ent = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
        if (ent == null) {
            player.sendMessage(ChatColor.RED + "You must be in an enterprise to redeem this.");
            return;
        }
        if(ent.isBoosted()){
            player.sendMessage(ChatColor.RED + "Only one booster can be used at a time.");
            return;
        }

        double multiplier = BoosterItemHelper.getMultiplier(item);
        long duration = BoosterItemHelper.getDurationTicks(item);

        EnterpriseManager.getEnterpriseManager().setBooster(ent, new Booster(multiplier, duration));
        if(item.getAmount() == 1){
            player.getInventory().removeItem(item);
        }else{
            item.setAmount(item.getAmount()-1);
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), item);
        }
        Bukkit.broadcastMessage(ChatColor.GOLD + ent.getName() + " activated a " +
                multiplier + "x booster!");
    }
}
