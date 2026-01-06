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
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.components.JobSiteGenerator;
import com.stoinkcraft.jobs.jobsites.sites.quarry.QuarryData;
import com.stoinkcraft.utils.ChatUtils;
import com.stoinkcraft.utils.RegionUtils;
import eu.decentsoftware.holograms.api.DHAPI;
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

    private final CuboidRegion cuboidRegion;
    private final String regionName;

    /** Seconds between regenerations */
    private final long regenIntervalSeconds;

    /** Seconds elapsed since last regen (persisted) */
    private long elapsedSeconds;

    public MineGenerator(
            Location corner1,
            Location corner2,
            JobSite parent,
            long regenIntervalSeconds,
            String regionName
    ) {
        super(parent);

        this.corner1 = corner1.clone();
        this.corner2 = corner2.clone();
        this.bukkitWorld = corner1.getWorld();
        this.regionName = regionName;
        this.regenIntervalSeconds = regenIntervalSeconds;

        this.cuboidRegion = createRegion(this.corner1, this.corner2);

        // restore persisted value
        if (parent.getData() instanceof QuarryData data) {
            this.elapsedSeconds = data.getElapsedSeconds();
        }
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

        elapsedSeconds++;

        if (elapsedSeconds >= regenIntervalSeconds) {
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

    private void regenerateMine() {
        elapsedSeconds = 0;
        persistElapsed();

        teleportPlayersOutOfMine();

        com.sk89q.worldedit.world.World weWorld =
                FaweAPI.getWorld(bukkitWorld.getName());

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

    private void teleportPlayersOutOfMine() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getParent().contains(player.getLocation())) {
                getParent().teleportPlayer(player, true);
                ChatUtils.sendMessage(
                        player,
                        ChatColor.YELLOW + "⛏ The quarry is regenerating. You've been moved to safety!"
                );
            }
        }
    }

    /* =========================
       HOLOGRAM SYNC
       ========================= */

    private void updateHologram() {
        long remaining = getRemainingSeconds();

        long minutes = remaining / 60;
        long seconds = remaining % 60;

        String line = ChatColor.GREEN +
                "Regenerates In: " + ChatColor.WHITE +
                minutes + "m " + seconds + "s";

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
       DATA PERSISTENCE
       ========================= */

    private void persistElapsed() {
        if (getParent().getData() instanceof QuarryData data) {
            data.setElapsedSeconds(elapsedSeconds);
        }
    }

    public long getRemainingSeconds() {
        return Math.max(0, regenIntervalSeconds - elapsedSeconds);
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