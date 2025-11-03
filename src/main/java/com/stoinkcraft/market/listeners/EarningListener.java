package com.stoinkcraft.market.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.market.MarketManager;
import com.stoinkcraft.utils.SCConstants;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
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
        if(enterprise.isBoosted()){
            value*=enterprise.getActiveBooster().getMultiplier();
        }

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
    public void onBlockDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;


        Block block = event.getBlock();
        Material dropType = event.getItems().get(0).getItemStack().getType();

        Enterprise e = EnterpriseManager.getEnterpriseManager()
                .getEnterpriseByMember(player.getUniqueId());
        if (e == null) return;

        double totalEarnings = 0.0;

        for (Item item : event.getItems()) {
            if(isCrop(item.getItemStack().getType()) && !isFullyGrown(event.getBlock())) continue;

            ItemStack stack = item.getItemStack();
            Material itemMaterial = stack.getType();
            int amount = stack.getAmount();
            double baseValue = MarketManager.getItemPrice(itemMaterial);

            totalEarnings += baseValue * amount;
        }

        if (totalEarnings > 0) {
            addEarnings(player, e, totalEarnings);
        }
    }

    private boolean isCrop(Material material) {
        return switch (material) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART -> true;
            default -> false;
        };
    }

    private boolean isFullyGrown(Block block) {
        BlockState state = block.getState();
        if (!(state instanceof Ageable ageable)) return false;
        return ageable.getAge() >= ageable.getMaximumAge();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer == null) return;

        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();
        if(em.getEnterpriseByMember(killer.getUniqueId()) == null) return;

            Enterprise e = em.getEnterpriseByMember(killer.getUniqueId());
            EntityType mobType = entity.getType();
            double value = MarketManager.getEntityPrice(mobType);
            if (value > 0) {
                addEarnings(killer, e, value);
            }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;

        Player player = event.getPlayer();
        EnterpriseManager em = EnterpriseManager.getEnterpriseManager();
        Enterprise e = em.getEnterpriseByMember(player.getUniqueId());
        if (e == null) return;

        Entity caughtEntity = event.getCaught();
        if (!(caughtEntity instanceof Item caughtItem)) return;

        ItemStack stack = caughtItem.getItemStack();
        Material type = stack.getType();
        int amount = stack.getAmount();
        player.sendMessage("Caught amount: " + amount);
        double baseValue = MarketManager.getItemPrice(type);

        double totalValue = baseValue * amount;

        if (totalValue > 0) {
            addEarnings(player, e, totalValue);
        }
    }

}
