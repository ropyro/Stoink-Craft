package com.stoinkcraft.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Listener that handles all custom StoinkItem interactions.
 */
public class StoinkItemListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-clicks
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Only main hand to prevent double-firing
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) return;

        // Check if this is a registered custom item
        StoinkItem stoinkItem = StoinkItemRegistry.getFromItemStack(item);
        if (stoinkItem == null) return;

        // Cancel the event to prevent any default behavior
        event.setCancelled(true);

        Player player = event.getPlayer();

        // Check if player can use this item
        if (!stoinkItem.canUse(player, event)) {
            return;
        }

        // Execute the item's effect
        boolean consumed = stoinkItem.onUse(player, event);

        // Consume the item if the effect succeeded
        if (consumed) {
            stoinkItem.consumeItem(player, item);
        }
    }
}