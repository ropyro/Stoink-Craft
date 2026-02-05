package com.stoinkcraft.items;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class StoinkItemListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) return;

        StoinkItem stoinkItem = StoinkItemRegistry.getFromItemStack(item);
        if (stoinkItem == null) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        if (!stoinkItem.canUse(player, event)) {
            return;
        }

        boolean consumed = stoinkItem.onUse(player, event);

        if (consumed) {
            stoinkItem.consumeItem(player, item);
        }
    }
}