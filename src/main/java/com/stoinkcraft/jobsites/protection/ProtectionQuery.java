package com.stoinkcraft.jobsites.protection;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ProtectionQuery(
        @NotNull Player player,
        @NotNull Location location,
        @NotNull ProtectionAction action,
        @Nullable ItemStack tool,
        @Nullable Entity targetEntity
) {
    /**
     * Convenience constructor for block-based actions
     */
    public static ProtectionQuery blockAction(Player player, Location location, ProtectionAction action, ItemStack tool) {
        return new ProtectionQuery(player, location, action, tool, null);
    }

    /**
     * Convenience constructor for entity-based actions
     */
    public static ProtectionQuery entityAction(Player player, Entity target, ProtectionAction action, ItemStack tool) {
        return new ProtectionQuery(player, target.getLocation(), action, tool, target);
    }
}