package com.stoinkcraft.misc;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.PlayerUtils;
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

        switch (identifier.toLowerCase()) {
            case "enterprise":
                if(e == null){
                    return "§7Unemployed";
                }
                return e.getName();
            case "role":
                if(e == null) return "§7Unemployed";
                return e.getMemberRole(player.getUniqueId()).getFormattedName();
            case "ent_balance":
                if(e == null){
                    return " ";
                }
                return "$" + ChatUtils.formatMoneyNoCents(e.getBankBalance());
            case "networth":
                if(e == null){
                    return " ";
                }
                return "$" + ChatUtils.formatMoneyNoCents(e.getNetWorth());
            case "player_networth":
                return "$" + ChatUtils.formatMoneyNoCents(PlayerUtils.getPlayerNetworth(player));
            default:
                return null;
        }
    }
}
