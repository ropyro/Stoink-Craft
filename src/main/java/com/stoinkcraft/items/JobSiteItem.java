package com.stoinkcraft.items;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.enterprise.Enterprise;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Abstract base for items that must be used within a specific JobSite type.
 */
public abstract class JobSiteItem extends StoinkItem {

    /**
     * @return The JobSiteType this item works with (e.g., FARMLAND, QUARRY)
     */
    public abstract JobSiteType getRequiredJobSiteType();

    /**
     * Called when the item is used inside a valid JobSite.
     *
     * @param player  The player using the item
     * @param jobSite The JobSite the player is in
     * @param event   The interaction event
     * @return true if the item should be consumed
     */
    public abstract boolean onUseAtJobSite(Player player, JobSite jobSite, PlayerInteractEvent event);

    @Override
    public boolean canUse(Player player, PlayerInteractEvent event) {
        JobSite jobSite = getPlayerJobSite(player);

        if (jobSite == null) {
            player.sendMessage(ChatColor.RED + "You must be inside a " +
                    getRequiredJobSiteType().getDisplayName() + " to use this item!");
            return false;
        }

        // Check enterprise membership
        Enterprise enterprise = jobSite.getEnterprise();
        if (!enterprise.isMember(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can only use this item in your own " +
                    getRequiredJobSiteType().getDisplayName() + "!");
            return false;
        }

        return true;
    }

    @Override
    public boolean onUse(Player player, PlayerInteractEvent event) {
        JobSite jobSite = getPlayerJobSite(player);

        if (jobSite == null) {
            // Shouldn't happen if canUse was checked, but safety first
            return false;
        }

        return onUseAtJobSite(player, jobSite, event);
    }

    /**
     * Finds the JobSite of the required type that contains the player.
     *
     * @return The JobSite, or null if player is not in one
     */
    protected JobSite getPlayerJobSite(Player player) {
        // Use protection manager's chunk index for efficient lookup
        return StoinkCore.getInstance().getProtectionManager()
                .getJobSiteAt(player.getLocation(), getRequiredJobSiteType());
    }
}