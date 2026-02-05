package com.stoinkcraft.items;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.enterprise.Enterprise;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class JobSiteItem extends StoinkItem {

    public abstract JobSiteType getRequiredJobSiteType();

    public abstract boolean onUseAtJobSite(Player player, JobSite jobSite, PlayerInteractEvent event);

    @Override
    public boolean canUse(Player player, PlayerInteractEvent event) {
        JobSite jobSite = getPlayerJobSite(player);

        if (jobSite == null) {
            player.sendMessage(ChatColor.RED + "You must be inside a " +
                    getRequiredJobSiteType().getDisplayName() + " to use this item!");
            return false;
        }

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
            return false;
        }

        return onUseAtJobSite(player, jobSite, event);
    }

    protected JobSite getPlayerJobSite(Player player) {
        return StoinkCore.getInstance().getProtectionManager()
                .getJobSiteAt(player.getLocation(), getRequiredJobSiteType());
    }
}