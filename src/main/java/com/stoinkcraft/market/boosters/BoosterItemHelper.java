package com.stoinkcraft.market.boosters;

import org.bukkit.inventory.ItemStack;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.stoinkcraft.StoinkCore;

import java.util.ArrayList;
import java.util.List;

public class BoosterItemHelper {

    private static final NamespacedKey MULTIPLIER_KEY = new NamespacedKey(StoinkCore.getInstance(), "booster_multiplier");
    private static final NamespacedKey DURATION_KEY = new NamespacedKey(StoinkCore.getInstance(), "booster_duration");

    public static boolean isBoosterItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.has(MULTIPLIER_KEY, PersistentDataType.DOUBLE)
                && data.has(DURATION_KEY, PersistentDataType.LONG);
    }

    public static double getMultiplier(ItemStack item) {
        if (!isBoosterItem(item)) return 1.0;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.getOrDefault(MULTIPLIER_KEY, PersistentDataType.DOUBLE, 1.0);
    }

    public static long getDurationTicks(ItemStack item) {
        if (!isBoosterItem(item)) return 0L;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.getOrDefault(DURATION_KEY, PersistentDataType.LONG, 0L);
    }

    public static ItemStack getBoosterItemStack(double multiplier, long durationTicks) {
        ItemStack item = new ItemStack(Material.FIRE_CHARGE);
        ItemMeta meta = item.getItemMeta();

        // Calculate human-readable time (seconds/minutes)
        long seconds = durationTicks / 20;
        String durationText = seconds >= 60
                ? (seconds / 60) + "m"
                : seconds + "s";

        // Display name
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + multiplier + "x Booster (" + durationText + ")");

        // Lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Activates a " + ChatColor.GOLD + multiplier + "x" + ChatColor.GRAY + " profit boost!");
        lore.add(ChatColor.GRAY + "Duration: " + ChatColor.YELLOW + durationText);
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Right-click to activate");
        meta.setLore(lore);

        // Persistent data
        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(MULTIPLIER_KEY, PersistentDataType.DOUBLE, multiplier);
        data.set(DURATION_KEY, PersistentDataType.LONG, durationTicks);

        item.setItemMeta(meta);
        return item;
    }
}

