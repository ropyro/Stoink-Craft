package com.stoinkcraft.enterprise.listeners.chatactions;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class EnterpriseChatAction implements ChatAction{

    private StoinkCore plugin;

    public EnterpriseChatAction(StoinkCore plugin){
        this.plugin = plugin;
    }

    @Override
    public void handleChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (EnterpriseManager.getEnterpriseManager().isInEnterprise(uuid)) {
            Enterprise ent = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(uuid);
            if(ent.getActiveEnterpriseChat().contains(uuid)){
                event.setCancelled(true);
                String message = "§8[§b"+ ent.getName()+"§8] [" + ent.getMemberRole(uuid).getFormattedName() + "§8] §7" + player.getName() + " §8» §a§l" + event.getMessage();
                Bukkit.getScheduler().runTask(plugin, () -> {
                    ent.getMembers().keySet().stream().filter(u -> Bukkit.getOfflinePlayer(u).isOnline()).map(u -> Bukkit.getPlayer(u)).forEach(p -> p.sendMessage(message));
                });
            }
        }
    }
}
