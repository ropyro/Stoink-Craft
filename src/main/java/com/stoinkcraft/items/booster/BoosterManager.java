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

public class BoosterManager {

    private final StoinkCore plugin;
    private final Map<UUID, BukkitTask> expirationTasks = new HashMap<>();

    public BoosterManager(StoinkCore plugin) {
        this.plugin = plugin;
    }

    public void activateBooster(Enterprise enterprise, BoosterTier tier) {
        cancelExpirationTask(enterprise.getID());

        Booster booster = new Booster(tier);
        enterprise.setActiveBooster(booster);

        scheduleExpiration(enterprise, booster);
    }

    public void restoreBoostersOnStartup() {
        for (Enterprise enterprise : EnterpriseManager.getEnterpriseManager().getEnterpriseList()) {
            Booster booster = enterprise.getActiveBooster();

            if (booster == null) {
                continue;
            }

            if (booster.isExpired()) {
                enterprise.setActiveBooster(null);

                plugin.getLogger().info("Cleared expired booster for enterprise: " + enterprise.getName());
            } else {
                scheduleExpiration(enterprise, booster);

                plugin.getLogger().info("Restored booster for enterprise: " + enterprise.getName() +
                        " (" + booster.getFormattedTimeRemaining() + " remaining)");
            }
        }
    }

    private void scheduleExpiration(Enterprise enterprise, Booster booster) {
        long remainingTicks = booster.getTimeRemainingTicks();

        if (remainingTicks <= 0) {
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

    private void expireBooster(Enterprise enterprise, Booster booster) {
        enterprise.setActiveBooster(null);
        expirationTasks.remove(enterprise.getID());

        for (UUID memberId : enterprise.getMembers().keySet()) {
            Player online = Bukkit.getPlayer(memberId);
            if (online != null && online.isOnline()) {
                ChatUtils.sendMessage(online,
                        ChatColor.GOLD + "" + booster.getMultiplier() + "x" +
                                ChatColor.YELLOW + " booster has expired!");
            }
        }
    }

    private void cancelExpirationTask(UUID enterpriseId) {
        BukkitTask task = expirationTasks.remove(enterpriseId);
        if (task != null) {
            task.cancel();
        }
    }

    public void shutdown() {
        for (BukkitTask task : expirationTasks.values()) {
            task.cancel();
        }
        expirationTasks.clear();
    }

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