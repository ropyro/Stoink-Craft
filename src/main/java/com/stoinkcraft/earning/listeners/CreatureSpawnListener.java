package com.stoinkcraft.earning.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {

    private World enterpriseWorld;

    public CreatureSpawnListener(World enterpriseWorld){
        this.enterpriseWorld = enterpriseWorld;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobSpawn(CreatureSpawnEvent event){
        if(event.getLocation().getWorld().equals(enterpriseWorld)){
            CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
            if(reason == CreatureSpawnEvent.SpawnReason.NATURAL ||
                    reason == CreatureSpawnEvent.SpawnReason.CHUNK_GEN ||
                    reason == CreatureSpawnEvent.SpawnReason.JOCKEY ||
                    reason == CreatureSpawnEvent.SpawnReason.MOUNT ||
                    reason == CreatureSpawnEvent.SpawnReason.NETHER_PORTAL ||
                    reason == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS ||
                    reason == CreatureSpawnEvent.SpawnReason.SILVERFISH_BLOCK) {
                event.setCancelled(true);
            }
        }
    }
}
