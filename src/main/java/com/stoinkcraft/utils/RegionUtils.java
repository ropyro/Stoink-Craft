package com.stoinkcraft.utils;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Location;

public class RegionUtils {

    public static BlockVector3 toBlockVector3(Location loc) {
        return BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
