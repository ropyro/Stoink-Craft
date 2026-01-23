package com.stoinkcraft.jobsites.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.components.JobSiteNPC;
import com.stoinkcraft.utils.ChatUtils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class NPCInteractListener implements Listener {

    private final StoinkCore plugin;

    public NPCInteractListener(StoinkCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        Player player = event.getClicker();

        // Get the job site data from NPC's persistent data
        String enterpriseID = npc.data().get("ENTERPRISE_ID");
        String jobSiteType = npc.data().get("JOBSITE_TYPE");

        if (enterpriseID == null) {
            return;
        }

        // Get the enterprise and job site
        Enterprise enterprise = plugin.getEnterpriseManager().getEnterpriseByID(UUID.fromString(enterpriseID));
        if (enterprise == null) {
            ChatUtils.sendMessage(player,ChatColor.RED + "Could not find associated enterprise!");
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

        JobSiteNPC jobSiteNPC = (JobSiteNPC) jobSite.getComponents().stream()
                .filter(component -> component instanceof JobSiteNPC)
                .filter(n -> ((JobSiteNPC) n).getNpc().getId() == event.getNPC()
                .getId())
                .findFirst()
                .orElse(null);

        if(jobSiteNPC != null)
            jobSiteNPC.onRightClick(event);
    }
}