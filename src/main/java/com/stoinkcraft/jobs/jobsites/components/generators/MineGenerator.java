package com.stoinkcraft.jobs.jobsites.components.generators;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.components.JobSiteGenerator;
import com.stoinkcraft.jobs.jobsites.sites.quarry.OreSet;
import com.stoinkcraft.jobs.jobsites.sites.quarry.QuarryData;
import com.stoinkcraft.jobs.jobsites.sites.quarry.QuarrySite;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.RegionUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MineGenerator extends JobSiteGenerator {

    private final Location corner1;
    private final Location corner2;
    private final World bukkitWorld;

    private final CuboidRegion cuboidRegion;
    private final String regionName;

    // Geode settings
    private static final double GEODE_SPAWN_CHANCE = 0.03; // 3% of blocks
    private static final BlockType GEODE_BLOCK = BlockTypes.AMETHYST_BLOCK;
    private static final BlockType GEODE_CLUSTER = BlockTypes.AMETHYST_CLUSTER;

    public MineGenerator(
            Location corner1,
            Location corner2,
            JobSite parent,
            String regionName
    ) {
        super(parent);

        this.corner1 = corner1.clone();
        this.corner2 = corner2.clone();
        this.bukkitWorld = corner1.getWorld();
        this.regionName = regionName;

        this.cuboidRegion = createRegion(this.corner1, this.corner2);
    }

    /* =========================
       LIFECYCLE
       ========================= */

    @Override
    public void build() {
        super.build();

        Map<StateFlag, StateFlag.State> flags = new HashMap<>();
        flags.put(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
        flags.put(Flags.INTERACT, StateFlag.State.ALLOW);
        flags.put(Flags.USE, StateFlag.State.ALLOW);
        flags.put(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        flags.put(Flags.MOB_SPAWNING, StateFlag.State.DENY);
        flags.put(Flags.MOB_DAMAGE, StateFlag.State.DENY);

        RegionUtils.createProtectedRegion(
                getParent().getSpawnPoint().getWorld(),
                cuboidRegion,
                regionName,
                flags,
                10
        );

        // First regen happens immediately on build
        regenerateMine();
    }

    @Override
    public void tick() {
        super.tick();

        QuarryData data = getQuarryData();
        data.incrementElapsedSeconds();

        if (data.getElapsedSeconds() >= getRegenIntervalSeconds()) {
            regenerateMine();
        }

        updateHologram();
    }

    @Override
    public void disband() {
        super.disband();
        RegionUtils.removeProtectedRegion(
                getParent().getSpawnPoint().getWorld(),
                regionName
        );
    }

    /* =========================
       REGEN LOGIC
       ========================= */

    public void regenerateMine() {
        QuarryData data = getQuarryData();
        data.setElapsedSeconds(0);

        teleportPlayersOutOfMine();

        com.sk89q.worldedit.world.World weWorld =
                FaweAPI.getWorld(bukkitWorld.getName());

        OreSet oreSet = data.getCurrentOreSet();

        try (EditSession session = WorldEdit.getInstance().newEditSession(weWorld)) {
            // Main ore pattern
            RandomPattern pattern = oreSet.toRandomPattern();
            session.setBlocks((Region) cuboidRegion, pattern);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Spawn geodes separately (after main generation)
        spawnGeodes();
    }

    private void spawnGeodes() {
        Random random = new Random();

        BlockVector3 min = cuboidRegion.getMinimumPoint();
        BlockVector3 max = cuboidRegion.getMaximumPoint();

        for (int x = min.x(); x <= max.x(); x++) {
            for (int y = min.y(); y <= max.y(); y++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    if (random.nextDouble() < GEODE_SPAWN_CHANCE) {
                        Location loc = new Location(bukkitWorld, x, y, z);
                        Block block = loc.getBlock();

                        // Randomly choose between amethyst block and cluster
                        if (random.nextBoolean()) {
                            block.setType(Material.AMETHYST_BLOCK);
                        } else {
                            block.setType(Material.AMETHYST_CLUSTER);
                        }
                    }
                }
            }
        }
    }

    private void teleportPlayersOutOfMine() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isInMineRegion(player.getLocation())) {
                getParent().teleportPlayer(player, true);
                ChatUtils.sendMessage(
                        player,
                        ChatColor.YELLOW + "⛏ The quarry is regenerating. You've been moved to safety!"
                );
            }
        }
    }

    public boolean isInMineRegion(Location loc) {
        if (!loc.getWorld().equals(bukkitWorld)) return false;
        BlockVector3 pos = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return cuboidRegion.contains(pos);
    }

    /* =========================
       HOLOGRAM SYNC
       ========================= */

    private void updateHologram() {
        String line = ChatColor.GREEN +
                "Regenerates In: " + ChatColor.WHITE +
                ChatUtils.formatDuration(getRemainingSeconds());

        String hologramId =
                getParent().getEnterprise().getID() + "_" +
                        JobSiteType.QUARRY.name() + "_welcome";

        try {
            DHAPI.setHologramLine(
                    DHAPI.getHologramLine(
                            DHAPI.getHologram(hologramId).getPage(0), 5
                    ),
                    line
            );
        } catch (Exception ignored) {}
    }

    /* =========================
       GETTERS
       ========================= */

    private QuarryData getQuarryData() {
        return (QuarryData) getParent().getData();
    }

    public long getRegenIntervalSeconds() {
        QuarryData data = getQuarryData();
        int speedLevel = data.getLevel("regen_speed");

        // Base 300 seconds (5 min), reduce by 30 seconds per level
        long base = QuarrySite.DEFAULT_REGEN_INTERVAL_SECONDS;
        long reduction = speedLevel * 30L;

        return Math.max(60, base - reduction); // Minimum 60 seconds
    }

    public long getRemainingSeconds() {
        return Math.max(0, getRegenIntervalSeconds() - getQuarryData().getElapsedSeconds());
    }

    public OreSet getCurrentOreSet() {
        return getQuarryData().getCurrentOreSet();
    }

    /* =========================
       REGION UTILS
       ========================= */

    private CuboidRegion createRegion(Location a, Location b) {
        BlockVector3 min = BlockVector3.at(
                Math.min(a.getBlockX(), b.getBlockX()),
                Math.min(a.getBlockY(), b.getBlockY()),
                Math.min(a.getBlockZ(), b.getBlockZ())
        );
        BlockVector3 max = BlockVector3.at(
                Math.max(a.getBlockX(), b.getBlockX()),
                Math.max(a.getBlockY(), b.getBlockY()),
                Math.max(a.getBlockZ(), b.getBlockZ())
        );
        return new CuboidRegion(min, max);
    }

    public CuboidRegion getCuboidRegion() {
        return cuboidRegion;
    }
}