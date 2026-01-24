package com.stoinkcraft.items.farmland;

import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobsites.sites.JobSite;
import com.stoinkcraft.jobsites.sites.JobSiteType;
import com.stoinkcraft.jobsites.components.generators.GreenhouseGenerator;
import com.stoinkcraft.jobsites.sites.farmland.FarmlandSite;
import com.stoinkcraft.items.JobSiteItem;
import com.stoinkcraft.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;

/**
 * A consumable item that instantly grows all crops in the greenhouse
 * the player is currently standing in.
 */
public class FertilizerBomb extends JobSiteItem {

    private static final String ITEM_ID = "fertilizer_bomb";

    @Override
    public String getItemId() {
        return ITEM_ID;
    }

    @Override
    public Material getMaterial() {
        return Material.SLIME_BALL;
    }

    @Override
    public String getDisplayName() {
        return ChatColor.GREEN + "" + ChatColor.BOLD + "Fertilizer Bomb";
    }

    @Override
    public List<String> getLore() {
        return List.of(
                "",
                ChatColor.GRAY + "Instantly grows all crops in",
                ChatColor.GRAY + "the greenhouse you're standing in!",
                "",
                ChatColor.YELLOW + "Must be used inside a greenhouse",
                "",
                ChatColor.DARK_GRAY + "Right-click to use"
        );
    }

    @Override
    public JobSiteType getRequiredJobSiteType() {
        return JobSiteType.FARMLAND;
    }

    @Override
    public boolean canUse(Player player, PlayerInteractEvent event) {
        // First check the standard JobSiteItem validation (in farmland, is member)
        if (!super.canUse(player, event)) {
            return false;
        }

        // Check if player is inside a greenhouse
        JobSite jobSite = getPlayerJobSite(player);
        if (jobSite == null) {
            return false;
        }

        FarmlandSite farmland = (FarmlandSite) jobSite;
        GreenhouseGenerator greenhouse = getGreenhouseAtLocation(farmland, event.getClickedBlock().getLocation());

        if (greenhouse == null) {
            ChatUtils.sendMessage(player, ChatColor.RED + "You must use this on crops in a greenhouse!");
            return false;
        }

        if (!greenhouse.isUnlocked()) {
            ChatUtils.sendMessage(player, ChatColor.RED + "This greenhouse is not unlocked yet!");
            return false;
        }

        return true;
    }

    @Override
    public boolean onUseAtJobSite(Player player, JobSite jobSite, PlayerInteractEvent event) {
        FarmlandSite farmland = (FarmlandSite) jobSite;
        GreenhouseGenerator greenhouse = getGreenhouseAtLocation(farmland, event.getClickedBlock().getLocation());

        if (greenhouse == null) {
            return false;
        }

        // Run the growth async using FAWE
        growAllCrops(greenhouse, player);

        return true; // Consume the item
    }

    /**
     * Finds which greenhouse the player is currently standing in.
     */
    private GreenhouseGenerator getGreenhouseAtLocation(FarmlandSite farmland, Location location) {
        BlockVector3 playerPos = BlockVector3.at(
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ()
        );

        for (Map.Entry<Integer, GreenhouseGenerator> entry : farmland.getGreenhouses().entrySet()) {
            GreenhouseGenerator greenhouse = entry.getValue();
            CuboidRegion region = greenhouse.getCuboidRegion();

            // Expand region vertically to catch player standing on crops
            CuboidRegion expandedRegion = new CuboidRegion(
                    region.getMinimumPoint().subtract(0, 1, 0),
                    region.getMaximumPoint().add(0, 2, 0)
            );

            if (expandedRegion.contains(playerPos)) {
                return greenhouse;
            }
        }

        return null;
    }

    /**
     * Instantly grows all crops in the greenhouse to max age.
     */
    private void growAllCrops(GreenhouseGenerator greenhouse, Player player) {
        CuboidRegion region = greenhouse.getCuboidRegion();
        String worldName = player.getWorld().getName();

        Bukkit.getScheduler().runTaskAsynchronously(StoinkCore.getInstance(), () -> {
            com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(worldName);
            int grownCount = 0;

            try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
                for (BlockVector3 pos : region) {
                    BlockState state = session.getBlock(pos);
                    BlockType type = state.getBlockType();

                    if (!isCropBlock(type)) {
                        continue;
                    }

                    Integer currentAge = state.getState(PropertyKey.AGE);
                    int maxAge = getMaxAge(type);

                    if (currentAge == null || currentAge >= maxAge) {
                        continue;
                    }

                    BlockState fullyGrown = state.with(PropertyKey.AGE, maxAge);
                    session.setBlock(pos, fullyGrown);
                    grownCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () -> {
                    ChatUtils.sendMessage(player, ChatColor.RED + "Something went wrong while growing crops!");
                });
                return;
            }

            // Send success message on main thread
            int finalCount = grownCount;
            Bukkit.getScheduler().runTask(StoinkCore.getInstance(), () -> {
                if (finalCount > 0) {
                    ChatUtils.sendMessage(player,
                            ChatColor.GREEN + "Fertilizer Bomb activated! " +
                                    ChatColor.YELLOW + finalCount + ChatColor.GREEN + " crops fully grown!");
                } else {
                    ChatUtils.sendMessage(player,
                            ChatColor.YELLOW + "All crops in this greenhouse were already fully grown!");
                }
            });
        });
    }

    private boolean isCropBlock(BlockType type) {
        return type == BlockTypes.WHEAT ||
                type == BlockTypes.CARROTS ||
                type == BlockTypes.POTATOES ||
                type == BlockTypes.BEETROOTS;
    }

    private int getMaxAge(BlockType type) {
        if (type == BlockTypes.BEETROOTS) {
            return 3; // Beetroot has max age 3
        }
        return 7; // Wheat, carrots, potatoes have max age 7
    }
}