package com.stoinkcraft.jobs.jobsites.sites.farmland.structures;

import com.stoinkcraft.StoinkCore;
import com.stoinkcraft.jobs.jobsites.JobSite;
import com.stoinkcraft.jobs.jobsites.JobSiteStructure;
import com.stoinkcraft.utils.SchematicUtils;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class BarnStructure extends JobSiteStructure {

    private static final Vector OFFSET = new Vector(-40, 0, 10);
    private static final File SCHEMATIC =
            new File(StoinkCore.getInstance().getDataFolder(), "/schematics/barn.schem");

    public BarnStructure() {
        super(
                "barn",
                "Animal Barn",
                10,
                TimeUnit.SECONDS.toMillis(10),
                () -> 150_000,
                site -> true
        );
    }

    @Override
    public void onBuildStart(JobSite site) {
        // Create hologram: "Building Barn (15:00)"
    }

    @Override
    public void onBuildTick(JobSite site, long remainingMillis) {
        // Update hologram countdown
    }

    @Override
    public void onBuildComplete(JobSite site) {
        Location pasteLoc = site.getSpawnPoint();
        SchematicUtils.pasteSchematic(SCHEMATIC, pasteLoc, true);

        // Enable new generator / region
        // Remove hologram
    }

    @Override
    public void onLoad(JobSite site) {
        if(site.getData().getStructure(this.getId()).getState().equals(StructureState.BUILT)){
            //enable structure stuff
        }
        // Recreate generators or regions on server restart
    }
}