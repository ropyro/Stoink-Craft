package com.stoinkcraft.jobsites.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.collections.CollectionManager;
import com.stoinkcraft.jobsites.contracts.ContractContext;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.sites.sites.graveyard.GraveyardSite;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

public class EntityDeathListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityKill(EntityDeathEvent event) {
        StoinkCore core = StoinkCore.getInstance();
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Enterprise enterprise = core.getEnterpriseManager()
                .getEnterpriseByMember(killer.getUniqueId());
        if (enterprise == null) return;

        LivingEntity entity = event.getEntity();
        Location entityLocation = entity.getLocation();

        JobSiteType jobSiteType = enterprise.getJobSiteManager().resolveJobsite(entityLocation);

        if (jobSiteType == null) return;

        // ==================== Graveyard Special Handling ====================
        if (jobSiteType == JobSiteType.GRAVEYARD) {
            GraveyardSite graveyard = enterprise.getJobSiteManager().getGraveyardSite();

            if (graveyard != null) {
                // Check if it's a mausoleum spider
                NamespacedKey spiderKey = new NamespacedKey(StoinkCore.getInstance(), "mausoleum_spider");
                if (entity.getPersistentDataContainer().has(spiderKey, PersistentDataType.STRING)) {
                    graveyard.getMausoleumStructure().onSpiderKilled(killer);
                }

                // Check if it's a graveyard mob (for soul drops)
                NamespacedKey graveyardKey = new NamespacedKey(StoinkCore.getInstance(), "graveyard_mob");
                if (entity.getPersistentDataContainer().has(graveyardKey, PersistentDataType.STRING)) {
                    graveyard.onMobKilled(killer, entity.getType());
                }
            }
        }

        // ==================== Contract Progress ====================
        ContractContext context = new ContractContext(
                killer,
                jobSiteType,
                event.getEntity().getType(),
                1
        );

        core.getContractManager().handleContext(enterprise, context);
        event.getDrops().clear();

        // =========================
        // COLLECTION PROGRESS
        // =========================
        JobSite jobSite = enterprise.getJobSiteManager().getJobSite(jobSiteType);
        if (jobSite != null) {
            CollectionManager.handleEntityCollection(
                    enterprise,
                    jobSite,
                    entity.getType(),
                    killer
            );
        }
    }

}
