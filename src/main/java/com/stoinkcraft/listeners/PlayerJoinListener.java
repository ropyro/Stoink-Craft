package com.stoinkcraft.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.guis.UnemployedGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final StoinkCore plugin;

    public PlayerJoinListener(StoinkCore plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(!EnterpriseManager.getEnterpriseManager().isInEnterprise(player.getUniqueId())) {
            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    new UnemployedGUI(player).openWindow();
                }
            };

            int delayTicks = 30 * 20;
            task.runTaskLater(plugin, delayTicks);
        }
    }
}
