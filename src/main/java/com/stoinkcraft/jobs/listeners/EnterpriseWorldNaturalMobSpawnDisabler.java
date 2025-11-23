package com.stoinkcraft.jobs.listeners;

import com.stoinkcraft.StoinkCore;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EnterpriseWorldNaturalMobSpawnDisabler implements Listener {

    private StoinkCore stoinkCore;
    private World enterpriseWorld;

    public EnterpriseWorldNaturalMobSpawnDisabler(StoinkCore stoinkCore, World enterpriseWorld){
        this.stoinkCore = stoinkCore;
        this.enterpriseWorld = enterpriseWorld;
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event){
        if(event.getLocation().getWorld().equals(enterpriseWorld)){
            if(event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)){
                event.setCancelled(true);
            }
        }
    }
}
