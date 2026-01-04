package com.stoinkcraft.jobs.jobsites.components.generators;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.components.JobSiteGenerator;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.RegionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MineGenerator extends JobSiteGenerator {

    private final Location corner1;
    private final Location corner2;
    private final World bukkitWorld;
    private int regenIntervalSeconds;
    private CuboidRegion cuboidRegion;
    private String regionName;

    public MineGenerator(Location corner1, Location corner2, JobSite parent, int regenIntervalSeconds, String regionName) {
        super(parent);
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.bukkitWorld = corner1.getWorld();
        this.regenIntervalSeconds = regenIntervalSeconds;
        setRegion(corner1, corner2);
        this.regionName = regionName;
    }

    @Override
    public void tick() {
        if (getTickCounter() % regenIntervalSeconds == 0) {
            regenerateMine();
        }
    }

    @Override
    public void build() {
        Map<StateFlag, StateFlag.State> flags = new HashMap<>();
        flags.put(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
        flags.put(Flags.INTERACT, StateFlag.State.ALLOW);
        flags.put(Flags.USE, StateFlag.State.ALLOW);
        flags.put(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        flags.put(Flags.MOB_SPAWNING, StateFlag.State.DENY);
        flags.put(Flags.MOB_DAMAGE, StateFlag.State.DENY);
        RegionUtils.createProtectedRegion(
                getParent().getSpawnPoint().getWorld(),
                getCuboidRegion(),
                regionName,
                flags,
                10);

        regenerateMine();
    }

    public long remainingTicks() {
        return getTickCounter() % regenIntervalSeconds;
    }

    public void setRegion(Location corner1, Location corner2) {
        BlockVector3 min = BlockVector3.at(
                Math.min(corner1.getBlockX(), corner2.getBlockX()),
                Math.min(corner1.getBlockY(), corner2.getBlockY()),
                Math.min(corner1.getBlockZ(), corner2.getBlockZ())
        );
        BlockVector3 max = BlockVector3.at(
                Math.max(corner1.getBlockX(), corner2.getBlockX()),
                Math.max(corner1.getBlockY(), corner2.getBlockY()),
                Math.max(corner1.getBlockZ(), corner2.getBlockZ())
        );
        cuboidRegion = new CuboidRegion(min, max);
    }

    public CuboidRegion getCuboidRegion() {
        return cuboidRegion;
    }

    public int getRegenInterval() {
        return regenIntervalSeconds;
    }

    private void teleportPlayersOutOfMine() {
        if (getParent() == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();

            if (getParent().contains(loc)) {
                getParent().teleportPlayer(player, true);
                ChatUtils.sendMessage(player, ChatColor.YELLOW + "⛏ The quarry is regenerating, you've been moved to safety!");
            }
        }
    }

    public void regenerateMine() {
        teleportPlayersOutOfMine();

        com.sk89q.worldedit.world.World weWorld = FaweAPI.getWorld(bukkitWorld.getName());

        try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
            RandomPattern pattern = new RandomPattern();
            pattern.add(BlockTypes.COBBLESTONE.getDefaultState(), 80);
            pattern.add(BlockTypes.COAL_ORE.getDefaultState(), 10);
            pattern.add(BlockTypes.IRON_ORE.getDefaultState(), 7);
            pattern.add(BlockTypes.DIAMOND_ORE.getDefaultState(), 3);

            session.setBlocks((Region) cuboidRegion, pattern);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}