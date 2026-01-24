package com.stoinkcraft.jobsites.protection;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.components.JobSiteComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class ProtectionListeners implements Listener {

    private StoinkCore plugin;
    private ProtectionManager protectionManager;

    public ProtectionListeners(StoinkCore plugin){
        this.plugin = plugin;
        this.protectionManager = plugin.getProtectionManager();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        ProtectionQuery query = ProtectionQuery.blockAction(
                event.getPlayer(),
                event.getBlock().getLocation(),
                ProtectionAction.BREAK,
                event.getPlayer().getInventory().getItemInMainHand()
        );

        ProtectionResult result = protectionManager.checkProtection(query, true);
        if (result == ProtectionResult.DENY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ProtectionQuery query = ProtectionQuery.blockAction(
                event.getPlayer(),
                event.getBlock().getLocation(),
                ProtectionAction.PLACE,
                event.getItemInHand()
        );

        ProtectionResult result = protectionManager.checkProtection(query, true);
        if (result == ProtectionResult.DENY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-clicks on blocks
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        ItemStack tool = event.getItem();

        // Determine action type
        ProtectionAction action;
        if (tool != null && tool.getType() == Material.SHEARS) {
            Material blockType = block.getType();
            if (blockType == Material.BEEHIVE || blockType == Material.BEE_NEST) {
                action = ProtectionAction.SHEAR;
            } else {
                action = ProtectionAction.RIGHT_CLICK;
            }
        } else {
            action = ProtectionAction.RIGHT_CLICK;
        }

        ProtectionQuery query = ProtectionQuery.blockAction(
                event.getPlayer(),
                block.getLocation(),
                action,
                tool
        );

        ProtectionResult result = protectionManager.checkProtection(query, true);
        if (result == ProtectionResult.DENY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        Entity target = event.getEntity();


        ProtectionQuery query;
        //disable pvp
        if(target instanceof Player){
            query = ProtectionQuery.entityAction(
                    player,
                    target,
                    ProtectionAction.PVP,
                    player.getInventory().getItemInMainHand()
            );
        }else{
            query = ProtectionQuery.entityAction(
                    player,
                    target,
                    ProtectionAction.PVE,
                    player.getInventory().getItemInMainHand()
            );
        }

        ProtectionResult result = protectionManager.checkProtection(query, true);
        if (result == ProtectionResult.DENY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onMoistureChange(MoistureChangeEvent event) {
        Block block = event.getBlock();

        JobSite jobSite = getJobSiteAt(block.getLocation());
        if (jobSite != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCropTrample(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.FARMLAND) return;

        JobSite jobSite = getJobSiteAt(block.getLocation());
        if (jobSite != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        processExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        processExplosion(event.blockList());
    }

    /**
     * Remove protected blocks from explosion block list.
     */
    private void processExplosion(List<Block> blocks) {
        blocks.removeIf(block -> {
            String chunkKey = protectionManager.chunkKey(block.getLocation());
            Set<JobSite> jobSites = protectionManager.getSitesFromKey(chunkKey);

            if (jobSites == null || jobSites.isEmpty()) {
                return false; // Not protected, allow destruction
            }

            for (JobSite site : jobSites) {
                if (site.contains(block.getLocation())) {
                    // Block is in a jobsite - check if any zone allows explosions
                    for (JobSiteComponent component : site.getComponents()) {
                        if (component instanceof ProtectedZone zone) {
                            // Create a dummy query for explosion check
                            // Note: No player for explosions, so we use a special check
                            ProtectionQuery query = new ProtectionQuery(
                                    null, // No player
                                    block.getLocation(),
                                    ProtectionAction.EXPLOSION,
                                    null,
                                    null
                            );
                            ProtectionResult result = zone.checkProtection(query);
                            if (result == ProtectionResult.ALLOW) {
                                return false; // Allow this block to be destroyed
                            }
                        }
                    }
                    // No zone explicitly allowed - protect the block
                    return true;
                }
            }

            return false; // Not in any jobsite bounds
        });
    }

    // =========================================================================
    // UTILITY
    // =========================================================================

    /**
     * Get the jobsite at a location, if any.
     */
    @Nullable
    public JobSite getJobSiteAt(@NotNull Location location) {
        String chunkKey = protectionManager.chunkKey(location);
        Set<JobSite> jobSites = protectionManager.getSitesFromKey(chunkKey);

        if (jobSites == null) return null;

        for (JobSite site : jobSites) {
            if (site.contains(location)) {
                return site;
            }
        }

        return null;
    }
}
