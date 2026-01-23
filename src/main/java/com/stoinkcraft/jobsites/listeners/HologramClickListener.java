package com.stoinkcraft.jobsites.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.components.JobSiteHologram;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.utils.ChatUtils;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class HologramClickListener implements Listener {

    private final StoinkCore plugin;

    public HologramClickListener(StoinkCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onHologramClick(HologramClickEvent event){
        Hologram hologram = event.getHologram();
        Player player = event.getPlayer();

        String id = event.getHologram().getId();
        String[] idComponents = id.split("_");

        String enterpriseID = idComponents[0];
        String jobSiteType = idComponents[1];

        // Get the enterprise and job site
        Enterprise enterprise = plugin.getEnterpriseManager().getEnterpriseByID(UUID.fromString(enterpriseID));
        if (enterprise == null) {
            ChatUtils.sendMessage(player, ChatColor.RED + "Could not find associated enterprise!");
            return;
        }

        // Check permissions
        if (!enterprise.isMember(player.getUniqueId())) {
            ChatUtils.sendMessage(player,ChatColor.RED + "You are not a member of this enterprise!");
            return;
        }

        // Get the farmland site
        JobSite jobSite = enterprise.getJobSiteManager().getJobSite(JobSiteType.valueOf(jobSiteType));
        if(jobSite == null){
            ChatUtils.sendMessage(player,ChatColor.RED + "Could not find jobsite!");
            return;
        }

        JobSiteHologram jobSiteHologram = (JobSiteHologram) jobSite.getComponents().stream()
                .filter(component -> component instanceof JobSiteHologram)
                .filter(n -> ((JobSiteHologram) n).getHologram().getId() == hologram.getId())
                .findFirst()
                .orElse(null);

        if(jobSiteHologram != null)
            jobSiteHologram.onHologramInteract(event);
        else
            ChatUtils.sendMessage(player, ChatColor.RED + "Jobsite hologram not found");
    }
}
