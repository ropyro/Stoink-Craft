package com.stoinkcraft.enterprise.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatInvestListener implements Listener {

    public static final Set<UUID> awaitingInvestment = new HashSet<>();

    private StoinkCore plugin;

    public ChatInvestListener(StoinkCore plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onChatInvest(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!awaitingInvestment.contains(uuid)) return;

        event.setCancelled(true); // prevent message from showing to others
        Bukkit.getScheduler().runTask(plugin, () -> { // move to sync thread
            try {
                double amount = Double.parseDouble(event.getMessage());
                if (amount <= 0) {
                    ChatUtils.sendMessage(player,"§cPlease enter a valid amount.");
                    return;
                }

                Enterprise ent = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(uuid);
                if (ent == null) {
                    ChatUtils.sendMessage(player,"§cYou're no longer in an enterprise.");
                    return;
                }

                if (ent.getBankBalance() < amount) {
                    ChatUtils.sendMessage(player,"§cInsufficient enterprise funds.");
                    return;
                }

                ent.decreaseBankBalance(amount);
                ChatUtils.sendMessage(player,"§aInvested §e$" + amount + " §afrom your enterprise bank into networth.");
                ent.increaseNetworth(amount);

            } catch (NumberFormatException ex) {
                ChatUtils.sendMessage(player,"§cInvalid number. Try again.");
            } finally {
                awaitingInvestment.remove(uuid);
            }
        });
    }
}
