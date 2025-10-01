package com.stoinkcraft.utils;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StoinkExpansion extends PlaceholderExpansion {

    private StoinkCore plugin;

    public StoinkExpansion(StoinkCore plugin){
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "stoinkcraft";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ropyro";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true; // Makes it survive /papi reload
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) return "";

        Enterprise e = EnterpriseManager.getEnterpriseManager().getEnterpriseByMember(player.getUniqueId());
        if (e == null){
            if(identifier.toLowerCase().equalsIgnoreCase("role")){
                return "§7Unemployed";
            }else{
                return "";
            }
        }

        switch (identifier.toLowerCase()) {
            case "enterprise":
                return e.getName();
            case "role":
                if(e.getMemberRole(player.getUniqueId()) == null) return "&7Unemployed";
                String rolename = e.getMemberRole(player.getUniqueId()).roleName();
                switch(rolename.toLowerCase()){
                    case "ceo":
                        return "§c§lCEO";
                    case "cfo":
                        return "§b§lCFO";
                    case "coo":
                        return "§aCOO";
                    case "employee":
                        return "§eEmployee";
                }
                return rolename;
            case "networth":
                return ChatUtils.formatMoney(e.getNetWorth());
            case "ent-balance":
                return ChatUtils.formatMoney(e.getBankBalance());
            default:
                return null;
        }
    }
}
