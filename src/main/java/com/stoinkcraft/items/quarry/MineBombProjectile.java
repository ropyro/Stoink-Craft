package com.stoinkcraft.items.quarry;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.collections.CollectionManager;
import com.stoinkcraft.earning.contracts.ContractContext;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.JobSiteType;
import com.stoinkcraft.earning.jobsites.components.generators.MineGenerator;
import com.stoinkcraft.earning.jobsites.sites.quarry.QuarrySite;
import com.stoinkcraft.enterprise.Enterprise;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Handles the thrown mine bomb projectile and explosion logic.
 */
public class MineBombProjectile {

    private static final long FUSE_TICKS = 50L; // 2.5 seconds
    private static final double THROW_VELOCITY = 1.5;

    /**
     * Launches a mine bomb projectile from the player.
     */
    public static void launch(Player player, JobSite jobSite, MineBombTier tier) {
        World world = player.getWorld();
        Location spawnLoc = player.getEyeLocation();

        // Spawn invisible armor stand as projectile
        ArmorStand projectile = world.spawn(spawnLoc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(true);
            stand.setSmall(true);
            stand.setMarker(false);
            stand.setInvulnerable(true);
            stand.setBasePlate(false);
            stand.getEquipment().setHelmet(new ItemStack(tier.getMaterial()));

            // Make helmet visible
            stand.setVisible(false);
        });

        // Apply throw velocity in player's look direction
        Vector velocity = player.getLocation().getDirection().multiply(THROW_VELOCITY);
        projectile.setVelocity(velocity);

        // Track the projectile and handle landing + explosion
        new BukkitRunnable() {
            private int ticksAlive = 0;
            private boolean landed = false;
            private int ticksSinceLanded = 0;
            private Location landLocation = null;

            @Override
            public void run() {
                ticksAlive++;

                // Safety timeout - remove after 10 seconds regardless
                if (ticksAlive > 200) {
                    cleanup();
                    return;
                }

                // Check if projectile is dead/removed
                if (projectile.isDead() || !projectile.isValid()) {
                    cleanup();
                    return;
                }

                if (!landed) {
                    // Spawn particle trail while flying
                    world.spawnParticle(
                            Particle.SMOKE,
                            projectile.getLocation(),
                            3, 0.1, 0.1, 0.1, 0.01
                    );

                    // Check if landed (velocity near zero or on ground)
                    if (projectile.isOnGround() || projectile.getVelocity().lengthSquared() < 0.01) {
                        landed = true;
                        landLocation = projectile.getLocation();

                        // Play fuse sound
                        world.playSound(landLocation, Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f);
                    }
                } else {
                    ticksSinceLanded++;

                    // Fuse particles
                    if (ticksSinceLanded % 5 == 0) {
                        world.spawnParticle(
                                Particle.FLAME,
                                landLocation.clone().add(0, 0.5, 0),
                                5, 0.2, 0.2, 0.2, 0.02
                        );
                    }

                    // Explode after fuse time
                    if (ticksSinceLanded >= FUSE_TICKS) {
                        explode(player, jobSite, tier, landLocation);
                        cleanup();
                    }
                }
            }

            private void cleanup() {
                cancel();
                if (!projectile.isDead()) {
                    projectile.remove();
                }
            }
        }.runTaskTimer(StoinkCore.getInstance(), 0L, 1L);
    }

    /**
     * Handles the explosion - breaks blocks and processes rewards.
     */
    private static void explode(Player player, JobSite jobSite, MineBombTier tier, Location center) {
        World world = center.getWorld();
        int radius = tier.getRadius();

        // Get the mine region
        QuarrySite quarry = (QuarrySite) jobSite;
        MineGenerator mine = quarry.getMineGenerator();
        CuboidRegion mineRegion = mine.getCuboidRegion();

        // Explosion effects
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.8f);
        world.spawnParticle(
                Particle.EXPLOSION,
                center,
                5, 1.0, 1.0, 1.0, 0
        );
        world.spawnParticle(
                Particle.SMOKE,
                center,
                30, 1.5, 1.5, 1.5, 0.05
        );

        // Collect blocks to break
        List<Block> blocksToBreak = new ArrayList<>();
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Spherical radius check
                    if (x * x + y * y + z * z > radius * radius) {
                        continue;
                    }

                    int blockX = centerX + x;
                    int blockY = centerY + y;
                    int blockZ = centerZ + z;

                    // Check if within mine region
                    BlockVector3 pos = BlockVector3.at(blockX, blockY, blockZ);
                    if (!mineRegion.contains(pos)) {
                        continue;
                    }

                    Block block = world.getBlockAt(blockX, blockY, blockZ);

                    // Skip air
                    if (block.getType() == Material.AIR) {
                        continue;
                    }

                    blocksToBreak.add(block);
                }
            }
        }

        // Process each block as if the player broke it
        Enterprise enterprise = StoinkCore.getInstance().getEnterpriseManager()
                .getEnterpriseByMember(player.getUniqueId());

        if (enterprise == null) {
            // Just break blocks without rewards
            blocksToBreak.forEach(block -> block.setType(Material.AIR));
            return;
        }

        int totalBlocks = 0;
        int geodeXp = 0;

        for (Block block : blocksToBreak) {
            Material material = block.getType();

            // Geode XP check
            if (material == Material.AMETHYST_BLOCK || material == Material.AMETHYST_CLUSTER) {
                int xpReward = QuarrySite.GEODE_XP_REWARD;
                if (material == Material.AMETHYST_CLUSTER) {
                    xpReward *= 2;
                }
                geodeXp += xpReward;
            }

            // Get drops for contract progress
            ItemStack tool = new ItemStack(Material.DIAMOND_PICKAXE);
            Collection<ItemStack> drops = block.getDrops(tool);

            int amount = drops.stream()
                    .mapToInt(ItemStack::getAmount)
                    .sum();

            if (amount > 0) {
                // Contract progress
                ContractContext context = new ContractContext(
                        player,
                        JobSiteType.QUARRY,
                        material,
                        amount
                );

                StoinkCore.getInstance().getContractManager().handleContext(enterprise, context);

                // Collection progress
                CollectionManager.handleBlockCollection(
                        enterprise,
                        jobSite,
                        material,
                        amount,
                        player
                );
            }

            // Break the block (no drops - already processed)
            block.setType(Material.AIR);
            totalBlocks++;
        }

        // Apply geode XP
        if (geodeXp > 0) {
            quarry.getData().incrementXp(geodeXp);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "+" + geodeXp + " Quarry XP " +
                    ChatColor.DARK_PURPLE + "⬢ Geodes!");
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 1.0f, 1.2f);
        }

        // Feedback
        player.sendMessage(
                tier.getColor() + "💥 " + tier.getDisplayName() + ChatColor.GREEN +
                        " destroyed " + ChatColor.WHITE + totalBlocks + ChatColor.GREEN + " blocks!"
        );
    }
}