package com.stoinkcraft.misc;

import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

public class EnderChestListener implements Listener {

    @EventHandler
    public void onEnderChestOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.ENDER_CHEST) {
            if (!(event.getPlayer() instanceof Player player)) return;
            event.setCancelled(true);
            player.closeInventory();
            player.performCommand("vaults");
        }
    }

    @EventHandler
    public void onEnderChestOpen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clicked = event.getClickedBlock();

        if (clicked == null) return;

        // Check for right-clicking an ender chest
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && clicked.getType().equals(BlockType.ENDER_CHEST)) {
            event.setCancelled(true);
            player.closeInventory(); // Make sure no UI flashes open
            player.performCommand("vaults");
        }
    }
}
