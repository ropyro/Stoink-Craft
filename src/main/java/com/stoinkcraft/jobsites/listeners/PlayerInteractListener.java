package com.stoinkcraft.jobsites.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobsites.collections.CollectionManager;
import com.stoinkcraft.jobsites.collections.CollectionType;
import com.stoinkcraft.jobsites.contracts.ContractContext;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.components.generators.HoneyGenerator;
import com.stoinkcraft.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Material type = block.getType();

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();


        StoinkCore core = StoinkCore.getInstance();

    /* =========================
       ENTERPRISE CHECK
       ========================= */

        Enterprise enterprise = core.getEnterpriseManager()
                .getEnterpriseByMember(player.getUniqueId());
        if (enterprise == null) return;

    /* =========================
       JOBSITE CHECK
       ========================= */

        JobSiteType jobSiteType =
                enterprise.getJobSiteManager()
                        .resolveJobsite(block.getLocation());

        if(jobSiteType == null) return;

        if(jobSiteType.equals(JobSiteType.QUARRY)){
            if(type.equals(Material.BEACON)){
                event.setCancelled(true);
                ChatUtils.sendMessage(player, ChatColor.RED + "Talk to Miner Bob to manage your power cell!");
                return;
            }
        }

        if (jobSiteType != JobSiteType.FARMLAND) return;

        // Require shears for honeycomb harvesting
        if (item.getType() != Material.SHEARS) return;
        if (type != Material.BEEHIVE && type != Material.BEE_NEST) return;

        FarmlandSite farmland =
                enterprise.getJobSiteManager().getFarmlandSite();

    /* =========================
       GENERATOR RESOLUTION
       ========================= */

        HoneyGenerator generator = farmland.getHoneyGenerators().stream()
                .filter(g -> g.getHiveLocation().getBlock().equals(block))
                .findFirst()
                .orElse(null);

        if (generator == null) return;

        if (!generator.canHarvest()) {
            event.setCancelled(true);
            ChatUtils.sendMessage(
                    player,
                    ChatColor.YELLOW + "🐝 This hive is empty!"
            );
            return;
        }

    /* =========================
       HARVEST
       ========================= */
        event.setCancelled(true);
        generator.consumeHoney();

        // =========================
        // COLLECTION PROGRESS
        // =========================
        CollectionManager.handleDirectCollection(
                enterprise,
                farmland,
                CollectionType.HONEYCOMB,
                1,
                player
        );

    /* =========================
       CONTRACT INTEGRATION
       ========================= */

        ContractContext context = new ContractContext(
                player,
                JobSiteType.FARMLAND,
                Material.HONEYCOMB,  // Fixed: was HONEY_BOTTLE
                1
        );

        core.getContractManager().handleContext(enterprise, context);
    }
}
