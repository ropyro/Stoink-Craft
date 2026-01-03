package com.stoinkcraft.jobs.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.contracts.ContractContext;
import com.stoinkcraft.jobs.jobsites.JobSiteManager;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityKill(EntityDeathEvent event) {
        StoinkCore core = StoinkCore.getInstance();
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Enterprise enterprise = core.getEnterpriseManager()
                .getEnterpriseByMember(killer.getUniqueId());
        if (enterprise == null) return;

        JobSiteType jobSiteType = enterprise.getJobSiteManager().resolveJobsite(
                event.getEntity().getLocation());

        if (jobSiteType == null) return;

        ContractContext context = new ContractContext(
                killer,
                jobSiteType,
                event.getEntity().getType(),
                1
        );

        core.getContractManager().handleContext(enterprise, context);
        event.getDrops().clear();
    }

}
