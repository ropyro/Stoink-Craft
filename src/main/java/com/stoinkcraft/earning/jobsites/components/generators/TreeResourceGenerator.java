package com.stoinkcraft.earning.jobsites.components.generators;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.earning.jobsites.JobSite;
import com.stoinkcraft.earning.jobsites.components.JobSiteGenerator;
import com.stoinkcraft.utils.SchematicUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.io.File;

public class TreeResourceGenerator extends JobSiteGenerator {

    private final Location baseLocation;   // where the tree is planted
    private long regenDelayTicks;           // how long to wait before replanting
    private long regenTimer = -1;           // countdown until next regrow

    public TreeResourceGenerator(JobSite parent, Location baseLocation, long regenDelayTicks) {
        super(parent);
        this.baseLocation = baseLocation;
        this.regenDelayTicks = regenDelayTicks;
    }

    @Override
    public void tick() {
        Block base = baseLocation.getBlock();

        // If tree stump is gone and regen timer not started
        if (!isTreePresent() && regenTimer == -1) {
            regenTimer = regenDelayTicks;
        }

        // Countdown regen timer
        if (regenTimer > 0) {
            regenTimer--;
        } else if (regenTimer == 0) {
            spawnTree();
            regenTimer = -1;
        }
    }

    @Override
    public void build() {

    }

    private boolean isTreePresent() {
        Material type = baseLocation.getBlock().getType();
        return switch (type) {
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG -> true;
            default -> false;
        };
    }

    private void spawnTree() {
        Bukkit.getScheduler().runTaskAsynchronously(
                Bukkit.getPluginManager().getPlugin("StoinkCore"),
                () -> {
                    SchematicUtils.pasteSchematic(new File(StoinkCore.getInstance().getDataFolder(), "/schematics/oak_tree"), baseLocation, true);
                }
        );
    }
}
