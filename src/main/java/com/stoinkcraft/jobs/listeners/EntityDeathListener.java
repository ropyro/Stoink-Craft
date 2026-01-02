package com.stoinkcraft.jobs.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.contracts.Contract;
import com.stoinkcraft.jobs.contracts.ContractManager;
import com.stoinkcraft.jobs.contracts.ContractType;
import com.stoinkcraft.jobs.jobsites.JobSiteManager;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;

public class EntityDeathListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onMobKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        Entity entity = event.getEntity();
        if (killer == null) return; // killed by environment or non-player

        StoinkCore core = StoinkCore.getInstance();

        // Only care about enterprise world
        World enterpriseWorld = core.getEnterpriseWorldManager().getWorld();
        if (!event.getEntity().getWorld().equals(enterpriseWorld)) return;

        // Find the killer's enterprise
        Enterprise enterprise = core.getEnterpriseManager()
                .getEnterpriseByMember(killer.getUniqueId());
        if (enterprise == null) return;

        // Determine which jobsite this kill happened in (if any)
        Location deathLoc = event.getEntity().getLocation();
        JobSiteType jobSiteType = null;

        JobSiteManager jobSiteManager = enterprise.getJobSiteManager(); // or however you access it

        if (jobSiteManager.getFarmlandSite() != null &&
                jobSiteManager.getFarmlandSite().contains(deathLoc)) {
            jobSiteType = JobSiteType.FARMLAND;
        }

        // If you later want quarry / skyrise kills to matter, add checks here
        // if (jobSiteManager.getQuarrySite() != null &&
        //     jobSiteManager.getQuarrySite().contains(deathLoc)) {
        //     jobSiteType = JobSiteType.QUARRY;
        // }

        if (jobSiteType == null) return; // not in any relevant jobsite

        // Get relevant contracts
        ContractManager contractManager = core.getContractManager();
        List<Contract> contracts = contractManager.getContracts(enterprise, jobSiteType);
        if (contracts.isEmpty()) return;

        //long now = System.currentTimeMillis();

        for (Contract contract : contracts) {
            // Skip expired or completed contracts
            if (contract.isExpired() || contract.isCompleted()) continue;

            // Optional: if you ever add "kill specific mob type", check event.getEntity().getType()
            // e.g. only count if it's COW, PIG, etc., depending on contract settings.

            // One kill = +1 progress (adjust if you want more complex weighting)
            if(contract.getContractType().equals(ContractType.COW_KILLS) && entity.getType().equals(EntityType.COW)){
                contract.addProgress(1);

                // Feedback to player (optional, but good UX)
                killer.sendMessage(ChatColor.YELLOW + "Contract progress: " +
                        contract.getCurrentProgress() + "/" + contract.getTargetAmount());

                if (contract.isCompleted()) {
                    killer.sendMessage(ChatColor.GREEN + "Contract complete! +" +
                            contract.getReward() + " net worth");
                }

                return;
            }
        }
    }
}
