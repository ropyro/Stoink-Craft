package com.stoinkcraft.earning.jobsites.protection;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.components.JobSiteComponent;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
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

import java.util.*;

public class ProtectionManager implements Listener {

    private static final String BYPASS_PERMISSION = "stoinkcore.protection.bypass";

    private final StoinkCore plugin;

    /**
     * Chunk-based index for fast lookups.
     * Key format: "worldName:chunkX:chunkZ"
     */
    private final Map<String, Set<JobSite>> chunkIndex = new HashMap<>();

    public ProtectionManager(StoinkCore plugin) {
        this.plugin = plugin;
    }

    // =========================================================================
    // INDEXING
    // =========================================================================

    /**
     * Call this after all enterprises/jobsites are loaded on server start.
     */
    public void rebuildIndex() {
        chunkIndex.clear();

        Collection<Enterprise> enterprises = plugin.getEnterpriseManager().getEnterpriseList();
        plugin.getLogger().info("[Protection] Found " + enterprises.size() + " enterprises");

        for (Enterprise enterprise : enterprises) {
            Collection<JobSite> jobSites = enterprise.getJobSiteManager().getAllJobSites();
            //plugin.getLogger().info("[Protection] Enterprise " + enterprise.getName() + " has " + jobSites.size() + " jobsites");

            for (JobSite jobSite : jobSites) {
               // plugin.getLogger().info("[Protection] Indexing jobsite: " + jobSite.getType() + " at " + jobSite.getSpawnPoint());

                var region = jobSite.getRegion();
                if (region == null) {
                  //  plugin.getLogger().warning("[Protection] JobSite " + jobSite.getType() + " has NULL region!");
                    continue;
                }

                //plugin.getLogger().info("[Protection] Region bounds: " + region.getMinimumPoint() + " to " + region.getMaximumPoint());

                indexJobSite(jobSite);
            }
        }

        plugin.getLogger().info("[Protection] Indexed " + chunkIndex.size() + " chunks for protection.");


    }

    /**
     * Add a single jobsite to the index. Call when a new jobsite is built.
     */
    public void indexJobSite(@NotNull JobSite jobSite) {
        Set<String> chunks = getChunksForJobSite(jobSite);
        for (String chunkKey : chunks) {
            chunkIndex.computeIfAbsent(chunkKey, k -> new HashSet<>()).add(jobSite);
        }
    }

    /**
     * Remove a jobsite from the index. Call when a jobsite is disbanded.
     */
    public void unindexJobSite(@NotNull JobSite jobSite) {
        Set<String> chunks = getChunksForJobSite(jobSite);
        for (String chunkKey : chunks) {
            Set<JobSite> sites = chunkIndex.get(chunkKey);
            if (sites != null) {
                sites.remove(jobSite);
                if (sites.isEmpty()) {
                    chunkIndex.remove(chunkKey);
                }
            }
        }
    }

