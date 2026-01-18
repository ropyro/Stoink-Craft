package com.stoinkcraft.earning.listeners;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.earning.collections.CollectionManager;
import com.stoinkcraft.earning.contracts.ContractContext;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.earning.jobsites.sites.quarry.QuarrySite;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class BlockBreakListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        StoinkCore core = StoinkCore.getInstance();

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        // =========================
        // ENTERPRISE CHECK
        // =========================
        Enterprise enterprise = core.getEnterpriseManager()
                .getEnterpriseByMember(player.getUniqueId());
        if (enterprise == null) return;

        // =========================
        // JOBSITE RESOLUTION
        // =========================
        JobSiteType jobSiteType = enterprise.getJobSiteManager()
                .resolveJobsite(block.getLocation());
        if (jobSiteType == null) return;

        // =========================
        // GEODE XP CHECK (Quarry)
        // =========================
        if (jobSiteType == JobSiteType.QUARRY) {
            if (material == Material.AMETHYST_BLOCK || material == Material.AMETHYST_CLUSTER) {
                QuarrySite quarry = enterprise.getJobSiteManager().getQuarrySite();
                int xpReward = QuarrySite.GEODE_XP_REWARD;

                // Bonus XP for clusters
                if (material == Material.AMETHYST_CLUSTER) {
                    xpReward *= 2;
                }

                quarry.getData().incrementXp(xpReward);

                // Visual feedback
                player.sendMessage(ChatColor.LIGHT_PURPLE + "+" + xpReward + " Quarry XP " +
                        ChatColor.DARK_PURPLE + "⬢ Geode!");

                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 1.2f);
            }
        }

        // =========================
        // CROP MATURITY CHECK
        // =========================
        boolean isCrop =
                material == Material.WHEAT ||
                        material == Material.CARROTS ||
                        material == Material.POTATOES ||
                        material == Material.BEETROOTS;

        if (isCrop) {
            BlockData data = block.getBlockData();
            if (data instanceof Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // =========================
        // DROP-BASED PROGRESS
        // =========================
        ItemStack tool = player.getInventory().getItemInMainHand();
        Collection<ItemStack> drops = block.getDrops(tool);

        int amount = drops.stream()
                .mapToInt(ItemStack::getAmount)
                .sum();

        if (amount > 0) {
            ContractContext context = new ContractContext(
                    player,
                    jobSiteType,
                    material,
                    amount
            );

            core.getContractManager().handleContext(enterprise, context);
            event.setDropItems(false);
        }

        // =========================
        // COLLECTION PROGRESS
        // =========================
        JobSite jobSite = enterprise.getJobSiteManager().getJobSite(jobSiteType);
        if (jobSite != null && amount > 0) {
            CollectionManager.handleBlockCollection(
                    enterprise,
                    jobSite,
                    material,
                    amount,
                    player
            );
        }

        // =========================
        // FARMLAND CROP REGEN
        // =========================
        if (isCrop &&
                player.getWorld().equals(core.getEnterpriseWorldManager().getWorld())) {

            Bukkit.getScheduler().runTaskLater(
                    core,
                    () -> enterprise.getJobSiteManager()
                            .getFarmlandSite()
                            .getGreenhouses()
                            .values()
                            .forEach(greenhouse -> greenhouse.replaceMissingCrops()),
                    1L
            );
        }
    }
}
