package com.stoinkcraft.jobs.jobsites.sites;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.data.QuarryData;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.MineGenerator;
import com.stoinkcraft.utils.RegionUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.io.File;
import java.util.*;

public class QuarrySite extends JobSite {

    private String welcomeHologramName;
    private MineGenerator mineGenerator;
    private String mineRegionID;
    private QuarryData data;

    public QuarrySite(Enterprise enterprise, Location spawnPoint, QuarryData data) {
        super(enterprise, JobSiteType.QUARRY, spawnPoint,
                new File(StoinkCore.getInstance().getDataFolder(), "/schematics/quarry.schem"),
                data.isBuilt());
        this.data = data;
        welcomeHologramName = enterprise.getID() + "_" + JobSiteType.QUARRY.name() + "_welcome";

        Location corner1 = spawnPoint.clone().add(data.getMineCorner1Offset());
        Location corner2 = spawnPoint.clone().add(data.getMineCorner2Offset());
        this.mineGenerator = new MineGenerator(corner1, corner2, this, (int) data.getRegenIntervalSeconds());
        mineGenerator.setTickCounter(data.getTickCounter());
        mineRegionID = enterprise.getID() + "_" + JobSiteType.QUARRY.name() + "_mine";
    }

    @Override
    public void initializeJobs() {}

    @Override
    public void initializeBuild() {
        List<String> entryHoloGramLines = new ArrayList<>();
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to the Quarry");
        entryHoloGramLines.add(ChatColor.WHITE + "Here you will mine ores and stones");
        entryHoloGramLines.add(ChatColor.WHITE + "to complete resource collection contracts!");
        entryHoloGramLines.add(ChatColor.WHITE + "Chat with Miner Joe to upgrade your quarry's");
        entryHoloGramLines.add(ChatColor.WHITE + "regeneration speed and unlock new ores!");
        entryHoloGramLines.add(ChatColor.GREEN + "Regenerates In: 5m 0s");
        Location holoLoc = spawnPoint.clone().add(data.getEntryHologramOffset());
        initializeHologram(welcomeHologramName, entryHoloGramLines, holoLoc);

        mineGenerator.regenerateMine();

        Map<StateFlag, StateFlag.State> flags = new HashMap<>();
        flags.put(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
        flags.put(Flags.INTERACT, StateFlag.State.ALLOW);
        flags.put(Flags.USE, StateFlag.State.ALLOW);
        flags.put(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        flags.put(Flags.MOB_SPAWNING, StateFlag.State.DENY);
        flags.put(Flags.MOB_DAMAGE, StateFlag.State.DENY);
        RegionUtils.createProtectedRegion(
                spawnPoint.getWorld(),
                mineGenerator.getCuboidRegion(),
                mineRegionID,
                flags,
                10);
    }

    @Override
    public void disband() {
        super.disband();

        try {
            if (DHAPI.getHologram(welcomeHologramName) != null)
                DHAPI.getHologram(welcomeHologramName).delete();
        } catch (IllegalArgumentException e) {}

        com.sk89q.worldguard.WorldGuard worldGuard = com.sk89q.worldguard.WorldGuard.getInstance();
        var container = worldGuard.getPlatform().getRegionContainer();
        var manager = container.get(com.fastasyncworldedit.core.FaweAPI.getWorld(spawnPoint.getWorld().getName()));
        if (manager != null) {
            manager.removeRegion(mineRegionID);
        }
    }

    @Override
    public void tick() {
        super.tick();
        mineGenerator.tick();
        try {
            Hologram welcomeHologram = DHAPI.getHologram(welcomeHologramName);
            if (welcomeHologram != null) {
                String timeFormatted = getFormattedTimeRemaining();
                DHAPI.setHologramLine(
                        DHAPI.getHologramLine(welcomeHologram.getPage(0), 5),
                        timeFormatted
                );
            }
        } catch (IllegalArgumentException ignored) {}
    }

    private String getFormattedTimeRemaining() {
        long secondsPerCycle = data.getRegenIntervalSeconds();
        long secondsElapsed = mineGenerator.getTickCounter();
        long secondsRemaining = secondsPerCycle - (secondsElapsed % secondsPerCycle);
        long minutes = secondsRemaining / 60;
        long seconds = secondsRemaining % 60;
        return String.format("§aRegenerates In: §f%dm %ds", minutes, seconds);
    }

    public MineGenerator getMineGenerator() {
        return mineGenerator;
    }

    // For serialization
    public QuarryData getData() {
        data.setBuilt(isBuilt);
        data.setTickCounter(mineGenerator.getTickCounter());
        return data;
    }
}