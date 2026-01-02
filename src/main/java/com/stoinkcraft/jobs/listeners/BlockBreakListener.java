package com.stoinkcraft.jobs.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Material material = block.getType();

        // Check if the block is a crop
        if (material == Material.WHEAT || material == Material.CARROTS ||
                material == Material.POTATOES || material == Material.BEETROOTS) {

            // Get the age/growth stage of the crop
            Ageable ageable = (Ageable) block.getBlockData();

            // Check if the crop is NOT fully grown
            if (ageable.getAge() < ageable.getMaximumAge()) {
                event.setCancelled(true);
            } else {
                Player player = event.getPlayer();
                Enterprise enterprise = StoinkCore.getInstance().getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
                if(player.getWorld().equals(StoinkCore.getInstance().getEnterpriseWorldManager().getWorld()) && enterprise != null){
                    Bukkit.getScheduler().runTaskLaterAsynchronously(StoinkCore.getInstance(), () -> {
                        enterprise.getJobSiteManager().getFarmlandSite().getCropGenerator().replaceMissingCrops();
                    }, 1L);
                }
            }
        }
    }
}
