package com.stoinkcraft.items;

import com.stoinkcraft.StoinkCore;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Abstract base class for all custom StoinkCore items.
 */
public abstract class StoinkItem {

    public static final NamespacedKey ITEM_ID_KEY = new NamespacedKey(StoinkCore.getInstance(), "stoink_item_id");

    /**
     * @return Unique identifier for this item type (e.g., "fertilizer_bomb", "small_mine_bomb")
     */
    public abstract String getItemId();

    /**
     * @return The base material for this item
     */
    public abstract Material getMaterial();

    /**
     * @return Display name for the item (with color codes)
     */
    public abstract String getDisplayName();

    /**
     * @return Lore lines for the item
     */
    public abstract List<String> getLore();

    /**
     * Called when a player uses this item (right-click).
     * The event will be cancelled after this runs if the item is consumed.
     *
     * @param player The player using the item
     * @param event  The interaction event
     * @return true if the item should be consumed, false otherwise
     */
    public abstract boolean onUse(Player player, PlayerInteractEvent event);

    /**
     * Validation check before onUse is called.
     * Override to add custom requirements (e.g., must be in a JobSite).
     *
     * @param player The player attempting to use the item
     * @param event  The interaction event
     * @return true if the player can use the item, false to block usage
     */
    public boolean canUse(Player player, PlayerInteractEvent event) {
        return true;
    }

    /**
     * Creates a new ItemStack of this custom item.
     *
     * @param amount Number of items
     * @return The created ItemStack
     */
    public ItemStack createItemStack(int amount) {
        ItemStack item = new ItemStack(getMaterial(), amount);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(getDisplayName());
        meta.setLore(getLore());

        // Tag with item ID for identification
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_ID_KEY, PersistentDataType.STRING, getItemId());

        // Allow subclasses to add additional data
        applyAdditionalData(meta);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a single ItemStack of this custom item.
     */
    public ItemStack createItemStack() {
        return createItemStack(1);
    }

    /**
     * Override to add additional persistent data or meta modifications.
     */
    protected void applyAdditionalData(ItemMeta meta) {
        // Default: no additional data
    }

    /**
     * Checks if the given ItemStack is this specific custom item.
     */
    public boolean isThisItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String id = pdc.get(ITEM_ID_KEY, PersistentDataType.STRING);
        return getItemId().equals(id);
    }

    /**
     * Utility to consume one item from the player's hand.
     */
    protected void consumeItem(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }
}