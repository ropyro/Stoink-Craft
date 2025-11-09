package com.stoinkcraft.jobs.jobsites.sites;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteType;
import com.stoinkcraft.jobs.jobsites.resourcegenerators.generators.MineGenerator;
import com.stoinkcraft.utils.RegionUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuarrySite extends JobSite {
    private String welcomeHologramName;
    private MineGenerator mineGenerator;
    private String mineRegionID;

    public QuarrySite(Enterprise enterprise, Location spawnPoint, boolean isbuilt) {
        super(enterprise, JobSiteType.SKYRISE, spawnPoint, new File(StoinkCore.getInstance().getDataFolder(), "/schematics/quarry.schem"), isbuilt);

        welcomeHologramName = enterprise.getID() + "_" + JobSiteType.SKYRISE.name() + "_" + "welcome";

        Location corner1 = spawnPoint.clone().add(-4, -1, -4);
        Location corner2 = spawnPoint.clone().add(-25, -21, 17);
        this.mineGenerator = new MineGenerator(corner1, corner2, this);

        mineRegionID = enterprise.getID() + "_" + JobSiteType.SKYRISE.name() + "_" + "mine";
    }

    @Override
    public void initializeJobs() {}

    @Override
    public void initializeBuild() {
        //create entryway holo
        List<String> entryHoloGramLines = new ArrayList<>();
        entryHoloGramLines.add(ChatColor.AQUA + "" + ChatColor.BOLD + "Welcome to the Quarry");
        entryHoloGramLines.add(ChatColor.WHITE + "Here you will mine ores and stones");
        entryHoloGramLines.add(ChatColor.WHITE + "to complete resource collection contracts!");
        entryHoloGramLines.add(ChatColor.WHITE + "Chat with Miner Joe to upgrade your quarry's");
        entryHoloGramLines.add(ChatColor.WHITE + "regeneration speed and unlock new ores!");
        entryHoloGramLines.add(ChatColor.GREEN + "Regenerates In: 5m 0s");
        initializeHologram(welcomeHologramName, entryHoloGramLines, spawnPoint.clone().add(-3.5, 3d, 0.5));

        //generate ores
        mineGenerator.regenerateMine();

        //create region to allow mining
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

        //remove holograms
        try{
            if(DHAPI.getHologram(welcomeHologramName) != null)
                DHAPI.getHologram(welcomeHologramName).delete();
        }catch (IllegalArgumentException e){}

        //remove ore region
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(FaweAPI.getWorld(spawnPoint.getWorld().getName()));
        manager.removeRegion(mineRegionID);
    }

    @Override
    public void tick() {
        super.tick();
        mineGenerator.tick();
        try {
            Hologram welcomeHologram = DHAPI.getHologram(welcomeHologramName);
            if (welcomeHologram != null) {
                String timeFormatted = getString();

                DHAPI.setHologramLine(
                        DHAPI.getHologramLine(welcomeHologram.getPage(0), 5),
                        timeFormatted
                );
            }
        } catch (IllegalArgumentException ignored) {}

    }

    private String getString() {
        long secondsPerCycle = mineGenerator.getRegenInterval(); // e.g., 300 for 5 minutes
        long secondsElapsed = mineGenerator.getTickCounter(); // increments once per second

        // how many seconds left until next regen
        long secondsRemaining = secondsPerCycle - (secondsElapsed % secondsPerCycle);

        long minutes = secondsRemaining / 60;
        long seconds = secondsRemaining % 60;

        String timeFormatted = String.format("§aRegenerates In: §f%dm %ds", minutes, seconds);
        return timeFormatted;
    }
}
