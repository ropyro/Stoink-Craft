package com.stoinkcraft.items.booster;

import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.enterprise.EnterpriseManager;
import com.stoinkcraft.items.StoinkItem;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * A consumable item that activates a booster for the player's enterprise.
 */
public class BoosterItem extends StoinkItem {

    private final BoosterTier tier;

    public BoosterItem(BoosterTier tier) {
        this.tier = tier;
    }

    @Override
    public String getItemId() {
        return tier.getItemId();
    }

    @Override
    public Material getMaterial() {
        return tier.getMaterial();
    }

    @Override
    public String getDisplayName() {
        return tier.getColor() + "" + ChatColor.BOLD + tier.getMultiplier() + "x " + tier.getDisplayName();
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "",
                ChatColor.GRAY + "Activates a " + tier.getColor() + tier.getMultiplier() + "x" +
                        ChatColor.GRAY + " earnings boost",
                ChatColor.GRAY + "for your entire enterprise!",
                "",
                ChatColor.GRAY + "Duration: " + ChatColor.YELLOW + tier.getFormattedDuration(),
                "",
                ChatColor.DARK_GRAY + "Right-click to activate"
        );
    }

    @Override
    public boolean canUse(Player player, PlayerInteractEvent event) {
        Enterprise enterprise = EnterpriseManager.getEnterpriseManager()
                .getEnterpriseByMember(player.getUniqueId());

        if (enterprise == null) {
            ChatUtils.sendMessage(player, ChatColor.RED + "You must be in an enterprise to use this!");
            return false;
        }

        if (enterprise.hasActiveBooster()) {
            Booster current = enterprise.getActiveBooster();
            ChatUtils.sendMessage(player, ChatColor.RED + "Your enterprise already has an active booster! (" +
                    current.getFormattedTimeRemaining() + " remaining)");
            return false;
        }

        return true;
    }

    @Override
    public boolean onUse(Player player, PlayerInteractEvent event) {
        Enterprise enterprise = EnterpriseManager.getEnterpriseManager()
                .getEnterpriseByMember(player.getUniqueId());

        if (enterprise == null) {
            return false;
        }

        // Activate the booster
        EnterpriseManager.getEnterpriseManager().activateBooster(enterprise, tier);

        // Broadcast to server
        Bukkit.broadcastMessage(
                tier.getColor() + "" + ChatColor.BOLD + enterprise.getName() +
                        ChatColor.YELLOW + " activated a " +
                        tier.getColor() + "" + ChatColor.BOLD + tier.getMultiplier() + "x" +
                        ChatColor.YELLOW + " booster for " +
                        ChatColor.WHITE + tier.getFormattedDuration() + ChatColor.YELLOW + "!"
        );

        return true; // Consume the item
    }

    /**
     * Gets the tier for this booster item.
     */
    public BoosterTier getTier() {
        return tier;
    }
}