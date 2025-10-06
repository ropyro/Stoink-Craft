package com.stoinkcraft.earnings;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.market.MarketManager;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EarningListener implements Listener {

    private final StoinkCore plugin;

    private final Map<UUID, Double> pendingEarnings = new HashMap<>();
    private final Map<UUID, BukkitRunnable> scheduledMessages = new HashMap<>();

    public EarningListener(StoinkCore plugin) {
        this.plugin = plugin;
    }

    private void addEarnings(Player player, Enterprise enterprise, double value) {
        double playerEarning = value* SCConstants.PLAYER_PAY_SPLIT_PERCENTAGE;
        double enterpriseEarning = value - playerEarning;

        UUID uuid = player.getUniqueId();

        // Update pending earnings
        pendingEarnings.put(uuid, pendingEarnings.getOrDefault(uuid, 0.0) + playerEarning);

        // Deposit to enterprise
        enterprise.increaseBankBalance(enterpriseEarning);

        // Reset or schedule payout message
        if (scheduledMessages.containsKey(uuid)) {
            return;
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                double total = pendingEarnings.remove(uuid);
                if (total > 0) {
                    StoinkCore.getEconomy().depositPlayer(player, total);
                    player.sendMessage("§a==== Earnings ====");
                    player.sendMessage("§aYou earned §e$" + String.format("%.2f", total) + " §afrom your recent work.");
                }
                scheduledMessages.remove(uuid);
            }
        };

        int delayTicks = 200; // 15 to 30 seconds
        task.runTaskLater(plugin, delayTicks);
        scheduledMessages.put(uuid, task);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (scheduledMessages.containsKey(uuid)) {
            scheduledMessages.get(uuid).cancel();
            scheduledMessages.remove(uuid);
        }
        pendingEarnings.remove(uuid);
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();
        if(em.getEnterpriseByMember(player.getUniqueId()) == null) return;
            Enterprise e = em.getEnterpriseByMember(player.getUniqueId());
            Material blockType = event.getBlock().getType();
            double value = MarketManager.getPrice(blockType.name(), MarketManager.JobType.RESOURCE_COLLECTION);
            if (value > 0) {
                addEarnings(player, e, value);
            }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;

        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();
        if(em.getEnterpriseByMember(killer.getUniqueId()) == null) return;

            Enterprise e = em.getEnterpriseByMember(killer.getUniqueId());
            String mobType = entity.getType().name();
            double value = MarketManager.getPrice(mobType, MarketManager.JobType.HUNTING);
            if (value > 0) {
                addEarnings(killer, e, value);
            }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = event.getPlayer();
        double value = MarketManager.getPrice(Material.COD.name(), MarketManager.JobType.FISHING);

        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();
        if(em.getEnterpriseByMember(player.getUniqueId()) == null) return;
        Enterprise e = em.getEnterpriseByMember(player.getUniqueId());

        if (value > 0) {
            addEarnings(player, e, value);
        }
    }
}
