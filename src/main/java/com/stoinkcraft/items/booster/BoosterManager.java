package com.stoinkcraft.items.booster;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages active boosters, including expiration scheduling and persistence recovery.
 */
public class BoosterManager {

    private final StoinkCore plugin;
    private final Map<UUID, BukkitTask> expirationTasks = new HashMap<>();

    public BoosterManager(StoinkCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Activates a new booster for an enterprise.
     */
    public void activateBooster(Enterprise enterprise, BoosterTier tier) {
        // Cancel any existing expiration task (shouldn't happen, but safety first)
        cancelExpirationTask(enterprise.getID());

        // Create and set the booster
        Booster booster = new Booster(tier);
        enterprise.setActiveBooster(booster);

        // Schedule expiration
        scheduleExpiration(enterprise, booster);
    }

    /**
     * Called on server startup to restore booster expiration tasks.
     */
    public void restoreBoostersOnStartup() {
        for (Enterprise enterprise : EnterpriseManager.getEnterpriseManager().getEnterpriseList()) {
            Booster booster = enterprise.getActiveBooster();

            if (booster == null) {
                continue;
            }

            if (booster.isExpired()) {
                // Booster expired while server was offline
                enterprise.setActiveBooster(null);

                plugin.getLogger().info("Cleared expired booster for enterprise: " + enterprise.getName());
            } else {
                // Schedule remaining time
                scheduleExpiration(enterprise, booster);

                plugin.getLogger().info("Restored booster for enterprise: " + enterprise.getName() +
                        " (" + booster.getFormattedTimeRemaining() + " remaining)");
            }
        }
    }

    /**
     * Schedules the expiration task for a booster.
     */
    private void scheduleExpiration(Enterprise enterprise, Booster booster) {
        long remainingTicks = booster.getTimeRemainingTicks();

        if (remainingTicks <= 0) {
            // Already expired
            expireBooster(enterprise, booster);
            return;
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                expireBooster(enterprise, booster);
            }
        }.runTaskLater(plugin, remainingTicks);

        expirationTasks.put(enterprise.getID(), task);
    }

    /**
     * Handles booster expiration.
     */
    private void expireBooster(Enterprise enterprise, Booster booster) {
        enterprise.setActiveBooster(null);
        expirationTasks.remove(enterprise.getID());

        // Notify online members
        for (UUID memberId : enterprise.getMembers().keySet()) {
            Player online = Bukkit.getPlayer(memberId);
            if (online != null && online.isOnline()) {
                ChatUtils.sendMessage(online,
                        ChatColor.GOLD + "" + booster.getMultiplier() + "x" +
                                ChatColor.YELLOW + " booster has expired!");
            }
        }
    }

    /**
     * Cancels an expiration task if one exists.
     */
    private void cancelExpirationTask(UUID enterpriseId) {
        BukkitTask task = expirationTasks.remove(enterpriseId);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Called on plugin disable to clean up tasks.
     */
    public void shutdown() {
        for (BukkitTask task : expirationTasks.values()) {
            task.cancel();
        }
        expirationTasks.clear();
    }

    /**
     * Gets the active multiplier for an enterprise (1.0 if no booster).
     */
    public double getMultiplier(Enterprise enterprise) {
        if (enterprise == null) {
            return 1.0;
        }

        Booster booster = enterprise.getActiveBooster();
        if (booster == null || booster.isExpired()) {
            return 1.0;
        }

        return booster.getMultiplier();
    }
}