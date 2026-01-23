package com.stoinkcraft.items.graveyard.hound;

import com.stoinkcraft.items.JobSiteItem;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * Spawns a temporary wolf companion that attacks undead mobs in the Graveyard.
 */
public class GraveyardHoundItem extends JobSiteItem {

    private static final String ITEM_ID = "graveyard_hound";

    @Override
    public String getItemId() {
        return ITEM_ID;
    }

    @Override
    public Material getMaterial() {
        return Material.WOLF_SPAWN_EGG;
    }

    @Override
    public String getDisplayName() {
        return ChatColor.AQUA + "" + ChatColor.BOLD + "Graveyard Hound";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "",
                ChatColor.GRAY + "Summon a spectral hound to",
                ChatColor.GRAY + "hunt undead in your Graveyard!",
                "",
                ChatColor.GRAY + "Duration: " + ChatColor.WHITE + "5 minutes " + ChatColor.GRAY + "(active time)",
                ChatColor.GRAY + "Target: " + ChatColor.RED + "Undead mobs",
                "",
                ChatColor.YELLOW + "Must be used in your Graveyard",
                "",
                ChatColor.DARK_GRAY + "Right-click to summon"
        );
    }

    @Override
    public JobSiteType getRequiredJobSiteType() {
        return JobSiteType.GRAVEYARD;
    }

    @Override
    public boolean onUseAtJobSite(Player player, JobSite jobSite, PlayerInteractEvent event) {
        GraveyardHoundManager.spawnHound(player, jobSite);

        ChatUtils.sendMessage(player,
                ChatColor.AQUA + "🐺 " + ChatColor.GREEN + "A Graveyard Hound answers your call!");

        return true; // Consume the item
    }
}