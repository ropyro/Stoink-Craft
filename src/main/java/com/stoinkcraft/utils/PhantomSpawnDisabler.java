package com.stoinkcraft.utils;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class PhantomSpawnDisabler implements Listener {

    @EventHandler
    public void onPhantomSpawn(EntitySpawnEvent event){
        if(event.getEntityType().equals(EntityType.PHANTOM))
            event.setCancelled(true);
    }
}
