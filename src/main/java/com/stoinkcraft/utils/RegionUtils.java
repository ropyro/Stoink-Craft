package com.stoinkcraft.utils;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Map;
import java.util.UUID;

public class RegionUtils {

    public static BlockVector3 toBlockVector3(Location loc) {
        return BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static void createProtectedRegion(World world, Region region, String id) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(FaweAPI.getWorld(world.getName()));

        if (manager == null) {
            Bukkit.getLogger().info("RegionManager is null for world " + world.getName());
            return;
        }

            // Use the region’s min and max points from the schematic
        ProtectedRegion protectedRegion = new ProtectedCuboidRegion(
                id,
                region.getMinimumPoint(),
                region.getMaximumPoint()
        );

        // Set flags
        protectedRegion.setFlag(Flags.BUILD, StateFlag.State.DENY);
        protectedRegion.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        protectedRegion.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        protectedRegion.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
        protectedRegion.setFlag(Flags.MOB_DAMAGE, StateFlag.State.DENY);
        protectedRegion.setFlag(Flags.USE, StateFlag.State.ALLOW);
        protectedRegion.setFlag(Flags.INTERACT, StateFlag.State.ALLOW);

        // Add to manager
        if(manager.getRegion(id) != null) manager.removeRegion(id);
        manager.addRegion(protectedRegion);

        Bukkit.getLogger().info("Created protected region: " + id + " for world " + world.getName());
    }

    public static void createProtectedRegion(World world, Region region, String id, Map<StateFlag, StateFlag.State> flags) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(FaweAPI.getWorld(world.getName()));

        if (manager == null) {
            Bukkit.getLogger().info("RegionManager is null for world " + world.getName());
            return;
        }

        // Use the region’s min and max points from the schematic
        ProtectedRegion protectedRegion = new ProtectedCuboidRegion(
                id,
                region.getMinimumPoint(),
                region.getMaximumPoint()
        );

        // Set flags
        flags.keySet().stream().forEach(flag -> {
            protectedRegion.setFlag(flag, flags.get(flag));
        });

        // Add to manager
        //manager.getRegion(id) != null
        manager.addRegion(protectedRegion);

        Bukkit.getLogger().info("Created protected region: " + id + " for world " + world.getName());
    }
}
