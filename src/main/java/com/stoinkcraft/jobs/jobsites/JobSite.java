package com.stoinkcraft.jobs.jobsites;

import com.fastasyncworldedit.core.FaweAPI;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.enterprise.Enterprise;
import com.stoinkcraft.jobs.JobActionHandler;
import com.stoinkcraft.jobs.JobActionType;
import com.stoinkcraft.utils.RegionUtils;
import com.stoinkcraft.utils.SchematicUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class JobSite {
    protected final Enterprise enterprise;
    protected final JobSiteType type;
    protected final Region region;
    protected final Location spawnPoint;
    protected final Map<JobActionType, JobActionHandler> handlers = new HashMap<>();
    protected File schematic;
    protected boolean isBuilt;

    protected String protectionRegionID;
    protected ProtectedRegion protectedRegion;

    public JobSite(Enterprise enterprise, JobSiteType type, Location spawnPoint, File schematic, boolean isBuilt) {
        this.enterprise = enterprise;
        this.type = type;
        this.region = SchematicUtils.getRegionFromSchematic(schematic, spawnPoint);
        this.spawnPoint = spawnPoint;
        this.schematic = schematic;
        this.isBuilt = isBuilt;
        this.protectionRegionID = "enterprise_" + enterprise.getID() + "_" + type.name();
    }

    public abstract void initializeJobs();

    public void protectRegion(){
        RegionUtils.createProtectedRegion(
                spawnPoint.getWorld(),
                region,
                protectionRegionID);
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(FaweAPI.getWorld(spawnPoint.getWorld().getName()));
        protectedRegion = manager.getRegion(protectionRegionID);
    }

    public void teleportPlayer(Player player){
        if(!isBuilt) build();
        Location spawn = spawnPoint.clone().add(0.5, 0, 0.5);
        spawn.setYaw(90);
        player.teleport(spawn);
    }

    public void build() {
        if (isBuilt) {
            StoinkCore.getInstance().getLogger().info(type + " site for " + enterprise.getName() + " already built.");
            return;
        }

        SchematicUtils.pasteSchematic(schematic, spawnPoint, true);
        protectRegion();
        isBuilt = true;
        StoinkCore.getInstance().getLogger().info("Built " + type + " job site for " + enterprise.getName() + " at " + spawnPoint);
    }

    public void tick() {
        handlers.values().forEach(JobActionHandler::onTick);
    }

    public boolean contains(Location loc) {
        return region.contains(RegionUtils.toBlockVector3(loc));
    }
}
