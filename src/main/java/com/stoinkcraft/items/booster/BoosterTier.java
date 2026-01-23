package com.stoinkcraft.items.booster;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Defines the three booster tiers with their multipliers and durations.
 */
public enum BoosterTier {

    SMALL(
            "small_booster",
            "Small Booster",
            ChatColor.GREEN,
            Material.FIRE_CHARGE,
            1.5,
            30 * 60 * 1000L // 5 minutes
    ),

    MEDIUM(
            "medium_booster",
            "Medium Booster",
            ChatColor.GOLD,
            Material.BLAZE_POWDER,
            2.0,
            45 * 60 * 1000L // 10 minutes
    ),

    LARGE(
            "large_booster",
            "Large Booster",
            ChatColor.LIGHT_PURPLE,
            Material.NETHER_STAR,
            3.0,
            60 * 60 * 1000L // 15 minutes
    );

    private final String itemId;
    private final String displayName;
    private final ChatColor color;
    private final Material material;
    private final double multiplier;
    private final long durationMillis;

    BoosterTier(String itemId, String displayName, ChatColor color, Material material,
                double multiplier, long durationMillis) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.color = color;
        this.material = material;
        this.multiplier = multiplier;
        this.durationMillis = durationMillis;
    }

    public String getItemId() {
        return itemId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public Material getMaterial() {
        return material;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public long getDurationTicks() {
        return durationMillis / 50;
    }

    public String getFormattedDuration() {
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;

        if (minutes > 0) {
            return minutes + "m";
        } else {
            return seconds + "s";
        }
    }

    /**
     * Gets a tier by its item ID.
     */
    public static BoosterTier fromItemId(String itemId) {
        for (BoosterTier tier : values()) {
            if (tier.getItemId().equals(itemId)) {
                return tier;
            }
        }
        return null;
    }
}