package com.stoinkcraft.items.quarry;

import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.items.JobSiteItem;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * A throwable bomb that explodes and breaks blocks in a Quarry mine.
 */
public class MineBomb extends JobSiteItem {

    private final MineBombTier tier;

    public MineBomb(MineBombTier tier) {
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
        return tier.getColor() + "" + ChatColor.BOLD + tier.getDisplayName();
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "",
                ChatColor.GRAY + "Throw this explosive into the mine",
                ChatColor.GRAY + "to blast a " + tier.getColor() + tier.getRadius() + " block radius" + ChatColor.GRAY + "!",
                "",
                ChatColor.GRAY + "Radius: " + tier.getColor() + tier.getRadius() + " blocks",
                ChatColor.GRAY + "Fuse: " + ChatColor.YELLOW + "2.5 seconds",
                "",
                ChatColor.YELLOW + "Must be used in your Quarry",
                "",
                ChatColor.DARK_GRAY + "Right-click to throw"
        );
    }

    @Override
    public JobSiteType getRequiredJobSiteType() {
        return JobSiteType.QUARRY;
    }

    @Override
    public boolean onUseAtJobSite(Player player, JobSite jobSite, PlayerInteractEvent event) {
        // Launch the projectile
        MineBombProjectile.launch(player, jobSite, tier);

        ChatUtils.sendMessage(player,
                tier.getColor() + "💣 " + tier.getDisplayName() + ChatColor.YELLOW + " thrown!");

        return true; // Consume the item
    }

    public MineBombTier getTier() {
        return tier;
    }
}