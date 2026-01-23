package com.stoinkcraft.items.graveyard.hound;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Handles events related to Graveyard Hounds.
 */
public class GraveyardHoundListener implements Listener {

    /**
     * Prevents players from making the hound sit via right-click.
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (GraveyardHoundManager.isGraveyardHound(event.getRightClicked())) {
            event.setCancelled(true);
        }
    }

    /**
     * Handles hound death.
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (GraveyardHoundManager.isGraveyardHound(event.getEntity())) {
            // Clear drops - spectral hound shouldn't drop anything
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    /**
     * Prevents players from damaging their own hounds.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();

        if (!GraveyardHoundManager.isGraveyardHound(damaged)) {
            return;
        }

        Wolf wolf = (Wolf) damaged;

        // Get the actual damager (handle projectiles)
        Entity damager = event.getDamager();
        if (damager instanceof org.bukkit.entity.Projectile projectile) {
            if (projectile.getShooter() instanceof Entity shooter) {
                damager = shooter;
            }
        }

        // Prevent owner from damaging their own hound
        if (damager instanceof Player player) {
            if (wolf.getOwner() != null && wolf.getOwner().getUniqueId().equals(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
}