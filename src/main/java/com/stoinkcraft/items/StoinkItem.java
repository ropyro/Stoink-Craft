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

public abstract class StoinkItem {

    public static final NamespacedKey ITEM_ID_KEY = new NamespacedKey(StoinkCore.getInstance(), "stoink_item_id");

    public abstract String getItemId();

    public abstract Material getMaterial();

    public abstract String getDisplayName();

    public abstract List<String> getLore();

    public abstract boolean onUse(Player player, PlayerInteractEvent event);

    public boolean canUse(Player player, PlayerInteractEvent event) {
        return true;
    }

    public ItemStack createItemStack(int amount) {
        ItemStack item = new ItemStack(getMaterial(), amount);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(getDisplayName());
        meta.setLore(getLore());

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_ID_KEY, PersistentDataType.STRING, getItemId());

        applyAdditionalData(meta);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createItemStack() {
        return createItemStack(1);
    }

    protected void applyAdditionalData(ItemMeta meta) {
    }

    public boolean isThisItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String id = pdc.get(ITEM_ID_KEY, PersistentDataType.STRING);
        return getItemId().equals(id);
    }

    protected void consumeItem(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }
}