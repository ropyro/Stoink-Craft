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
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

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

            for (JobSite jobSite : jobSites) {

                var region = jobSite.getRegion();
                if (region == null) {
                    continue;
                }

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

    public String chunkKey(Location location) {
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
        // Bypass permission check
        if (query.player().hasPermission(BYPASS_PERMISSION)) {
            return ProtectionResult.ALLOW;
        }

        // Fast chunk lookup
        String chunkKey = chunkKey(query.location());
        Set<JobSite> jobSites = chunkIndex.get(chunkKey);

        if (jobSites == null || jobSites.isEmpty()) {
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
            return ProtectionResult.ALLOW;
        }

        // Check enterprise membership
        Enterprise enterprise = containingJobSite.getEnterprise();
        boolean isMember = enterprise.isMember(query.player().getUniqueId());

        if (!isMember) {
            if (sendDenyMessage) {
                ChatUtils.sendMessage(
                        query.player(),
                        ChatColor.RED + "You don't have permission to do that here!"
                );
            }
            return ProtectionResult.DENY;
        }

        // Player is a member - check component zones
        for (JobSiteComponent component : containingJobSite.getComponents()) {
            if (component instanceof ProtectedZone zone) {
                ProtectionResult result = zone.checkProtection(query);
                if (result != ProtectionResult.ABSTAIN) {
                    return result;
                }
            }
        }

        // All zones abstained - apply default rules
        ProtectionResult defaultResult = applyDefaultRules(query);
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
            }else if(query.tool().getType().isEdible()){
                return ProtectionResult.ALLOW;
            }
        }

        if(query.action() == ProtectionAction.PVE) {
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

    public Set<JobSite> getSitesFromKey(String chunkKey){
        return chunkIndex.get(chunkKey);
    }

}