    private Set<String> getChunksForJobSite(JobSite jobSite) {
        Set<String> chunks = new HashSet<>();

        // Get the jobsite's region bounds
        var region = jobSite.getRegion();
        if (region == null) return chunks;

        var min = region.getMinimumPoint();
        var max = region.getMaximumPoint();
        String worldName = jobSite.getSpawnPoint().getWorld().getName();

        // Convert block coords to chunk coords
        int minChunkX = min.x() >> 4;
        int maxChunkX = max.x() >> 4;
        int minChunkZ = min.z() >> 4;
        int maxChunkZ = max.z() >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                chunks.add(chunkKey(worldName, cx, cz));
            }
        }

        return chunks;
    }

    private String chunkKey(String worldName, int chunkX, int chunkZ) {
        return worldName + ":" + chunkX + ":" + chunkZ;
    }

    private String chunkKey(Location location) {
        return chunkKey(
                location.getWorld().getName(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4
        );
    }

    // =========================================================================
    // CORE PROTECTION CHECK
    // =========================================================================

    /**
     * Main protection check. Returns ALLOW if action should proceed, DENY if blocked.
     */
    public ProtectionResult checkProtection(@NotNull ProtectionQuery query, boolean sendDenyMessage) {
//        plugin.getLogger().info("[Protection] Checking: " + query.player().getName() +
//                " | Action: " + query.action() +
//                " | Location: " + query.location().getBlockX() + "," + query.location().getBlockY() + "," + query.location().getBlockZ());

        // Bypass permission check
        if (query.player().hasPermission(BYPASS_PERMISSION)) {
       //     plugin.getLogger().info("[Protection] Result: ALLOW (bypass permission)");
            return ProtectionResult.ALLOW;
        }

        // Fast chunk lookup
        String chunkKey = chunkKey(query.location());
        Set<JobSite> jobSites = chunkIndex.get(chunkKey);

        if (jobSites == null || jobSites.isEmpty()) {
         //   plugin.getLogger().info("[Protection] Result: ALLOW (no jobsites in chunk " + chunkKey + ")");
            return ProtectionResult.ALLOW;
        }

        // Find which jobsite contains this location
        JobSite containingJobSite = null;
        for (JobSite site : jobSites) {
            if (site.contains(query.location())) {
                containingJobSite = site;
                break;
            }
        }

        if (containingJobSite == null) {
         //   plugin.getLogger().info("[Protection] Result: ALLOW (not inside any jobsite bounds)");
            return ProtectionResult.ALLOW;
        }

      //  plugin.getLogger().info("[Protection] Found JobSite: " + containingJobSite.getType());

        // Check enterprise membership
        Enterprise enterprise = containingJobSite.getEnterprise();
        boolean isMember = enterprise.isMember(query.player().getUniqueId());

       // plugin.getLogger().info("[Protection] Enterprise: " + enterprise.getName() + " | isMember: " + isMember);

        if (!isMember) {
            if (sendDenyMessage) {
                ChatUtils.sendMessage(
                        query.player(),
                        ChatColor.RED + "You don't have permission to do that here!"
                );
            }
           // plugin.getLogger().info("[Protection] Result: DENY (not a member)");
            return ProtectionResult.DENY;
        }

        // Player is a member - check component zones
        for (JobSiteComponent component : containingJobSite.getComponents()) {
            if (component instanceof ProtectedZone zone) {
                ProtectionResult result = zone.checkProtection(query);
               // plugin.getLogger().info("[Protection] Zone " + component.getClass().getSimpleName() + " returned: " + result);
                if (result != ProtectionResult.ABSTAIN) {
                   // plugin.getLogger().info("[Protection] Result: " + result);
                    return result;
                }
            }
        }

        // All zones abstained - apply default rules
        ProtectionResult defaultResult = applyDefaultRules(query);
        //plugin.getLogger().info("[Protection] Result: " + defaultResult + " (default rules)");
        return defaultResult;
    }

    /**
     * Default rules when no component claims the location.
     */
    private ProtectionResult applyDefaultRules(ProtectionQuery query) {
        // Allow interacting with doors, gates, buttons, etc.
        if (query.action() == ProtectionAction.RIGHT_CLICK) {
            Block block = query.location().getBlock();
            Material type = block.getType();

            if (isInteractableBlock(type)) {
                return ProtectionResult.ALLOW;
            }
        }

        if(query.action() == ProtectionAction.KILL_ENTITY) {
            return ProtectionResult.ALLOW;
        }

        // Deny everything else by default
        return ProtectionResult.DENY;
    }

    private boolean isInteractableBlock(Material type) {
        // Doors
        if (Tag.DOORS.isTagged(type)) return true;
        // Fence gates
        if (Tag.FENCE_GATES.isTagged(type)) return true;
        // Trapdoors
        if (Tag.TRAPDOORS.isTagged(type)) return true;
        // Buttons
        if (Tag.BUTTONS.isTagged(type)) return true;
        // Levers
        if (type == Material.LEVER) return true;

        return false;
    }

    // =========================================================================
    // EVENT LISTENERS
    // =========================================================================

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        ProtectionQuery query = ProtectionQuery.blockAction(
                event.getPlayer(),
                event.getBlock().getLocation(),
                ProtectionAction.BREAK,
                event.getPlayer().getInventory().getItemInMainHand()
        );

        ProtectionResult result = checkProtection(query, true);
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

        ProtectionResult result = checkProtection(query, true);
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

        ProtectionResult result = checkProtection(query, true);
        if (result == ProtectionResult.DENY) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        Entity target = event.getEntity();

        ProtectionQuery query = ProtectionQuery.entityAction(
                player,
                target,
                ProtectionAction.KILL_ENTITY,
                player.getInventory().getItemInMainHand()
        );

        ProtectionResult result = checkProtection(query, true);
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
            String chunkKey = chunkKey(block.getLocation());
            Set<JobSite> jobSites = chunkIndex.get(chunkKey);

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
        String chunkKey = chunkKey(location);
        Set<JobSite> jobSites = chunkIndex.get(chunkKey);

        if (jobSites == null) return null;

        for (JobSite site : jobSites) {
            if (site.contains(location)) {
                return site;
            }
        }

        return null;
    }
}