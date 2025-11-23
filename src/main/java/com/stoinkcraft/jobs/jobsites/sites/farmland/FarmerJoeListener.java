package com.stoinkcraft.jobs.jobsites.sites.farmland;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.TimeUtils;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

public class FarmerJoeListener implements Listener {

    private final StoinkCore plugin;

    public FarmerJoeListener(StoinkCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        Player player = event.getClicker();

        // Get the job site data from NPC's persistent data
        String enterpriseID = npc.data().get("jobsite").toString();
        String jobSiteType = npc.data().get("jobsitetype").toString();

        if (enterpriseID == null || !JobSiteType.FARMLAND.name().equals(jobSiteType)) {
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
        FarmlandSite farmlandSite = enterprise.getJobSiteManager().getFarmlandSite();
        if (farmlandSite == null) {
            ChatUtils.sendMessage(player,ChatColor.RED + "Could not find farmland site!");
            return;
        }

        // Open your upgrade GUI
        if(TimeUtils.isDay(farmlandSite.getSpawnPoint().getWorld())){
            openFarmlandUpgradeGUI(player, farmlandSite);
        }else{
            ChatUtils.sendMessage(player,ChatColor.RED + "It's night time silly the crops are sleeping...");
        }
    }

    private void openFarmlandUpgradeGUI(Player player, FarmlandSite farmlandSite) {
        // Your GUI opening code here
        new FarmlandGui(farmlandSite, player).openWindow();
        ChatUtils.sendMessage(player,ChatColor.GREEN + "Opening Farmland Upgrades...");
    }
}