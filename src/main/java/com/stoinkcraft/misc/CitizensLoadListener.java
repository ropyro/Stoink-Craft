package com.stoinkcraft.misc;

import com.stoinkcraft.StoinkCore;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CitizensLoadListener implements Listener {

    private final StoinkCore plugin;

    public CitizensLoadListener(StoinkCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCitizensEnable(CitizensEnableEvent event) {
        plugin.getLogger().info("Citizens fully loaded, initializing JobSites...");
        plugin.onCitizensReady();
    }
}