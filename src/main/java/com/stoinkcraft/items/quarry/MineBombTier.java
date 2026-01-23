package com.stoinkcraft.items.quarry;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Defines the three mine bomb tiers with configurable radius.
 */
public enum MineBombTier {

    SMALL(
            "small_mine_bomb",
            "Small Mine Bomb",
            ChatColor.GREEN,
            Material.MAGMA_CREAM,
            3 // radius
    ),

    MEDIUM(
            "medium_mine_bomb",
            "Medium Mine Bomb",
            ChatColor.GOLD,
            Material.MAGMA_CREAM,
            5 // radius
    ),

    LARGE(
            "large_mine_bomb",
            "Large Mine Bomb",
            ChatColor.RED,
            Material.MAGMA_CREAM,
            7 // radius
    );

    private final String itemId;
    private final String displayName;
    private final ChatColor color;
    private final Material material;
    private int radius;

    MineBombTier(String itemId, String displayName, ChatColor color, Material material, int radius) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.color = color;
        this.material = material;
        this.radius = radius;
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

    public int getRadius() {
        return radius;
    }

    /**
     * Gets a tier by its item ID.
     */
    public static MineBombTier fromItemId(String itemId) {
        for (MineBombTier tier : values()) {
            if (tier.getItemId().equals(itemId)) {
                return tier;
            }
        }
        return null;
    }
}