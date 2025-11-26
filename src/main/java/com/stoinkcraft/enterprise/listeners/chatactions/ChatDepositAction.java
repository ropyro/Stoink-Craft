package com.stoinkcraft.enterprise.listeners.chatactions;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChatDepositAction implements ChatAction {

    public static final Set<UUID> awaitingDeposit = new HashSet<>();

    private StoinkCore plugin;

    public ChatDepositAction(StoinkCore plugin){
        this.plugin = plugin;
    }

    @Override
    public void handleChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!awaitingDeposit.contains(uuid)) return;

        event.setCancelled(true); // prevent message from showing to others
        Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () -> { // move to sync thread
            try {
                double amount = Double.parseDouble(event.getMessage());
                if (amount <= 0) {
                    ChatUtils.sendMessage(player, "§cPlease enter a valid amount.");
                    return;
                }

                Enterprise ent = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(uuid);
                if (ent == null) {
                    ChatUtils.sendMessage(player,"§cYou're no longer in an enterprise.");
                    return;
                }

                if (StoinkCore.getEconomy().getBalance(player) < amount) {
                    ChatUtils.sendMessage(player,"§cInsufficient funds.");
                    return;
                }

                ent.increaseBankBalance(amount);
                ChatUtils.sendMessage(player,"§aDeposited §e$" + amount + " §ainto your enterprise bank.");
                // Add to player's personal balance here (Economy API)
                StoinkCore.getEconomy().withdrawPlayer(player, amount); // Assuming Vault integration

            } catch (NumberFormatException ex) {
                ChatUtils.sendMessage(player,"§cInvalid number. Try again.");
            } finally {
                awaitingDeposit.remove(uuid);
            }
        });
    }
}
